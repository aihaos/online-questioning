package com.xzf.onlineq.async.handler;

import com.xzf.onlineq.async.EventHandler;
import com.xzf.onlineq.async.EventModel;
import com.xzf.onlineq.async.EventType;
import com.xzf.onlineq.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class AddQuestionHandler  implements EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(AddQuestionHandler.class);
    @Autowired
    private SearchService searchService;

    @Override
    public void doHandle(EventModel model) {
        try {
            boolean ret = searchService.indexQuestion(model.getEntityId(),model.getExt("title"),model.getExt("content"));
            if(!ret){
                logger.error("建立问题索引失败");
            }
        } catch (Exception e){
            logger.error("建立问题索引失败"+e.getMessage());
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.ADD_QUESTION);
    }
}
