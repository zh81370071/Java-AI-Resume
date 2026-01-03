/**
 * @projectName springAi
 * @package com.example.springai.mapper
 * @className com.example.springai.mapper.UserMapper
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springai.model.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}