package com.mercedesbenz.carversation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.carversation.data.NearByUsers;
import com.mercedesbenz.carversation.data.entity.UserEntity;
import com.mercedesbenz.carversation.payload.*;
import com.mercedesbenz.carversation.repository.UsersRepository;
import com.mercedesbenz.carversation.service.ChatService;
import com.mercedesbenz.carversation.service.UsersService;
import com.mercedesbenz.carversation.util.GlobalStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class ChatRequestController implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatService chatService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UsersService usersService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection initiated: {}", session.getId());
        String vin = extractVinFromSession(session);
        if(vin == null){
            session.sendMessage(new org.springframework.web.socket.TextMessage("MISSING_VIN_PARAMETER"));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        String sessionId = session.getId();
        GlobalStore.CLIENT_WEBSOCKET_SESSIONS.put(vin, session);
        ConnectionResponsePayload connectionResponsePayload = new ConnectionResponsePayload();
        UserEntity user = usersRepository.findByVin(vin);
        NearByUsers nearByUsers = usersService.findUsersWithinRadius(user.getLat(), user.getLng(), vin, 7000);
        connectionResponsePayload.setMessageType(MessageTypeEnum.NEARBY_USERS);
        connectionResponsePayload.setNearByUsers(nearByUsers);
        sendJson(session, connectionResponsePayload);
        log.info("Connection established with Session Id: {} and VIN: {}", sessionId, vin);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> webSocketMessage) throws Exception {
        String senderVin = extractVinFromSession(session);
        if(senderVin == null){
            session.sendMessage(new org.springframework.web.socket.TextMessage("MISSING_VIN_PARAMETER"));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        String sessionId = session.getId();
        if(!isSessionIsAlreadyExistingForVin(senderVin, sessionId)) {
            GlobalStore.CLIENT_WEBSOCKET_SESSIONS.put(senderVin, session);
        }
        log.info("Received request from senderVin: {} with Session ID: {}", senderVin, sessionId);


        String stringMessage = (String) webSocketMessage.getPayload();
        ChatPayload payload = objectMapper.readValue(stringMessage, ChatPayload.class);

        if (payload.getVin() == null || payload.getVin().isEmpty()) {
            session.sendMessage(new org.springframework.web.socket.TextMessage("MISSING_VIN"));
            return;
        }

        // For Chat Request, we will send the webSocketMessage to the session with the given VIN
        if (payload.getMessageType() == MessageTypeEnum.CHAT_REQUEST) {
            handleChatRequest(senderVin, session, payload);
        }

        // For Chat Message, we will send the webSocketMessage to the session with the given VIN
        if (payload.getMessageType() == MessageTypeEnum.CHAT_MESSAGE) {
            handleChatMessage(senderVin, session, payload);
        }

        if (payload.getMessageType() == MessageTypeEnum.CHAT_REQUEST_RESPONSE) {
            // For Chat Response, we will send the webSocketMessage to the session with the given VIN
            handleChatRequestResponse(senderVin, session, payload);
        }

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // Log the error
        log.error("Exception occurred: {} on session: {}", exception.getMessage(), session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // Log the closure of the connection
        log.info("Connection closed on session: {} with status: {}", session.getId(), closeStatus.getCode());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleChatMessage(String senderVin, WebSocketSession session, ChatPayload payload) throws IOException {
        WebSocketSession clientSession = getActiveClientSession(payload.getVin(), session);
        if (clientSession == null) {
            return; // No active session found or session is not open
        }
        //clientSession.sendMessage(new org.springframework.web.socket.TextMessage(payload.getMessage()));
        ChatMessagePayload chatMessagePayload = new ChatMessagePayload();
        chatMessagePayload.setMessage(payload.getMessage());
        chatMessagePayload.setSenderVin(senderVin);
        chatMessagePayload.setReceiverVin(payload.getVin());
        chatMessagePayload.setMessageTypeEnum(MessageTypeEnum.CHAT_MESSAGE);
        sendJson(clientSession, chatMessagePayload);
        // Get the conversation ID from the global store
        String conversationId = GlobalStore.CONVERSATION_ID_MAP.get(Set.of(senderVin, payload.getVin()));
        if (conversationId == null) {
            log.error("No conversation found for senderVin: {} and receiverVin: {}", senderVin, payload.getVin());
            session.sendMessage(new org.springframework.web.socket.TextMessage("No conversation found for senderVin: " + senderVin + " and receiverVin: " + payload.getVin()));
            return;
        }
        // save the sent message to the chat service
        chatService.SaveMessages(conversationId, senderVin, payload.getVin(), payload.getMessage());
        log.info("Message sent to senderVin: {} with message: {}", payload.getVin(), payload.getMessage());
    }



    private void handleChatRequest(String senderVin, WebSocketSession session, ChatPayload payload) throws IOException {
        WebSocketSession clientSession = getActiveClientSession(payload.getVin(), session);
        if (clientSession == null) {
            return; // No active session found or session is not open
        }
        // send the chat request message to the client session
        //clientSession.sendMessage(new org.springframework.web.socket.TextMessage("Chat request from: " + senderVin));
        ChatRequestPayload chatRequestPayload = new ChatRequestPayload();
        chatRequestPayload.setRequestFrom(senderVin);
        chatRequestPayload.setRequestTo(payload.getVin());
        chatRequestPayload.setMessageTypeEnum(MessageTypeEnum.CHAT_REQUEST_RECEIVED);
        sendJson(clientSession, chatRequestPayload);
        // save the chat request to the chat service
        String conversationId = String.valueOf(UUID.randomUUID());
        GlobalStore.CONVERSATION_ID_MAP.put(Set.of(senderVin, payload.getVin()), conversationId);

        chatService.SaveOrUpdateChatRequest(conversationId, senderVin, payload.getVin(), "PENDING");
        log.info("Chat request sent to : {}, from vin {}", payload.getVin(), senderVin);
    }


    private void handleChatRequestResponse(String senderVin, WebSocketSession session, ChatPayload payload) throws IOException {
        WebSocketSession clientSession = getActiveClientSession(payload.getVin(), session);
        if (clientSession == null) {
            return;
        }
        // Get the conversation ID from the global store
        String conversationId = GlobalStore.CONVERSATION_ID_MAP.get(Set.of(senderVin, payload.getVin()));
         if (conversationId == null) {
             log.error("No chat request found for senderVin: {} and receiverVin: {}", senderVin, payload.getVin());
             session.sendMessage(new org.springframework.web.socket.TextMessage("No chat request found for senderVin: " + senderVin + " and receiverVin: " + payload.getVin()));
             return;
         }
        // save the chat request response to the chat service
        chatService.SaveOrUpdateChatRequest(conversationId, senderVin, payload.getVin(), payload.getStatus());
        // create a conversation if the status is ACCEPTED
        if ("ACCEPTED".equalsIgnoreCase(payload.getStatus())) {
            ChatRequestPayload chatRequestPayload = new ChatRequestPayload();
            chatRequestPayload.setRequestFrom(senderVin);
            chatRequestPayload.setRequestTo(payload.getVin());
            chatRequestPayload.setMessageTypeEnum(MessageTypeEnum.CHAT_REQUEST_ACCEPTED);
            sendJson(clientSession, chatRequestPayload);
            chatService.createConversation(conversationId, senderVin, payload.getVin());
            log.info("Conversation created with ID: {} for senderVin: {} and receiverVin: {}", conversationId, senderVin, payload.getVin());
        }else {
            ChatRequestPayload chatRequestPayload = new ChatRequestPayload();
            chatRequestPayload.setRequestFrom(senderVin);
            chatRequestPayload.setRequestTo(payload.getVin());
            chatRequestPayload.setMessageTypeEnum(MessageTypeEnum.CHAT_REQUEST_REJECTED);
            sendJson(clientSession, chatRequestPayload);
            log.info("Chat request rejected for senderVin: {} and receiverVin: {}", senderVin, payload.getVin());
        }
    }


    private Boolean isSessionIsAlreadyExistingForVin(String VIN, String sessionId) {
        WebSocketSession existingSessionForVin = GlobalStore.CLIENT_WEBSOCKET_SESSIONS.get(VIN);
        return existingSessionForVin != null && existingSessionForVin.getId().equals(sessionId) && existingSessionForVin.isOpen();
    }

    private WebSocketSession getActiveClientSession(String vin, WebSocketSession requesterSession) throws IOException {
        if (!GlobalStore.CLIENT_WEBSOCKET_SESSIONS.containsKey(vin)) {
            log.error("No active session found for senderVin: {}", vin);
            requesterSession.sendMessage(new org.springframework.web.socket.TextMessage("No active session found for senderVin: " + vin));
            return null;
        }
        WebSocketSession clientSession = GlobalStore.CLIENT_WEBSOCKET_SESSIONS.get(vin);
        if (clientSession == null || !clientSession.isOpen()) {
            log.error("Session for senderVin: {} is not open or does not exist", vin);
            requesterSession.sendMessage(new org.springframework.web.socket.TextMessage("Session for senderVin: " + vin + " is not open or does not exist"));
            return null;
        }
        return clientSession;
    }


    private String extractVinFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null) {
            String query = uri.getQuery(); // e.g., vin=VIN123
            if (query != null) {
                Map<String, String> queryParams = Arrays.stream(query.split("&"))
                        .map(param -> param.split("=", 2))
                        .filter(pair -> pair.length == 2)
                        .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));
                return queryParams.get("vin");
            }
        }
        return null;
    }

    public void sendJson(WebSocketSession session, Object responseObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(responseObject);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            e.printStackTrace(); // log or handle the error
        }
    }

}
