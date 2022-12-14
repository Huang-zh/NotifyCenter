package org.lemontechnology.notifycenter.logging;


/**
 * @description: slf4j 1.6 以前版本的日志实现适配器
 * @author: huang.zh
 * @create: 2021-12-13 11:45
 **/
public class Slf4jLoggerImpl implements Logger{

    private org.slf4j.Logger log;

    public Slf4jLoggerImpl(org.slf4j.Logger logger) {
        this.log = logger;
    }

    @Override
    public boolean isEnabled() {
        return log != null;
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        log.error(s, e);
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }

    @Override
    public void info(String s) {
        log.info(s);
    }

    @Override
    public void trace(String s) {
        log.trace(s);
    }

    @Override
    public void warn(String s) {
        log.warn(s);
    }
}

