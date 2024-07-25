package com.nobug.public_opinion_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询DTO
 *
 * @date：2023/2/18
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDTO {

    private Integer pageSize;
    private Integer pageNo;
    private String hotWord;
    private String location;
    private String startDate;
    private String endDate;
    private Integer sentiment;
    private Integer status;

}
