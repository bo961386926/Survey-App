package com.qhiot.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhiot.survey.entity.SurveyPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface SurveyPointMapper extends BaseMapper<SurveyPoint> {
    List<Map<String, Object>> selectPointsWithStatus(@Param("projectId") Long projectId);
}
