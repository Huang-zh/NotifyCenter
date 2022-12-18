package org.lemontechnology.notifycenter.event;

/**
 * @description: 存在一下两种状态的事件会被转化成消亡事件：
 *                  1.超过重试次数之后的普通事件
 *                  2.执行本地事务异常（超出本地事务确认次数）的事务事件
 *               一旦被转化为消亡事件，就会被记录在二进制文件
 * @author: huang.zh
 * @create: 2022-12-18 14:44
 **/
public class DeadEvent implements Event{

    //事件原有的数据
    private Object data;
    //事件类型：普通事件或事务事件
    private EventType eventType;
    //持久化进入磁盘的时间戳
    private long timestamp;
    //消费标记
    private boolean consumeFlag;

    public DeadEvent(Object data, EventType eventType, long timestamp, boolean consumeFlag) {
        this.data = data;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.consumeFlag = consumeFlag;
    }

    @Override
    public String description() {
        return "消亡事件";
    }

    @Override
    public Object data() {
        return data;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isConsumeFlag() {
        return consumeFlag;
    }

    public void setConsumeFlag(boolean consumeFlag) {
        this.consumeFlag = consumeFlag;
    }

    enum EventType{
        NORMAL,
        TRANSACTION;
    }
}
