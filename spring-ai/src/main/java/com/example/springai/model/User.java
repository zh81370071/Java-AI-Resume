/**
 * @projectName springAi
 * @package com.example.springai.model
 * @className com.example.springai.model.User
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.model;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")  // 绑定数据库中的 'user' 表
public class User {

    private String id;
    private String username;
    private String password;
    private String email;
    private String phone;
    /**
     * 用户类型: 0-普通用户, 1-管理员
     */
    private Integer userType;
}
