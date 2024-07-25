package com.nobug.public_opinion_monitor.service.impl;

import com.nobug.public_opinion_monitor.common.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import java.util.Date;

/**
 * 邮件工具类
 *
 * @date：2023/2/25
 * @author：nobug
 */
@Component
@Slf4j
public class MailService {
    //注入邮箱工具类
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String sendMailer;

    /**
     * 检测邮件信息类
     * @param to
     * @param subject
     * @param text
     */
    private void checkMail(String to,String subject,String text){
        if(StringUtils.isEmpty(to)){
            throw new GlobalException("邮件收信人不能为空");
        }
        if(StringUtils.isEmpty(subject)){
            throw new GlobalException("邮件主题不能为空");
        }
        if(StringUtils.isEmpty(text)){
            throw new GlobalException("邮件内容不能为空");
        }
    }

    /**
     * 发送纯文本邮件
     * @param to
     * @param subject
     * @param text
     */
    public void sendTextMailMessage(String to,String subject,String text){

        try {
            //校验参数
            checkMail(to, subject ,text);
            //true 代表支持复杂的类型
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(),true);
            //邮件发信人
            mimeMessageHelper.setFrom(sendMailer);
            //邮件收信人  1或多个
            mimeMessageHelper.setTo(to.split(","));
            //邮件主题
            mimeMessageHelper.setSubject(subject);
            //邮件内容
            mimeMessageHelper.setText(text);
            //邮件发送时间
            mimeMessageHelper.setSentDate(new Date());

            //发送邮件
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            System.out.println("发送邮件成功："+sendMailer+"->"+to);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("发送邮件失败："+e.getMessage());
        }
    }

}
