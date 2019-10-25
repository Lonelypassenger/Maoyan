package com.stylefeng.guns.rest.modular.auth.controller;

import com.alibaba.dubbo.config.annotation.Reference;
//import com.stylefeng.guns.core.exception.GunsException;
import com.stylefeng.guns.rest.common.exception.BizExceptionEnum;
import com.stylefeng.guns.rest.modular.auth.controller.dto.AuthRequest;
import com.stylefeng.guns.rest.modular.auth.controller.dto.AuthResponse;
import com.stylefeng.guns.rest.modular.auth.util.JwtTokenUtil;
import com.stylefeng.guns.rest.modular.auth.validator.IReqValidator;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import com.stylefeng.guns.user.UserApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 请求验证的
 *
 * @author fengshuonan
 * @Date 2017/8/24 14:22
 */
@RestController
public class AuthController {

    @Reference(interfaceClass = UserApi.class)
    private UserApi userApi;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

//    @Resource(name = "simpleValidator")
//    private IReqValidator reqValidator;

    @RequestMapping(value = "${jwt.auth-path}")
    public ResponseVO<?> createAuthenticationToken(AuthRequest authRequest) {
        //userApi.login(authRequest.getUserName(),authRequest.getPassword());

        //这里就是在验证用户名和密码是否有效
        boolean validate = true;
        //返回一个唯一的userid，用来标志用户。
        int userId= userApi.login(authRequest.getUserName(),authRequest.getPassword());
        //int userId = 3;
        //如果validate为0，说明验证是不通过的。
        if(userId == 0) {
            validate = false;
        }

        if (validate) {
            final String randomKey = jwtTokenUtil.getRandomKey();
            final String token = jwtTokenUtil.generateToken(""+userId, randomKey);
            //后台处理结果返回
            return ResponseVO.success(new AuthResponse(token,randomKey));
        } else {
            //throw new GunsException(BizExceptionEnum.AUTH_REQUEST_ERROR);
            //抛出异常，使用我们自定义的那个异常。
            return ResponseVO.appFail("用户名或者密码错误");
        }
    }
}
