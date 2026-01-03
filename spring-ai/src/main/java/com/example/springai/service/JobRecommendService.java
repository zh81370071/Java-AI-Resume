package com.example.springai.service;

import com.example.springai.dto.JobRecommendRequest;
import com.example.springai.model.JobPosition;

import java.util.List;
import java.util.Map;

public interface JobRecommendService {
    
    /**
     * 根据用户技能和偏好推荐职位
     * @param request 推荐请求，包含用户信息和技能
     * @return 推荐的职位列表及其匹配理由
     */
    Map<String, Object> recommendJobs(JobRecommendRequest request);
    
    /**
     * 基于内容的协同过滤推荐算法
     * @param request 用户请求信息
     * @return 匹配的职位列表
     */
    List<JobPosition> contentBasedRecommendation(JobRecommendRequest request);
    
    /**
     * 使用AI模型生成职位推荐理由
     * @param userSkills 用户技能
     * @param job 推荐的职位
     * @return 个性化的推荐理由
     */
    String generateRecommendationReason(List<String> userSkills, JobPosition job);
} 