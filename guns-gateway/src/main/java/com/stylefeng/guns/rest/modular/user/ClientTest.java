package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.user.UserApi;
import org.springframework.stereotype.Component;

/**
 * @AUTHOR :yuankejia
 * @DESCRIPTION:
 * @DATE:CRETED: IN 11:02 2019/10/23
 * @MODIFY:
 */
@Component
public class ClientTest {
    @Reference(interfaceClass = UserApi.class)
    private UserApi userApi;

    public void run(){
        userApi.login("admin","kejia");

    }

}
