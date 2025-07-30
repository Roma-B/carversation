package com.mercedesbenz.carversation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.carversation.payload.ChatPayload;
import com.mercedesbenz.carversation.util.GlobalStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;

@Slf4j
public class ChatRequestController implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection initiated: {}", session.getId());
        String VIN = session.getHandshakeHeaders().getFirst("X-VIN");
        String sessionId = session.getId();
        GlobalStore.CLIENT_WEBSOCKET_SESSIONS.put(VIN, session);
        log.info("Connection established with Session Id: {} and VIN; {}", sessionId, VIN);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String VIN = session.getHandshakeHeaders().getFirst("X-VIN");
        String sessionId = session.getId();
        // Check if session is already present for the VIN else put into the map if connection status is open,
        // This check I added in case if we lost session from our side from memory by any chance, then we will re add it
        if(!isSessionIsAlreadyExistingForVin(VIN, sessionId)) {
            GlobalStore.CLIENT_WEBSOCKET_SESSIONS.put(VIN, session);
        }
        log.info("Received request from VIN: {} with Session ID: {}", VIN, sessionId);
        // Here we can handle the logic for routing incoming chat messages like chat payloads (ChatPayload)
        String stringMessage = (String) message.getPayload();
        ChatPayload chatMessage = objectMapper.readValue(stringMessage, ChatPayload.class);
        log.info("Received message: {} for VIN : {}", chatMessage.getMessage(), chatMessage.getVin());
        // look for the session Id in GlobalStore for the given vin in the payload
        if (!GlobalStore.CLIENT_WEBSOCKET_SESSIONS.containsKey(chatMessage.getVin())) {
            log.error("No active session found for VIN: {}", chatMessage.getVin());
            session.sendMessage(new org.springframework.web.socket.TextMessage("No active session found for VIN: " + chatMessage.getVin()));
            return;
        }
        //if found, send the message to the session
        WebSocketSession clientSession = GlobalStore.CLIENT_WEBSOCKET_SESSIONS.get(chatMessage.getVin());
        if (clientSession == null || !clientSession.isOpen()) {
            log.error("Session for VIN: {} is not open or does not exist", chatMessage.getVin());
            session.sendMessage(new org.springframework.web.socket.TextMessage("Session for VIN: " + chatMessage.getVin() + " is not open or does not exist"));
            return;
        }
        clientSession.sendMessage(new org.springframework.web.socket.TextMessage(chatMessage.getMessage()));
        session.sendMessage(new org.springframework.web.socket.TextMessage("Message sent to VIN: " + chatMessage.getVin()));
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

    private Boolean isSessionIsAlreadyExistingForVin(String VIN, String sessionId) {
        WebSocketSession existingSessionForVin = GlobalStore.CLIENT_WEBSOCKET_SESSIONS.get(VIN);
        return existingSessionForVin != null && existingSessionForVin.getId().equals(sessionId) && existingSessionForVin.isOpen();
    }

}
