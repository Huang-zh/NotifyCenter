package org.lemontechnology.notifycenter.parser;


import org.lemontechnology.notifycenter.logging.Logger;
import org.lemontechnology.notifycenter.logging.LoggerFactory;
import org.lemontechnology.notifycenter.properties.NotifyCenterProperties;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author huang.zh
 * @Description 配置解析器顶层接口
 * @Date 2:51 下午 2021/12/10
 * @Param
 * @return
 **/
public interface ConfigurationParser {

    String FILE_NAME = "NotifyCenter";

    String PUBLISHER_MAX = "notify-center.publisherMax";

    String EVENT_MAX = "notify-center.eventMax";

    String RETRY_TIME = "notify-center.retryTime";

    String RETRY_EVENT_MAX = "notify-center.retryEventMax";

    String RETRY_SECONDS = "notify-center.retrySeconds";

    Logger logger = LoggerFactory.getLog(ConfigurationParser.class.getName());

    /**
     * @Author huang.zh
     * @Description 获取配置入口方法
     * @Date 8:38 下午 2022/12/2
     * @Param [files]
     * @return
     **/
    default NotifyCenterProperties obtainNotifyCenterProperties(List<File> files){
        File expectConfigurationFile = doFindExpectConfigurationFile(files, getParser());
        if (expectConfigurationFile == null){
            logger.info("暂无可用的推送中心配置文件，将使用默认配置启动推送中心组件！");
            //如果无配置文件就使用默认配置
            return new NotifyCenterProperties();
        }
        Map<String, String> configurationMap;
        try {
            configurationMap = doParseConfigurationFile(expectConfigurationFile);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.info("推送中心配置文件解析出错，将使用默认配置启动推送中心组件！");
            return new NotifyCenterProperties();
        }
        if (configurationMap.isEmpty()){
            //如果配置文件为空就使用默认配置
            logger.info("推送中心配置文件无配置项，将使用默认配置启动推送中心组件！");
            return new NotifyCenterProperties();
        }
        return doObtainNotifyCenterProperties(configurationMap);
    }

    /**
     * @Author huang.zh
     * @Description 递归查找符合尾缀要求的配置文件
     * @Date 8:38 下午 2022/12/2
     * @Param [files, parser]
     * @return
     **/
    default File doFindExpectConfigurationFile(List<File> files, ConfigurationParser parser){
        if (files.isEmpty()){
            throw new RuntimeException("根目录下无文件！请检查编译目录！");
        }
        String fileName = FILE_NAME + getSuffix();
        List<File> list = files.stream().filter(file -> {
            String path = file.getName();
            return fileName.equals(path.substring(path.lastIndexOf("\\")+1));
        }).collect(Collectors.toList());
        if (list.isEmpty()){
            ConfigurationParser nextParser = parser.getNextParser();
            if (nextParser == null){
                return null;
            }
            return doFindExpectConfigurationFile(files, nextParser);
        }
        return list.get(list.size() - 1);
    }

    /**
     * @Author huang.zh
     * @Description 转化配置map为最终的实体类
     * @Date 8:38 下午 2022/12/2
     * @Param [configurationMap]
     * @return
     **/
    default NotifyCenterProperties doObtainNotifyCenterProperties(Map<String, String> configurationMap){
        NotifyCenterProperties notifyCenterProperties = new NotifyCenterProperties();
        Optional.ofNullable(configurationMap.get(EVENT_MAX)).ifPresent(value -> notifyCenterProperties.setEventMax(Integer.parseInt(value)));
        Optional.ofNullable(configurationMap.get(PUBLISHER_MAX)).ifPresent(value -> notifyCenterProperties.setPublisherMax(Integer.parseInt(value)));
        Optional.ofNullable(configurationMap.get(RETRY_TIME)).ifPresent(value -> notifyCenterProperties.setRetryTime(Integer.parseInt(value)));
        Optional.ofNullable(configurationMap.get(RETRY_EVENT_MAX)).ifPresent(value -> notifyCenterProperties.setRetryEventMax(Integer.parseInt(value)));
        Optional.ofNullable(configurationMap.get(RETRY_SECONDS)).ifPresent(value -> notifyCenterProperties.setRetrySeconds(Integer.parseInt(value)));
        return notifyCenterProperties;
    }

    /**
     * @Author huang.zh
     * @Description 将文件解析为配置项的集合，由子类自行实现
     * @Date 8:39 下午 2022/12/2
     * @Param [file]
     * @return
     **/
    Map<String,String> doParseConfigurationFile(File file) throws Exception;

    /**
     * @Author huang.zh
     * @Description 获取当前配置解析器
     * @Date 8:39 下午 2022/12/2
     * @Param []
     * @return
     **/
    ConfigurationParser getParser();

    /**
     * @Author huang.zh
     * @Description 获取下一个解析器
     * @Date 8:40 下午 2022/12/2
     * @Param []
     * @return
     **/
    ConfigurationParser getNextParser();

    /**
     * @Author huang.zh
     * @Description 获取当前解析器负责解析文件的尾缀
     * @Date 8:40 下午 2022/12/2
     * @Param []
     * @return
     **/
    String getSuffix();

}
