package com.nobug.public_opinion_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 话题列表DTO
 *
 * @date：2023/2/18
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicDTO {

    private List<HotDTO> topicList;
    private Integer total;
    private Integer pageSize;
    private Integer pageNo;

}
