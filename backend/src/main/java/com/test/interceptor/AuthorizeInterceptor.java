package com.test.interceptor;

import com.test.entity.AccountUser;
import com.test.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//拦截器
@Component
public class AuthorizeInterceptor implements HandlerInterceptor {
    @Resource
    UserMapper mapper;

    @Override//上下文如果含有User则在session中存入account
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //从security上下文中获得user对象
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = user.getUsername();
        AccountUser account = mapper.findAccountUserByNameOrEmail(username);
        //把查询到的account存入session中
        request.getSession().setAttribute("account", account);
        return true;
    }

}
