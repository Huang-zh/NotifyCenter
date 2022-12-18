package org.lemontechnology.notifycenter.util;


import org.apache.commons.lang3.StringUtils;
import org.lemontechnology.notifycenter.logging.Logger;
import org.lemontechnology.notifycenter.logging.LoggerFactory;
import org.lemontechnology.notifycenter.parser.PropertiesConfigurationParser;
import org.lemontechnology.notifycenter.properties.NotifyCenterProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 配置文件加载工具类
 * @author: huang.zh
 * @create: 2021-12-10 11:24
 **/
public class LoadConfigurationsUtil {

    private static PropertiesConfigurationParser parser = new PropertiesConfigurationParser();

    private static Logger logger = LoggerFactory.getLog(LoadConfigurationsUtil.class.getName());

    public LoadConfigurationsUtil() {

    }

    /**
     * @Author huang.zh
     * @Description  寻找项目resource目录下的关于Notify-Center的配置文件
     * @Date 4:02 下午 2021/12/10
     * @Param []
     * @return
     **/
    public static NotifyCenterProperties notifyCenterProperties() {
        NotifyCenterProperties notifyCenterProperties = null;
        try{
            List<File> fileList = getClassRootPathFiles();
            notifyCenterProperties = parser.obtainNotifyCenterProperties(fileList);
            logger.info("成功解析配置项："+notifyCenterProperties.toString());
        } catch (Exception e){
            logger.error("尝试获取NotifyCenter配置文件失败，将使用默认配置！");
            e.printStackTrace();
        }
        return notifyCenterProperties;
    }

    /**
     * @Author huang.zh
     * @Description 获得resources目录下的所有文件（不包括文件夹哦）
     * @Date 5:06 下午 2021/12/10
     * @Param []
     * @return
     **/
    private static List<File> getClassRootPathFiles() throws FileNotFoundException {
        String rootPathString = LoadConfigurationsUtil.class.getClassLoader().getResource(StringUtils.EMPTY).getFile();
        File rootPath = new File(rootPathString);
        if (!rootPath.exists()){
            throw new FileNotFoundException("指定路径不存在！请检查编译过程是否出错！");
        }
        //返回根目录下的所有文件
        return Arrays.stream(rootPath.listFiles()).filter(file -> file.isFile()).collect(Collectors.toList());
    }

}
