package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Description:定时关单
 * User: ting
 * Date: 2018-04-26
 * Time: 下午9:44
 */
@Component
public class CloseOrderTask {
    private Logger logger = LoggerFactory.getLogger(CloseOrderTask.class);

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedissonManager redissonManager;

    //    @Scheduled(cron = "0 */1 * * * ?") //每分钟执行一次(/为增量，每一分钟)
    public void closeOrderTaskV1() {
        logger.info("关闭订单开始");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour"));
        orderService.closeOrder(hour);
        logger.info("关闭订单结束");

    }

    //redis分布锁
//    @Scheduled(cron = "0 */1 * * * ?") //每分钟执行一次(/为增量，每一分钟)
    public void closeOrderTaskV2() {
        logger.info("关闭订单v2开始");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        Long result = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(lockTimeout + System.currentTimeMillis()));
        //如果返回值是1，代表设置成功，获取锁
        if (result != null && result.intValue() == 1) {
            RedisShardedPoolUtil.expire(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, 50);//有效期50s
            //执行业务
            int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour"));
            orderService.closeOrder(hour);
            //释放锁
            RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            logger.info("没有获得分布式锁:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }

        logger.info("关闭订单结束");

    }

    //优化版本的redis分布式锁，不仅设置expire，也为了防止由于服务中断没执行到expire导致该锁永远不会释放形成死锁的问题，锁的值加入了过期时间以作判断
    //@Scheduled(cron = "0 */1 * * * ?") //每分钟执行一次(/为增量，每一分钟)
    public void closeOrderTaskV3() {
        logger.info("关闭订单v3开始");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour"));
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "5000"));
        Long result = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(lockTimeout + System.currentTimeMillis()));
        //如果返回值是1，代表设置成功，获取锁
        if (result != null && result.intValue() == 1) {
            RedisShardedPoolUtil.expire(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, 50);//有效期50s
            logger.info("获取{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            //执行业务
            orderService.closeOrder(hour);
            //释放锁
            RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            logger.info("释放{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            logger.info("===============================");
        } else {
            //未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            //判断如果锁的值小于当前时间说明该锁已过期，可以获取锁
            if (lockValueStr != null && Long.valueOf(lockValueStr) < System.currentTimeMillis()) {
                //获得锁
                String getSetResult = RedisShardedPoolUtil.getset(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(lockTimeout + System.currentTimeMillis()));
                //再次用当前时间戳getset。
                //两种情况可以获取锁：
                //1.返回给定的key的旧值，->旧值判断，是否可以获取锁
                //2.当key没有旧值时，即key不存在时，返回nil ->获取锁
                //这里我们set了一个新的value值，获取旧的值。
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(getSetResult, lockValueStr))) {
                    RedisShardedPoolUtil.expire(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, 50);//有效期50s
                    //获得锁，执行业务
                    logger.info("优化后获取{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                    orderService.closeOrder(hour);
                    RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                    logger.info("优化后释放{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                    logger.info("===============================");
                }
            } else {
                logger.info("没有获得分布式锁:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        logger.info("关闭订单定时任务结束");
    }

    //redisson
    @Scheduled(cron = "0 */1 * * * ?") //每分钟执行一次(/为增量，每一分钟)
    public void closeOrderTaskV4() {
        logger.info("关闭订单v4开始");
        boolean getLock = false;
        RLock rLock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        try {
            //不等待放锁（0s）（注意如果大于0s的话，有可能其他进程已经完成业务操作释放锁，这个进程也可以获得锁，导致同一时间两个进程都能获得锁的情况，因此设置为0s）
            // 获得锁后5s释放锁
            getLock = rLock.tryLock(0,5, TimeUnit.SECONDS);
            if(getLock){
                logger.info("Redisson获取{},ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour"));
                orderService.closeOrder(hour);
            }else{
                logger.info("Redisson没有获得分布式锁:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //没获得锁不做任何操作并返回
            if(!getLock){
                return;
            }
            rLock.unlock();
            logger.info("Redisson释放锁");
        }


    }
}
