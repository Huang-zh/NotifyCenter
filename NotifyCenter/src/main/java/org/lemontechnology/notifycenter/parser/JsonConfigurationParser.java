package org.lemontechnology.notifycenter.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.lemontechnology.notifycenter.logging.Logger;
import org.lemontechnology.notifycenter.logging.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @description: json配置文件解析器
 * @author: huang.zh
 * @create: 2021-12-10 16:43
 **/
class JsonConfigurationParser implements ConfigurationParser {

    private static final String suffix = ".json";

    private ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLog(JsonConfigurationParser.class.getName());


    @Override
    public Map<String, String> doParseConfigurationFile(File file)  throws Exception{
        this.logger.info("将使用"+suffix+"尾缀的配置文件启动推送中心组件！");
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null){
            builder.append(line);
        }
        Map map = objectMapper.readValue(builder.toString(), Map.class);
        return map;
    }

    @Override
    public ConfigurationParser getParser() {
        return this;
    }


    @Override
    public ConfigurationParser getNextParser() {
        return null;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }
}

