package com.mmall.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * Description:全局异常
 * User: ting
 * Date: 2018-04-20
 * Time: 下午10:39
 */
@Component
public class ExceptionResolver implements HandlerExceptionResolver{
    private Logger logger = LoggerFactory.getLogger(ExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        //如果没这句话则不会打印到日志，无法定位到错误
        logger.error("{} Exception",httpServletRequest.getRequestURI(),e);
        //返回的view,如果jackson版本大于2.x的话用MappingJackson2JsonView
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());
        modelAndView.addObject("status",ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg","接口异常，详情请查看服务端日志");
        modelAndView.addObject("data",e.toString());
        return modelAndView;
    }
}
