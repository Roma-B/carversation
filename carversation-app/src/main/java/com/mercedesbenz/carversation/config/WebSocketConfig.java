package com.mercedesbenz.carversation.config;

import com.mercedesbenz.carversation.controller.ChatRequestController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ChatRequestController(), "/chatRequest")
                .setAllowedOrigins("*");
    }

    @Bean
    WebSocketHandler ChatRequestController() {
        return new ChatRequestController();
    }

}
