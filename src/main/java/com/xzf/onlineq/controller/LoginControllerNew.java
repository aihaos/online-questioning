package com.xzf.onlineq.controller;


import com.xzf.onlineq.domain.User;
import com.xzf.onlineq.service.CaptchaService;
import com.xzf.onlineq.service.UserServiceNew;
import com.xzf.onlineq.util.MailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginControllerNew {
    private static final Logger logger = LoggerFactory.getLogger(LoginControllerNew.class);

    @Autowired
    private UserServiceNew userServiceNew;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private MailSender mailSender;

    @RequestMapping(path = {"/login"}, method = {RequestMethod.POST})
    public String login(User user,Model model,
                        @CookieValue("captchaKey") String captchaKey,
                        @RequestParam("captcha") String captcha,
                        @RequestParam(value = "next", required = false) String next,
                        HttpServletResponse response) {
        try {

            Map<String, Object> map = userServiceNew.login(user,captcha,captchaKey);
            if(map.containsKey("ticket")){
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                response.addCookie(cookie);
                return "redirect:"+next;
            }else{
                model.addAttribute("msg", map.get("msg"));
                model.addAttribute("email",user.getEmail());
                model.addAttribute("index",1);   //表示是登录
                return "reglogin";
            }
        }catch (Exception e){
            logger.error("登陆异常" + e.getMessage());
            model.addAttribute("msg", "服务器内部错误");
            model.addAttribute("email",user.getEmail());
            model.addAttribute("index",1);   //表示是登录
            return "reglogin";
        }
    }

    @ResponseBody
    @RequestMapping("/emaillogin")
    public String emailLoginHandler(@RequestParam("email") String email,
                                    @RequestParam("captcha") String captcha,
                                    @CookieValue("emailCaptchaKey") String emailCaptchaKey,
                                    @RequestParam(value = "next", required = false) String next,
                                    HttpServletResponse response){
        Map<String, Object> map = userServiceNew.emailLogin(email,captcha,emailCaptchaKey);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath("/");
            response.addCookie(cookie);
            if(StringUtils.isEmpty(next)){
                next="/";
            }
            return next;
        }else {
            return map.get("msg").toString();
        }
    }


    @RequestMapping(path = {"/register"}, method = {RequestMethod.POST})
    public String reg(User user, Model model,
                      @RequestParam("captcha")String captcha,
                      @CookieValue("captchaKey") String captchaKey,
                      HttpServletResponse response) {
        try {
            Map<String, Object> map = userServiceNew.register(user, captcha,captchaKey);
            if (!map.containsKey("msg")){
                model.addAttribute("msg", "注册成功，请登录");
                model.addAttribute("index",1);
                return "reglogin";
            } else {
                model.addAttribute("msg", map.get("msg"));
                model.addAttribute("name",user.getName());
                model.addAttribute("email",user.getEmail());
                model.addAttribute("index",0);   //表示是注册
                return "reglogin";
            }

        } catch (Exception e) {
            logger.error("注册异常" + e.getMessage());
            model.addAttribute("msg", "服务器错误");
            return "reglogin";
        }
    }


    @RequestMapping(path = {"/loginReg"}, method = {RequestMethod.GET})
    public String loginReg(@RequestParam(value = "next", required = false) String next,
                           Model model) {
        model.addAttribute("next",next);
        return "reglogin";
    }

    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET})
    public String logout(@CookieValue("ticket")String ticket) {
        userServiceNew.logout(ticket);
        return "redirect:/";
    }


    @ResponseBody
    @RequestMapping("/sendmail")
    public String sendEmailCaptcha(@RequestParam("email") String email,HttpServletResponse response){
        //创建随机字符串并存入redis，返回key
        String emailCaptchaKey = captchaService.createEmailCaptcha();
        //根据key从redis中获取随机字符
        String captcha = captchaService.getCaptcha(emailCaptchaKey);
        String title = "[橙子答]登录验证码";
        String massage ="您的验证码为："+captcha+",我们不会以任何方式向您询问该验证码，请妥善保管，以防诈骗。";
        try {
            mailSender.sendEmail(email, title,massage);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("邮件发送异常，" + e.getMessage());
            return "error";
        }
        Cookie cookie = new Cookie("emailCaptchaKey", emailCaptchaKey);
        response.addCookie(cookie);
        return "success";
    }


    /**
     * 获取图片验证码
     * @param response
     */
    @RequestMapping("/getCaptcha")
    public void getCaptcha(HttpServletResponse response){
        try {
            //创建随机字符串并存入redis，返回key
            String captchaKey = captchaService.createCaptcha();
            //根据key从redis中获取随机字符
            String captcha = captchaService.getCaptcha(captchaKey);
            Cookie cookie = new Cookie("captchaKey", captchaKey);
            response.addCookie(cookie);
            OutputStream os = response.getOutputStream();
            captchaService.sendCaptchaJPG(captcha, os);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取验证码图片异常，" + e.getMessage());
        }
    }
}
