package com.example.springai.websocket;

import com.example.springai.dto.EmailDTO;
import com.example.springai.service.SendEmail;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;


import com.example.springai.model.ChatRequest;
import com.example.springai.model.History;
import com.example.springai.service.HistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final HistoryService historyService;
    private final wsOpenAiChatModel openAiChatModel;
    private final SendEmail sendEmail;

    // 构造方法注入 HistoryService 和 wsOpenAiChatModel
    public ChatWebSocketHandler(HistoryService historyService, wsOpenAiChatModel openAiChatModel,
                               SendEmail sendEmail) {
        this.historyService = historyService;
        this.openAiChatModel = openAiChatModel;

        this.sendEmail = sendEmail;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {

        String payload = message.getPayload(); // 从客户端接收消息
        ChatRequest request = parseMessage(payload); // 定义一个方法解析 JSON
        String question = request.getText();
        String userId = request.getUserId();
        String username = request.getUsername();

        // 初始化累积响应的 StringBuilder
        StringBuilder responseBuilder = new StringBuilder();

        // 使用 Flux<String> 流式返回数据给客户端
        Flux<String> aiResponse = openAiChatModel.stream(question)
                .doOnNext(chunk -> {
                    if (!"[DONE]".equalsIgnoreCase(chunk)) {
                        responseBuilder.append(chunk); // 累加 AI 响应内容
                    }
                })
                .map(chunk -> "data:" + chunk + "\n\n")
                .concatWith(Mono.just("data:[DONE]\n\n"));

        // 向客户端发送流式消息，同时监听完成事件
        aiResponse.subscribe(response -> {
            try {
                session.sendMessage(new TextMessage(response));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, error -> {
            // 异常处理
            error.printStackTrace();
        }, () -> {
//             在流式响应结束后保存到数据库
            History history = new History();
            history.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            history.setQuestion(question);
            history.setResult(responseBuilder.toString());
            history.setUserId(userId);
            history.setUsername(username);
            history.setTime(new Date(System.currentTimeMillis()));
            historyService.saveHistory(history);



        });
    }


    private ChatRequest parseMessage(String payload) {
        // 假设 payload 是 JSON 格式，可以使用库解析
        // 例如使用 Jackson ObjectMapper 解析为 ChatRequest 对象
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(payload, ChatRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid message format: " + payload, e);
        }
    }
}
