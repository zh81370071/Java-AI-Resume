package com.example.springai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springai.model.JobPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JobPositionMapper extends BaseMapper<JobPosition> {
    @Select("<script>" +
            "SELECT j.*, c.name as category_name FROM job_position j " +
            "LEFT JOIN job_category c ON j.category_id = c.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (j.title LIKE CONCAT('%',#{keyword},'%') " +
            "       OR j.description LIKE CONCAT('%',#{keyword},'%') " +
            "       OR j.company_name LIKE CONCAT('%',#{keyword},'%') " +
            "       OR j.location LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "  AND j.category_id = #{categoryId} " +
            "</if>" +
            "ORDER BY j.create_time DESC" +
            "</script>")
    IPage<JobPosition> findJobs(Page<JobPosition> page, @Param("keyword") String keyword, @Param("categoryId") Integer categoryId);
}
