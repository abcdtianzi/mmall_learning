package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-03-24
 * Time: 下午2:39
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private  static Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private IOrderService orderService;



    //创建订单
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId,HttpServletRequest request){
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.createOrder(user.getId(),shippingId);
    }

    //取消订单
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo,HttpServletRequest request){
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.cancel(user.getId(),orderNo);
    }

    //获取购物车中的商品信息
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session,HttpServletRequest request){
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getOrderCartProduct(user.getId());
    }



    //预下单生成支付二维码
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request) {
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }


        String path = request.getSession().getServletContext().getRealPath("upload");
        return orderService.pay(orderNo, user.getId(), path);
    }


    //支付宝回调，回调的参数都存放在request里面
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        Map<String,String> params = Maps.newHashMap();

        Map requestParams = request.getParameterMap();
        for (Iterator iterator = requestParams.keySet().iterator(); iterator.hasNext();){
            String name =(String) iterator.next();
            String[] values = (String[])requestParams.get(name);
            StringBuilder stringBuilder = new StringBuilder();
            //将数组拼接成以逗号分隔的字符串
            for (int i = 0; i<values.length;i++){
                stringBuilder.append(i == values.length-1?values[i]:values[i]+",");
            }
            params.put(name,stringBuilder.toString());

        }
        //sign:签名 trade_status:交易状态	  详细查看：https://docs.open.alipay.com/194/103296/
        logger.info("支付宝回调：sign:{},trade_status:{},参数{}",params.get("sign"),
                params.get("trade_status"),params.toString());

        //验证回调的正确性，确定是不是支付宝发的，还要避免重复通知
//        第一步： 在通知返回参数列表中，除去sign、sign_type两个参数外，凡是通知返回回来的参数皆是待验签的参数。
//        第二步： 将剩下参数进行url_decode, 然后进行字典排序，组成字符串，得到待签名字符串
//        第三步： 将签名参数（sign）使用base64解码为字节码串。
//        第四步： 使用RSA的验签方法，通过签名字符串、签名参数（经过base64解码）及支付宝公钥验证签名。
//          第五步：需要严格按照如下描述校验通知数据的正确性。
//        商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，并判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
//      同时需要校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），上述有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。

        //除去sign、sign_type两个参数，源码已经除去sign，因此我们只需要除去sign_type，不去掉验证通不过
        params.remove("sign_type");

        //验签,注意用需要输入signType的构造方法，否则取的不是我们想要的rsa2的验签方法,是默认的rsa1的方法，会导致验签失败
        //另外公钥取得是支付宝公钥，不是商家公钥
        try {
            boolean rsaCheckV2=AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8",Configs.getSignType());

            //验证不通过
            if(!rsaCheckV2){
                return ServerResponse.createByErrorMessage("非法请求，请自重");
            }


        } catch (AlipayApiException e) {
            logger.info("支付宝回调验证异常");
            e.printStackTrace();
        }
        //todo 验证注释中的各种数据，写在service里面
        ServerResponse serverResponse = orderService.aliCallback(params);

        if(serverResponse.isSuccess()){
            return Const.AlipayCallBack.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallBack.RESPONSE_FAILED;

    }

    //查看是否付款成功
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo,HttpServletRequest request){
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        ServerResponse serverResponse = orderService.queryOrderPayStatus(user.getId(),orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }



}
