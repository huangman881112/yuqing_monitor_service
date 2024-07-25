package com.nobug.public_opinion_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索分析DTO
 *
 * @date：2023/2/22
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchAnalysisDTO extends SearchDTO{

    private String searchWord;
    private String ignoreWord;
    private Integer day;

}
