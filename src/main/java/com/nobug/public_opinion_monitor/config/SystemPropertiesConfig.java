package com.nobug.public_opinion_monitor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置文件类
 *
 * @date：2023/2/10
 * @author：nobug
 */
@Component
@ConfigurationProperties("system.config")
@Getter
@Setter
public class SystemPropertiesConfig {

    //签名失效时间
    private Long jwtFailureTime;

    //签名私钥
    private String jwtKey;

    //密钥 (需要前端和后端保持一致)十六位作为密钥
    private String aesKey = "ABCDEFGHIJKL_key";

    //密钥偏移量 (需要前端和后端保持一致)十六位作为密钥偏移量
    private String aesIv = "ABCDEFGHIJKLM_iv";

    //算法
    private String aesAlgorithmstr = "AES/CBC/PKCS5Padding";


}
