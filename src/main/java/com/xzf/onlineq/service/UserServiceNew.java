package com.xzf.onlineq.service;

import com.xzf.onlineq.async.EventProducer;
import com.xzf.onlineq.dao.LoginTicketDao;
import com.xzf.onlineq.dao.UserDao;
import com.xzf.onlineq.domain.LoginTicket;
import com.xzf.onlineq.domain.User;
import com.xzf.onlineq.service.Redis.RedisAdapter;
import com.xzf.onlineq.service.Redis.RedisService;
import com.xzf.onlineq.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceNew {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceNew.class);
    @Autowired
    private UserDao userDao;

    @Autowired
    private LoginTicketDao loginTicketDao;

    @Autowired
    private RedisService<Integer> redisService;

    @Autowired
    private RedisAdapter redisAdapter;

    @Autowired
    private EventProducer eventProducer;

    public User getUser(int id) {
        return userDao.selectById(id);
    }

    public User getUserByName(String name){
        return userDao.selectByName(name);
    }

    public Map<String, Object> login(User user, String captcha, String captchaKey) {
        Map<String, Object> map = new HashMap<String, Object>();
        //忽略大小写，判断验证码是否相同
        if (!captcha.equalsIgnoreCase(redisService.getCacheObject(captchaKey))) {
            map.put("msg", "验证码错误");
            return map;
        }
        //相同，删除该key
        redisService.delete(captchaKey);
        User checkUser = userDao.selectByEmail(user.getEmail());
        if (checkUser == null) {
            map.put("msg", "该账号不存在");
            return map;
        }
        if(!checkUser.getPassword().equals(MD5Util.MD5(user.getPassword() + checkUser.getSalt()))){
            map.put("msg", "密码不正确");
            return map;
        }
        String ticket = addLoginTicket(checkUser.getId());
        map.put("ticket", ticket);
        return map;
    }

    public String addLoginTicket(int userId) {
        LoginTicket ticket = new LoginTicket();
        Date now = new Date();
        now.setTime(3600 * 24 * 15 * 1000 + now.getTime());
        ticket.setUserId(userId);
        ticket.setExpired(now);
        ticket.setStatus(0);
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        loginTicketDao.addTicket(ticket);
        return ticket.getTicket();
    }

    public void logout(String ticket) {
        loginTicketDao.updateStatus(ticket, 1);
    }

    public Map<String, Object> register(User user, String captcha, String captchaKey) {
        Map<String, Object> map = new HashMap<String, Object>();

        //忽略大小写，判断验证码是否相同
        if (!captcha.equalsIgnoreCase(redisService.getCacheObject(captchaKey))){
            map.put("msg", "验证码错误");
            return map;
        }
        //相同，删除该key
        redisService.delete(captchaKey);

        User checkUser = userDao.selectByName(user.getName());
        if (checkUser != null) {
            map.put("msg", "用户名已经被注册");
            return map;
        }

        checkUser = userDao.selectByEmail(user.getEmail());
        if (checkUser != null) {
            map.put("msg", "邮箱已经被注册");
            return map;
        }

        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
        String head = String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
        user.setHeadUrl(head);
        user.setPassword(MD5Util.MD5(user.getPassword() + user.getSalt()));
        userDao.addUser(user);

        return map;
    }

    public Map<String, Object> emailLogin(String email, String captcha, String emailCaptchaKey) {
        Map<String, Object> map = new HashMap<String, Object>();
        //忽略大小写，判断验证码是否相同
        if (!captcha.equalsIgnoreCase(redisService.getCacheObject(emailCaptchaKey))) {
            map.put("msg", "验证码错误");
            return map;
        }
        //相同，删除该key
        redisService.delete(emailCaptchaKey);
        User checkUser = userDao.selectByEmail(email);
        if (checkUser == null) {
            map.put("msg", "该账号不存在");
            return map;
        }
        String ticket = addLoginTicket(checkUser.getId());
        map.put("ticket", ticket);
        return map;
    }

}
