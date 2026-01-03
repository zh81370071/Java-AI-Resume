package com.example.springai.dto;

import lombok.Data;
import java.util.List;

@Data
public class JobRecommendRequest {
    private String userId;
    private List<String> skills;
    private String educationLevel;
    private String experienceYears;
    private String preferredLocation;
    private String resumeText; // 可能包含用户简历文本信息，用于提取关键技能
} 