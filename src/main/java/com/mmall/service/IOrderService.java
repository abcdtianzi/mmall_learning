package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-03-24
 * Time: 下午2:47
 */
public interface IOrderService {

    ServerResponse createOrder(Integer userId, Integer shippingId);

    ServerResponse<String> cancel(Integer userId, Long orderNo);

    ServerResponse pay(Long orderNo, Integer userId, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

}
