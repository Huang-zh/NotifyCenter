package org.lemontechnology.notifycenter.logging;


import java.lang.reflect.Constructor;

/**
 * @description: 统一日志适配工厂
 * @author: huang.zh
 * @create: 2021-12-13 10:48
 **/
public class LoggerFactory {

    public static final String MARKER = "Notify-Center";

    private static Constructor<? extends Logger> loggerConstructor;

    static {
        tryImplementation(LoggerFactory::useSlf4jLoggerImpl);
    }

    private LoggerFactory() {
        //不允许外部调用该工厂类的构造函数
    }

    /**
     * @Author huang.zh
     * @Description 对外提供获取日志类的静态方法
     * @Date 11:17 上午 2021/12/13
     * @Param [logger]
     * @return
     **/
    public static Logger getLog(String logger) {
        try {
            return loggerConstructor.newInstance(logger);
        } catch (Throwable t) {
            throw new RuntimeException("初始化日志类失败！"+t.getMessage());
        }
    }

    /**
     * @Author huang.zh
     * @Description 使用sfl4j作为默认日志实现
     * @Date 11:21 上午 2021/12/13
     * @Param []
     * @return
     **/
    private static synchronized void useSlf4jLoggerImpl(){
        setImplementation(Slf4jImpl.class);
    }

    /**
     * @Author huang.zh
     * @Description 来自于mybatis的优雅实现，多线程模式寻找日志实现类的构造器
     * @Date 11:18 上午 2021/12/13
     * @Param [runnable]
     * @return
     **/
    private static void tryImplementation(Runnable runnable) {
        if (loggerConstructor == null) {
            try {
                runnable.run();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    /**
     * @Author huang.zh
     * @Description 构造默认的日志实现类
     * @Date 11:19 上午 2021/12/13
     * @Param [implClass]
     * @return
     **/
    private static void setImplementation(Class<? extends Logger> implClass) {
        try {
            Constructor<? extends Logger> candidate = implClass.getConstructor(String.class);
            Logger log = candidate.newInstance(LoggerFactory.class.getName());
            if (log.isDebugEnabled()) {
                log.debug("将使用"+implClass.getName()+"作为Notify-Center的全局日志记录工具！");
            }
            if (log.isEnabled()){
                loggerConstructor = candidate;
            }
        } catch (Throwable t) {
            throw new RuntimeException("查找目录下的日志实现类时发生异常！"+t.getMessage());
        }
    }
}

