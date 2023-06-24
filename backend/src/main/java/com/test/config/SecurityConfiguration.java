package com.test.config;

import com.alibaba.fastjson.JSONObject;
import com.test.entity.RestBean;
import com.test.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Resource
    AuthorizeService authorizeService;

    @Bean//验证管理器
    public AuthenticationManager authenticationManager(HttpSecurity security) throws Exception {//验证管理器
        return security
                .getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(authorizeService)
                .and()
                .build();
    }

    @Resource
    DataSource dataSource;

    /*过滤器链
    *       登录接口集合全部放行
    * and() 登录:路径,成功处理,失败处理
    * and() 注销:路径,成功处理
    * and() 记住我:修改参数(默认参数不用改); 令牌库:有效时间
    * and() csrf & cors
    * and() 异常处理
    * */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PersistentTokenRepository repository) throws Exception {
        return http
                .authorizeHttpRequests()//授权HTTP请求
                .requestMatchers("/api/auth/**").permitAll()//localhost:8080/api/auth/**  允许所有人访问
                .anyRequest().authenticated()//任何请求都可以调用这个路径的接口
                .and()
                .formLogin()//表单登录
                .loginProcessingUrl("/api/auth/login")//登入路径
                .successHandler(this::onAuthenticationSuccess)//成功处理程序
                .failureHandler(this::onAuthenticationFailure)//故障处理程序
                .and()
                .logout().logoutUrl("/api/auth/logout")//注销路径
                .logoutSuccessHandler(this::onAuthenticationSuccess)//注销成功处理程序
                .and()
                .rememberMe().rememberMeParameter("remember")//记住我参数,默认remember-me
                .tokenRepository(repository)//令牌库
                .tokenValiditySeconds(3600 * 24 * 7)//令牌有效性秒数
                .and()
                .csrf().disable()//关闭跨站请求伪造漏洞
                .cors().configurationSource(this.corsConfigurationSource())//跨域资源共享
                .and()
                .exceptionHandling()//异常处理
                .authenticationEntryPoint(this::onAuthenticationFailure)//验证入口点
                .and()
                .build();
    }

    @Bean//固定配置
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        jdbcTokenRepository.setCreateTableOnStartup(false);
        return jdbcTokenRepository;
    }

    private CorsConfigurationSource corsConfigurationSource() {//所有源可以共享资源
        CorsConfiguration cors = new CorsConfiguration();
        cors.addAllowedOriginPattern("*");
        cors.setAllowCredentials(true);
        cors.addAllowedHeader("*");
        cors.addAllowedMethod("*");
        cors.addExposedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //关于认证成功
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setCharacterEncoding("utf-8");
        if(request.getRequestURI().endsWith("/login"))
            response.getWriter().write(JSONObject.toJSONString(RestBean.success("登录成功")));
        else if(request.getRequestURI().endsWith("/logout"))
            response.getWriter().write(JSONObject.toJSONString(RestBean.success("退出登录成功")));
    }

    //关于认证失败
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(JSONObject.toJSONString(RestBean.failure(401, exception.getMessage())));
    }

}
