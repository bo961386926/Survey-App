package com.qhiot.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhiot.survey.entity.OfflineDataSync;
import org.apache.ibatis.annotations.Mapper;

/**
 * 离线数据同步Mapper
 */
@Mapper
public interface OfflineDataSyncMapper extends BaseMapper<OfflineDataSync> {
}
