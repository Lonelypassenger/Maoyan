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
    //这种类型的ThreadLocal如果出现线程且换它可以帮助我们保存当前的数据。
    private static final InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<>();

        public static void saveUserId(String id){
        threadLocal.set(id);
        }
        public static String getCurrentUser(){
        return threadLocal.get();
    }

}
