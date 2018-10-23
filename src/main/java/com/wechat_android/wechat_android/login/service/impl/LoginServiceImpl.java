package com.wechat_android.wechat_android.login.service.impl;

import com.wechat_android.wechat_android.login.service.LoginService;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Override
    public String login(String userName, String pwd){
        return "登录成功";
    }
}
