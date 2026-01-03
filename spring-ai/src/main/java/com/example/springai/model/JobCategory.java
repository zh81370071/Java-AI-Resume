package com.example.springai.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("job_category")
public class JobCategory {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;

    @TableField("create_time")
    private Date createTime;
}
