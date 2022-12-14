package org.lemontechnology.notifycenter.parser;


import org.lemontechnology.notifycenter.logging.Logger;
import org.lemontechnology.notifycenter.logging.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @description: yml文件解析器
 * @author: huang.zh
 * @create: 2021-12-10 15:25
 **/
class YmlConfigurationParser implements ConfigurationParser {

    private static final String suffix = ".yml";

    private JsonConfigurationParser jsonConfigurationParser = new JsonConfigurationParser();

    private Logger logger = LoggerFactory.getLog(YmlConfigurationParser.class.getName());

    @Override
    public Map<String, String> doParseConfigurationFile(File file)  throws Exception{
        this.logger.info("将使用"+suffix+"尾缀的配置文件启动推送中心组件！");
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        StringBuilder sb = new StringBuilder();
        LinkedList<String> keyQueue = new LinkedList<>();
        LinkedList<Integer> numberQueue = new LinkedList<>();
        LinkedHashMap<String,String> configuration = new LinkedHashMap<>();
        while ((line = reader.readLine()) != null){
            line = line.split("#")[0];
            String trim = line.trim();
            if (trim.length() == 0) {
                continue;
            }
            int number = 0;
            char[] val = line.toCharArray();
            while ((number < line.length()) && (val[number] <= ' ')) {
                //子节点
                number++;
            }
            if (number == 0) {
                //首节点
                keyQueue.clear();
                numberQueue.clear();
            } else {
                Integer last;
                while ((last = numberQueue.getFirst()) != null && last >= number) {
                    keyQueue.pop();
                    numberQueue.pop();
                }
            }
            String[] split = trim.split(": ");
            if (split.length == 1) {
                //不包含值的子节点
                keyQueue.push(trim.substring(0, trim.length() - 1));
                numberQueue.push(number);
            } else {
                //包含值的子节点
                sb.setLength(0);
                for (int i = keyQueue.size() - 1; i > -1; i--) {
                    String s = keyQueue.get(i);
                    sb.append(s).append(".");
                }
                sb.append(split[0]);
                String key = sb.toString();
                configuration.put(key, split[1]);
            }
        }
        return configuration;
    }

    @Override
    public ConfigurationParser getParser() {
        return this;
    }


    @Override
    public ConfigurationParser getNextParser() {
        return jsonConfigurationParser;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }
}

