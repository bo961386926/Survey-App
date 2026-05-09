package com.qhiot.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.dto.SurveyPointDTO;
import com.qhiot.survey.entity.SurveyPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface SurveyPointMapper extends BaseMapper<SurveyPoint> {
    List<Map<String, Object>> selectPointsWithStatus(@Param("projectId") Long projectId);
    
    /**
     * 分页查询点位列表（关联项目名称）
     */
    Page<SurveyPointDTO> selectPointPageWithProject(
            Page<SurveyPointDTO> page,
            @Param("projectId") Long projectId,
            @Param("sectionId") Long sectionId,
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );
}
