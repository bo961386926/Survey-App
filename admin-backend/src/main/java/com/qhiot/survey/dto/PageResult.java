package com.qhiot.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分页查询结果")
public class PageResult<T> {

    @Schema(description = "数据记录列表")
    private List<T> records;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize;

    @Schema(description = "总页数", example = "10")
    private Integer totalPages;
}
