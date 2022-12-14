package org.lemontechnology.notifycenter.subscriber;


import org.lemontechnology.notifycenter.event.Event;

import java.util.concurrent.ExecutorService;

/**
 * @author Huang.zh
 * @date 2021/4/10 11:58
 * @Description: 事情订阅者抽象
 */
public abstract class Subscriber <T extends Event>{

    public abstract Boolean onEvent(T event);

    public abstract Class<? extends Event> subscribeType();

    public ExecutorService executor() {
        return null;
    }
}
