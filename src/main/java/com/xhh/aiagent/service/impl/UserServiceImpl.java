package com.xhh.aiagent.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhh.aiagent.constant.UserConstant;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.exception.ThrowUtils;
import com.xhh.aiagent.manager.captcha.CaptchaManager;
import com.xhh.aiagent.manager.codemail.CodeMailManager;
import com.xhh.aiagent.mapper.UserMapper;
import com.xhh.aiagent.model.request.UserLoginRequest;
import com.xhh.aiagent.model.request.UserRegisterRequest;
import com.xhh.aiagent.model.entity.User;
import com.xhh.aiagent.model.vo.UserVO;
import com.xhh.aiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * @author 机hui难得
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-11-21 15:11:14
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private CodeMailManager codeMailManager;

    @Resource
    private CaptchaManager captchaManager;

    // 邮箱验证正则表达式
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public Long register(UserRegisterRequest dto) {
        // 参数校验
        String userAccount = dto.getUserAccount();
        String userPassword = dto.getUserPassword();
        String rePassword = dto.getRePassword();
        String code = dto.getCode();
        ThrowUtils.throwIf(StrUtil.isBlank(userAccount), ErrorCode.PARAMS_ERROR, "账号不能为空");
        // 正则判断邮箱格式
        ThrowUtils.throwIf(EMAIL_PATTERN.matcher(userAccount).matches(), ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        ThrowUtils.throwIf(StrUtil.isBlank(userPassword), ErrorCode.PARAMS_ERROR, "密码不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(rePassword) || !userPassword.equals(rePassword),
                ErrorCode.PARAMS_ERROR, "两次输入密码不相同");
        // 校验邮箱验证码
        String cacheValue = codeMailManager.getCacheValue(dto.getKey());
        ThrowUtils.throwIf(StrUtil.isBlank(cacheValue), ErrorCode.PARAMS_ERROR, "验证码已过期");
        ThrowUtils.throwIf(!code.equals(cacheValue), ErrorCode.PARAMS_ERROR, "验证码错误");

        // 判断账号是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        boolean existed = this.exists(queryWrapper);
        ThrowUtils.throwIf(existed, ErrorCode.PARAMS_ERROR, "账号已存在");

        // 添加数据
        User newUser = new User();
        newUser.setUserAccount(userAccount);
        newUser.setUserPassword(encryptPassword(userPassword)); // 加密后的密码
        newUser.setUserRole(UserConstant.DEFAULT_ROLE); // 默认角色
        String userName = userAccount.split("@")[0]; // 用户名，取 @ 符号前的字符串
        newUser.setUserName(userName);
        this.save(newUser);

        return newUser.getId();
    }

    @Override
    public UserVO login(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        // 参数校验
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(StrUtil.isBlank(userAccount), ErrorCode.PARAMS_ERROR, "账号不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(userPassword), ErrorCode.PARAMS_ERROR, "密码不能为空");
        // 校验图片验证码
        String imageCode = userLoginRequest.getImageCode();
        ThrowUtils.throwIf(StrUtil.isBlank(imageCode), ErrorCode.PARAMS_ERROR, "验证码不能为空");
        String key = userLoginRequest.getKey();
        String cacheValue = captchaManager.getCacheValue(key);
        ThrowUtils.throwIf(StrUtil.isBlank(cacheValue), ErrorCode.PARAMS_ERROR, "验证码已过期");
        ThrowUtils.throwIf(!imageCode.equals(cacheValue), ErrorCode.PARAMS_ERROR, "验证码错误");
        // 根据账号查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User user = this.getOne(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        // 判断密码是否正确
        String encryptPassword = encryptPassword(userPassword);
        ThrowUtils.throwIf(!encryptPassword.equals(user.getUserPassword()), ErrorCode.PARAMS_ERROR, "密码错误");

        // 记录用户的登录态
        httpServletRequest.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return user.objToVo(user);
    }

    /**
     * 加密密码
     *
     * @param password
     * @return
     */
    private String encryptPassword(String password) {
        String salt = "AI AGENT";
        return DigestUtil.md5Hex(password + salt);
    }

}




