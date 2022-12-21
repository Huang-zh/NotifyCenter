package org.lemontechnology.notifycenter.event;

/**
 * @description: 存在以下两种状态的事务事件会被转化成消亡事件：
 *                  1.执行本地事务异常（要求回滚，返回DROPPED状态）的事务事件，
 *                  2.执行本地事务时间超长，即超出本地事务状态确认次数的事务事件
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
    //事务事件的状态
    private TransactionEvent.FiniteEventStateMachine eventState;

    public DeadEvent() {
    }

    public DeadEvent(Object data, EventType eventType, long timestamp, boolean consumeFlag, TransactionEvent.FiniteEventStateMachine eventState) {
        this.data = data;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.consumeFlag = consumeFlag;
        this.eventState = eventState;
    }

    @Override
    public String description() {
        return "消亡事件";
    }

    @Override
    public Object data() {
        return data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
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

    public TransactionEvent.FiniteEventStateMachine getEventState() {
        return eventState;
    }

    public void setEventState(TransactionEvent.FiniteEventStateMachine eventState) {
        this.eventState = eventState;
    }

    public enum EventType{
        NORMAL,
        TRANSACTION;
    }
}
