package com.example.springai.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("job_position")
public class JobPosition {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("category_id")
    private Integer categoryId;

    private String title;
    private String description;

    @TableField("required_skills")
    private String requiredSkills;

    @TableField("experience_requirement")
    private String experienceRequirement;

    @TableField("education_requirement")
    private String educationRequirement;

    @TableField("salary_range")
    private String salaryRange;

    private Integer status;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField(exist = false)
    private String categoryName;

    private String companyName;
    private String location;
}
