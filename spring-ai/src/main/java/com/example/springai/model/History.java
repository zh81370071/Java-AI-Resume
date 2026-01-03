/**
 * @projectName springAi
 * @package com.example.springai.model
 * @className com.example.springai.model.History
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.model;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("history")
public class History {
    private String id;
    private String question;
    private String result;
    private String username;
    private String userId;
    private Date time;
}
