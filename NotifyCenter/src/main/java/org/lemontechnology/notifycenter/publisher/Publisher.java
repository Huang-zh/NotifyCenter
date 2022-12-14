package org.lemontechnology.notifycenter.publisher;

import org.lemontechnology.notifycenter.event.Event;

/**
 * @author Huang.zh
 * @date 2021/4/10 11:49
 * @Description: 事件推送者抽象
 */
public interface Publisher {

    void init(Class<? extends Event> clazz);

    boolean publish(Event event);

    Class<? extends Event> eventType();
}
