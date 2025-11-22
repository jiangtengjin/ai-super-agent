package com.xhh.aiagent.service;

import com.xhh.aiagent.model.request.UserLoginRequest;
import com.xhh.aiagent.model.request.UserRegisterRequest;
import com.xhh.aiagent.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhh.aiagent.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author 机hui难得
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-11-21 15:11:14
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param dto 请求参数对象
     * @return 用户ID
     */
    Long register(UserRegisterRequest dto);

    /**
     * 用户登录
     *
     * @param userLoginRequest      请求参数对象
     * @param httpServletRequest    httpServletRequest
     * @return 用户信息
     */
    UserVO login(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest);

}
