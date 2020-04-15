package com.xzf.onlineq.async.handler;

import com.xzf.onlineq.async.EventHandler;
import com.xzf.onlineq.async.EventModel;
import com.xzf.onlineq.async.EventType;
import com.xzf.onlineq.domain.Message;
import com.xzf.onlineq.domain.User;
import com.xzf.onlineq.service.MessageService;
//import com.xzf.onlineq.service.UserService;
import com.xzf.onlineq.service.UserServiceNew;
import com.xzf.onlineq.util.OnlineQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Component
public class LikeHandler implements EventHandler {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserServiceNew userService;

    @Override
    public void doHandle(EventModel model) {
        //如果是自己给自己点赞，就不给自己发私信了
        if (model.getEntityOwnerId() == model.getActorId()) {
            return;
        }
        Message message = new Message();
        message.setFromId(OnlineQUtil.LIKE_USER_ID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());
        message.setContent("用户 " + user.getName() + " 赞了你的评论, <a href=\"http://www.guomzh.com/question/" + model.getExt("questionId") + "\">点击查看该问题</a>");
        message.setConversationId(message.getConversationId());
        messageService.addMessage(message);
    }

    //本handler只处理点赞（like）类型的消息
    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}
