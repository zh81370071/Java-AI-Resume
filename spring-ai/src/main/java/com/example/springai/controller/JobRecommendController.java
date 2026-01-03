package com.example.springai.controller;

import com.example.springai.dto.JobRecommendRequest;
import com.example.springai.service.JobRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/job-recommend")
@CrossOrigin
public class JobRecommendController {

    @Autowired
    private JobRecommendService jobRecommendService;

    @PostMapping("/recommend")
    public Map<String, Object> recommendJobs(@RequestBody JobRecommendRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> recommendations = jobRecommendService.recommendJobs(request);
            response.put("success", true);
            response.put("data", recommendations);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
} 