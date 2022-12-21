import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.junit.Test;
import org.lemontechnology.notifycenter.event.DeadEvent;
import org.lemontechnology.notifycenter.event.TransactionEvent;
import org.lemontechnology.notifycenter.strategy.DefaultPersistenceStrategy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 测试二进制文件分割用例
 * @author: huang.zh
 * @create: 2022-12-21 20:12
 **/
public class DefaultPersistenceStrategyTest {

    @Test
    public void test(){
//        DefaultPersistenceStrategy strategy = new DefaultPersistenceStrategy();
//        List<DeadEvent> list = new ArrayList<>(2048);
//        for (int i = 1; i <= 2048; i++) {
//            list.add(new DeadEvent("testData"+i, DeadEvent.EventType.TRANSACTION,
//                    System.currentTimeMillis(),false, TransactionEvent.FiniteEventStateMachine.PREPARED));
//        }
//        strategy.doPersistence(list);
        Kryo kryo = new Kryo();
        kryo.register(DeadEvent.class);
        kryo.register(DeadEvent.EventType.class);
        kryo.register(TransactionEvent.FiniteEventStateMachine.class);
        try {
            Input input = new Input(new FileInputStream("NotifyCenter.bin"));
            DeadEvent event = kryo.readObject(input,  DeadEvent.class) ;
            System.out.println(kryo.getNextRegistrationId());
            while (event!=null){
                System.out.println(event.data()+" "+kryo.isFinal(DeadEvent.class) + " "+kryo.isClosure(DeadEvent.class));
                event = kryo.readObject(input,  DeadEvent.class);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
