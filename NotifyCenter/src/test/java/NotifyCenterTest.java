import org.junit.Test;
import org.lemontechnology.notifycenter.NotifyCenter;
import org.lemontechnology.notifycenter.event.TransactionEvent;

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
        notifyCenter.publishEvent(transactionEvent);
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
