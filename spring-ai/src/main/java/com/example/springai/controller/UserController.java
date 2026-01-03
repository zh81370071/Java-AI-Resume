/**
 * @projectName springAi
 * @package com.example.springai.controller
 * @className com.example.springai.controller.UserController
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;


import com.example.springai.model.User;
import com.example.springai.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public String addUser(@RequestBody User user) {
        boolean result = userService.addUser(user);
        return result ? "新增用户成功！" : "用户已存在";
    }

    //用户登录
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        User userInfo = userService.login(user);
        if (userInfo != null) {
            // 直接返回 User 对象，Spring 会自动将其转换为 JSON 格式
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}