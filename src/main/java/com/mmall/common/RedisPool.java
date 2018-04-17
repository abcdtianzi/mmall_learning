package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.security.PrivateKey;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-04-10
 * Time: 下午10:03
 */
public class RedisPool {
    private static JedisPool pool;
    //最大连接数
    private static Integer maxTotal = Integer.valueOf(PropertiesUtil.getProperty("redis.max.total", "20"));
    //最大空闲实例个数，idle状态（空闲状态）
    private static Integer maxIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.max.idle", "20"));
    //最小空闲实例个数
    private static Integer minIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.min.idle", "20"));
    //在Borrow一个实例的时候，是否要进行验证操作，如果为true，则得到的实例肯定可用
    private static Boolean testOnBorrow = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    //在return一个实例的时候，是否要进行验证操作，如果为true，则得到的实例肯定可用
    private static Boolean testOnReturn = Boolean.valueOf(PropertiesUtil.getProperty("redis.test.retuen", "true"));

    private static String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort = Integer.valueOf(PropertiesUtil.getProperty("redis.port"));

    //初始化连接池
    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        //连接耗尽时，true为阻塞，直到超时抛出超时异常，false为抛出异常，默认为true
        config.setBlockWhenExhausted(true);
        pool = new JedisPool(config, redisIp, redisPort, 1000 * 2);
    }

    static {
        initPool();
    }

    //取出实例
    public static Jedis getJedis() {
        return pool.getResource();
    }

    //放回实例
    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    //放回损坏实例
    public static void returnBokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void main(String args[]) {
       Jedis jedis = pool.getResource();
        jedis.set("idea","ideavalue");
        returnResource(jedis);
        pool.destroy(); //销毁连接池中的所有连接
        System.out.println("end");



    }
}