package com.nobug.public_opinion_monitor.dto;

import com.nobug.public_opinion_monitor.entity.ESMblog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 博文列表DTO
 *
 * @date：2023/2/18
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDTO {

    private List<ESMblog> articleList;
    private Integer total;
    private Integer pageSize;
    private Integer pageNo;

}
