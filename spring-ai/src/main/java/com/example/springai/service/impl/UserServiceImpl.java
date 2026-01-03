/**
 * @projectName springAi
 * @package com.example.springai.service.impl
 * @className com.example.springai.service.impl.UserServiceImpl
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springai.config.uuid;
import com.example.springai.mapper.UserMapper;
import com.example.springai.model.User;
import com.example.springai.service.UserService;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private uuid.UuidGenerator uuidGenerator;


    @Override
    public boolean addUser(User user) {
        // 检查用户名或邮箱是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername()).or().eq("email", user.getEmail());
        if (userMapper.selectCount(queryWrapper) > 0) {
            return false; // 用户名或邮箱已存在
        }
        user.setId(uuidGenerator.generateUuid32());
        return userMapper.insert(user) > 0;
    }

    @Override
    public User login(User user) {
        // 支持用户名或邮箱登录
        String account = user.getEmail(); // 前端传入的账号（可能是用户名或邮箱）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", account).or().eq("username", account);
        User dbUser = userMapper.selectOne(queryWrapper);
        if (dbUser != null && dbUser.getPassword().equals(user.getPassword())) {
            return dbUser;
        } else {
            return null;
        }
    }

    @Override
    public User getUser(String id) {
        return userMapper.selectById(id);
    }
}
