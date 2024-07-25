package com.nobug.public_opinion_monitor.dto;

import com.nobug.public_opinion_monitor.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User实体DTO类
 *
 * @date：2023/3/22
 * @author：nobug
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO extends User {

    private String account;

    private String captcha;

}
