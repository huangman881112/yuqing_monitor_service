package com.nobug.public_opinion_monitor.utils;

import com.nobug.public_opinion_monitor.config.SystemPropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES解密工具类
 *
 * @date：2023/2/10
 * @author：nobug
 */
@Component
public class AESUtil {

    private static SystemPropertiesConfig systemPropertiesConfig;

    @Autowired
    public void init(SystemPropertiesConfig systemPropertiesConfig) {
        AESUtil.systemPropertiesConfig = systemPropertiesConfig;
    }

    /**
     * base 64 decode
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     * @throws Exception
     */
    public static byte[] base64Decode(String base64Code) throws Exception{
        return StringUtils.isEmpty(base64Code) ? null : new BASE64Decoder().decodeBuffer(base64Code);
    }

    /**
     * AES解密
     * @param encryptBytes 待解密的byte[]
     * @return 解密后的String
     * @throws Exception
     */
    public static String aesDecryptByBytes(byte[] encryptBytes) throws Exception {

        Cipher cipher = Cipher.getInstance(systemPropertiesConfig.getAesAlgorithmstr());

        byte[] temp = systemPropertiesConfig.getAesIv().getBytes("UTF-8");
        IvParameterSpec iv = new IvParameterSpec(temp);

        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(systemPropertiesConfig.getAesKey().getBytes(), "AES"), iv);
        byte[] decryptBytes = cipher.doFinal(encryptBytes);

        System.out.print(new String(decryptBytes));
        return new String(decryptBytes);
    }

    /**
     * 将base 64 code AES解密
     * @param encryptStr 待解密的base 64 code
     * @return 解密后的string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr) throws Exception {
        return StringUtils.isEmpty(encryptStr) ? null : aesDecryptByBytes(base64Decode(encryptStr));
    }

}
