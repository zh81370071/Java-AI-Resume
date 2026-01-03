/**
 * @projectName springAi
 * @package com.example.springai.websocket
 * @className com.example.springai.websocket.OpenAiChatModel
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.websocket;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import reactor.core.publisher.Flux;

@Service
public class wsOpenAiChatModel {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    // 接收消息msg并返回流式数据
    public Flux<String> stream(String msg) {
        // 将msg传递给openAiChatModel并返回流式数据
        return openAiChatModel.stream(msg);
    }
}

