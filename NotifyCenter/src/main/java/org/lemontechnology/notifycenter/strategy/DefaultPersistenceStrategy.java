package org.lemontechnology.notifycenter.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang3.StringUtils;
import org.lemontechnology.notifycenter.event.DeadEvent;
import org.lemontechnology.notifycenter.event.TransactionEvent;
import org.lemontechnology.notifycenter.logging.Logger;
import org.lemontechnology.notifycenter.logging.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

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
    private static long fileSize = 16L;

    private static Kryo kryo = new Kryo();
    //文件分割尾缀
    private static int partition = 0;

    private static Logger logger = LoggerFactory.getLog(DefaultPersistenceStrategy.class.getName());

    static {
        kryo.register(DeadEvent.class);
        kryo.register(DeadEvent.EventType.class);
        kryo.register(TransactionEvent.FiniteEventStateMachine.class);
    }

    /**
     * @Author huang.zh
     * @Description 讲消亡事件持久化到磁盘
     * @Date 8:37 下午 2022/12/20
     * @Param [event]
     * @return
     **/
    @Override
    public void doPersistence(Collection<DeadEvent> events) {
        String filePath = FILE_PATH + FILE_NAME;
        //检查文件是否存在
        File file = checkFile(filePath);
        try {
            Output output = new Output(new FileOutputStream(file));
            //写入二进制文件
            for (DeadEvent event : events) {
                kryo.writeObject(output, event);
                //检查是否需要分割当前二进制文件
                if (splitFile(filePath)){
                    output.flush();
                    output.close();
                    output = new Output(new FileOutputStream(filePath));
                }
            }
            output.close();
        } catch (FileNotFoundException e){
            logger.error("尝试写入消亡事件到二进制文件失败！");
        }
    }

    /**
     * @Author huang.zh
     * @Description 检查当前路径下的文件是否存在
     * @Date 7:21 下午 2022/12/21
     * @Param []
     * @return
     **/
    private File checkFile(String filePath){
        File file = new File(filePath);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.error("尝试创建二进制文件失败！");
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * @Author huang.zh
     * @Description 分割文件检查，达到单个二进制文件大小后进行分割
     * @Date 7:48 下午 2022/12/21
     * @Param [filePath]
     * @return
     **/
    private boolean splitFile(String filePath){
        File file = new File(filePath);
        boolean renamed = false;
        if (file.exists() && file.isFile()){
            long size = file.length() / 1024;
            if (renamed = (size > fileSize)){
                String fileName = file.getName();
                String path = fileName.replaceAll(".bin", StringUtils.EMPTY) + "-" + (++partition) + ".bin";
                file.renameTo(checkFile(path));
                //创建新的文件，即NotifyCenter.bin
                checkFile(filePath);
            }
        }
        return renamed;
    }
}
