package org.lemontechnology.notifycenter.event;


/**
 * @description: 全局默认通用事件载体
 * @author: huang.zh
 * @create: 2022-11-27 19:59
 **/
public class CommonEvent implements Event{

    private Object data;

    public CommonEvent(Object data) {
        this.data = data;
    }

    @Override
    public String description() {
        return "通用事件";
    }

    @Override
    public Object data() {
        return data;
    }
}
