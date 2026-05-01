package com.qhiot.survey.common.util;

import com.qhiot.survey.service.SysDictionaryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * 数据字典工具类
 * 参考qh_ol项目的DictUtil设计
 */
@Component
public class DictUtil {

    private static SysDictionaryDataService sysDictionaryDataService;

    @Autowired
    private SysDictionaryDataService dictionaryDataService;

    @PostConstruct
    public void init() {
        sysDictionaryDataService = this.dictionaryDataService;
    }

    private DictUtil() {
    }

    /**
     * 获取字典Map（值->名称）
     *
     * @param dictCode 字典代码
     * @return 字典Map
     */
    public static Map<String, String> getDictMap(String dictCode) {
        if (sysDictionaryDataService == null) {
            return Map.of();
        }
        return sysDictionaryDataService.getDictMap(dictCode);
    }

    /**
     * 根据字典代码和值获取名称
     *
     * @param dictCode 字典代码
     * @param dataValue 字典值
     * @return 字典名称
     */
    public static String getDictName(String dictCode, String dataValue) {
        if (sysDictionaryDataService == null) {
            return dataValue;
        }
        return sysDictionaryDataService.getDictName(dictCode, dataValue);
    }

    /**
     * 根据字典代码和值获取名称（整型值）
     *
     * @param dictCode 字典代码
     * @param dataValue 字典值
     * @return 字典名称
     */
    public static String getDictName(String dictCode, Integer dataValue) {
        return getDictName(dictCode, String.valueOf(dataValue));
    }

    /**
     * 根据字典代码和名称获取值
     *
     * @param dictCode 字典代码
     * @param dataName 字典名称
     * @return 字典值
     */
    public static String getDictValue(String dictCode, String dataName) {
        if (sysDictionaryDataService == null) {
            return dataName;
        }
        return sysDictionaryDataService.getDictValue(dictCode, dataName);
    }

    /**
     * 获取所有字典数据
     *
     * @return 所有字典数据
     */
    public static Map<String, Map<String, String>> getAllDictMaps() {
        if (sysDictionaryDataService == null) {
            return Map.of();
        }
        return sysDictionaryDataService.getAllDictMaps();
    }

    /**
     * 刷新字典缓存
     */
    public static void refreshCache() {
        if (sysDictionaryDataService != null) {
            sysDictionaryDataService.refreshCache();
        }
    }
}