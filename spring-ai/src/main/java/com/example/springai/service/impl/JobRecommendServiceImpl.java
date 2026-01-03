package com.example.springai.service.impl;

import com.example.springai.dto.JobRecommendRequest;
import com.example.springai.mapper.JobPositionMapper;
import com.example.springai.model.JobPosition;
import com.example.springai.service.JobRecommendService;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobRecommendServiceImpl implements JobRecommendService {

    @Autowired
    private JobPositionMapper jobPositionMapper;
    
    @Autowired
    private OpenAiChatModel openAiChatModel;
    
    @Override
    public Map<String, Object> recommendJobs(JobRecommendRequest request) {
        // 1. 获取基于内容的职位推荐
        List<JobPosition> recommendedJobs = contentBasedRecommendation(request);
        
        // 2. 为每个推荐的职位生成推荐理由
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (JobPosition job : recommendedJobs) {
            Map<String, Object> jobResult = new HashMap<>();
            jobResult.put("job", job);
            jobResult.put("reason", generateRecommendationReason(request.getSkills(), job));
            // 计算匹配度，用于排序
            double matchScore = calculateMatchScore(request.getSkills(), job);
            jobResult.put("matchScore", matchScore);
            resultList.add(jobResult);
        }
        
        // 3. 按匹配度排序
        resultList.sort((a, b) -> Double.compare((Double) b.get("matchScore"), (Double) a.get("matchScore")));
        
        Map<String, Object> result = new HashMap<>();
        result.put("recommendations", resultList);
        return result;
    }

    @Override
    public List<JobPosition> contentBasedRecommendation(JobRecommendRequest request) {
        // 获取所有职位
        List<JobPosition> allJobs = jobPositionMapper.selectList(null);
        
        // 如果没有提供任何技能信息，返回空列表
        if (request.getSkills() == null || request.getSkills().isEmpty()) {
            if (request.getResumeText() == null || request.getResumeText().isEmpty()) {
                return new ArrayList<>();
            }
            // 如果提供了简历文本但没有明确的技能列表，可以尝试从简历中提取关键词
            request.setSkills(extractSkillsFromResume(request.getResumeText()));
        }
        
        // 计算每个职位与用户技能的匹配度
        Map<JobPosition, Double> jobScores = new HashMap<>();
        for (JobPosition job : allJobs) {
            double score = calculateMatchScore(request.getSkills(), job);
            
            // 考虑教育水平和经验要求的匹配度
            if (request.getEducationLevel() != null && !request.getEducationLevel().isEmpty()) {
                score += calculateEducationMatch(request.getEducationLevel(), job.getEducationRequirement());
            }
            
            if (request.getExperienceYears() != null && !request.getExperienceYears().isEmpty()) {
                score += calculateExperienceMatch(request.getExperienceYears(), job.getExperienceRequirement());
            }
            
            // 考虑地点匹配
            if (request.getPreferredLocation() != null && !request.getPreferredLocation().isEmpty() 
                    && job.getLocation() != null && !job.getLocation().isEmpty()) {
                if (job.getLocation().contains(request.getPreferredLocation())) {
                    score += 0.1; // 地点匹配加分
                }
            }
            
            jobScores.put(job, score);
        }
        
        // 按匹配度排序，并取前5个职位
        return jobScores.entrySet().stream()
                .sorted(Map.Entry.<JobPosition, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public String generateRecommendationReason(List<String> userSkills, JobPosition job) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位职业规划顾问，现在需要你为一位求职者推荐职位并给出理由。\n\n");
        prompt.append("求职者技能：").append(String.join(", ", userSkills)).append("\n\n");
        prompt.append("推荐职位：").append(job.getTitle()).append("\n");
        prompt.append("职位所需技能：").append(job.getRequiredSkills()).append("\n");
        prompt.append("职位描述：").append(job.getDescription()).append("\n\n");
        prompt.append("请简洁地说明为什么这个职位适合这位求职者，主要基于技能匹配度、职业发展前景等方面。回答不要超过100字。");
        
        // 调用OpenAI生成推荐理由
        return openAiChatModel.call(prompt.toString());
    }
    
    /**
     * 计算用户技能与职位的匹配度
     */
    private double calculateMatchScore(List<String> userSkills, JobPosition job) {
        if (userSkills == null || userSkills.isEmpty() || job.getRequiredSkills() == null) {
            return 0.0;
        }
        
        // 将职位要求的技能转为小写并分割为列表
        String[] jobSkillsArray = job.getRequiredSkills().toLowerCase().split(",|，|\\s+");
        List<String> jobSkills = Arrays.asList(jobSkillsArray);
        
        // 计算匹配的技能数量
        int matchCount = 0;
        for (String userSkill : userSkills) {
            for (String jobSkill : jobSkills) {
                if (jobSkill.contains(userSkill.toLowerCase()) || userSkill.toLowerCase().contains(jobSkill)) {
                    matchCount++;
                    break;
                }
            }
        }
        
        // 计算匹配度分数
        double matchScore = (double) matchCount / userSkills.size();
        
        return matchScore;
    }
    
    /**
     * 从简历文本中提取技能关键词
     */
    private List<String> extractSkillsFromResume(String resumeText) {
        if (resumeText == null || resumeText.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 这里可以使用更复杂的NLP技术提取关键词
        // 简单实现：使用预定义的技能关键词列表进行匹配
        String[] commonSkills = {
            "java", "python", "c++", "javascript", "html", "css", "vue", "react", "angular",
            "spring", "node.js", "sql", "mysql", "mongodb", "redis", "docker", "kubernetes",
            "aws", "linux", "git", "ci/cd", "微服务", "分布式系统", "设计模式", "数据结构", "算法",
            "产品设计", "ui设计", "ux设计", "photoshop", "sketch", "figma", "illustrator",
            "市场营销", "销售", "商务拓展", "客户关系", "谈判", "市场分析",
            "人力资源", "招聘", "培训", "绩效管理", "财务分析", "会计",
            "教育", "培训", "课程设计", "教学",
            "运营", "内容运营", "用户运营", "活动策划", "社区运营",
            "数据分析", "机器学习", "深度学习", "人工智能", "大数据"
        };
        
        List<String> extractedSkills = new ArrayList<>();
        String lowerResumeText = resumeText.toLowerCase();
        
        for (String skill : commonSkills) {
            if (lowerResumeText.contains(skill.toLowerCase())) {
                extractedSkills.add(skill);
            }
        }
        
        return extractedSkills;
    }
    
    /**
     * 计算教育水平匹配度
     */
    private double calculateEducationMatch(String userEducation, String jobEducation) {
        if (userEducation == null || jobEducation == null) {
            return 0.0;
        }
        
        // 教育水平等级
        Map<String, Integer> educationLevels = new HashMap<>();
        educationLevels.put("不限", 0);
        educationLevels.put("高中", 1);
        educationLevels.put("大专", 2);
        educationLevels.put("本科", 3);
        educationLevels.put("硕士", 4);
        educationLevels.put("博士", 5);
        
        int userLevel = educationLevels.getOrDefault(userEducation, 0);
        int jobLevel = educationLevels.getOrDefault(jobEducation, 0);
        
        // 如果用户教育水平达到或超过职位要求，给予满分
        if (userLevel >= jobLevel) {
            return 0.2;
        } else {
            return 0.0;
        }
    }
    
    /**
     * 计算工作经验匹配度
     */
    private double calculateExperienceMatch(String userExperience, String jobExperience) {
        if (userExperience == null || jobExperience == null) {
            return 0.0;
        }
        
        try {
            int userYears = Integer.parseInt(userExperience);
            int jobYears = Integer.parseInt(jobExperience);
            
            // 如果用户经验达到或超过职位要求，给予满分
            if (userYears >= jobYears) {
                return 0.2;
            } else if (userYears >= jobYears - 1) {
                // 如果只差一年，给部分分数
                return 0.1;
            } else {
                return 0.0;
            }
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
} 