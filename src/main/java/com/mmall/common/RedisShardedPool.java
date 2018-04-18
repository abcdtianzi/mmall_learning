package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-04-17
 * Time: 下午11:39
 */
public class RedisShardedPool {
    private static ShardedJedisPool pool;  //分片连接池
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

    private static String redisIp = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redisPort = Integer.valueOf(PropertiesUtil.getProperty("redis1.port"));

    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.valueOf(PropertiesUtil.getProperty("redis2.port"));

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
        JedisShardInfo info1 = new JedisShardInfo(redisIp, redisPort, 1000 * 2);//2s超时时间
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 1000 * 2);//2s超时时间
        //如果有密码调用：info1.setPassword();

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>(2);
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        //MURMUR_HASH对应一致性hash算法
        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static {
        initPool();
    }

    //取出实例
    public static ShardedJedis getJedis() {
        return pool.getResource();
    }

    //放回实例
    public static void returnResource(ShardedJedis jedis) {
        pool.returnResource(jedis);
    }

    //放回损坏实例
    public static void returnBokenResource(ShardedJedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void main(String args[]) {
        ShardedJedis jedis = pool.getResource();
        for (int i = 0; i < 10; i++) {
            jedis.set("idea" + i, "ideavalue" + i);
        }
        returnResource(jedis);
        //pool.destroy(); //销毁连接池中的所有连接
        System.out.println("end");


    }
}
