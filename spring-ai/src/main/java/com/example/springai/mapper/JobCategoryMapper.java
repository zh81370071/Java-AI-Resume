package com.example.springai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springai.model.JobCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobCategoryMapper extends BaseMapper<JobCategory> {
}
