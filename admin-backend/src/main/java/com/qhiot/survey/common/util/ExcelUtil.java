package com.qhiot.survey.common.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel工具类
 */
public class ExcelUtil {

    /**
     * 读取Excel文件，返回数据列表
     * @param file Excel文件
     * @param headerCount 表头列数
     * @return 数据列表，每行数据是一个Map，key为列索引
     */
    public static List<Map<Integer, String>> readExcel(MultipartFile file, int headerCount) throws IOException {
        List<Map<Integer, String>> dataList = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            
            // 从第二行开始读取（第一行是表头）
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                Map<Integer, String> rowData = new HashMap<>();
                for (int j = 0; j < headerCount; j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(j, getCellValue(cell));
                }
                dataList.add(rowData);
            }
        }
        
        return dataList;
    }

    /**
     * 创建Excel工作簿
     * @param headers 表头数组
     * @param dataList 数据列表
     * @return Workbook对象
     */
    public static Workbook createExcel(String[] headers, List<Map<String, Object>> dataList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        
        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        for (int i = 0; i < dataList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, Object> data = dataList.get(i);
            for (int j = 0; j < headers.length; j++) {
                Cell cell = row.createCell(j);
                Object value = data.get(headers[j]);
                if (value != null) {
                    cell.setCellValue(value.toString());
                }
            }
        }
        
        return workbook;
    }

    /**
     * 获取单元格值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字，避免科学计数法
                    double num = cell.getNumericCellValue();
                    if (num == (long) num) {
                        return String.valueOf((long) num);
                    }
                    return String.valueOf(num);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}