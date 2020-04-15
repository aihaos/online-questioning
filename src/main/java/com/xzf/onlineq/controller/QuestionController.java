package com.xzf.onlineq.controller;

import com.xzf.onlineq.async.EventModel;
import com.xzf.onlineq.async.EventProducer;
import com.xzf.onlineq.async.EventType;
import com.xzf.onlineq.domain.*;
import com.xzf.onlineq.service.*;
import com.xzf.onlineq.service.*;
import com.xzf.onlineq.util.OnlineQUtil;
import com.xzf.onlineq.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    private EnvContext envContext;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserServiceNew userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(value = "/question/add", method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title, @RequestParam("content") String content) {
        try {
            Question question = new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCreatedDate(new Date());
            question.setCommentCount(0);
            if (envContext.getUser() == null) {
                return OnlineQUtil.getJSONString(999);
            } else {
                question.setUserId(envContext.getUser().getId());
            }
            if (questionService.addQuestion(question) > 0) {
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
                .setActorId(question.getUserId())
                .setExt("title",question.getTitle())
                .setExt("content",question.getContent())
                .setEntityId(question.getId()));
                return OnlineQUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("增加问题失败" + e.getMessage());
        }
        return OnlineQUtil.getJSONString(1, "失败");
    }

    @RequestMapping(path = {"/question/{id}"} , method = {RequestMethod.GET})
    public String questionDetail(Model model,
                                 @PathVariable("id") int id) {
        Question question = questionService.getById(id);
        List<Comment> commentList = commentService.getCommentsByEntity(id, OnlineQUtil.ENTITY_QUESTION);
        List<ViewObject> comments = new ArrayList<>();
        for (Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);

            if(envContext.getUser()==null){
                vo.set("liked", 0);
            } else{
                vo.set("liked", likeService.getLikeStatus(envContext.getUser().getId(),OnlineQUtil.ENTITY_COMMENT,comment.getId()));
            }
            vo.set("likeCount",likeService.getLikeCount(OnlineQUtil.ENTITY_COMMENT,comment.getId())+"赞同");
            vo.set("user", userService.getUser(comment.getUserId()));
            comments.add(vo);
        }
        model.addAttribute("question", question);
        model.addAttribute("comments", comments);
        List<ViewObject> followUsers = new ArrayList<ViewObject>();
        // 获取关注的用户信息
        List<Integer> users = followService.getFollowers(OnlineQUtil.ENTITY_QUESTION, id, 20);
        for (Integer userId : users) {
            ViewObject vo = new ViewObject();
            User u = userService.getUser(userId);
            if (u == null) {
                continue;
            }
            vo.set("name", u.getName());
            vo.set("headUrl", u.getHeadUrl());
            vo.set("id", u.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers", followUsers);
        if (envContext.getUser() != null) {
            model.addAttribute("followed", followService.isFollower(envContext.getUser().getId(), OnlineQUtil.ENTITY_QUESTION, id));
        } else {
            model.addAttribute("followed", false);
        }
        return "detail";
    }
}
