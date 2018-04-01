package com.mmall.util;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-03-16
 * Time: 下午11:55
 */
public class BigDecimalUtil {
    private BigDecimalUtil() {

    }

    //加法
    public static BigDecimal add(double v1, double v2) {
        BigDecimal bigDecimal1 = new BigDecimal(Double.toString(v1));
        BigDecimal bigDecimal2 = new BigDecimal(Double.toString(v2));
        return bigDecimal1.add(bigDecimal2);
    }

    //减法
    public static BigDecimal sub(double v1, double v2) {
        BigDecimal bigDecimal1 = new BigDecimal(Double.toString(v1));
        BigDecimal bigDecimal2 = new BigDecimal(Double.toString(v2));
        return bigDecimal1.subtract(bigDecimal2);
    }

    //乘法
    public static BigDecimal mul(double v1, double v2) {
        BigDecimal bigDecimal1 = new BigDecimal(Double.toString(v1));
        BigDecimal bigDecimal2 = new BigDecimal(Double.toString(v2));
        return bigDecimal1.multiply(bigDecimal2);
    }

    //除法
    public static BigDecimal div(double v1, double v2) {
        BigDecimal bigDecimal1 = new BigDecimal(Double.toString(v1));
        BigDecimal bigDecimal2 = new BigDecimal(Double.toString(v2));
        return bigDecimal1.divide(bigDecimal2, 2, BigDecimal.ROUND_HALF_UP);//保留两位小数,四舍五入模式
    }
}