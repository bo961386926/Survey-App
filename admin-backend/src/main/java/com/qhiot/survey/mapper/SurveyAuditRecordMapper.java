package com.qhiot.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhiot.survey.entity.SurveyAuditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SurveyAuditRecordMapper extends BaseMapper<SurveyAuditRecord> {
    
    /**
     * 查询勘察结果最新的审核记录
     * @param resultId 勘察结果ID
     * @return 最新的审核记录
     */
    @Select("SELECT id, result_id, auditor_id, audit_status, audit_comment, " +
            "audit_time, create_time, update_time, create_by, update_by " +
            "FROM survey_audit_record " +
            "WHERE result_id = #{resultId} " +
            "ORDER BY create_time DESC LIMIT 1")
    SurveyAuditRecord selectLatestByResultId(@Param("resultId") Long resultId);
}
