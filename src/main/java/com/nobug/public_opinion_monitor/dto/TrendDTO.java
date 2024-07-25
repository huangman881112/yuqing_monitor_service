package com.nobug.public_opinion_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 舆情趋势图 DTO类
 *
 * @date：2023/2/17
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendDTO {

    private List<String> timeSeries;
    private List<Long> positiveSeries;
    private List<Long> neuterSeries;
    private List<Long> negativeSeries;

}
