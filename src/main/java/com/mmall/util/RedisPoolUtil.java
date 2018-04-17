package com.mmall.util;

import com.mmall.common.RedisPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-04-11
 * Time: 上午12:12
 */
public class RedisPoolUtil {
    private static Logger logger = LoggerFactory.getLogger(RedisPoolUtil.class);


    //设置key有效时间
    public static Long expire(String key,int endTime){
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.expire(key, endTime);
        } catch (Exception e) {
            logger.error("expire key:{}  error",key,e);
            //返回损坏实例
            RedisPool.returnBokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String set(String key,String value){
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            logger.error("set key:{} value {} error",key,value,e);
            //返回损坏实例
            RedisPool.returnBokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

//endTime单位为s
    public static String setEx(String key,String value,int endTime){
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.setex(key,endTime, value);
        } catch (Exception e) {
            logger.error("setex key:{} value {} error",key,value,e);
            //返回损坏实例
            RedisPool.returnBokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String get(String key){
        Jedis jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            logger.error("get key:{}  error",key,e);
            //返回损坏实例
            RedisPool.returnBokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }
    public static Long del(String key){
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            logger.error("del key:{}  error",key,e);
            //返回损坏实例
            RedisPool.returnBokenResource(jedis);
            return result;
        }
        RedisPool.returnResource(jedis);
        return result;
    }

     public static void main(String args[]) {
         RedisPoolUtil.set("test","testVal");
         String val = RedisPoolUtil.get("test");
         System.out.println(val);

         RedisPoolUtil.setEx("testex","testvalex",60*10);

         RedisPoolUtil.expire("test",60*20);
         RedisPoolUtil.del("test");

         }

}
