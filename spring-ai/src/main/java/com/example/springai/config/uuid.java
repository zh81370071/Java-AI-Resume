/**
 * @projectName springAi
 * @package com.example.springai.config
 * @className com.example.springai.config.UUID
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class uuid {
    @Bean
    public UuidGenerator uuidGenerator() {
        return new UuidGenerator();
    }

    public static class UuidGenerator {
        public String generateUuid32() {
            return UUID.randomUUID().toString().replaceAll("-", "");
        }
    }
}