package org.lemontechnology.notifycenter.event;


import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @description: 发布失败后尝试重试发布的事件
 * @author: huang.zh
 * @create: 2022-11-30 22:01
 **/
public class RetryEvent implements Event, Delayed {

    private Event event;

    private long now;

    private long endTimestamp;

    private Integer retryTime;

    public RetryEvent(Event event,long time) {
        this.retryTime = 1;
        this.event = event;
        this.now = System.currentTimeMillis();
        this.endTimestamp = now + time;
    }

    @Override
    public String description() {
        return "重试发布事件";
    }

    @Override
    public Object data() {
        return event.data();
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(endTimestamp - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }

    public Integer getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(Integer retryTime) {
        this.retryTime = retryTime;
    }

    /**
     * @Author huang.zh
     * @Description 刷新当前重试发布事件到期时间
     * @Date 10:05 下午 2022/12/1
     * @Param [time]
     * @return
     **/
    public void refresh(long time){
        this.now = System.currentTimeMillis();
        this.endTimestamp = now + time;
        this.retryTime += 1;
    }
}

