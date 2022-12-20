package org.lemontechnology.notifycenter;


import com.esotericsoftware.kryo.Kryo;
import org.lemontechnology.notifycenter.event.*;
import org.lemontechnology.notifycenter.function.BuildFunction;
import org.lemontechnology.notifycenter.logging.Logger;
import org.lemontechnology.notifycenter.logging.LoggerFactory;
import org.lemontechnology.notifycenter.properties.NotifyCenterProperties;
import org.lemontechnology.notifycenter.publisher.Publisher;
import org.lemontechnology.notifycenter.strategy.DefaultPersistenceStrategy;
import org.lemontechnology.notifycenter.strategy.PersistenceStrategy;
import org.lemontechnology.notifycenter.subscriber.Subscriber;
import org.lemontechnology.notifycenter.util.LoadConfigurationsUtil;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Huang.zh
 * @date 2021/4/9 18:04
 * @Description: 事件通知中心
 */
public class NotifyCenter {
    //消息推送者集合
    private static ConcurrentHashMap<Class<? extends Event>, Publisher> publishers;
    //事件和对应生产事件的构造函数
    private static ConcurrentHashMap<Class<? extends Event>, BuildFunction<Class<? extends Event>,Publisher>> eventPublisherFactoryMap;

    private static Map<Class<? extends Event>,NotifyNode> subscriberMap;

    private static NotifyCenter INSTANCE;

    //事件循环总线
    private static EventLoop eventLoop;

    //发布事件重试
    private static EventRetryLoop eventRetryLoop;

    //消亡事件总线
    private static DeadEventLoop deadEventLoop;

    private static Logger logger = LoggerFactory.getLog(NotifyCenter.class.getName());

    static {
        //懒汉模式
        if (null == INSTANCE){
            synchronized (NotifyCenter.class){
                //双重检查，避免线程安全问题
                if (null == INSTANCE){
                    INSTANCE = new NotifyCenter();
                }
            }
        }
    }

    private NotifyCenter() {
        //寻找resources目录下的配置项
        NotifyCenterProperties notifyCenterProperties = LoadConfigurationsUtil.notifyCenterProperties();
        publishers = new ConcurrentHashMap<>(notifyCenterProperties.getPublisherMax());
        eventPublisherFactoryMap = new ConcurrentHashMap<>(notifyCenterProperties.getEventMax());
        subscriberMap = new ConcurrentHashMap<>(notifyCenterProperties.getEventMax());
        //查找消息推送者，即class路径下Publisher接口的实现类
        Collection<Class<? extends Publisher>> collection = findCandidatePublisher();
        //加载所有的推送者工厂类
        initPublisherFactories(collection);
        //加载所有的推送者
        initPublishers();
        //加载总线
        initLoops(notifyCenterProperties);
        if (logger.isDebugEnabled()){
            logger.debug("-----NotifyCenter成功初始化-----");
        }
    }

    public static NotifyCenter instance(){
        return INSTANCE;
    }

    /**
     * @Author Huang.zh
     * @Description 寻找合适的推送者，如果继承重写了publisher，就可以覆盖当前默认的CommonEventPublisher
     * @Date 2021/4/10 12:43
     */
    private Collection<Class<? extends Publisher>> findCandidatePublisher(){
        List<Class<? extends Publisher>> classList = new ArrayList<>();
        Class<? extends Publisher> clazz;
        ServiceLoader<Publisher> loader = ServiceLoader.load(Publisher.class);
        Iterator<Publisher> iterator = loader.iterator();
        while (iterator.hasNext()){
            clazz = iterator.next().getClass();
            classList.add(clazz);
        }
        return classList;
    }

