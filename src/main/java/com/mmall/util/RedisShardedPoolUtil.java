package com.mmall.util;

import com.mmall.common.RedisShardedPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ShardedJedis;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-04-11
 * Time: 上午12:12
 */
public class RedisShardedPoolUtil {
    private static Logger logger = LoggerFactory.getLogger(RedisShardedPoolUtil.class);


    //设置key有效时间,单位s
    public static Long expire(String key,int endTime){
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.expire(key, endTime);
        } catch (Exception e) {
            logger.error("expire key:{}  error",key,e);
            //返回损坏实例
            RedisShardedPool.returnBokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String set(String key,String value){
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            logger.error("set key:{} value {} error",key,value,e);
            //返回损坏实例
            RedisShardedPool.returnBokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static Long setnx(String key,String value){
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setnx(key, value);
        } catch (Exception e) {
            logger.error("setnx key:{} value {} error",key,value,e);
            //返回损坏实例
            RedisShardedPool.returnBokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

//endTime单位为s
    public static String setEx(String key,String value,int endTime){
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setex(key,endTime, value);
        } catch (Exception e) {
            logger.error("setex key:{} value {} error",key,value,e);
            //返回损坏实例
            RedisShardedPool.returnBokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String get(String key){
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            logger.error("get key:{}  error",key,e);
            //返回损坏实例
            RedisShardedPool.returnBokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String getset(String key,String value){
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.getSet(key,value);
        } catch (Exception e) {
            logger.error("getset key:{}  error",key,e);
            //返回损坏实例
            RedisShardedPool.returnBokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }



    public static Long del(String key){
        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            logger.error("del key:{}  error",key,e);
            //返回损坏实例
            RedisShardedPool.returnBokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

     public static void main(String args[]) {
         RedisShardedPoolUtil.set("test","testVal");
         String val = RedisShardedPoolUtil.get("test");
         System.out.println(val);

         for(int i = 0;i<10;i++){
             RedisShardedPoolUtil.setEx("testex"+i,"testvalex",60*10);
         }

//         RedisShardedPoolUtil.expire("test",60*20);
//         RedisShardedPoolUtil.del("test");

         }

}
