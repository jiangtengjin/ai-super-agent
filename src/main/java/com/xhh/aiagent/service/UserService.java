package com.xhh.aiagent.service;

import com.xhh.aiagent.model.dto.UserRegisterDto;
import com.xhh.aiagent.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

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
    Long register(UserRegisterDto dto);

}
