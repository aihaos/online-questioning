package com.xzf.onlineq.service;

import com.xzf.onlineq.service.Redis.RedisService;
import com.xzf.onlineq.util.CaptchaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaService.class);
    public static final int CAPTCHA_WIDTH = 120;
    public static final int CAPTCHA_HEIGHT = 30;

    @Autowired
    private RedisService<String> redisService;

    public String createCaptcha(){
        String captcha = CaptchaUtil.generateCaptchaCode(4);
        String captchaKey = UUID.randomUUID().toString().replace("-", "");               //使用uuid作为captchaKey
        redisService.setCacheObject(captchaKey, captcha).expire(3, TimeUnit.MINUTES);       //存储到redis并设置过期时间为3分钟
        return captchaKey;
    }

    //邮箱验证码失效时间为1分钟
    public String createEmailCaptcha(){
        String captcha = CaptchaUtil.generateCaptchaCode(4);
        String captchaKey = UUID.randomUUID().toString().replace("-", "");               //使用uuid作为captchaKey
        redisService.setCacheObject(captchaKey, captcha).expire(80, TimeUnit.SECONDS);       //存储到redis并设置过期时间为80秒，20秒为发邮件过程这个时间做缓冲（这是一个不成熟的方案）
        return captchaKey;
    }

    public String getCaptcha(String captchaKey){
        String captcha = redisService.getCacheObject(captchaKey);
        return captcha;
    }

    public void sendCaptchaJPG(String captcha, OutputStream os){
        try {
            CaptchaUtil.outputImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, os, captcha);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }


}
