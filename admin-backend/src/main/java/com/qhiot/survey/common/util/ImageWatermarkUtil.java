package com.qhiot.survey.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 图片水印工具类
 * 用于在勘察图片上添加水印信息（采集人、时间、经纬度）
 */
@Slf4j
public class ImageWatermarkUtil {

    /**
     * 默认水印透明度 (0.0 - 1.0)
     */
    private static final float DEFAULT_ALPHA = 0.7f;

    /**
     * 默认字体大小
     */
    private static final int DEFAULT_FONT_SIZE = 24;

    /**
     * 默认边距
     */
    private static final int DEFAULT_MARGIN = 20;

    /**
     * 默认行高
     */
    private static final int DEFAULT_LINE_HEIGHT = 35;

    /**
     * 为图片添加水印
     *
     * @param originalImage 原始图片输入流
     * @param collector     采集人姓名
     * @param longitude     经度
     * @param latitude      纬度
     * @return 添加水印后的图片字节数组
     */
    public static byte[] addWatermark(InputStream originalImage, String collector, 
                                     Double longitude, Double latitude) throws IOException {
        return addWatermark(originalImage, collector, longitude, latitude, 
                           DEFAULT_ALPHA, DEFAULT_FONT_SIZE);
    }

    /**
     * 为图片添加水印（可自定义透明度和字体大小）
     *
     * @param originalImage 原始图片输入流
     * @param collector     采集人姓名
     * @param longitude     经度
     * @param latitude      纬度
     * @param alpha         透明度 (0.0 - 1.0)
     * @param fontSize      字体大小
     * @return 添加水印后的图片字节数组
     */
    public static byte[] addWatermark(InputStream originalImage, String collector,
                                     Double longitude, Double latitude,
                                     float alpha, int fontSize) throws IOException {
        // 读取原始图片
        BufferedImage original = ImageIO.read(originalImage);
        if (original == null) {
            throw new IOException("无法读取图片");
        }

        // 创建带水印的图片
        BufferedImage watermarked = new BufferedImage(
            original.getWidth(), 
            original.getHeight(), 
            original.getType()
        );

        Graphics2D g2d = watermarked.createGraphics();

        // 绘制原始图片
        g2d.drawImage(original, 0, 0, null);

        // 设置水印透明度
        AlphaComposite alphaComposite = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, alpha
        );
        g2d.setComposite(alphaComposite);

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 设置字体
        Font font = new Font("Microsoft YaHei", Font.BOLD, fontSize);
        g2d.setFont(font);

        // 准备水印文本
        String collectorText = "采集人: " + (collector != null ? collector : "未知");
        String timeText = "时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String locationText = "位置: " + 
            (longitude != null && latitude != null ? 
             String.format("%.6f, %.6f", longitude, latitude) : "未知");

        // 计算水印位置（右下角）
        int x = original.getWidth() - DEFAULT_MARGIN;
        int y = original.getHeight() - DEFAULT_MARGIN - DEFAULT_LINE_HEIGHT * 2;

        // 绘制半透明背景
        int textWidth = Math.max(
            Math.max(g2d.getFontMetrics().stringWidth(collectorText),
                    g2d.getFontMetrics().stringWidth(timeText)),
            g2d.getFontMetrics().stringWidth(locationText)
        );
        int bgWidth = textWidth + DEFAULT_MARGIN * 2;
        int bgHeight = DEFAULT_LINE_HEIGHT * 3 + DEFAULT_MARGIN;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setColor(new Color(0, 0, 0));
        g2d.fillRoundRect(x - bgWidth + DEFAULT_MARGIN, y - fontSize, bgWidth, bgHeight, 10, 10);

        // 恢复透明度用于文字
        g2d.setComposite(alphaComposite);
        g2d.setColor(Color.WHITE);

        // 绘制水印文字
        y += DEFAULT_LINE_HEIGHT;
        g2d.drawString(collectorText, x - bgWidth + DEFAULT_MARGIN * 2, y);
        
        y += DEFAULT_LINE_HEIGHT;
        g2d.drawString(timeText, x - bgWidth + DEFAULT_MARGIN * 2, y);
        
        y += DEFAULT_LINE_HEIGHT;
        g2d.drawString(locationText, x - bgWidth + DEFAULT_MARGIN * 2, y);

        // 释放资源
        g2d.dispose();
        original.flush();

        // 转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = getFormatName(originalImage);
        ImageIO.write(watermarked, formatName, baos);
        
        return baos.toByteArray();
    }

    /**
     * 检测图片格式
     */
    private static String getFormatName(InputStream inputStream) throws IOException {
        // 简化实现，默认返回jpg
        return "jpg";
    }

    /**
     * 为图片添加文字水印（简化版）
     *
     * @param imageBytes 原始图片字节数组
     * @param watermarkText 水印文本
     * @return 添加水印后的图片字节数组
     */
    public static byte[] addTextWatermark(byte[] imageBytes, String watermarkText) throws IOException {
        return addTextWatermark(imageBytes, watermarkText, DEFAULT_ALPHA);
    }

    /**
     * 为图片添加文字水印（可自定义透明度）
     *
     * @param imageBytes 原始图片字节数组
     * @param watermarkText 水印文本
     * @param alpha 透明度
     * @return 添加水印后的图片字节数组
     */
    public static byte[] addTextWatermark(byte[] imageBytes, String watermarkText, float alpha) throws IOException {
        InputStream is = new ByteArrayInputStream(imageBytes);
        BufferedImage original = ImageIO.read(is);
        
        if (original == null) {
            throw new IOException("无法读取图片");
        }

        BufferedImage watermarked = new BufferedImage(
            original.getWidth(), 
            original.getHeight(), 
            original.getType()
        );

        Graphics2D g2d = watermarked.createGraphics();
        g2d.drawImage(original, 0, 0, null);

        // 设置透明度和字体
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, 48));
        g2d.setColor(new Color(255, 255, 255, 180));

        // 在图片中心绘制水印
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int x = (original.getWidth() - fontMetrics.stringWidth(watermarkText)) / 2;
        int y = (original.getHeight() + fontMetrics.getAscent()) / 2;
        
        g2d.drawString(watermarkText, x, y);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(watermarked, "jpg", baos);
        
        return baos.toByteArray();
    }
}
