package com.example.springai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springai.mapper.JobPositionMapper;
import com.example.springai.model.JobPosition;
import com.example.springai.model.Resume;
import com.example.springai.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
@CrossOrigin
public class RecommendController {

    @Autowired
    private JobPositionMapper jobPositionMapper;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private OpenAiChatModel openAiChatModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/jobs")
    public ResponseEntity<?> recommendJobs(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");

            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "用户ID不能为空"));
            }

            Resume resume = resumeService.getLatestResume(userId);
            if (resume == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "未找到用户简历信息，请先完善简历"));
            }

            Map<String, Object> resumeData = extractResumeData(resume);
            String skills = (String) resumeData.get("skills");

            if (skills == null || skills.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "简历中未找到技能信息，请完善简历中的专业技能"));
            }

            String educationLevel = (String) resumeData.get("educationLevel");
            Integer experience = (Integer) resumeData.get("experience");
            String location = (String) resumeData.get("location");


            List<String> skillsList = Arrays.stream(skills.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());


            LambdaQueryWrapper<JobPosition> queryWrapper = new LambdaQueryWrapper<>();

            System.out.println("提取到的技能列表: " + skillsList);

            if (!skillsList.isEmpty()) {
                queryWrapper.and(wrapper -> {
                    for (String skill : skillsList) {
                        // 使用模糊匹配，确保添加通配符
                        String likeSkill = "%" + skill + "%";
                        System.out.println("添加匹配条件: " + likeSkill);
                        wrapper.or().like(JobPosition::getRequiredSkills, likeSkill);
                    }
                });
            } else {
                // 如果技能列表为空，获取所有职位作为备选
                System.out.println("警告: 技能列表为空，将获取所有职位");
            }

            if (educationLevel != null && !educationLevel.trim().isEmpty()) {
                String likeEducation = "%" + educationLevel + "%";
                System.out.println("添加教育条件: " + likeEducation);
                queryWrapper.like(JobPosition::getEducationRequirement, likeEducation);
            }

            if (location != null && !location.trim().isEmpty()) {
                String likeLocation = "%" + location + "%";
                System.out.println("添加位置条件: " + likeLocation);
                queryWrapper.like(JobPosition::getLocation, likeLocation);
            }

            Long totalCount = jobPositionMapper.selectCount(null);
            System.out.println("数据库中职位总数: " + totalCount);

            if (totalCount == 0) {
                return ResponseEntity.ok(Collections.singletonMap("message", "数据库中没有职位数据，请先添加职位信息"));
            }

            List<JobPosition> jobPositions = jobPositionMapper.selectList(queryWrapper);
            System.out.println("查询结果数量: " + (jobPositions != null ? jobPositions.size() : 0));

            if (jobPositions == null || jobPositions.isEmpty()) {
                System.out.println("未找到匹配职位，尝试放宽查询条件");

                // 重置查询条件，只使用技能的第一个关键词作为条件
                queryWrapper = new LambdaQueryWrapper<>();
                if (!skillsList.isEmpty()) {
                    String firstSkill = "%" + skillsList.get(0) + "%";
                    System.out.println("使用首要技能进行查询: " + firstSkill);
                    queryWrapper.like(JobPosition::getRequiredSkills, firstSkill);
                }

                // 重新执行查询
                jobPositions = jobPositionMapper.selectList(queryWrapper);
                System.out.println("放宽条件后查询结果数量: " + (jobPositions != null ? jobPositions.size() : 0));

            // 如果仍然没有结果，返回所有职位的前九个作为备选
                if (jobPositions == null || jobPositions.isEmpty()) {
                    System.out.println("仍未找到匹配职位，返回所有职位的前九个");
                    jobPositions = jobPositionMapper.selectList(null);
                    if (jobPositions != null && jobPositions.size() > 9) {
                        jobPositions = jobPositions.subList(0, 9);
                    }
                    System.out.println("返回所有职位数量: " + (jobPositions != null ? jobPositions.size() : 0));
                }

            }

            if (jobPositions == null) {
                jobPositions = new ArrayList<>();
            }

            // 计算每个职位的匹配得分
            List<Map<String, Object>> recommendedJobs = new ArrayList<>();
            for (JobPosition job : jobPositions) {
                // 计算匹配得分
                int matchScore = calculateMatchScore(job.getRequiredSkills(), skillsList,
                                                   educationLevel, experience, job.getLocation());

                Map<String, Object> jobMap = new HashMap<>();
                jobMap.put("id", job.getId());
                jobMap.put("title", job.getTitle());
                jobMap.put("company_name", job.getCompanyName());
                jobMap.put("salary_range", job.getSalaryRange());
                jobMap.put("location", job.getLocation());
                jobMap.put("education_requirements", job.getEducationRequirement());
                jobMap.put("experience_requirements", job.getExperienceRequirement());
                jobMap.put("matchScore", matchScore);

                recommendedJobs.add(jobMap);
            }

            // 按匹配得分降序排序
            recommendedJobs.sort((a, b) -> {
                Integer scoreA = (Integer) a.get("matchScore");
                Integer scoreB = (Integer) b.get("matchScore");
                return scoreB.compareTo(scoreA);
            });

            // 限制返回前10个最匹配的职位
            if (recommendedJobs.size() > 10) {
                recommendedJobs = recommendedJobs.subList(0, 10);
            }

            // 只为最高分的前3个职位添加个性化推荐理由和简历优化建议
            int topJobCount = Math.min(3, recommendedJobs.size());
            System.out.println("将为排名前 " + topJobCount + " 的职位添加个性化推荐");

            for (int i = 0; i < topJobCount; i++) {
                Map<String, Object> jobMap = recommendedJobs.get(i);
                Object jobIdObj = jobMap.get("id");
                Long jobId = null;

                // 安全地转换ID，处理可能的类型差异
                if (jobIdObj instanceof Integer) {
                    jobId = ((Integer) jobIdObj).longValue();
                } else if (jobIdObj instanceof Long) {
                    jobId = (Long) jobIdObj;
                } else if (jobIdObj instanceof String) {
                    try {
                        jobId = Long.parseLong((String) jobIdObj);
                    } catch (NumberFormatException e) {
                        System.out.println("无法解析职位ID: " + jobIdObj);
                    }
                }

                if (jobId == null) {
                    System.out.println("跳过职位，ID转换失败: " + jobIdObj);
                    continue;
                }

                // 查找对应的职位对象
                final Long finalJobId = jobId;
                JobPosition job = jobPositions.stream()
                                           .filter(j -> j.getId().equals(finalJobId))
                                           .findFirst()
                                           .orElse(null);

                if (job != null) {
                    // 添加个性化推荐理由
                    String recommendationReason = generateRecommendationReason(resume, job, skillsList);
                    jobMap.put("recommendation_reason", recommendationReason);
                    System.out.println("为职位 " + job.getTitle() + " 生成推荐理由");

                    // 生成针对这个职位的简历优化建议
                    String resumeImprovement = generateResumeImprovementSuggestion(resume, job);
                    jobMap.put("resume_improvement", resumeImprovement);
                    System.out.println("为职位 " + job.getTitle() + " 生成简历优化建议");
                } else {
                    System.out.println("未找到ID为 " + jobId + " 的职位对象");
                }
            }

            // 添加职业发展建议（仅当找到至少一个匹配的职位时）
            if (!recommendedJobs.isEmpty()) {
                Map<String, Object> careerAdvice = generateCareerAdvice(resume, recommendedJobs);
                Map<String, Object> response = new HashMap<>();
                response.put("jobs", recommendedJobs);
                response.put("career_advice", careerAdvice);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(recommendedJobs);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 生成个性化的职位推荐理由
     */
    private String generateRecommendationReason(Resume resume, JobPosition job, List<String> skills) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("我需要你作为一名职业顾问，为求职者推荐一个职位并给出个性化的推荐理由。\n\n");

            // 添加求职者信息
            prompt.append("求职者信息：\n");
            prompt.append("- 教育背景：").append(resume.getEducation()).append("\n");
            prompt.append("- 专业技能：").append(String.join(", ", skills)).append("\n");
            prompt.append("- 工作经验：").append(resume.getWork()).append("\n");
            prompt.append("- 期望职位：").append(resume.getJobTitle()).append("\n\n");

            // 添加职位信息
            prompt.append("职位信息：\n");
            prompt.append("- 职位名称：").append(job.getTitle()).append("\n");
            prompt.append("- 公司名称：").append(job.getCompanyName()).append("\n");
            prompt.append("- 所需技能：").append(job.getRequiredSkills()).append("\n");
            prompt.append("- 职位描述：").append(job.getDescription()).append("\n\n");

            prompt.append("请针对这位求职者的背景和技能，生成一段个性化的职位推荐理由，解释为什么这个职位适合他/她。");
            prompt.append("理由应该分析求职者的技能如何与职位要求匹配，以及这个职位如何有助于他/她的职业发展。");
            prompt.append("回答应简洁有力，不超过100字。\n");

            String aiResponse = openAiChatModel.call(prompt.toString());

            // 清理AI响应，去除可能的引号或格式标记
            return aiResponse.trim().replaceAll("^\"|\"$", "");

        } catch (Exception e) {
            e.printStackTrace();
            return "这个职位与您的技能和经验非常匹配，值得考虑。";
        }
    }

    /**
     * 生成针对特定职位的简历优化建议
     */
    private String generateResumeImprovementSuggestion(Resume resume, JobPosition job) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为一名专业的简历顾问，我需要你分析一位求职者的简历与特定职位的匹配度，并提供简历优化建议。\n\n");

            // 添加求职者简历信息
            prompt.append("求职者简历信息：\n");
            prompt.append("- 教育背景：").append(resume.getEducation()).append("\n");
            prompt.append("- 专业技能：").append(resume.getProfession()).append("\n");
            prompt.append("- 工作经验：").append(resume.getWork()).append("\n");
            prompt.append("- 项目经历：").append(resume.getProject()).append("\n\n");

            // 添加目标职位信息
            prompt.append("目标职位信息：\n");
            prompt.append("- 职位名称：").append(job.getTitle()).append("\n");
            prompt.append("- 所需技能：").append(job.getRequiredSkills()).append("\n");
            prompt.append("- 职位要求：").append(job.getDescription()).append("\n\n");

            prompt.append("请针对这位求职者的简历与目标职位的匹配情况，提供1-2条具体的简历优化建议，");
            prompt.append("帮助求职者提高简历对这个特定职位的竞争力。建议应该简洁明了，不超过50字。\n");

            String aiResponse = openAiChatModel.call(prompt.toString());

            // 清理AI响应
            return aiResponse.trim().replaceAll("^\"|\"$", "");

        } catch (Exception e) {
            e.printStackTrace();
            return "建议突出展示与该职位最相关的技能和经验，并量化您的成就。";
        }
    }

    /**
     * 生成整体职业发展建议
     */
    private Map<String, Object> generateCareerAdvice(Resume resume, List<Map<String, Object>> recommendedJobs) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为一名职业规划专家，我需要你为一位求职者提供职业发展建议。\n\n");

            // 添加求职者背景信息
            prompt.append("求职者背景：\n");
            if (resume.getEducation() != null) prompt.append("- 教育背景：").append(resume.getEducation()).append("\n");
            if (resume.getProfession() != null) prompt.append("- 专业技能：").append(resume.getProfession()).append("\n");
            if (resume.getWork() != null) prompt.append("- 工作经验：").append(resume.getWork()).append("\n");
            if (resume.getJobTitle() != null) prompt.append("- 期望职位：").append(resume.getJobTitle()).append("\n\n");

            // 添加推荐职位信息
            prompt.append("推荐职位：\n");
            for (int i = 0; i < Math.min(3, recommendedJobs.size()); i++) {
                Map<String, Object> job = recommendedJobs.get(i);
                prompt.append("- ").append(job.get("title")).append(" at ").append(job.get("company_name")).append("\n");
            }
            prompt.append("\n");

            prompt.append("请提供以下三个方面的建议，每个建议1-2句话即可：\n");
            prompt.append("1. 短期职业目标：未来1年内应该如何发展\n");
            prompt.append("2. 技能提升建议：应该学习或提升哪些技能\n");
            prompt.append("3. 长期职业规划：3-5年的职业发展方向\n");
            prompt.append("请按照JSON格式返回，如下所示：\n");
            prompt.append("{\n");
            prompt.append("  \"short_term\": \"短期建议\",\n");
            prompt.append("  \"skills_improvement\": \"技能提升建议\",\n");
            prompt.append("  \"long_term\": \"长期规划\"\n");
            prompt.append("}\n");

            String aiResponse = openAiChatModel.call(prompt.toString());

            // 尝试解析JSON响应
            try {
                String cleanedResponse = cleanAiResponse(aiResponse);
                return objectMapper.readValue(cleanedResponse, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
                // 返回备用建议
                Map<String, Object> fallbackAdvice = new HashMap<>();
                fallbackAdvice.put("short_term", "专注于提升与推荐职位相关的核心技能，参与相关项目积累经验。");
                fallbackAdvice.put("skills_improvement", "关注行业技术趋势，学习推荐职位所需的新兴技能。");
                fallbackAdvice.put("long_term", "逐步积累专业领域影响力，向高级职位或管理岗位发展。");
                return fallbackAdvice;
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 返回备用建议
            Map<String, Object> fallbackAdvice = new HashMap<>();
            fallbackAdvice.put("short_term", "专注于提升与推荐职位相关的核心技能，参与相关项目积累经验。");
            fallbackAdvice.put("skills_improvement", "关注行业技术趋势，学习推荐职位所需的新兴技能。");
            fallbackAdvice.put("long_term", "逐步积累专业领域影响力，向高级职位或管理岗位发展。");
            return fallbackAdvice;
        }
    }

    /**
     * 清理AI响应中的JSON
     */
    private String cleanAiResponse(String aiResponse) {
        // 移除可能的前缀和后缀文本
        String cleaned = aiResponse.trim();

        // 查找第一个 { 和最后一个 } 的位置
        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }

        return cleaned;
    }

    /**
     * 从简历中提取推荐所需的数据
     */
    private Map<String, Object> extractResumeData(Resume resume) {
        Map<String, Object> data = new HashMap<>();

        try {
            // 从专业技能中提取技能列表
            if (resume.getProfession() != null && !resume.getProfession().isEmpty()) {
                // 提取技能关键词
                List<String> skills = extractSkillsFromProfession(resume.getProfession());
                data.put("skills", String.join(", ", skills));
            }

            // 提取教育信息
            if (resume.getEducation() != null && !resume.getEducation().isEmpty()) {
                try {
                    Map<?, ?> educationMap = objectMapper.readValue(resume.getEducation(), Map.class);
                    if (educationMap.get("degree") != null) {
                        data.put("educationLevel", educationMap.get("degree").toString());
                    }
                } catch (Exception e) {
                    // 尝试解析为数组
                    try {
                        List<?> educationList = objectMapper.readValue(resume.getEducation(), List.class);
                        if (!educationList.isEmpty() && educationList.get(0) instanceof Map) {
                            Map<?, ?> firstEducation = (Map<?, ?>) educationList.get(0);
                            if (firstEducation.get("degree") != null) {
                                data.put("educationLevel", firstEducation.get("degree").toString());
                            }
                        }
                    } catch (Exception ex) {
                        // 解析失败时忽略
                    }
                }
            }

            // 提取工作经验
            if (resume.getWork() != null && !resume.getWork().isEmpty()) {
                // 从工作经历中估算工作年限
                try {
                    // 尝试解析为对象
                    Map<?, ?> workMap = objectMapper.readValue(resume.getWork(), Map.class);
                    if (workMap.containsKey("period") && workMap.get("period") instanceof List) {
                        List<?> periodList = (List<?>) workMap.get("period");
                        if (periodList.size() >= 2) {
                            // 简单估算：假设1个工作经历 = 1年
                            data.put("experience", periodList.size() / 2);
                        }
                    }
                } catch (Exception e) {
                    // 尝试解析为数组
                    try {
                        List<?> workList = objectMapper.readValue(resume.getWork(), List.class);
                        // 简单估算：工作经历数量 = 工作年限
                        if (!workList.isEmpty()) {
                            data.put("experience", workList.size());
                        } else {
                            data.put("experience", 0);
                        }
                    } catch (Exception ex) {
                        // 解析失败时，使用默认值
                        data.put("experience", 0);
                    }
                }
            } else {
                data.put("experience", 0);
            }

            // 提取位置信息，可以从期望职位或求职状态中获取
            if (resume.getJobStatus() != null && !resume.getJobStatus().isEmpty()) {
                data.put("location", resume.getJobStatus());
            }

            // 如果从jobStatus获取不到位置信息，可以从jobTitle尝试提取
            if ((!data.containsKey("location") || data.get("location") == null) &&
                resume.getJobTitle() != null && !resume.getJobTitle().isEmpty()) {
                data.put("location", resume.getJobTitle());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * 从专业技能字段中提取技能列表
     */
    private List<String> extractSkillsFromProfession(String professionJson) {
        List<String> skills = new ArrayList<>();

        try {
            // 尝试解析为数组
            try {
                List<?> professionList = objectMapper.readValue(professionJson, List.class);
                for (Object item : professionList) {
                    if (item instanceof Map) {
                        Map<?, ?> professionItem = (Map<?, ?>) item;
                        if (professionItem.containsKey("content")) {
                            skills.add(professionItem.get("content").toString());
                        }
                    } else if (item instanceof String) {
                        skills.add(item.toString());
                    }
                }
            } catch (Exception e) {
                // 如果不是数组，尝试解析为对象
                try {
                    Map<?, ?> professionMap = objectMapper.readValue(professionJson, Map.class);
                    if (professionMap.containsKey("skill")) {
                        String skillText = professionMap.get("skill").toString();
                        // 按照常见分隔符分割技能
                        String[] skillArray = skillText.split("[,;，；、\n]");
                        skills.addAll(Arrays.asList(skillArray));
                    }
                } catch (Exception ex) {
                    // 如果也不是对象，直接使用文本
                    // 按照常见分隔符分割技能
                    String[] skillArray = professionJson.split("[,;，；、\n]");
                    skills.addAll(Arrays.asList(skillArray));
                }
            }

            // 清理空白字符
            skills = skills.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            // 如果没有提取到任何技能，使用一些通用技能关键词
            if (skills.isEmpty()) {
                skills.add("通用技能");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 出错时返回默认值
            skills.add("通用技能");
        }

        return skills;
    }

    /**
     * 计算职位与用户技能、条件的匹配得分
     */
    private int calculateMatchScore(String requirements, List<String> skills, String educationLevel,
                                   Integer experience, String jobLocation) {
        int score = 0;

        // 技能匹配得分（每匹配一个技能加10分）
        if (requirements != null) {
            for (String skill : skills) {
                if (requirements.toLowerCase().contains(skill.toLowerCase())) {
                    score += 10;
                }
            }
        }

        // 教育水平匹配（如果匹配则加15分）
        if (educationLevel != null && requirements != null &&
            requirements.toLowerCase().contains(educationLevel.toLowerCase())) {
            score += 15;
        }

        // 工作经验匹配（如果用户经验充足则加20分）
        if (experience != null && requirements != null) {
            String lowerReq = requirements.toLowerCase();
            if (lowerReq.contains(experience + " 年") ||
                lowerReq.contains(experience + "+ 年") ||
                lowerReq.contains("低于 " + (experience + 1) + " 年")) {
                score += 20;
            }
        }

        // 位置匹配（如果匹配则加15分）
        if (jobLocation != null && !jobLocation.isEmpty()) {
            score += 15;
        }

        return score;
    }
}
