package org.lemontechnology.notifycenter.publisher;


import org.lemontechnology.notifycenter.event.CommonEvent;
import org.lemontechnology.notifycenter.event.Event;
import org.lemontechnology.notifycenter.logging.Logger;
import org.lemontechnology.notifycenter.logging.LoggerFactory;

/**
 * @author Huang.zh
 * @date 2021/4/10 11:43
 * @Description:  普通事件推送者
 */
public class CommonEventPublisher implements Publisher{

    private Class<? extends Event> eventType;

    private Logger logger = LoggerFactory.getLog(CommonEventPublisher.class.getName());

    public CommonEventPublisher() {
    }

    public CommonEventPublisher(Class<? extends Event> eventType) {
        this.eventType = eventType;
    }

    @Override
    public void init(Class<? extends Event> clazz){
        eventType = clazz;
    }

    @Override
    public Class<? extends Event> eventType() {
        return eventType == null ? CommonEvent.class:eventType;
    }

    @Override
    public boolean publish(Event event) {
        //这里简单记录一下日志
        logger.info("推送事件："+event.description());
        return true;
    }

}