    /**
     * @Author huang.zh
     * @Description 初始化所有的推送者构造工厂
     * @Date 3:42 下午 2022/11/20
     * @Param [publishers]
     * @return
     **/
    private void initPublisherFactories(Collection<Class<? extends Publisher>> publishers){
        publishers.forEach(publisherClass -> {
            try {
                Publisher publisher = publisherClass.newInstance();
                BuildFunction<Class<? extends Event>, Publisher> function = new BuildFunction<Class<? extends Event>, Publisher>() {
                    @Override
                    public Publisher apply(Class<? extends Event> aClass) {
                        try {
                            Class<? extends Event> eventType = publisher.eventType();
                            publisher.init(eventType);
                            return publisher;
                        } catch (Throwable ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };
                eventPublisherFactoryMap.put(publisher.eventType(),function);
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * @Author huang.zh
     * @Description 初始化所有推送者
     * @Date 3:48 下午 2022/11/20
     * @Param []
     * @return
     **/
    private void initPublishers(){
        eventPublisherFactoryMap.forEach((eventClass,buildFunction) -> {
            Optional.ofNullable(buildFunction).ifPresent(bf ->{
                Publisher publisher = bf.apply(eventClass);
                INSTANCE.publishers.putIfAbsent(eventClass,publisher);
            });
        });
    }

    /**
     * @Author huang.zh
     * @Description 初始化Loop总线
     * @Date 10:13 下午 2022/12/1
     * @Param []
     * @return
     *
     * @param notifyCenterProperties */
    private void initLoops(NotifyCenterProperties notifyCenterProperties){
        //初始化EventLoop，用于通知订阅者消费发布的事件
        eventLoop = new EventLoop(notifyCenterProperties);
        eventLoop.start();

        //初始化事件发布失败总线，用于重试发布失败的事件
        eventRetryLoop = new EventRetryLoop(notifyCenterProperties);
        eventRetryLoop.start();

        //初始化消亡事件总线
        deadEventLoop = new DeadEventLoop();
    }

    public static Publisher findPublisher(Class<Event> clazz){
        return INSTANCE.publishers.getOrDefault(clazz,null);
    }

    /**
     * @Author huang.zh
     * @Description 注册一个事件的订阅
     * @Date 11:23 上午 2021/12/10
     * @Param [subscriber]
     * @return
     **/
    public static void registerSubscriber(Subscriber subscriber) {
        //订阅者根据事件类型进行订阅
        Class<? extends Event> type = subscriber.subscribeType();
        NotifyNode head = subscriberMap.get(type);
        NotifyNode tail = new NotifyNode(type,subscriber,null,null);
        if (head == null){
            head = tail;
        } else {
            NotifyNode node = head;
            while (node.next != null){
                node = node.next;
            }
            //赋值新的尾部节点
            node.next = tail;
            tail.previous = node;
        }
        subscriberMap.put(type,head);
    }

    /**
     * @Author huang.zh
     * @Description 注销当前存在的订阅者
     * @Date 11:23 上午 2021/12/10
     * @Param [subscriber]
     * @return
     **/
    public static boolean deregisterSubscriber(Subscriber subscriber) {
        Class<? extends Event> type = subscriber.subscribeType();
        return deregisterSubscriber(type);
    }

    /**
     * @Author huang.zh
     * @Description 注销当前存在的订阅者
     * @Date 11:23 上午 2021/12/10
     * @Param [subscriber]
     * @return
     **/
    public static boolean deregisterSubscriber(Class<? extends Event> type) {
        if (!INSTANCE.publishers.containsKey(type)){
            return true;
        }
        return INSTANCE.publishers.remove(type) != null;
    }

    /**
     * @Author huang.zh
     * @Description 对外推送事件方法，该方法只能讲事件传送到发布者
     * @Date 11:22 上午 2021/12/10
     * @Param [event]
     * @return
     **/
    public static void publishEvent(Event event) {
        boolean faild = false;
        try {
            faild = Boolean.FALSE.equals(doPublishEvent(event));
        } catch (Throwable ex) {
            ex.printStackTrace();
            faild = true;
        }
        if (faild && !TransactionEvent.class.isInstance(event)){
            // 事件发布失败，触发重试机制
            // 注意，事务事件不支持重试
            eventRetryLoop.retryFailedEvents(event);
        }
    }

    /**
     * @Author huang.zh
     * @Description 正式推送暂存到EventLoop的事务事件
     * @Date 9:20 下午 2022/12/6
     * @Param [transactionEvent]
     * @return
     **/
    public static void doPushTransactionEvent(TransactionEvent transactionEvent){
        eventLoop.doPushTransactionEvent(transactionEvent);
    }


    /**
     * @Author huang.zh
     * @Description  调用对应事件的publisher监听发布的事件，并将事件推送到总线
     * @Date 7:52 下午 2022/11/27
     * @Param [eventType, event]
     * @return
     **/
    private static boolean doPublishEvent(Event event) {
        boolean success = false;
        if (event == null){
            return success;
        }
        Publisher publisher;
        Class<? extends Event> clz = event.getClass();
        Class<?> superclass = clz.getSuperclass();
        if (!INSTANCE.publishers.containsKey(clz) && !INSTANCE.publishers.containsKey(superclass)){
            //如果当前不存在该事件类型的推送者，使用全局默认的CommonEventPublisher
            publisher = publishers.get(CommonEvent.class);
        } else {
            publisher = publishers.getOrDefault(clz,publishers.get(superclass));
        }
        if (publisher != null && (success = publisher.publish(event))) {
            //推送要发布的事件
            if (eventLoop.needNotifySubscribers(event)){
                success &= eventLoop.pushEvent(event);
            }
        }
        return success;
    }


    /**
     * @Author huang.zh
     * @Description 通知节点，维护event下不同类型的订阅者
     * @Date 3:36 下午 2022/11/20
     **/
    private static class NotifyNode {

        private Class<? extends Event> subscribeEvent;

        private Subscriber subscriber;

        private NotifyNode previous;

        private NotifyNode next;

        public NotifyNode(Class<? extends Event> subscribeEvent, Subscriber subscriber, NotifyNode previous, NotifyNode next) {
            this.subscribeEvent = subscribeEvent;
            this.subscriber = subscriber;
            this.previous = previous;
            this.next = next;
        }
    }


    /**
     * @Author huang.zh
     * @Description 事件循环线程，负责从队列中取出推送的事件，交予订阅者处理
     * @Date 10:24 下午 2022/11/25
     * @Param
     * @return
     **/
    private class EventLoop extends Thread{

        BlockingQueue<Event> eventQueue;

        ExecutorService worker;

        final Integer transactionEventMax;

        EventLoop(NotifyCenterProperties notifyCenterProperties) {
            transactionEventMax = notifyCenterProperties.getEventMax();
            //消息总线有界队列
            this.eventQueue = new ArrayBlockingQueue<>(notifyCenterProperties.getEventMax());
            //总线自带一个worker线程，用于异步通知未提供线程池的订阅者消费发布的事件，防止事件堆积
            worker = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("EventLoopWorkerThread");
                    return thread;
                }
            });
        }

        @Override
        public void run() {
            for (;;){
                try {
                    Event event = eventQueue.take();
                    notifySubscribers(event);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * @Author huang.zh
         * @Description 判断当前事件是否已经到了发布的时间点，主要用于判断事务事件的状态
         * @Date 11:50 下午 2022/12/3
         * @Param [event]
         * @return
         **/
        boolean needNotifySubscribers(Event event){
            boolean need = true;
            if (TransactionEvent.class.isInstance(event)){
                TransactionEvent transactionEvent = (TransactionEvent) event;
                need &= TransactionEvent.FiniteEventStateMachine.PUBLISHED.equals(transactionEvent.getState());
            }
            return need;
        }

        boolean pushEvent(Event event){
            if (event == null){
                logger.error("不允许发布值为null的事件！");
                return false;
            }
            //使用offer，确保事件发布顺序
            return eventQueue.offer(event);
        }

        /**
         * @Author huang.zh
         * @Description 将就绪的半事务事件发布到事件队列
         * @Date 9:03 下午 2022/12/6
         * @Param [transactionEvent]
         * @return
         **/
        void doPushTransactionEvent(TransactionEvent transactionEvent){
            if (TransactionEvent.FiniteEventStateMachine.PUBLISHED.equals(transactionEvent.getState())){
                eventQueue.offer(transactionEvent);
            }
        }


        /**
         * @Author huang.zh
         * @Description 唤醒指定事件的订阅者
         * @Date 3:25 下午 2022/11/20
         * @Param [event]
         * @return
         **/
        void notifySubscribers(Event event) {
            Class<? extends Event> eventType = event.getClass();
            if (!subscriberMap.containsKey(eventType) && eventType.getName().contains("$")){
                eventType = (Class<? extends Event>) eventType.getSuperclass();
            }
            if (subscriberMap.containsKey(eventType)){
                NotifyNode notifyNode = subscriberMap.get(eventType);
                do {
                    Subscriber subscriber = notifyNode.subscriber;
                    ExecutorService executor = subscriber.executor();
                    EventCallable eventCallable = new EventCallable(subscriber, event);
                    Future<Boolean> future;
                    if (executor != null){
                        logger.info("当前订阅者"+subscriber.getClass().getSimpleName()+"提供线程池，委托订阅者执行消费事件");
                        //如果当前订阅者提供了线程池，就委托给订阅者执行
                        future = executor.submit(eventCallable);
                    } else {
                        logger.info("当前订阅者"+subscriber.getClass().getSimpleName()+"未提供线程池，由当前线程调用worker执行消费事件");
                        //订阅者未提供线程池，没有异步条件，由当前线程调用消费事件
                        future = worker.submit(eventCallable);
                    }
                    notifyNode = notifyNode.next;
                } while (notifyNode != null);
            } else {
                logger.warn("当前暂无事件"+eventType.getSimpleName()+"的订阅者注册到NotifyCenter。");
            }
        }
    }

    /**
     * @Author huang.zh
     * @Description 发布失败事件的重试队列
     * @Date 9:57 下午 2022/11/30
     * @Param
     * @return
     **/
    private static class EventRetryLoop extends Thread{

        private BlockingQueue<RetryEvent> retryQueue;

        private Integer retryTime;

        private Long retryMills;

        private Integer retryEventMax = 1 << 8;

        public EventRetryLoop(NotifyCenterProperties notifyCenterProperties) {
            retryTime = notifyCenterProperties.getRetryTime();
            retryEventMax = notifyCenterProperties.getRetryEventMax();
            retryMills = TimeUnit.SECONDS.toMillis(notifyCenterProperties.getRetrySeconds());
            retryQueue = new DelayQueue<>();
        }

        @Override
        public void run() {
            for (;;){
                try {
                    RetryEvent retryEvent = retryQueue.take();
                    if (retryTime > 0 && retryEvent.getRetryTime() <= retryTime){
                        Event event = (Event) retryEvent.getEvent();
                        logger.info("重试投递："+event.description()+"，重试次数"+retryEvent.getRetryTime());
                        if (!doPublishEvent(event)){
                            //发布失败，继续尝试投递
                            retryEvent.refresh(retryMills);
                            retryFailedEvents(retryEvent);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void retryFailedEvents(Event event){
            if (retryTime <= 0){
                logger.warn("当前事件发布重试次数为0，不尝试重新发布事件！");
                return;
            }
            RetryEvent retryEvent;
            if (!(event instanceof RetryEvent)){
                retryEvent = new RetryEvent(event,retryMills);
            } else {
                retryEvent = (RetryEvent) event;
            }
            if (retryEvent.getRetryTime() > retryTime){
                logger.warn("超出最大允许重试次数");
                return;
            }
            if (retryQueue.size() >= retryEventMax){
                logger.warn("超出最大允许重试事件数");
                return;
            }
            retryQueue.offer(retryEvent);
        }

    }

    /**
     * @Author huang.zh
     * @Description 消费事件回调任务
     * @Date 6:26 下午 2022/11/27
     * @Param
     * @return
     **/
    private class EventCallable implements Callable<Boolean>{

        Subscriber subscriber;

        Event event;

        EventCallable(Subscriber sc,Event e){
            subscriber = sc;
            event = e;
        }

        @Override
        public Boolean call() throws Exception {
            return subscriber.onEvent(event);
        }
    }

    /**
     * @description: 消亡事件总线，负责对消亡的事件进行持久化
     * @author: huangzh
     * @date: 2022/12/19 21:08
     **/
    private class DeadEventLoop {
        //定时任务线程池，负责定时记录达到消亡要求的事件
        private ScheduledExecutorService deadEventWorker;
        //本地缓存，暂存待持久化的消亡事件
        private List<DeadEvent> deadEvents;
        //持久化策略
        private PersistenceStrategy persistenceStrategy;

        public DeadEventLoop() {
            //单线程记录
            deadEventWorker = Executors.newSingleThreadScheduledExecutor();
            deadEvents = new CopyOnWriteArrayList<>();
            //初始化后，首次将于10秒后启动检查任务，往后间隔30秒检查一次是否有消亡事件需要持久化
            deadEventWorker.scheduleWithFixedDelay(new CheckDeadEventTask(),10L,30L,TimeUnit.SECONDS);
            persistenceStrategy = new DefaultPersistenceStrategy();
        }

        /**
         * @description: 消亡事件持久化检查任务
         * @author: huangzh
         * @date: 2022/12/19 21:27
         **/
        private class CheckDeadEventTask implements Runnable{
            @Override
            public void run() {
                logger.info("开始执行消亡事件持久化检查任务");
                if (deadEvents.isEmpty()){
                    logger.info("暂无需要持久化的消亡事件");
                    return;
                } else {
                    deadEvents.forEach(event -> {
                        persistenceStrategy.doPersistence(event);
                        deadEvents.remove(event);
                    });
                    logger.info("结束执行消亡事件持久化检查任务");
                }
            }
        }
    }

}

