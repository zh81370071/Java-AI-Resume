/**
 * @projectName springAi
 * @package com.example.springai.config
 * @className com.example.springai.websocket.WebSocketConfig
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.websocket;

import com.example.springai.service.SendEmail;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.springai.service.HistoryService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final wsOpenAiChatModel openAiChatModel;
    private final HistoryService historyService;
    private final SendEmail sendEmail;

    @Autowired
    public WebSocketConfig(wsOpenAiChatModel openAiChatModel, HistoryService historyService,
                            SendEmail sendEmail) {
        this.openAiChatModel = openAiChatModel; // 通过构造器注入
        this.historyService = historyService;  // 通过构造器注入
        this.sendEmail = sendEmail;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(historyService, openAiChatModel, sendEmail), "/ws/chat")
                .setAllowedOrigins("*") // 允许所有源
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
