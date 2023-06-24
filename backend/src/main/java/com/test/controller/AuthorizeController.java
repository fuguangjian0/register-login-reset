package com.test.controller;

import com.test.entity.RestBean;
import com.test.service.AuthorizeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    private final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$";
    private final String USERNAME_REGEX = "^[a-zA-Z0-9一-龥]+$";


    @Resource
    AuthorizeService service;

    @PostMapping("/valid-register-email")//注册发送邮箱验证码
    public RestBean<String> validateRegisterEmail(@Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                                  HttpSession session){
        //发送验证电子邮件
        String s = service.sendValidateEmail(email, session.getId(), false);
        if (s == null)//返回空字符串,则发送邮件成功,前端弹窗 '邮件已发送，请注意查收'
            return RestBean.success("邮件已发送，请注意查收");
        else//错误,报错400,返回报错字符串,前端弹窗警告
            return RestBean.failure(400, s);
    }

    @PostMapping("/valid-reset-email")//忘记密码发送邮箱验证码
    public RestBean<String> validateResetEmail(@Pattern(regexp=EMAIL_REGEX) @RequestParam("email")String email,
                                               HttpSession session) {
        String s = service.sendValidateEmail(email, session.getId(), true);
        if (s == null)//返回空字符串,则发送邮件成功,前端弹窗 '邮件已发送，请注意查收'
            return RestBean.success("邮件已发送，请注意查收");
        else//错误,报错400,返回报错字符串,前端弹窗警告
            return RestBean.failure(400, s);
    }

    @PostMapping("/register")//注册按钮,往数据库存入注册数据
    public RestBean<String> registerUser(@Pattern(regexp = USERNAME_REGEX) @Length(min = 2, max = 8)
                                             @RequestParam("username") String username,
                                         @Length(min = 6, max = 16) @RequestParam("password") String password,
                                         @Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                         @Length(min = 6, max = 6) @RequestParam("code") String code,
                                         HttpSession session) {
        String s = service.validateAndRegister(username, password, email, code, session.getId());//验证和注册
        if (s == null) return RestBean.success("注册成功");
        else return RestBean.failure(400, s);//弹窗信息
    }


    @PostMapping("/start-reset")//准备重置密码按钮,往session中存入email地址
    public RestBean<String> startReset(@Pattern(regexp=EMAIL_REGEX)@RequestParam("email")String email,
                                       @Length(min = 6,max = 6)@RequestParam("code")String code, HttpSession session) {
        //作一步验证, 验证是否已经使用该邮箱登陆过
        String s = service.validateOnly(email, code, session.getId());
        if (s==null) {
            //把邮箱存入session,第②步要用到
            session.setAttribute("reset-password", email);
            return RestBean.success();
        }else {
            return RestBean.failure(400,s);
        }
    }


    @PostMapping("/do-reset")//重置密码按钮,修改数据库密码
    public RestBean<String> resetPassword(@Length(min = 6,max = 16) @RequestParam("password") String password,
                                          HttpSession session) {
        String email = (String) session.getAttribute("reset-password");
        if (email == null) return RestBean.failure(401, "请先完成邮件验证");//上一步没有在session中存入对应的email
        else if (service.resetPassword(password, email)) {//重置密码操作,修改数据库中的密码
            session.removeAttribute("reset-password");//session中的数据删掉
            return RestBean.success("密码重置成功");
        }else {
            return RestBean.failure(500, "内部错误,联系管理员");
        }
    }

}
