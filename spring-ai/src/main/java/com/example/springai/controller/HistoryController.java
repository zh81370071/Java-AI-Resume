/**
 * @projectName springAi
 * @package com.example.springai.controller
 * @className com.example.springai.controller.HistoryController
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springai.model.History;
import com.example.springai.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@CrossOrigin
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping("/getHistory")
    public ResponseEntity<Map<String, Object>> getHistory(
            @RequestParam String userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<History> historyPage = historyService.getHistoryList(userId, pageNum, pageSize);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", historyPage.getRecords());
        response.put("total", historyPage.getTotal());
        response.put("current", historyPage.getCurrent());
        response.put("size", historyPage.getSize());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getHistoryList(
            @RequestParam String userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return getHistory(userId, pageNum, pageSize);
    }

    @GetMapping("/detail/{historyId}")
    public ResponseEntity<History> getHistoryDetail(@PathVariable String historyId) {
        History history = historyService.getHistoryDetail(historyId);
        if (history != null) {
            return ResponseEntity.ok(history);
        }
        return ResponseEntity.notFound().build();
    }
}