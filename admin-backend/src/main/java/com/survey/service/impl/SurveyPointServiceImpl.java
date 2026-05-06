package com.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.survey.entity.SurveyPoint;
import com.survey.mapper.SurveyPointMapper;
import com.survey.service.SurveyPointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SurveyPointServiceImpl extends ServiceImpl<SurveyPointMapper, SurveyPoint> implements SurveyPointService {

    @Override
    public List<SurveyPoint> getPointList(Long projectId) {
        QueryWrapper<SurveyPoint> queryWrapper = new QueryWrapper<>();
        if (projectId != null) {
            queryWrapper.eq("project_id", projectId);
        }
        return list(queryWrapper);
    }

    @Override
    public SurveyPoint getPointById(Long id) {
        return getById(id);
    }

    @Override
    @Transactional
    public boolean createPoint(SurveyPoint point) {
        return save(point);
    }

    @Override
    @Transactional
    public boolean updatePoint(SurveyPoint point) {
        return updateById(point);
    }

    @Override
    @Transactional
    public boolean deletePoint(Long id) {
        return removeById(id);
    }

    @Override
    public Map<String, Object> getPointDetail(Long id) {
        SurveyPoint point = getById(id);
        if (point == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("point", point);
        // TODO: 查询最新勘查结果
        return result;
    }
}