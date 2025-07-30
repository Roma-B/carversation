package com.mercedesbenz.carversation.util;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalStore {

    /*
    * This can be stored in Redis or any other distributed cache.
    * Key -> Client ID (VIN / Encrypted in VIN)
    * Value -> WebSocketSession of a client
    * */
    public static final Map<String, WebSocketSession> CLIENT_WEBSOCKET_SESSIONS = new ConcurrentHashMap<>();

   /* *//*
    * This can be stored in Database (As we are doing storing SVP request in gen20xi3).
    * Key -> Client IDs (VIN / Encrypted in VIN)
    * Value -> Coordinates by comma separated as String [lat,long]
    * *//*
    public static final Map<String, String> CLIENT_LOCATIONS = new ConcurrentHashMap<>();*/
}
