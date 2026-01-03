package com.example.springai.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户个人信息实体类（用于自动填充简历）
 */
@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private String userId;

    /** 姓名 */
    private String name;

    /** 性别 */
    private String gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 现居地址 */
    private String address;

    /** 求职状态(在职/离职/应届生) */
    private String jobStatus;

    /** 期望职位 */
    private String jobTitle;

    /** 期望工作城市 */
    private String jobCity;

    /** 期望薪资下限(K) */
    private Integer salaryMin;

    /** 期望薪资上限(K) */
    private Integer salaryMax;

    /** 毕业院校 */
    private String school;

    /** 专业 */
    private String major;

    /** 学历 */
    private String degree;

    /** 毕业时间 */
    private LocalDate graduationDate;

    /** 技能标签(逗号分隔) */
    private String skills;

    /** 自我介绍 */
    private String selfIntroduction;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
