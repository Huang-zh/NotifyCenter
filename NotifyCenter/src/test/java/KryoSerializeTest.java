import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.junit.Test;
import org.lemontechnology.notifycenter.event.Event;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsPackage:
 * @Author: huang.zh
 * @CreateTime: 2022-12-17  18:52
 * @Description: 测试kryo序列化事件到本地
 */
public class KryoSerializeTest {

    @Test
    public void test(){
        Kryo kryo = new Kryo();
        kryo.register(ArrayList.class);
        kryo.register(TestEvent.class);
        ArrayList<TestEvent> list = new ArrayList<>(1024);
        for (int i = 0; i < 1024; i++) {
            list.add(new TestEvent(""+i));
        }
        try (Output output = new Output(new FileOutputStream("file.bin"))){
            kryo.writeObject(output, list);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try (Input input = new Input(new FileInputStream("file.bin"))){
            list = kryo.readObject(input,  ArrayList.class);
            for (int i = 0; i < list.size(); i++) {
                TestEvent testEvent = list.get(i);
                System.out.println(testEvent.description()+testEvent.data());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private static class TestEvent implements Event{

        private String data;

        public TestEvent() {
        }

        public TestEvent(String data) {
            this.data = data;
        }

        @Override
        public String description() {
            return "测试事件";
        }

        @Override
        public Object data() {
            return data;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
