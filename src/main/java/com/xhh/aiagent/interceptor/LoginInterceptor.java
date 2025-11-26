package com.xhh.aiagent.interceptor;

import com.xhh.aiagent.exception.BusinessException;
import com.xhh.aiagent.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * 登录拦截器
 */
@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 白名单
     */
    private static final List<String> EXCLUDE_PATH = Arrays.asList(
            "/api/user/**",
            "/api/code/**",
            "/api/captcha/image/code",
            "/api/swagger-ui/**",
            "/api/v3/api-docs/**",
            "/api/doc.html"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        // 检查是否在白名单中
        if (isExcludePath(requestUri)) {
            return true;
        }
        // 需要登录才能访问
        HttpSession session = request.getSession();
        if (session == null || session.getAttribute("user") == null) {
            log.warn("未登录用户尝试访问受保护资源: {} {}", request.getMethod(), requestUri);
            // 返回统一的未登录响应
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录或会话已过期，请重新登录");
        }

        // 会话有效，放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    /**
     * 判断是否为需要登录的接口
     *
     * @param requestUri
     * @return
     */
    private boolean isExcludePath(String requestUri) {
        for (String path : EXCLUDE_PATH) {
            if (path.endsWith("/**")) {
                // 处理通配符路径
                String basePath = path.substring(0, path.length() - 3);
                if (requestUri.startsWith(basePath)) {
                    return true;
                }
            } else if (path.equals(requestUri)) {
                // 处理普通路径
                return true;
            }
        }
        return false;
    }

}
