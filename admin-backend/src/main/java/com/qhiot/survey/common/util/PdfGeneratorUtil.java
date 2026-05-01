package com.qhiot.survey.common.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * PDF生成工具类
 * 用于生成单点位勘察报告PDF
 */
@Slf4j
public class PdfGeneratorUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成单点位勘察报告PDF
     *
     * @param pointData 点位数据
     * @param surveyData 勘察数据
     * @param auditData 审核数据（可为null）
     * @return PDF字节数组
     */
    public static byte[] generateSurveyReportPdf(Map<String, Object> pointData,
                                                  Map<String, Object> surveyData,
                                                  Map<String, Object> auditData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // 创建PDF文档
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            // 设置中文字体（使用iText内置的字体）
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            document.setFont(font);
            document.setFontSize(12);

            // 设置页边距
            document.setMargins(36, 36, 36, 36);

            // 标题
            Paragraph title = new Paragraph("项目勘察报告")
                    .setFont(font)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLACK);
            document.add(title);

            // 报告编号和生成时间
            Paragraph subtitle = new Paragraph("报告编号: " + generateReportNo())
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY);
            document.add(subtitle);

            Paragraph genTime = new Paragraph("生成时间: " + java.time.LocalDateTime.now().format(DATE_FORMAT))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY);
            document.add(genTime);

            // 空行
            document.add(new Paragraph(""));

            // 点位基本信息
            document.add(createSectionTitle("一、点位基本信息"));
            document.add(createInfoTable(pointData, font));

            // 空行
            document.add(new Paragraph(""));

            // 勘察数据
            document.add(createSectionTitle("二、勘察数据"));
            document.add(createSurveyTable(surveyData, font));

            // 空行
            document.add(new Paragraph(""));

            // 审核信息（如果有）
            if (auditData != null && !auditData.isEmpty()) {
                document.add(createSectionTitle("三、审核信息"));
                document.add(createAuditTable(auditData, font));
            }

            // 空行
            document.add(new Paragraph(""));

            // 声明
            document.add(createSectionTitle("声明"));
            Paragraph declaration = new Paragraph(
                    "本报告由系统自动生成，仅作为勘察工作的记录参考。" +
                    "报告内容应真实、准确、完整，如有异议请及时联系项目负责人。"
            ).setFont(font).setFontSize(10);
            document.add(declaration);

            document.close();
            pdf.close();

            log.info("PDF生成成功，大小: {} bytes", baos.size());

        } catch (IOException e) {
            log.error("PDF生成失败", e);
            throw new RuntimeException("PDF生成失败: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    /**
     * 创建章节标题
     */
    private static Paragraph createSectionTitle(String title) {
        return new Paragraph(title)
                .setBold()
                .setFontSize(14)
                .setMarginBottom(10)
                .setFontColor(ColorConstants.BLACK);
    }

    /**
     * 创建点位基本信息表格
     */
    private static Table createInfoTable(Map<String, Object> data, PdfFont font) {
        float[] columnWidths = {150, 350};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // 表头
        table.addHeaderCell(createHeaderCell("字段", font));
        table.addHeaderCell(createHeaderCell("内容", font));

        // 数据行
        addTableRow(table, "点位编号", getStringValue(data, "pointCode"), font);
        addTableRow(table, "点位名称", getStringValue(data, "pointName"), font);
        addTableRow(table, "排口类型", getStringValue(data, "outfallType"), font);
        addTableRow(table, "所属项目", getStringValue(data, "projectName"), font);
        addTableRow(table, "所属标段", getStringValue(data, "sectionName"), font);
        addTableRow(table, "经度", getStringValue(data, "longitude"), font);
        addTableRow(table, "纬度", getStringValue(data, "latitude"), font);
        addTableRow(table, "点位状态", getStringValue(data, "pointStatus"), font);
        addTableRow(table, "采集人员", getStringValue(data, "collectorName"), font);
        addTableRow(table, "采集时间", getStringValue(data, "collectTime"), font);

        return table;
    }

    /**
     * 创建勘察数据表格
     */
    private static Table createSurveyTable(Map<String, Object> data, PdfFont font) {
        float[] columnWidths = {150, 350};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // 表头
        table.addHeaderCell(createHeaderCell("字段", font));
        table.addHeaderCell(createHeaderCell("内容", font));

        // 动态添加勘察表单数据
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                addTableRow(table, entry.getKey(), String.valueOf(entry.getValue()), font);
            }
        }

        return table;
    }

    /**
     * 创建审核信息表格
     */
    private static Table createAuditTable(Map<String, Object> data, PdfFont font) {
        float[] columnWidths = {150, 350};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // 表头
        table.addHeaderCell(createHeaderCell("字段", font));
        table.addHeaderCell(createHeaderCell("内容", font));

        addTableRow(table, "审核状态", getStringValue(data, "auditStatus"), font);
        addTableRow(table, "审核人员", getStringValue(data, "auditorName"), font);
        addTableRow(table, "审核时间", getStringValue(data, "auditTime"), font);
        addTableRow(table, "审核意见", getStringValue(data, "auditComment"), font);

        return table;
    }

    /**
     * 创建表头单元格
     */
    private static Cell createHeaderCell(String text, PdfFont font) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text).setFont(font).setBold());
        cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        cell.setTextAlignment(TextAlignment.CENTER);
        return cell;
    }

    /**
     * 添加表格行
     */
    private static void addTableRow(Table table, String label, String value, PdfFont font) {
        table.addCell(createCell(label, font, true));
        table.addCell(createCell(value != null ? value : "-", font, false));
    }

    /**
     * 创建表格单元格
     */
    private static Cell createCell(String text, PdfFont font, boolean isHeader) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text).setFont(font));
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
        }
        return cell;
    }

    /**
     * 从Map中获取字符串值
     */
    private static String getStringValue(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return "-";
        }
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : "-";
    }

    /**
     * 生成报告编号
     */
    private static String generateReportNo() {
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RPT-" + timestamp + "-" + (int)(Math.random() * 10000);
    }
}