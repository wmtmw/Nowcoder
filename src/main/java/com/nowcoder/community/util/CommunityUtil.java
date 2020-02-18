package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");

    }
    //MD5加密(只能加密，不能解密)
    //为了安全，会加一个随机字符串后再加密
    public static String md5(String key){
        if (StringUtils.isBlank(key)){
            //判断是否是空串，空格视为空串
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
