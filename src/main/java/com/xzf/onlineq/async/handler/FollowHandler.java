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
public class FollowHandler  implements EventHandler {

    @Autowired
    private UserServiceNew userService;
    @Autowired
    private MessageService messageService;
    @Override
    public void doHandle(EventModel model) {
        //如果是自己给自己关注，就不给自己发私信了
        if (model.getEntityOwnerId() == model.getActorId()) {
            return;
        }
        Message message = new Message();
        message.setFromId(OnlineQUtil.FOLLOW_USER_ID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());
        if(model.getEntityType()==OnlineQUtil.ENTITY_QUESTION){
            message.setContent("用户 " + user.getName() +
                    " 关注了您的问题 \" "+ model.getExt("questionTitle")+ " \" <a href=\"/question/" + model.getEntityId() + "\">点击查看</a>");
        }
        else if(model.getEntityType()==OnlineQUtil.ENTITY_USER){
            message.setContent("用户 " + user.getName() +
                    " 关注了您, <a href=\"http://www.guomzh.com/user/" + model.getActorId() + "\">点击查看该用户主页</a>");
        }
        message.setConversationId(message.getConversationId());
        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.FOLLOW);
    }
}
