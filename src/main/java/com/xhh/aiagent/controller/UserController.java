package com.xhh.aiagent.controller;

import cn.hutool.core.util.ObjectUtil;
import com.xhh.aiagent.common.BaseResponse;
import com.xhh.aiagent.common.ResultUtils;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.exception.ThrowUtils;
import com.xhh.aiagent.model.request.UserLoginRequest;
import com.xhh.aiagent.model.request.UserRegisterRequest;
import com.xhh.aiagent.model.vo.LoginUserVO;
import com.xhh.aiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户模块接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest dto) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(dto), ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        String userPassword = dto.getUserPassword();
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度过短");
        Long userId = userService.register(dto);
        return ResultUtils.success(userId);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest loginRequest,
                                               HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(loginRequest), ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        LoginUserVO loginUserVo = userService.login(loginRequest, httpServletRequest);
        return ResultUtils.success(loginUserVo);
    }

    /**
     *  用户登出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

}
