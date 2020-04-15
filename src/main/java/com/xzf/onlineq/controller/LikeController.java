package com.xzf.onlineq.controller;

import com.xzf.onlineq.async.EventModel;
import com.xzf.onlineq.async.EventProducer;
import com.xzf.onlineq.async.EventType;
import com.xzf.onlineq.domain.Comment;
import com.xzf.onlineq.domain.EnvContext;
import com.xzf.onlineq.service.CommentService;
import com.xzf.onlineq.service.LikeService;
import com.xzf.onlineq.util.OnlineQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private EnvContext envContext;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private CommentService commentService;

    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        if (envContext.getUser() == null) {
            return OnlineQUtil.getJSONString(999);
        }
        long likeCount = likeService.like(envContext.getUser().getId(), OnlineQUtil.ENTITY_COMMENT, commentId);

        Comment comment = commentService.getCommentById(commentId);

        eventProducer.fireEvent(new EventModel(EventType.LIKE)
                .setActorId(envContext.getUser().getId())
                .setEntityType(OnlineQUtil.ENTITY_COMMENT)
                .setEntityId(commentId)
                .setEntityOwnerId(comment.getUserId())
                .setExt("questionId",String.valueOf(comment.getEntityId()))
        );

        return OnlineQUtil.getJSONString(0, String.valueOf(likeCount) + "赞同");
    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String disLike(@RequestParam("commentId") int commentId) {
        if (envContext.getUser() == null) {
            return OnlineQUtil.getJSONString(999);
        }
        long likeCount = likeService.disLike(envContext.getUser().getId(), OnlineQUtil.ENTITY_COMMENT, commentId);
        return OnlineQUtil.getJSONString(0, String.valueOf(likeCount) + "赞同");
    }

}
