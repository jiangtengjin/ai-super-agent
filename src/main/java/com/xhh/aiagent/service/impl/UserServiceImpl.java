package com.xhh.aiagent.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.exception.ThrowUtils;
import com.xhh.aiagent.manager.CodeMailManager;
import com.xhh.aiagent.mapper.UserMapper;
import com.xhh.aiagent.model.dto.UserRegisterDto;
import com.xhh.aiagent.model.entity.User;
import com.xhh.aiagent.service.UserService;
import jakarta.annotation.Resource;
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

    // 邮箱验证正则表达式
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public Long register(UserRegisterDto dto) {
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
        User user = this.getOne(queryWrapper);
        ThrowUtils.throwIf(ObjectUtil.isNotNull(user), ErrorCode.PARAMS_ERROR, "账号已存在");

        // 添加数据
        User newUser = new User();
        newUser.setUserAccount(userAccount);
        newUser.setUserPassword(userPassword);
        this.save(newUser);

        return newUser.getId();
    }

}




