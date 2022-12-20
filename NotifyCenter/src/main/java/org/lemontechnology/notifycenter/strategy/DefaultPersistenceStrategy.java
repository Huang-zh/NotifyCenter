package org.lemontechnology.notifycenter.strategy;

import com.esotericsoftware.kryo.Kryo;
import org.lemontechnology.notifycenter.event.DeadEvent;

import java.io.File;
import java.io.IOException;

/**
 * @description: 默认的持久化策略，使用Kryo进行持久化
 * @author: huang.zh
 * @create: 2022-12-20 20:35
 **/
public class DefaultPersistenceStrategy implements PersistenceStrategy{
    //文件路径
    private static final String FILE_PATH= "";
    //文件名
    private static final String FILE_NAME= "NotifyCenter.bin";
    //默认1M，达到后开启文件分割
    private static Integer fileSize = 1024;

    private static Kryo kryo = new Kryo();

    static {
        kryo.register(DeadEvent.class);
    }

    /**
     * @Author huang.zh
     * @Description 讲消亡事件持久化到磁盘
     * @Date 8:37 下午 2022/12/20
     * @Param [event]
     * @return
     **/
    @Override
    public void doPersistence(DeadEvent event) {
        File file = new File(FILE_PATH + FILE_NAME);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // TODO: 2022/12/20 具体持久化策略，以及文件分割策略。。。
    }
}
