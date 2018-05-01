package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created with IntelliJ IDEA.
 * Description:Redisson
 * User: ting
 * Date: 2018-04-29
 * Time: 下午10:46
 */
@Component
public class RedissonManager {
    private static Logger logger = LoggerFactory.getLogger(RedissonManager.class);

    private Config config = new Config();
    private Redisson redisson = null;

    public Redisson getRedisson() {
        return redisson;
    }

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
//    redisson不支持分布式redis
//    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
//    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    @PostConstruct  //方法上加该注解会在项目启动的时候执行该方法,也可以理解为在spring容器初始化的时候执行该方法。
    private  void init(){
        try {
            config.useSingleServer().setAddress(new StringBuilder().append(redis1Ip).append(":").append(redis1Port).toString());
            redisson = (Redisson) Redisson.create(config);
            logger.info("初始化redisson结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
