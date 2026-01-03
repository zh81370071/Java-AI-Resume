package com.example.springai.service;

import com.example.springai.model.Resume;
import java.util.Map;

public interface ResumeService {
    Resume saveResume(String userId, Map<String, Object> resumeData);
    Resume getLatestResume(String userId);
    Resume updateResume(String userId, Map<String, Object> resumeData);
} 