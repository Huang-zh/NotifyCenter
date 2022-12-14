package org.lemontechnology.notifycenter.publisher;


import org.lemontechnology.notifycenter.NotifyCenter;
import org.lemontechnology.notifycenter.event.Event;
import org.lemontechnology.notifycenter.event.TransactionEvent;
import org.lemontechnology.notifycenter.listener.TransactionEventListener;
import org.lemontechnology.notifycenter.logging.Logger;
import org.lemontechnology.notifycenter.logging.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @description: 事务事件专用发布者，采用二阶段提交+最大努力通知确保事件投递和业务执行的原子性
 * @author: huang.zh
 * @create: 2022-12-02 21:50
 **/
public class TransactionEventPublisher implements Publisher{

    private Logger logger = LoggerFactory.getLog(TransactionEventPublisher.class.getName());

    //事务事件处理核心管理者
    private TransactionEventManager transactionEventManager;

    //事务事件执行状态确认者
    private PollingManager pollingManager;

    //对未知状态的事务事件进行结果轮训的时间间隔（毫秒）
    private Long pollingMills = TimeUnit.SECONDS.toMillis(10l);

    @Override
    public void init(Class<? extends Event> clazz) {
        if (!eventType().equals(clazz)){
            logger.error("事务事件发布者初始化失败！");
            throw new RuntimeException("事务事件发布者初始化失败，请确保使用TransactionEvent初始化！");
        }
        transactionEventManager = new TransactionEventManager();
        pollingManager = new PollingManager();
    }

    @Override
    public boolean publish(Event event) {
        boolean success = false;
        if (eventType().isInstance(event)){
            // 处理事务事件
            transactionEventManager.doTransaction((TransactionEvent) event);
            success = true;
        } else {
            logger.error("事务事件发布者不允许发布非事务事件！");
            throw new RuntimeException("事务事件发布者不允许发布非事务事件！");
        }
        return success;
    }

    @Override
    public Class<? extends Event> eventType() {
        return TransactionEvent.class;
    }


    /**
     * @Author huang.zh
     * @Description 事务事件管理类，类似于分布式事务中的TM角色
     * @Date 11:25 下午 2022/12/3
     **/
    private class TransactionEventManager {

        private Map<String,TransactionEvent> transactionEvents;

        private ScheduledExecutorService scheduledExecutorService;

        private final Long pullingMills = 3000L;


        public TransactionEventManager() {
            transactionEvents = new ConcurrentHashMap<>();
            scheduledExecutorService = Executors.newScheduledThreadPool(4);
        }

