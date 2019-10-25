package com.stylefeng.guns.user;

import com.stylefeng.guns.user.vo.UserInfoModel;
import com.stylefeng.guns.user.vo.UserModel;

/**
 * @AUTHOR :yuankejia
 * @DESCRIPTION:
 * @DATE:CRETED: IN 10:52 2019/10/23
 * @MODIFY:
 */
public interface UserApi {
    public int login(String username,String password);

    boolean register(UserModel userModel);

    boolean checkUsername(String username);

    UserInfoModel getUserInfo(int uuid);

    UserInfoModel updateUserInfo(UserInfoModel userInfoModel);


}
