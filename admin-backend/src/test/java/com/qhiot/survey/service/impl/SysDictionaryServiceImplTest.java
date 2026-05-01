package com.qhiot.survey.service.impl;

import com.qhiot.survey.entity.SysDictionary;
import com.qhiot.survey.entity.SysDictionaryData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysDictionaryService 简单单元测试
 * 测试实体类和基本功能
 */
class SysDictionaryServiceImplTest {

    @Test
    void testSysDictionaryEntity() {
        // Given
        SysDictionary dictionary = new SysDictionary();
        dictionary.setId(1L);
        dictionary.setDictCode("POINT_STATUS");
        dictionary.setDictName("点位状态");
        dictionary.setStatus(1);
        dictionary.setDescription("点位状态字典");

        // When & Then
        assertNotNull(dictionary);
        assertEquals(1L, dictionary.getId());
        assertEquals("POINT_STATUS", dictionary.getDictCode());
        assertEquals("点位状态", dictionary.getDictName());
        assertEquals(1, dictionary.getStatus());
    }

    @Test
    void testSysDictionaryDataEntity() {
        // Given
        SysDictionaryData data = new SysDictionaryData();
        data.setId(1L);
        data.setDictId(1L);
        data.setDictCode("POINT_STATUS");
        data.setDataValue("0");
        data.setDataName("待采集");
        data.setDataOrder(1);
        data.setStatus(1);
        data.setIsReadonly(0);

        // When & Then
        assertNotNull(data);
        assertEquals(1L, data.getId());
        assertEquals("POINT_STATUS", data.getDictCode());
        assertEquals("0", data.getDataValue());
        assertEquals("待采集", data.getDataName());
        assertEquals(1, data.getDataOrder());
    }

    @Test
    void testDictMap() {
        // Given
        Map<String, String> dictMap = new HashMap<>();
        dictMap.put("0", "待采集");
        dictMap.put("1", "采集中");
        dictMap.put("2", "已完成");

        // When & Then
        assertEquals(3, dictMap.size());
        assertEquals("待采集", dictMap.get("0"));
        assertEquals("采集中", dictMap.get("1"));
        assertEquals("已完成", dictMap.get("2"));
    }

    @Test
    void testDictionaryDataConversion() {
        // Given
        SysDictionaryData data = new SysDictionaryData();
        data.setDataValue("1");
        data.setDataName("启用");

        // When
        Map<String, String> result = new HashMap<>();
        result.put("value", data.getDataValue());
        result.put("label", data.getDataName());

        // Then
        assertEquals("1", result.get("value"));
        assertEquals("启用", result.get("label"));
    }
}