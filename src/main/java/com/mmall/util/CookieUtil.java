package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-04-15
 * Time: 下午5:09
 */
public class CookieUtil {
    private final static Logger logger = LoggerFactory.getLogger(CookieUtil.class);
//    private final static String COOKIE_DOMAIN = "ting.com";  //url只有在相应的领域里面才会存到cookie，这里是所有的ting.com结尾的领域
    private final static String COOKIE_DOMAIN = "localhost";  //url只有在相应的领域里面才会存到cookie，这里是所有的ting.com结尾的领域
    private final static String COOKIE_NAME = "mmall_login_token";

    public static void writeLoginToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/");//代表设置在根目录,如果设置/mmall的话，只会在/mmall路径上写入cookie,
        cookie.setHttpOnly(true);//防止脚本攻击带来的信息泄漏风险
        //单位是s
        //如果没设置MaxAge不会写入硬盘，而是写入内存，只在当前页面有效
        cookie.setMaxAge(60 * 60 * 24 * 365);//-1代表永久，这里设置一年
        logger.info("write cookieName:{},cookieValue，{}", cookie.getName(), cookie.getValue());
        response.addCookie(cookie);
    }

    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                logger.info("Read cookieName:{},cookieValue，{}", cookie.getName(), cookie.getValue());
                if (StringUtils.equals(cookie.getName(), COOKIE_NAME)) {
                    logger.info("return cookieName:{},cookieValue，{}", cookie.getName(), cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    //登出需要删除cookie
    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equals(cookie.getName(), COOKIE_NAME)) {
                    logger.info("delete cookieName:{},cookieValue，{}", cookie.getName(), cookie.getValue());
                    cookie.setDomain(COOKIE_DOMAIN);
                    cookie.setPath("/");
                    cookie.setMaxAge(0);//设置成0代表删除此cookie
                    response.addCookie(cookie);
                }
            }
        }

    }
}
