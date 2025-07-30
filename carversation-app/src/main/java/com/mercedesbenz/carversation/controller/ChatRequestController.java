package com.mercedesbenz.carversation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class ChatRequestController implements WebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //log connection establishment
        log.info("Connection established: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // add handling logic for incoming messages later
        String chatMessage = (String) message.getPayload();
        log.info("Received message: {}", chatMessage);
        // Simulate processing the chat message
        session.sendMessage(new org.springframework.web.socket.TextMessage("Processing chat request: " + chatMessage));
        // Simulate a delay for processing
        Thread.sleep(50);
        session.sendMessage(new org.springframework.web.socket.TextMessage("Finished processing chat request: " + chatMessage));
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

}
