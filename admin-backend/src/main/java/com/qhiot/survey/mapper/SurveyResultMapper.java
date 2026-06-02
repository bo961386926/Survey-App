package com.qhiot.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhiot.survey.entity.SurveyResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SurveyResultMapper extends BaseMapper<SurveyResult> {
    
    /**
     * 查询点位最新的已审核勘察结果
     * @param pointId 点位ID
     * @return 最新的已审核结果
     */
    @Select("SELECT * FROM survey_result " +
            "WHERE point_id = #{pointId} AND is_deleted = 0 " +
            "ORDER BY version_no DESC LIMIT 1")
    SurveyResult selectLatestByPointId(@Param("pointId") Long pointId);
}
