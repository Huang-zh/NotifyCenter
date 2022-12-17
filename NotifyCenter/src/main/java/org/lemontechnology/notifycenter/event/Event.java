package org.lemontechnology.notifycenter.event;

import java.io.Serializable;

/**
 * @author Huang.zh
 * @date 2021/4/9 18:05
 * @Description: 通用事件抽象
 */
public interface Event extends Serializable {

    long serialVersionUID = 121L;

    String description();

    Object data();

}

