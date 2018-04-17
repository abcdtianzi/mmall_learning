package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.RedisPool;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by ting on 2018/3/10.
 */

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService userService;


    /**
     * 用户登录
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session,
                                      HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
        ServerResponse<User> response = userService.login(username, password);
        //设置session
        if (response.isSuccess()) {
            //session.setAttribute(Const.CURRENT_USER, response.getData());
            //将token写入cookie
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());

            //将sessionID存入redis缓存
            RedisPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    /**
     * 用户登出
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session,HttpServletRequest request, HttpServletResponse response) {
       // session.removeAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        //redis删除该token信息
        RedisPoolUtil.del(loginToken);
        //删除cookie的token信息
        CookieUtil.delLoginToken(request,response);

        return ServerResponse.createBySuccess();
    }

    /**
     * 注册
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return userService.register(user);
    }

    /**
     * 校验用户名、Email是否已存在
     * @param str 校验字段
     * @param type 校验类型（用户名、Email）
     * @return
     */
    @RequestMapping(value = "checkValid.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return userService.checkValid(str, type);
    }

    /**
     * 获取session用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session,HttpServletRequest request){
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
         User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
    }


    /**
     * 取忘记密码问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return userService.selectQuestion(username);
    }

    /**
     * 校验忘记密码问题答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return userService.checkAnswer(username,question,answer);
    }


    /**
     * 忘记密码校验成功后的重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
        return userService.forgetResetPassword(username,passwordNew,forgetToken);
    }


    /**
     * 登录状态下的修改密码
     * @param session
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew,HttpServletRequest request){
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr,User.class);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return userService.resetPassword(passwordOld,passwordNew,user);
    }

    /**
     * 登录状态下更新用户信息
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session,User user,HttpServletRequest request){
//        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr,User.class);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //从session获取不可变值来防止用户横向越权
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = userService.updateInformation(user);
        if(response.isSuccess()){
            // 为了更严谨。保证我们是返回的时候username是正确,防止被用户修改
            response.getData().setUsername(currentUser.getUsername());
            //更新Session信息
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session,HttpServletRequest request){
//        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr,User.class);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");
        }
        return userService.getInformation(currentUser.getId());
    }



}