        /**
         * @Author huang.zh
         * @Description 处理事务事件，类似二阶段提交的方式：
         *              1.第一阶段，事件处于PREPARE状态，表示可继续执行
         *              2.第二阶段，执行本地事务，根据本地事务执行结果决定事件发布与否
         * @Date 11:23 下午 2022/12/3
         * @Param [transactionEvent]
         * @return
         **/
        public void doTransaction(TransactionEvent transactionEvent){
            TransactionEventListener listener = transactionEvent.getListener();
            if (listener == null){
                logger.error("请为当前事务事件指定监听器！");
                return;
            }
            //执行事务
            try {
                //第一阶段
                transactionEvent = prepare(transactionEvent);
                //第二阶段
                TransactionEvent.FiniteEventStateMachine state = listener.executeLocalTransaction(transactionEvent);
                if (state.equals(TransactionEvent.FiniteEventStateMachine.PUBLISHED)){
                    transactionEvent = publish(transactionEvent);
                } else if (state.equals(TransactionEvent.FiniteEventStateMachine.DROPPED)){
                    transactionEvent = drop(transactionEvent);
                } else {
                    //本地事务暂未返回执行结果，创建轮询任务
                    pollingManager.addPollingTask(transactionEvent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("执行事务事件失败！");
                //移除事件，防止事件堆积
                transactionEvent = transactionEvents.remove(transactionEvent.getTransactionId());
            }
            // TODO: 2022/12/3  保存执行完的事务事件，方便回溯
        }

        TransactionEvent prepare(TransactionEvent transactionEvent) throws Exception{
            TransactionEvent.FiniteEventStateMachine state = transactionEvent.getState();
            transactionEvent.setState(state.prepare());
            transactionEvents.put(transactionEvent.getTransactionId(),transactionEvent);
            return transactionEvent;
        }

        TransactionEvent publish(TransactionEvent transactionEvent) throws Exception{
            String transactionId = transactionEvent.getTransactionId();
            if (transactionEvents.containsKey(transactionId)){
                TransactionEvent.FiniteEventStateMachine state = transactionEvents.get(transactionId).getState();
                transactionEvent.setState(state.publish());
                return transactionEvents.remove(transactionId);
            }
            return transactionEvent;
        }

        TransactionEvent drop(TransactionEvent transactionEvent) throws Exception{
            String transactionId = transactionEvent.getTransactionId();
            if (transactionEvents.containsKey(transactionId)){
                TransactionEvent.FiniteEventStateMachine state = transactionEvents.get(transactionId).getState();
                transactionEvent.setState(state.drop());
                return transactionEvents.remove(transactionId);
            }
            return transactionEvent;
        }


        boolean hasTransactionEvent(String transactionId){
            return transactionEvents.containsKey(transactionId);
        }

        TransactionEvent getTransactionEvent(String transactionId){
            return transactionEvents.get(transactionId);
        }

        void removeTransactionEvent(String transactionId){
            transactionEvents.remove(transactionId);
        }
    }

    /**
     * @Author huang.zh
     * @Description 负责对事务事件进行轮询，确认本地事务提交状态
     * @Date 10:17 下午 2022/12/9
     * @Param
     * @return
     **/
    private class PollingManager{

        private ScheduledExecutorService scheduledExecutorService;

        private final Long pullingMills = 3000L;

        private final Integer pullingMaxTimes = 15;

        public PollingManager() {
            scheduledExecutorService = Executors.newScheduledThreadPool(4);
        }

        /**
         * @description: 为执行本地事务中的事务事件创建轮询任务
         * @author: huangzh
         * @date: 2022/12/15 19:57
         * @param: [transactionEvent]
         * @return: void
         **/
        void addPollingTask(TransactionEvent transactionEvent){
            if (TransactionEvent.FiniteEventStateMachine.PREPARED.equals(transactionEvent.getState())){
                //创建定时任务
                PullingTask task = new PullingTask(transactionEvent);
                scheduledExecutorService.schedule(task,pullingMills,TimeUnit.MILLISECONDS);
            }
        }

        /**
         * @Author huang.zh
         * @Description 对未知状态事务事件进行确认的轮询任务
         * @Date 10:16 下午 2022/12/9
         * @Param
         * @return
         **/
        class PullingTask implements Callable<Integer> {
            //事件
            private TransactionEvent transactionEvent;
            //事件的事务id
            private String transactionId;
            //当前轮询的次数
            private Integer pullingTime;

            PullingTask(TransactionEvent transactionEvent){
                this.transactionEvent = transactionEvent;
                this.transactionId = transactionEvent.getTransactionId();
                //默认生成之后轮询次数直接置为1
                pullingTime = 1;
            }

            @Override
            public Integer call() throws Exception {
                if (transactionEventManager.hasTransactionEvent(transactionId)){
                    TransactionEvent transactionEvent = transactionEventManager.getTransactionEvent(transactionId);
                    TransactionEventListener listener = transactionEvent.getListener();
                    TransactionEvent.FiniteEventStateMachine state = listener.checkLocalTransactionState(transactionEvent);
                    if (TransactionEvent.FiniteEventStateMachine.PREPARED.equals(state)){
                        //本地事务暂未执行完成，继续保持轮询
                        if ((pullingTime += 1) <= pullingMaxTimes) {
                            scheduledExecutorService.schedule(this,pullingMills,TimeUnit.MILLISECONDS);
                        } else {
                            //超出最大轮询次数
                            logger.warn("超出事务事件状态允许最大轮询次数，放弃重试！事务事件id："+transactionId+
                                    "，当前重试次数："+pullingTime+"，允许最大轮询次数："+pullingMaxTimes);
                            transactionEventManager.removeTransactionEvent(transactionId);
                        }
                    } else {
                        if (TransactionEvent.FiniteEventStateMachine.PUBLISHED.equals(state)){
                            //如果本地事务执行成功，通知订阅者消费事件
                            logger.info("当前本地事务执行成功，通知订阅者消费事件！事务事件id："+transactionId);
                            transactionEvent.setState(state);
                            NotifyCenter.doPushTransactionEvent(transactionEvent);
                        } else if (TransactionEvent.FiniteEventStateMachine.DROPPED.equals(state)){
                            logger.info("当前本地事务执行失败，将丢弃该事件！事务事件id："+transactionId);
                        }
                        //本地事务处理成功，无论提交还是回滚，都需要防止事件积压
                        transactionEventManager.removeTransactionEvent(transactionId);
                    }
                }
                return pullingTime;
            }
        }

    }


}

