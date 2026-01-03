package com.example.springai.service;

import com.example.springai.dto.EmailDTO;


public interface SendEmail {
    public void sendMsg(EmailDTO emailDTO);
}
