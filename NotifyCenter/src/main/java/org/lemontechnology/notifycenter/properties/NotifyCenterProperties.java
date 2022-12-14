package org.lemontechnology.notifycenter.properties;


/**
 * @description: 相关配置项属性类，可以由配置文件解析获得，也允许使用者直接定义在代码中
 * @author: huang.zh
 * @create: 2021-12-10 11:27
 **/
public class NotifyCenterProperties {

    //允许最大的推送者数量
    private static Integer publisherMax = 1 << 8;
    //允许最大的同时投递事件数
    private static Integer eventMax = 1 << 8;
    //事件发布失败时的重试次数
    private static Integer retryTime = 0;
    //允许最大的发布失败重试事件数
    private static Integer retryEventMax = 1 << 8;
    //重试发布间隔时间，默认十秒
    private static Integer retrySeconds = 10;

    public NotifyCenterProperties() {
    }

    public NotifyCenterProperties(Integer publisherMax, Integer eventMax, Integer retryTime, Integer retryEventMax) {
        this.publisherMax = publisherMax;
        this.eventMax = eventMax;
        this.retryTime = retryTime;
        this.retryEventMax = retryEventMax;
    }

    public static Integer getPublisherMax() {
        return publisherMax;
    }

    public  void setPublisherMax(Integer publisherMax) {
        this.publisherMax = publisherMax;
    }

    public static Integer getEventMax() {
        return eventMax;
    }

    public void setEventMax(Integer eventMax) {
        this.eventMax = eventMax;
    }

    public static Integer getRetryTime(){
        return retryTime;
    }

    public void setRetryTime(Integer retryTime){
        this.retryTime = retryTime;
    }

    public static Integer getRetryEventMax() {
        return retryEventMax;
    }

    public void setRetryEventMax(Integer retryEventMax){
        this.retryEventMax = retryEventMax;
    }

    public static Integer getRetrySeconds() {
        return retrySeconds;
    }

    public void setRetrySeconds(Integer retrySeconds){
        this.retrySeconds = retrySeconds;
    }

}

