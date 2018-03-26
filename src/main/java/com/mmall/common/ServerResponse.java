package com.mmall.common;

import com.sun.org.apache.xpath.internal.operations.String;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * Created by ting on 2018/3/10.
 */
/*高可复用响应对象*/
    /*为null的属性不写入json中*/
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int statue;

    private String msg;

    private T data;


    /*构造函数*/
    public ServerResponse(int statue, String msg, T data) {
        this.statue = statue;
        this.msg = msg;
        this.data = data;
    }

    public ServerResponse(int statue, String msg) {
        this.statue = statue;
        this.msg = msg;
    }

    public ServerResponse(int statue, T data) {
        this.data = data;
        this.statue = statue;
    }

    public ServerResponse(int statue) {
        this.statue = statue;
    }


    /*使之不在json序列化中，不传给前端*/
    @JsonIgnore
    public boolean isSuccess() {
        return this.statue == ResponseCode.SUCCESS.getCode();
    }
    /*封装构造函数*/

    public static <T> ServerResponse<T> createBySuccess() {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }


    //方法名不一样是为了区分String和T泛型类型的冲突，调用createBySuccess是用T，调用createBySuccessMessage是用String
    public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }


    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }

    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage){
        return new ServerResponse<T>(errorCode,errorMessage);
    }




    /*javabean*/
    public int getStatue() {
        return statue;
    }

    public void setStatue(int statue) {
        this.statue = statue;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
