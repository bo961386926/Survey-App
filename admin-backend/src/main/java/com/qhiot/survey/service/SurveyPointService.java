package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.dto.SurveyPointDTO;
import com.qhiot.survey.entity.SurveyPoint;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface SurveyPointService extends IService<SurveyPoint> {

    /**
     * 根据项目ID获取点位列表
     */
    List<SurveyPoint> getPointsByProjectId(Long projectId);

    /**
     * 获取当前用户可访问的点位列表，可按项目过滤
     */
    List<SurveyPoint> getAccessiblePoints(Long projectId);

    /**
     * 获取当前用户可访问的点位详情
     */
    SurveyPoint getAccessiblePointById(Long id);

    /**
     * 创建点位
     */
    SurveyPoint createPoint(SurveyPoint point);

    /**
     * 更新点位
     */
    SurveyPoint updatePoint(Long id, SurveyPoint point);

    /**
     * 删除点位
     */
    void deletePoint(Long id);

    /**
     * 根据状态获取点位列表
     */
    List<SurveyPoint> getPointsByStatus(Integer status);

    /**
     * 批量创建点位
     */
    boolean batchCreatePoints(List<SurveyPoint> points);

    /**
     * 分页查询点位列表
     */
    Page<SurveyPoint> listByPage(Long projectId, Long sectionId, String keyword, Integer status, Integer pageNum, Integer pageSize);
    
    /**
     * 分页查询点位列表（包含项目名称）
     */
    Page<SurveyPointDTO> listByPageWithProject(Long projectId, Long sectionId, String keyword, Integer status, Integer pageNum, Integer pageSize);

    /**
     * Excel导入点位
     */
    Map<String, Object> importFromExcel(MultipartFile file, Long projectId);

    /**
     * 批量分配点位
     */
    void batchAssign(Long projectId, List<Long> pointIds, Long assigneeId);

    /**
     * 点位作废
     */
    void invalidatePoint(Long id, String reason);

    /**
     * 获取点位历史版本
     */
    List<Map<String, Object>> getPointHistory(Long pointId);

    /**
     * 批量设置排口类型
     */
    void batchSetOutfallType(List<Long> pointIds, String outfallType);

    // =========== 地图相关 API ===========

    /**
     * 获取地图点位数据（含经纬度+状态），支持项目/标段/状态筛选
     */
    List<SurveyPointDTO> getMapPoints(Long projectId, Long sectionId, Integer status);

    /**
     * 获取点位地图状态统计
     */
    Map<String, Long> getMapPointStatistics(Long projectId);

    /**
     * 获取异常点位列表
     */
    List<SurveyPointDTO> getAbnormalPoints(Long projectId);
}
