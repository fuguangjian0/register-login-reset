## 前后端分离项目模板

功能:登录,注册,密码重置

- 登录(用户名,邮箱登录)
- 注册用户(邮箱注册)
- 重置密码(通过邮箱重置密码)

前端vue命令,在idea命令行窗口下执行

![8d7592736c27211e5cb8a330e0e8d05c.png](https://i2.mjj.rip/2023/06/20/8d7592736c27211e5cb8a330e0e8d05c.png)



这是一个vue3项目,删除项目示例页面,然后编写新的页面



Vue  的目录结构解释

```diff
src
--components 业务组件,应用的业务逻辑部分
--net  axios 
--router  放置你的路由文件
--stores  Vuex 相关文件
--views  它包含的也是业务组件。但其实它更应该是路由的一种映射
--App.vue  项目的主组件，页面入口文件 ，所有页面都在App.vue下进行切换
--main.js  导入全局组件
index.html 主页
package.json  组件集合
package-lock.json  组件集合详细说明
```



配置路由

```diff
-router
--WelcomeView
---LoginPage
---RegisterPage
---ForgetPage
--IndexView
```

后端目录

```diff
src
 ├─main
    ├─java
    │  └─com
    │      └─test
    │          ├─config
    │          ├─controller
    │          ├─entity
    │          ├─interceptor
    │          ├─mapper
    │          └─service
    └─resources
```

```diff
-config
--SecurityConfiguration
--WebConfiguration
```

SecurityConfiguration.java注意filterChain的配置

```java
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
```

WebConfiguration.java 添加了一个拦截器 AuthorizeInterceptor

AuthorizeInterceptor.java

```java
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
```

这个account - session在 AuthorizeServiceImpl 类的 loadUserByUsername方法中用到

```java
@Override //按用户名加载用户
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username==null) throw new UsernameNotFoundException("用户名不能为空");
        Account account = mapper.findAccountByNameOrEmail(username);
        if (account==null) throw new UsernameNotFoundException("用户名或密码错误");
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("user")
                .build();
    }
```











































