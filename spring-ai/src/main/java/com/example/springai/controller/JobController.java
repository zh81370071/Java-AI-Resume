package com.example.springai.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springai.model.JobCategory;
import com.example.springai.model.JobPosition;
import com.example.springai.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job")
@CrossOrigin
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/categories")
    public Map<String, Object> getCategories() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<JobCategory> categories = jobService.getAllCategories();
            result.put("success", true);
            result.put("data", categories);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> getJobList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId
    ) {
        Map<String, Object> result = new HashMap<>();
        try {
            IPage<JobPosition> pageResult = jobService.getJobList(page, size, keyword, categoryId);

            Map<String, Object> data = new HashMap<>();
            data.put("list", pageResult.getRecords());
            data.put("total", pageResult.getTotal());

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/detail/{id}")
    public Map<String, Object> getJobDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            JobPosition job = jobService.getJobById(id);
            result.put("success", true);
            result.put("data", job);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> addJob(@RequestBody JobPosition job) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = jobService.addJob(job);
            result.put("success", success);
            if (success) {
                result.put("data", job);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> updateJob(@RequestBody JobPosition job) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = jobService.updateJob(job);
            result.put("success", success);
            if (success) {
                result.put("data", job);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteJob(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = jobService.deleteJob(id);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // 职位分类相关接口
    @PostMapping("/category/add")
    public Map<String, Object> addCategory(@RequestBody JobCategory category) {
        Map<String, Object> result = new HashMap<>();
        try {
            JobCategory saved = jobService.addCategory(category);
            result.put("success", true);
            result.put("data", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/category/delete/{id}")
    public Map<String, Object> deleteCategory(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = jobService.deleteCategory(id);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
