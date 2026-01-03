/**
 * @projectName springAi
 * @package com.example.springai.mapper
 * @className com.example.springai.mapper.HistoryMapper
 * @copyright Copyright 2024 Thunisoft, Inc All rights reserved.
 */
package com.example.springai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springai.model.History;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HistoryMapper extends BaseMapper<History> {
}