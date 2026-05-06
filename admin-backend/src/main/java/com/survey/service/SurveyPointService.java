package com.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.survey.entity.SurveyPoint;

import java.util.List;
import java.util.Map;

public interface SurveyPointService extends IService<SurveyPoint> {
    List<SurveyPoint> getPointList(Long projectId);
    SurveyPoint getPointById(Long id);
    boolean createPoint(SurveyPoint point);
    boolean updatePoint(SurveyPoint point);
    boolean deletePoint(Long id);
    Map<String, Object> getPointDetail(Long id);
}