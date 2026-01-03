/**
 * @projectName springAi
 * @package com.example.springai.service.impl
 * @className com.example.springai.service.impl.HistoryServiceImpl
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springai.mapper.HistoryMapper;
import com.example.springai.model.History;
import com.example.springai.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HistoryServiceImpl implements HistoryService {

    @Autowired
    private HistoryMapper historyMapper;

    @Override
    public Page<History> getHistoryList(String userId, int pageNum, int pageSize) {
        Page<History> page = new Page<>(pageNum, pageSize);
        QueryWrapper<History> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .orderByDesc("time");
        return historyMapper.selectPage(page, wrapper);
    }

    @Override
    public History getHistoryDetail(String historyId) {
        return historyMapper.selectById(historyId);
    }

    @Override
    public void saveHistory(History history) {
        historyMapper.insert(history);
    }

}
