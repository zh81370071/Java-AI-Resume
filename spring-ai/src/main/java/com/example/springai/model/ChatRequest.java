/**
 * @projectName springAi
 * @package com.example.springai.model
 * @className com.example.springai.model.ChatRequest
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.model;


import lombok.Data;

@Data
public class ChatRequest {
    private String type;
    private String text;
    private String msg;
    private String userId;
    private String username;
}
