package org.lemontechnology.notifycenter.parser;

import org.lemontechnology.notifycenter.logging.Logger;
import org.lemontechnology.notifycenter.logging.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @description: properties尾缀文件解析器
 * @author: huang.zh
 * @create: 2021-12-10 14:50
 **/
public class PropertiesConfigurationParser implements ConfigurationParser {

    private static final String suffix = ".properties";

    private YmlConfigurationParser ymlConfigurationParser = new YmlConfigurationParser();

    private Logger logger = LoggerFactory.getLog(PropertiesConfigurationParser.class.getName());

    @Override
    public Map<String, String> doParseConfigurationFile(File file)  throws Exception{
        this.logger.info("将使用"+suffix+"尾缀的配置文件启动推送中心组件！");
        FileInputStream inputStream = null;
        inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        Set<String> propertyNames = properties.stringPropertyNames();
        Map<String,String> configuration = new HashMap<>();
        propertyNames.forEach(name -> configuration.putIfAbsent(name,properties.getProperty(name)));
        return configuration;
    }

    @Override
    public ConfigurationParser getParser() {
        return this;
    }

    @Override
    public ConfigurationParser getNextParser() {
        return ymlConfigurationParser;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }
}
