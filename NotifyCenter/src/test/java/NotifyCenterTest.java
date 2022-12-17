import org.junit.Test;
import org.lemontechnology.notifycenter.NotifyCenter;
import org.lemontechnology.notifycenter.event.Event;
import org.lemontechnology.notifycenter.event.TransactionEvent;
import org.lemontechnology.notifycenter.listener.TransactionEventListener;
import org.lemontechnology.notifycenter.subscriber.Subscriber;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: huang.zh
 * @CreateTime: 2022-12-16  23:16
 * @Description: 测试用例集合
 */
public class NotifyCenterTest {

    private static NotifyCenter notifyCenter = NotifyCenter.instance();

    /**
     * @description: 测试发布事务事件
     * @author: huangzh
     * @date: 2022/12/16 23:18
     * @param: []
     * @return: void
     **/
    @Test
    public void transactionPublisherTest(){
        TransactionEvent transactionEvent = new TransactionEvent() {
            @Override
            public String description() {
                return "测试事务事件";
            }

            @Override
            public Object data() {
                return "test data";
            }
        };

        transactionEvent.setListener(new TransactionEventListener() {
            @Override
            public TransactionEvent.FiniteEventStateMachine executeLocalTransaction(TransactionEvent transactionEvent) {
                System.out.println("-------执行本地事务-------");
                try {
                    //模拟本地事务执行
                    Thread.sleep(1000l);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return TransactionEvent.FiniteEventStateMachine.PREPARED;
            }

            @Override
            public TransactionEvent.FiniteEventStateMachine checkLocalTransactionState(TransactionEvent transactionEvent) {
                return TransactionEvent.FiniteEventStateMachine.PUBLISHED;
            }
        });
        NotifyCenter.registerSubscriber(new Subscriber() {
            @Override
            public Boolean onEvent(Event event) {
                System.out.println(event.data());
                return true;
            }

            @Override
            public Class<? extends Event> subscribeType() {
                return transactionEvent.getClass();
            }
        });

        notifyCenter.publishEvent(transactionEvent);
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description: 测试发布普通事件
     * @author: huangzh
     * @date: 2022/12/17 16:37
     * @param: []
     * @return: void
     **/
    @Test
    public void normalPublisherTest(){

        NotifyCenter.registerSubscriber(new Subscriber() {
            @Override
            public Boolean onEvent(Event event) {
                System.out.println(event.data());
                return true;
            }

            @Override
            public Class<? extends Event> subscribeType() {
                return MyEvent.class;
            }
        });

        notifyCenter.publishEvent(new MyEvent());
        try {
            new CountDownLatch(1).await();
        }catch (InterruptedException e){
            //ignore
        }
    }

    private class MyEvent implements Event{
        @Override
        public String description() {
            return "自定义事件";
        }

        @Override
        public Object data() {
            return "my test data";
        }
    }


}
