/**
 * @projectName springAi
 * @package com.example.springai.service
 * @className com.example.springai.service.HistoryService
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springai.model.History;

public interface HistoryService {

    void saveHistory(History history);

    Page<History> getHistoryList(String userId, int pageNum, int pageSize);

    History getHistoryDetail(String historyId);
}
