package com.stylefeng.guns.rest.common;

import com.stylefeng.guns.user.vo.UserInfoModel;

/**
 * @AUTHOR :yuankejia
 * @DESCRIPTION:
 * @DATE:CRETED: IN 13:05 2019/10/23
 * @MODIFY:
 */
public class CurrentUser {
    //线程绑定的存储空间
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

        public static void saveUserId(String id){
        threadLocal.set(id);
        }
        public static String getCurrentUser(){
        return threadLocal.get();
    }

}
