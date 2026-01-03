package com.example.springai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springai.model.JobCategory;
import com.example.springai.model.JobPosition;

import java.util.List;

public interface JobService {
    // 职位分类相关
    List<JobCategory> getAllCategories();
    JobCategory addCategory(JobCategory category);
    boolean deleteCategory(Integer id);

    // 职位相关
    IPage<JobPosition> getJobList(int page, int size, String keyword, Integer categoryId);
    JobPosition getJobById(Long id);
    boolean addJob(JobPosition job);
    boolean updateJob(JobPosition job);
    boolean deleteJob(Long id);
}
