package org.lemontechnology.notifycenter.strategy;

import org.lemontechnology.notifycenter.event.DeadEvent;

/**
 * @Author huang.zh
 * @Description 消亡事件持久化策略顶层接口抽象
 * @Date 7:55 下午 2022/12/20
 **/
public interface PersistenceStrategy {

    void doPersistence(DeadEvent event);
}
