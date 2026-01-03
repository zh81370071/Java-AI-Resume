package com.example.springai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springai.mapper.UserProfileMapper;
import com.example.springai.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户个人信息控制器
 */
@RestController
@RequestMapping("/api/profile")
@CrossOrigin
public class UserProfileController {

    @Autowired
    private UserProfileMapper userProfileMapper;

    /**
     * 获取用户个人信息
     */
    @GetMapping("/{userId}")
    public Map<String, Object> getProfile(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserProfile::getUserId, userId);
            UserProfile profile = userProfileMapper.selectOne(wrapper);

            if (profile == null) {
                // 如果不存在，返回空对象
                response.put("success", true);
                response.put("data", null);
                response.put("message", "用户信息不存在，请先完善个人信息");
            } else {
                response.put("success", true);
                response.put("data", profile);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /**
     * 保存或更新用户个人信息
     */
    @PostMapping("/save")
    public Map<String, Object> saveProfile(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userId = (String) request.get("userId");
            if (userId == null || userId.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "用户ID不能为空");
                return response;
            }

            // 查询是否已存在
            LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserProfile::getUserId, userId);
            UserProfile existingProfile = userProfileMapper.selectOne(wrapper);

            UserProfile profile;
            if (existingProfile != null) {
                profile = existingProfile;
            } else {
                profile = new UserProfile();
                profile.setUserId(userId);
                profile.setCreateTime(LocalDateTime.now());
            }

            // 更新字段
            if (request.containsKey("name")) {
                profile.setName((String) request.get("name"));
            }
            if (request.containsKey("gender")) {
                profile.setGender((String) request.get("gender"));
            }
            if (request.containsKey("birthDate") && request.get("birthDate") != null) {
                profile.setBirthDate(LocalDate.parse((String) request.get("birthDate")));
            }
            if (request.containsKey("phone")) {
                profile.setPhone((String) request.get("phone"));
            }
            if (request.containsKey("email")) {
                profile.setEmail((String) request.get("email"));
            }
            if (request.containsKey("address")) {
                profile.setAddress((String) request.get("address"));
            }
            if (request.containsKey("jobStatus")) {
                profile.setJobStatus((String) request.get("jobStatus"));
            }
            if (request.containsKey("jobTitle")) {
                profile.setJobTitle((String) request.get("jobTitle"));
            }
            if (request.containsKey("jobCity")) {
                profile.setJobCity((String) request.get("jobCity"));
            }
            if (request.containsKey("salaryMin") && request.get("salaryMin") != null) {
                profile.setSalaryMin(((Number) request.get("salaryMin")).intValue());
            }
            if (request.containsKey("salaryMax") && request.get("salaryMax") != null) {
                profile.setSalaryMax(((Number) request.get("salaryMax")).intValue());
            }
            if (request.containsKey("school")) {
                profile.setSchool((String) request.get("school"));
            }
            if (request.containsKey("major")) {
                profile.setMajor((String) request.get("major"));
            }
            if (request.containsKey("degree")) {
                profile.setDegree((String) request.get("degree"));
            }
            if (request.containsKey("graduationDate") && request.get("graduationDate") != null) {
                profile.setGraduationDate(LocalDate.parse((String) request.get("graduationDate")));
            }
            if (request.containsKey("skills")) {
                profile.setSkills((String) request.get("skills"));
            }
            if (request.containsKey("selfIntroduction")) {
                profile.setSelfIntroduction((String) request.get("selfIntroduction"));
            }

            profile.setUpdateTime(LocalDateTime.now());

            // 保存或更新
            if (existingProfile != null) {
                userProfileMapper.updateById(profile);
            } else {
                userProfileMapper.insert(profile);
            }

            response.put("success", true);
            response.put("data", profile);
            response.put("message", "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    /**
     * 获取用于填充简历的数据
     */
    @GetMapping("/resume-data/{userId}")
    public Map<String, Object> getResumeData(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserProfile::getUserId, userId);
            UserProfile profile = userProfileMapper.selectOne(wrapper);

            if (profile == null) {
                response.put("success", false);
                response.put("error", "用户信息不存在");
                return response;
            }

            // 构建简历填充数据
            Map<String, Object> resumeData = new HashMap<>();
            resumeData.put("name", profile.getName());
            resumeData.put("phone", profile.getPhone());
            resumeData.put("email", profile.getEmail());
            resumeData.put("jobStatus", profile.getJobStatus());
            resumeData.put("jobTitle", profile.getJobTitle());

            // 构建薪资期望字符串
            if (profile.getSalaryMin() != null && profile.getSalaryMax() != null) {
                resumeData.put("salaryExpectation", profile.getSalaryMin() + "K-" + profile.getSalaryMax() + "K");
            } else if (profile.getSalaryMin() != null) {
                resumeData.put("salaryExpectation", profile.getSalaryMin() + "K以上");
            } else if (profile.getSalaryMax() != null) {
                resumeData.put("salaryExpectation", profile.getSalaryMax() + "K以内");
            }

            // 构建教育经历
            Map<String, Object> education = new HashMap<>();
            education.put("school", profile.getSchool());
            education.put("major", profile.getMajor());
            education.put("degree", profile.getDegree());
            if (profile.getGraduationDate() != null) {
                education.put("graduationDate", profile.getGraduationDate().toString());
            }
            resumeData.put("education", education);

            // 构建专业技能
            Map<String, Object> profession = new HashMap<>();
            profession.put("skill", profile.getSkills());
            resumeData.put("profession", profession);

            response.put("success", true);
            response.put("data", resumeData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
