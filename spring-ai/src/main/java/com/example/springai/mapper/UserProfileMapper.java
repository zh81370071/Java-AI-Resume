package com.example.springai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springai.model.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户个人信息 Mapper
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
