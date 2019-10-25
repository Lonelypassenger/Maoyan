package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.core.util.MD5Util;
import com.stylefeng.guns.rest.common.persistence.dao.MoocUserTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocUserT;
import com.stylefeng.guns.user.UserApi;
import com.stylefeng.guns.user.vo.UserInfoModel;
import com.stylefeng.guns.user.vo.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @AUTHOR :yuankejia
 * @DESCRIPTION:
 * @DATE:CRETED: IN 10:53 2019/10/23
 * @MODIFY:
 */
@Service(interfaceClass = UserApi.class)
@Component
public class UserServiceImpl implements UserApi {
    @Autowired
    private MoocUserTMapper moocUserTMapper;


    @Override
    public int login(String username, String password) {
        //根据登录账号获取数据库
        MoocUserT moocUserT = new MoocUserT();
        moocUserT.setUserName(username);
        MoocUserT result = moocUserTMapper.selectOne(moocUserT);
        //将获取到的结果来与加密后的password进行比较
        if(result!=null&&result.getUuid()>0){
            String p = MD5Util.encrypt(password);
            if(result.getUserPwd().equals(p)){
                return result.getUuid();
            }
        }
        return 1;
    }

    @Override
    public boolean register(UserModel userModel) {
        //获取注册信息

        //将注册信息实体转换为数据实体【mooc_user_t】
        MoocUserT moocUserT = new MoocUserT();
        //将数据实体存入数据库
        moocUserT.setUserName(userModel.getUsername());

        moocUserT.setEmail(userModel.getEmail());
        moocUserT.setAddress(userModel.getAddress());
        moocUserT.setUserPhone(userModel.getPhone());
        //创建时间和修改时间

        //数据加密[MD5数据加密+salt]就是指两次md5
        String md5Password = MD5Util.encrypt(userModel.getPassword());

        moocUserT.setUserPwd(md5Password);
        //将数据插入到数据库当中
        Integer insert = moocUserTMapper.insert(moocUserT);
        if(insert>0){
            return true;
        }else return false;
    }

    /**
     *   这个方法是查询username为指定值的记录的个数。从而完成check
     * @param username
     * @return
     */
    @Override
    public boolean checkUsername(String username) {
        EntityWrapper<MoocUserT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("user_name",username);
        Integer result = moocUserTMapper.selectCount(entityWrapper);
        System.out.println("执行到这里了");
        if(result!=null && result>0){
            return false;
        }else{
            return true;
        }
    }
    private UserInfoModel do2UserInfo(MoocUserT moocUserT){
        UserInfoModel userInfoModel = new UserInfoModel();

        userInfoModel.setUuid(moocUserT.getUuid());
        userInfoModel.setHeadAddress(moocUserT.getHeadUrl());
        userInfoModel.setPhone(moocUserT.getUserPhone());
        userInfoModel.setUpdateTime(moocUserT.getUpdateTime().getTime());
        userInfoModel.setEmail(moocUserT.getEmail());
        userInfoModel.setUsername(moocUserT.getUserName());
        userInfoModel.setNickname(moocUserT.getNickName());
        userInfoModel.setLifeState(""+moocUserT.getLifeState());
        userInfoModel.setBirthday(moocUserT.getBirthday());
        userInfoModel.setAddress(moocUserT.getAddress());
        userInfoModel.setSex(moocUserT.getUserSex());
        userInfoModel.setBeginTime(moocUserT.getBeginTime().getTime());
        userInfoModel.setBiography(moocUserT.getBiography());

        return userInfoModel;
    }
    @Override
    public UserInfoModel getUserInfo(int uuid) {
        System.out.println(uuid);
        // 根据主键查询用户信息 [MoocUserT]
        MoocUserT moocUserT = moocUserTMapper.selectById(uuid);
        if(moocUserT==null){
            System.out.println("没有获取到数据");
            return null;}
        // 将MoocUserT转换UserInfoModel
        UserInfoModel userInfoModel = do2UserInfo(moocUserT);
        // 返回UserInfoModel
        return userInfoModel;
    }

    @Override
    public UserInfoModel updateUserInfo(UserInfoModel userInfoModel) {
        // 将传入的参数转换为DO 【MoocUserT】
        MoocUserT moocUserT = new MoocUserT();
        moocUserT.setUuid(userInfoModel.getUuid());
        moocUserT.setNickName(userInfoModel.getNickname());
        moocUserT.setLifeState(Integer.parseInt(userInfoModel.getLifeState()));
        moocUserT.setBirthday(userInfoModel.getBirthday());
        moocUserT.setBiography(userInfoModel.getBiography());
        moocUserT.setBeginTime(null);
        moocUserT.setHeadUrl(userInfoModel.getHeadAddress());
        moocUserT.setEmail(userInfoModel.getEmail());
        moocUserT.setAddress(userInfoModel.getAddress());
        moocUserT.setUserPhone(userInfoModel.getPhone());
        moocUserT.setUserSex(userInfoModel.getSex());
        moocUserT.setUpdateTime(null);

        // DO存入数据库
        Integer integer = moocUserTMapper.updateById(moocUserT);
        if(integer>0){
            // 将数据从数据库中读取出来
            UserInfoModel userInfo = getUserInfo(moocUserT.getUuid());
            // 将结果返回给前端
            return userInfo;
        }else{
            return null;
        }
    }
}
