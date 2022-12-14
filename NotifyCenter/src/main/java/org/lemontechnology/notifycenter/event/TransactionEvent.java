package org.lemontechnology.notifycenter.event;


import org.lemontechnology.notifycenter.listener.TransactionEventListener;

import java.util.UUID;

/**
 * @Author huang.zh
 * @Description 事务事件抽象
 * @Date 2:17 下午 2022/12/3
 * @Param
 * @return
 **/
public abstract class TransactionEvent implements Event{

    FiniteEventStateMachine state;

    String transactionId;

    TransactionEventListener listener;

    public TransactionEvent(){
        //初始状态
        state = FiniteEventStateMachine.READY;
        //生成唯一id
        transactionId = UUID.randomUUID().toString();
    }

    public TransactionEvent(TransactionEventListener listener) {
        this();
        this.listener = listener;
    }

    public FiniteEventStateMachine getState(){
        return state;
    }

    public void setState(FiniteEventStateMachine finiteEventStateMachine){
        this.state = finiteEventStateMachine;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionEventListener getListener() {
        return listener;
    }

    public void setListener(TransactionEventListener listener){
        this.listener = listener;
    }

    /**
     * @Author huang.zh
     * @Description 事务事件有限状态机
     * @Date 2:16 下午 2022/12/3
     * @Param
     * @return
     **/
    public enum FiniteEventStateMachine {
        //就绪状态，作为初始状态存在
        READY{
            @Override
            public FiniteEventStateMachine prepare() {
                return FiniteEventStateMachine.PREPARED;
            }

            @Override
            public FiniteEventStateMachine publish() {
                return FiniteEventStateMachine.READY;
            }

            @Override
            public FiniteEventStateMachine drop() {
                return FiniteEventStateMachine.READY;
            }
        },
        //一阶段准备状态
        PREPARED{
            @Override
            public FiniteEventStateMachine prepare() {
                return FiniteEventStateMachine.PREPARED;
            }

            @Override
            public FiniteEventStateMachine publish() {
                return FiniteEventStateMachine.PUBLISHED;
            }

            @Override
            public FiniteEventStateMachine drop() {
                return FiniteEventStateMachine.DROPPED;
            }
        },
        PUBLISHED{
            @Override
            public FiniteEventStateMachine prepare() {
                return FiniteEventStateMachine.PUBLISHED;
            }

            @Override
            public FiniteEventStateMachine publish() {
                return FiniteEventStateMachine.PUBLISHED;
            }

            @Override
            public FiniteEventStateMachine drop() {
                return FiniteEventStateMachine.PUBLISHED;
            }
        },
        DROPPED{
            @Override
            public FiniteEventStateMachine prepare() {
                return FiniteEventStateMachine.DROPPED;
            }

            @Override
            public FiniteEventStateMachine publish() {
                return FiniteEventStateMachine.DROPPED;
            }

            @Override
            public FiniteEventStateMachine drop() {
                return FiniteEventStateMachine.DROPPED;
            }
        };

        //准备动作
        public abstract FiniteEventStateMachine prepare();
        //发布动作
        public abstract FiniteEventStateMachine publish();
        //丢弃动作
        public abstract FiniteEventStateMachine drop();
    }

}
