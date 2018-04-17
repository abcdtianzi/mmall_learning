package com.mmall.util;

import com.google.common.collect.Lists;
import com.mmall.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:jackson工具类
 * User: ting
 * Date: 2018-04-11
 * Time: 下午8:12
 */
public class JsonUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);
        //非空字段的列入
        //objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
        //字段的默认值被改变才会被列入
        //objectMapper.setSerializationInclusion(Inclusion.NON_DEFAULT);
        //字段为null或者.length()为0的不列入
        //objectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);


        /*序列化设置,即转成json*/
        //取消默认转换timestamp形式,默认为时间戳
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);

        //忽略空Bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        //所有的日期格式都统一为以下样式,yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        /*反序列化设置*/
        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况,防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    //object转json字符串
    public static <T> String obj2String(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            logger.warn("Parse object to String error", e);
            return null;
        }
    }

    //Object转化为格式化好的json
    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            logger.warn("Parse object to String error", e);
            return null;
        }
    }

    //json字符串转对象  泛型：第一个T是将此方法声明为泛型方法，第二个为返回类型
    public static <T> T string2Obj(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            logger.warn("Parse String to Object error", e);
            return null;
        }
    }

    //解决List的字符串反序列化后里面对象被强制转成LinkedHashMap的问题
    //解决方法1
    public static <T> T string2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class) ? (T) str : objectMapper.readValue(str, typeReference));
        } catch (IOException e) {
            logger.warn("Parse String to Object error", e);
            return null;
        }
    }

    //解决方法2
    public static <T> T string2Obj(String str,Class<?> collectionClass,Class<?> ...elementClass) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClass);

        try {
            return objectMapper.readValue(str,javaType);
        } catch (IOException e) {
            logger.warn("Parse String to Object error", e);
            return null;
        }
    }

    public static void main(String args[]) {
        User u1 = new User();
        u1.setId(1);
        u1.setEmail("281623@qq.com");

        String user1Json = JsonUtil.obj2String(u1);
        String user1JsonPretty = JsonUtil.obj2StringPretty(u1);
        logger.info("user1Json:{}", user1Json);
        logger.info("user1JsonPretty:{}", user1JsonPretty);

        User u2 = JsonUtil.string2Obj(user1Json, User.class);

        //List
        List<User> userList1 = Lists.newArrayList();
        userList1.add(u1);
        userList1.add(u2);
        String userList1Str = JsonUtil.obj2StringPretty(userList1);

        logger.info("userList1Str:{}", userList1Str);

        //List被反序列化后里面的对象会默认转为LinkedHashMap,因此直接反序列化失败！！
        List<User> userListObj = JsonUtil.string2Obj(userList1Str, List.class);
        logger.info("userListObj:{}", userListObj);

        //解决方法1
        List<User> userListObj2 = JsonUtil.string2Obj(userList1Str, new TypeReference<List<User>>() {
        });
        logger.info("userListObj2:{}", userListObj2 );

        //解决方法2
        List<User> userListObj3 = JsonUtil.string2Obj(userList1Str,List.class,User.class);
        logger.info("userListObj3:{}", userListObj3 );







    }


}
