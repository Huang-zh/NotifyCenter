package org.lemontechnology.notifycenter.logging;

/**
 * @Author huang.zh
 * @Description 统一日志抽象接口，要求兼容的所有日志实现类必须实现该接口
 *              只有实现了该接口的日志实现类，才能被正确加载，提供日志记录功能
 * @Date 10:49 上午 2021/12/13
 * @Param
 * @return
 **/
public interface Logger {

    boolean isEnabled();

    boolean isDebugEnabled();

    boolean isTraceEnabled();

    void error(String s, Throwable e);

    void error(String s);

    void debug(String s);

    void info(String s);

    void trace(String s);

    void warn(String s);
}

