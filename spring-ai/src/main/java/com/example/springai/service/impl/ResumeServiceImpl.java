package com.example.springai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.springai.mapper.ResumeMapper;
import com.example.springai.model.Resume;
import com.example.springai.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Resume saveResume(String userId, Map<String, Object> resumeData) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        updateResumeFields(resume, resumeData);
        resume.setUpdateTime(new Date());

        resumeMapper.insert(resume);
        return resume;
    }

    @Override
    public Resume getLatestResume(String userId) {
        LambdaQueryWrapper<Resume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resume::getUserId, userId)
                .orderByDesc(Resume::getUpdateTime)
                .last("LIMIT 1");
        return resumeMapper.selectOne(queryWrapper);
    }


    @Override
    @Transactional
    public Resume updateResume(String userId, Map<String, Object> resumeData) {
        Resume resume = getLatestResume(userId);
        if (resume == null) {
            return saveResume(userId, resumeData);
        }

        updateResumeFields(resume, resumeData);
        resume.setUpdateTime(new Date());

        resumeMapper.updateById(resume);
        return resume;
    }

    private void updateResumeFields(Resume resume, Map<String, Object> data) {
        try {
            if (data.get("name") != null) resume.setName((String) data.get("name"));
            if (data.get("phone") != null) resume.setPhone((String) data.get("phone"));
            if (data.get("email") != null) resume.setEmail((String) data.get("email"));
            if (data.get("avatar") != null) resume.setAvatar((String) data.get("avatar"));
            if (data.get("jobStatus") != null) resume.setJobStatus((String) data.get("jobStatus"));
            if (data.get("jobTitle") != null) resume.setJobTitle((String) data.get("jobTitle"));
            if (data.get("salaryExpectation") != null) resume.setSalaryExpectation((String) data.get("salaryExpectation"));

            // 将复杂对象转换为JSON字符串存储
            if (data.get("education") != null) {
                resume.setEducation(objectMapper.writeValueAsString(data.get("education")));
            }
            if (data.get("profession") != null) {
                resume.setProfession(objectMapper.writeValueAsString(data.get("profession")));
            }
            if (data.get("work") != null) {
                resume.setWork(objectMapper.writeValueAsString(data.get("work")));
            }
            if (data.get("project") != null) {
                resume.setProject(objectMapper.writeValueAsString(data.get("project")));
            }
            if (data.get("award") != null) {
                resume.setAward(objectMapper.writeValueAsString(data.get("award")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update resume fields", e);
        }
    }
}
