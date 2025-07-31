package com.mercedesbenz.carversation.util;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalStore {

    /*
     * This can be stored in Redis or any other distributed cache.
     * Key -> Client ID (VIN / Encrypted in VIN)
     * Value -> WebSocketSession of a client
     * */
    public static final ConcurrentHashMap<String, WebSocketSession> CLIENT_WEBSOCKET_SESSIONS = new ConcurrentHashMap<>();

    /*
    This can be kept as a global store for conversation IDs along with their participants.
    The participant order is not important, so we use an Set to store them.
     */
    public static final Map<Set<String>, String> CONVERSATION_ID_MAP = new ConcurrentHashMap<>();
}
