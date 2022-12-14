package org.lemontechnology.notifycenter.listener;

import org.lemontechnology.notifycenter.event.TransactionEvent;

/**
 * @Author huang.zh
 * @Description 事务事件监听器
 * @Date 10:39 下午 2022/12/3
 * @Param
 * @return
 **/
public interface TransactionEventListener {

    /**
     * @Author huang.zh
     * @Description 根据本地事务的执行结果返回对应的事件状态
     * @Date 10:41 下午 2022/12/3
     * @Param [transactionEvent]
     * @return
     **/
    TransactionEvent.FiniteEventStateMachine executeLocalTransaction(TransactionEvent transactionEvent);

    /**
     * @Author huang.zh
     * @Description 对未知状态的事务事件进行结果确认
     * @Date 9:49 下午 2022/12/7
     * @Param [transactionEvent]
     * @return
     **/
    TransactionEvent.FiniteEventStateMachine checkLocalTransactionState(TransactionEvent transactionEvent);
}