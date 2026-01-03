/**
 * @projectName springAi
 * @package com.example.springai.controller
 * @className com.example.springai.controller.ChatController
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.controller;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import com.example.springai.model.User;
import com.example.springai.service.UserService;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.springai.config.uuid;
import com.example.springai.model.History;
import com.example.springai.model.Resume;
import com.example.springai.service.HistoryService;
import com.example.springai.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/ai")
@CrossOrigin
public class ChatController {

    @Autowired
    private OpenAiChatModel openAiChatModel;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private UserService userService;
    @Autowired
    private ResumeService resumeService;
    @Autowired
    private uuid.UuidGenerator uuidGenerator;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            String username = (String) request.get("username");
            String resumeVersion = (String) request.getOrDefault("resumeVersion", "");
            Map<String, Object> formData = (Map<String, Object>) request.getOrDefault("formData", new HashMap<>());
            // 获取个人信息数据（用于联动）
            Map<String, Object> profileData = (Map<String, Object>) request.getOrDefault("profileData", null);

            String prompt = buildResumePrompt(resumeVersion, formData, profileData);
            String aiResponse = openAiChatModel.call(prompt);
            Map<String, Object> structuredResponse = parseAiResponse(aiResponse);

            // 从个人信息中补充基本数据
            if (!structuredResponse.containsKey("error")) {
                // 优先使用个人信息中的数据
                if (profileData != null) {
                    if (profileData.get("name") != null) {
                        structuredResponse.put("name", profileData.get("name"));
                    }
                    if (profileData.get("phone") != null) {
                        structuredResponse.put("phone", profileData.get("phone"));
                    }
                    if (profileData.get("email") != null) {
                        structuredResponse.put("email", profileData.get("email"));
                    }
                    if (profileData.get("jobStatus") != null) {
                        structuredResponse.put("jobStatus", profileData.get("jobStatus"));
                    }
                    // 补充教育信息
                    if (structuredResponse.get("education") instanceof Map) {
                        Map<String, Object> education = (Map<String, Object>) structuredResponse.get("education");
                        if (profileData.get("school") != null && (education.get("school") == null || education.get("school").toString().isEmpty())) {
                            education.put("school", profileData.get("school"));
                        }
                        if (profileData.get("major") != null && (education.get("major") == null || education.get("major").toString().isEmpty())) {
                            education.put("major", profileData.get("major"));
                        }
                        if (profileData.get("degree") != null && (education.get("degree") == null || education.get("degree").toString().isEmpty())) {
                            education.put("degree", profileData.get("degree"));
                        }
                    }
                    // 补充技能信息
                    if (profileData.get("skills") != null && structuredResponse.get("profession") instanceof Map) {
                        Map<String, Object> profession = (Map<String, Object>) structuredResponse.get("profession");
                        String existingSkill = profession.get("skill") != null ? profession.get("skill").toString() : "";
                        String profileSkills = profileData.get("skills").toString();
                        if (!profileSkills.isEmpty() && !existingSkill.contains(profileSkills)) {
                            profession.put("skill", existingSkill.isEmpty() ? profileSkills : existingSkill + "、" + profileSkills);
                        }
                    }
                } else {
                    // 没有个人信息时，使用用户基本信息
                    User user = userService.getUser(userId);
                    if (user != null) {
                        structuredResponse.put("name", user.getUsername());
                        structuredResponse.put("phone", user.getPhone());
                        structuredResponse.put("email", user.getEmail());
                    } else {
                        structuredResponse.put("name", username != null ? username : "");
                    }
                }
                
                // 保存简历到数据库
                resumeService.saveResume(userId, structuredResponse);
            }

            // 保存历史记录
            History history = new History();
            history.setId(uuidGenerator.generateUuid32());
            history.setQuestion(prompt);
            history.setResult(aiResponse);
            history.setUserId(userId);
            history.setUsername(username);
            history.setTime(new Date());
            historyService.saveHistory(history);

            // 返回结构化的响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", structuredResponse);
            response.put("success", true);
            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @GetMapping("/resume/latest")
    public Map<String, Object> getLatestResume(@RequestParam String userId) {
        try {
            Resume resume = resumeService.getLatestResume(userId);
            if (resume == null) {
                throw new RuntimeException("No resume found");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", convertResumeToMap(resume));
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 优化简历模块内容
     * @param request 包含 moduleType(模块类型) 和 content(当前内容)
     * @return 优化后的内容
     */
    @PostMapping("/resume/optimize")
    public Map<String, Object> optimizeResumeModule(@RequestBody Map<String, Object> request) {
        try {
            String moduleType = (String) request.get("moduleType");
            Object content = request.get("content");
            String targetPosition = (String) request.getOrDefault("targetPosition", "");
            
            String prompt = buildOptimizePrompt(moduleType, content, targetPosition);
            String aiResponse = openAiChatModel.call(prompt);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("optimizedContent", parseOptimizedContent(moduleType, aiResponse));
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 优化整份简历
     */
    @PostMapping("/resume/optimize-all")
    public Map<String, Object> optimizeFullResume(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> resumeData = (Map<String, Object>) request.get("resumeData");
            String targetPosition = (String) request.getOrDefault("targetPosition", "");
            
            String prompt = buildFullOptimizePrompt(resumeData, targetPosition);
            String aiResponse = openAiChatModel.call(prompt);
            Map<String, Object> optimizedData = parseAiResponse(aiResponse);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("optimizedResume", optimizedData);
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @PostMapping("/resume/update")
    public Map<String, Object> updateResume(@RequestBody Map<String, Object> request) {
        try {
            String userId = (String) request.get("userId");
            Map<String, Object> resumeData = (Map<String, Object>) request.get("resumeData");
            
            Resume resume = resumeService.updateResume(userId, resumeData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", convertResumeToMap(resume));
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    private Map<String, Object> convertResumeToMap(Resume resume) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("name", resume.getName());
            result.put("phone", resume.getPhone());
            result.put("email", resume.getEmail());
            result.put("avatar", resume.getAvatar());
            result.put("jobStatus", resume.getJobStatus());
            result.put("jobTitle", resume.getJobTitle());
            result.put("salaryExpectation", resume.getSalaryExpectation());
            
            // 解析JSON字符串为对象
            if (resume.getEducation() != null) {
                result.put("education", objectMapper.readValue(resume.getEducation(), Map.class));
            }
            if (resume.getProfession() != null) {
                result.put("profession", objectMapper.readValue(resume.getProfession(), Map.class));
            }
            if (resume.getWork() != null) {
                result.put("work", objectMapper.readValue(resume.getWork(), Map.class));
            }
            if (resume.getProject() != null) {
                result.put("project", objectMapper.readValue(resume.getProject(), Map.class));
            }
            if (resume.getAward() != null) {
                result.put("award", objectMapper.readValue(resume.getAward(), Map.class));
            }
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert resume to map", e);
        }
    }

    private String buildResumePrompt(String resumeVersion, Map<String, Object> formData, Map<String, Object> profileData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请生成一份结构化的简历信息，严格按照以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"jobStatus\": \"在职/离职/应届生\",\n");
        prompt.append("  \"jobTitle\": \"期望职位\",\n");
        prompt.append("  \"salaryExpectation\": \"期望薪资\",\n");
        prompt.append("  \"education\": {\n");
        prompt.append("    \"school\": \"学校名称\",\n");
        prompt.append("    \"major\": \"专业名称\",\n");
        prompt.append("    \"degree\": \"学历\"\n");
        prompt.append("  },\n");
        prompt.append("  \"profession\": {\n");
        prompt.append("    \"skill\": \"技能描述\"\n");
        prompt.append("  },\n");
        prompt.append("  \"work\": {\n");
        prompt.append("    \"company\": \"公司名称\",\n");
        prompt.append("    \"department\": \"部门名称\",\n");
        prompt.append("    \"position\": \"职位名称\",\n");
        prompt.append("    \"details\": \"工作内容描述\"\n");
        prompt.append("  },\n");
        prompt.append("  \"project\": {\n");
        prompt.append("    \"name\": \"项目名称\",\n");
        prompt.append("    \"details\": \"项目描述\"\n");
        prompt.append("  },\n");
        prompt.append("  \"award\": {\n");
        prompt.append("    \"details\": \"获奖情况\"\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");

        // 添加个人信息作为参考
        if (profileData != null) {
            prompt.append("【用户个人信息（请参考并融入简历）】\n");
            if (profileData.get("name") != null) {
                prompt.append("姓名：").append(profileData.get("name")).append("\n");
            }
            if (profileData.get("jobStatus") != null) {
                prompt.append("求职状态：").append(profileData.get("jobStatus")).append("\n");
            }
            if (profileData.get("jobTitle") != null) {
                prompt.append("期望职位：").append(profileData.get("jobTitle")).append("\n");
            }
            if (profileData.get("jobCity") != null) {
                prompt.append("期望城市：").append(profileData.get("jobCity")).append("\n");
            }
            if (profileData.get("salaryMin") != null && profileData.get("salaryMax") != null) {
                prompt.append("期望薪资：").append(profileData.get("salaryMin")).append("K-").append(profileData.get("salaryMax")).append("K\n");
            }
            if (profileData.get("school") != null) {
                prompt.append("毕业院校：").append(profileData.get("school")).append("\n");
            }
            if (profileData.get("major") != null) {
                prompt.append("专业：").append(profileData.get("major")).append("\n");
            }
            if (profileData.get("degree") != null) {
                prompt.append("学历：").append(profileData.get("degree")).append("\n");
            }
            if (profileData.get("skills") != null) {
                prompt.append("技能标签：").append(profileData.get("skills")).append("\n");
            }
            if (profileData.get("selfIntroduction") != null) {
                prompt.append("自我介绍：").append(profileData.get("selfIntroduction")).append("\n");
            }
            prompt.append("\n");
        }

        if ("应届生版".equals(resumeVersion)) {
            prompt.append("基于以下信息生成应届生简历：\n");
            prompt.append("专业：").append(formData.get("major")).append("\n");
            prompt.append("期望职位：").append(formData.get("position")).append("\n");
            prompt.append("补充信息：").append(formData.get("extra")).append("\n");
        } else {
            prompt.append("基于以下信息生成标准简历：\n");
            prompt.append("工作经历：").append(formData.get("experience")).append("\n");
            prompt.append("期望职位：").append(formData.get("position")).append("\n");
            prompt.append("补充信息：").append(formData.get("extra")).append("\n");
        }

        return prompt.toString();
    }

    private Map<String, Object> parseAiResponse(String aiResponse) {
        try {
            // 清理和预处理 AI 响应
            String cleanedResponse = cleanAiResponse(aiResponse);

            // 使用 ObjectMapper 解析 JSON
            Map<String, Object> parsedData = objectMapper.readValue(cleanedResponse, Map.class);

            // 验证必要的字段
            validateResumeData(parsedData);

            return parsedData;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("error", "AI响应解析失败: " + e.getMessage());
            return fallback;
        }
    }

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

    private void validateResumeData(Map<String, Object> data) {
        // 检查必要字段是否存在
        String[] requiredFields = {"jobStatus", "jobTitle", "salaryExpectation", "education", "profession", "work", "project", "award"};

        for (String field : requiredFields) {
            if (!data.containsKey(field)) {
                throw new IllegalArgumentException("缺少必要字段: " + field);
            }
        }

        // 验证嵌套对象
        validateNestedObject(data, "education", new String[]{"school", "major", "degree"});
        validateNestedObject(data, "profession", new String[]{"skill"});
        validateNestedObject(data, "work", new String[]{"company", "department", "position", "details"});
        validateNestedObject(data, "project", new String[]{"name", "details"});
        validateNestedObject(data, "award", new String[]{"details"});
    }

    private void validateNestedObject(Map<String, Object> data, String objectKey, String[] requiredFields) {
        Object obj = data.get(objectKey);
        if (!(obj instanceof Map)) {
            throw new IllegalArgumentException(objectKey + " 必须是一个对象");
        }

        Map<String, Object> nestedObj = (Map<String, Object>) obj;
        for (String field : requiredFields) {
            if (!nestedObj.containsKey(field)) {
                throw new IllegalArgumentException(objectKey + " 缺少必要字段: " + field);
            }
        }
    }

    /**
     * 构建模块优化的 Prompt
     */
    private String buildOptimizePrompt(String moduleType, Object content, String targetPosition) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的简历优化顾问。请优化以下简历内容，使其更加专业、有吸引力。\n");
        prompt.append("【重要】返回的内容必须是纯文本格式，严禁使用任何Markdown标记（如**、##、-、*等），不要使用列表符号，直接用逗号或顿号分隔即可。\n\n");
        
        if (targetPosition != null && !targetPosition.isEmpty()) {
            prompt.append("目标职位：").append(targetPosition).append("\n\n");
        }
        
        // 将content转换为字符串
        String contentStr;
        try {
            if (content instanceof String) {
                contentStr = (String) content;
            } else {
                contentStr = objectMapper.writeValueAsString(content);
            }
        } catch (Exception e) {
            contentStr = content.toString();
        }

        switch (moduleType) {
            case "profession":
                prompt.append("请优化以下专业技能描述，使其更加专业和有条理。\n");
                prompt.append("当前内容：").append(contentStr).append("\n\n");
                prompt.append("请直接返回优化后的技能描述纯文本，不要使用Markdown格式，不要使用列表符号，技能之间用顿号或逗号分隔。");
                break;
            case "work":
                prompt.append("请优化以下工作经历描述，突出成就和贡献，使用STAR法则。\n");
                prompt.append("当前内容：").append(contentStr).append("\n\n");
                prompt.append("请返回JSON格式：{\"company\":\"公司\",\"department\":\"部门\",\"position\":\"职位\",\"details\":\"优化后的工作内容描述（纯文本，不要Markdown）\"}");
                break;
            case "project":
                prompt.append("请优化以下项目经历描述，突出技术栈、职责和成果。\n");
                prompt.append("当前内容：").append(contentStr).append("\n\n");
                prompt.append("请返回JSON格式：{\"name\":\"项目名称\",\"details\":\"优化后的项目描述（纯文本，不要Markdown）\"}");
                break;
            case "award":
                prompt.append("请优化以下荣誉奖项描述，使其更加规范和专业。\n");
                prompt.append("当前内容：").append(contentStr).append("\n\n");
                prompt.append("请直接返回优化后的奖项描述纯文本，不要使用Markdown格式。");
                break;
            default:
                prompt.append("请优化以下内容：\n").append(contentStr);
                prompt.append("\n请直接返回优化后的纯文本，不要使用Markdown格式。");
        }
        
        return prompt.toString();
    }

    /**
     * 构建整份简历优化的 Prompt
     */
    private String buildFullOptimizePrompt(Map<String, Object> resumeData, String targetPosition) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的简历优化顾问。请全面优化以下简历内容，使其更加专业、有吸引力，并针对目标职位进行优化。\n");
        prompt.append("【重要】所有文本内容必须是纯文本格式，严禁使用任何Markdown标记（如**、##、-、*等），不要使用列表符号，直接用逗号或顿号分隔即可。\n\n");
        
        if (targetPosition != null && !targetPosition.isEmpty()) {
            prompt.append("目标职位：").append(targetPosition).append("\n\n");
        }
        
        prompt.append("当前简历内容：\n");
        prompt.append(objectMapper.valueToTree(resumeData).toString()).append("\n\n");
        
        prompt.append("请严格按照以下JSON格式返回优化后的简历：\n");
        prompt.append("{\n");
        prompt.append("  \"jobStatus\": \"在职/离职/应届生\",\n");
        prompt.append("  \"jobTitle\": \"期望职位\",\n");
        prompt.append("  \"salaryExpectation\": \"期望薪资\",\n");
        prompt.append("  \"education\": {\"school\": \"学校\", \"major\": \"专业\", \"degree\": \"学历\"},\n");
        prompt.append("  \"profession\": {\"skill\": \"优化后的技能描述\"},\n");
        prompt.append("  \"work\": {\"company\": \"公司\", \"department\": \"部门\", \"position\": \"职位\", \"details\": \"优化后的工作描述\"},\n");
        prompt.append("  \"project\": {\"name\": \"项目名称\", \"details\": \"优化后的项目描述\"},\n");
        prompt.append("  \"award\": {\"details\": \"优化后的奖项描述\"}\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    /**
     * 解析优化后的内容
     */
    private Object parseOptimizedContent(String moduleType, String aiResponse) {
        try {
            String cleaned = aiResponse.trim();
            
            // 对于需要返回JSON的模块
            if ("work".equals(moduleType) || "project".equals(moduleType)) {
                int start = cleaned.indexOf("{");
                int end = cleaned.lastIndexOf("}");
                if (start >= 0 && end > start) {
                    cleaned = cleaned.substring(start, end + 1);
                    return objectMapper.readValue(cleaned, Map.class);
                }
            }
            
            // 对于返回纯文本的模块
            return cleaned;
        } catch (Exception e) {
            return aiResponse.trim();
        }
    }
}
