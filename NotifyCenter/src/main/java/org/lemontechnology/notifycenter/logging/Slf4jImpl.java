package org.lemontechnology.notifycenter.logging;


import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @description: slf4j日志类默认实现
 * @author: huang.zh
 * @create: 2021-12-13 11:21
 **/
public class Slf4jImpl implements Logger{

    private Logger loggerImpl;

    public Slf4jImpl(String className) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(className);
        if (logger instanceof LocationAwareLogger) {
            //slf4j在1.6和以上版本的时候，api不再和之前一样，需要特殊处理，直接照搬了Mybatis的处理方式
            try {
                // check for slf4j >= 1.6 method signature
                logger.getClass().getMethod("log", Marker.class, String.class, int.class, String.class, Object[].class, Throwable.class);
                loggerImpl = new Slf4jLocationAwareLoggerImpl((LocationAwareLogger) logger);
                return;
            } catch (SecurityException | NoSuchMethodException e) {
                // fail-back to Slf4jLoggerImpl
            }
        }
        loggerImpl = new Slf4jLoggerImpl(logger);
    }

    @Override
    public boolean isEnabled() {
        return loggerImpl != null;
    }

    @Override
    public boolean isDebugEnabled() {
        return loggerImpl.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return loggerImpl.isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        loggerImpl.error(s, e);
    }

    @Override
    public void error(String s) {
        loggerImpl.error(s);
    }

    @Override
    public void debug(String s) {
        loggerImpl.debug(s);
    }

    @Override
    public void info(String s) {
        loggerImpl.info(s);
    }

    @Override
    public void trace(String s) {
        loggerImpl.trace(s);
    }

    @Override
    public void warn(String s) {
        loggerImpl.warn(s);
    }
}

