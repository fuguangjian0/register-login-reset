package com.test.service;

import com.test.entity.Account;
import com.test.entity.AccountUser;
import com.test.entity.RestBean;
import com.test.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthorizeServiceImpl implements AuthorizeService{

    @Value("${spring.mail.username}")
    String emailFrom;

    @Resource
    UserMapper mapper;

    @Resource
    MailSender mailSender;

    @Resource
    StringRedisTemplate template;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    //发送验证电子邮件方法
    @Override
    public String sendValidateEmail(String email, String sessionId, boolean hasAccount) {
        String key = "email:"+sessionId+":"+email+":"+hasAccount;
        if(Boolean.TRUE.equals(template.hasKey(key))) {//如果redis中已经存入对应key
            Long expire = Optional.ofNullable(template.getExpire(key, TimeUnit.SECONDS)).orElse(0L);
            if(expire > 120) return "请求频繁，请稍后再试";//这个key剩余时间大于120秒,(由于前端需要等60秒,而过期时间180秒)
        }
        Account account = mapper.findAccountByNameOrEmail(email);
        //重置密码时,数据库没有这个邮箱,不能重置,没有用这个邮箱注册过
        if (hasAccount && account==null) return "没有此邮件地址账户";
        //注册时,数据库查到这个邮箱,不能再次注册
        if (!hasAccount && account!=null) return "此邮箱已经被注册过了";
        //生成随机六位验证码
        int code = new Random().nextInt(899999)+100000;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);//发邮件的官方邮箱
        message.setTo(email);//接收邮件的用户邮箱
        message.setSubject("您的验证邮件:");//设置主题
        message.setText("验证码是:"+code);//设置内容
        log.info("验证码是: {}", code);

        try {
            mailSender.send(message);
            //存入redis的key, value, 3, 分钟过期
            template.opsForValue().set(key, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }catch (MailException e){
            e.printStackTrace();
            return "邮件发送失败,请检查邮件地址";//前端弹窗警告
        }

    }

    //验证和注册
    @Override
    public String validateAndRegister(String username, String password, String email, String code, String sessionId) {
        String key = "email:" + sessionId + ":" + email + ":false";
        if(Boolean.TRUE.equals(template.hasKey(key))) {//redis内含有这个验证码
            String s = template.opsForValue().get(key);//查找key
            if (s==null)return "验证码错误,请重新请求";
            if (s.equals(code)) {
                Account account = mapper.findAccountByNameOrEmail(username);//根据用户名查找账户,避免重复注册
                if (account != null) return "用户已经注册";
                password = encoder.encode(password);//加密密码
                if (mapper.createAccount(username, password, email) > 0) return null; //存入数据库,username,password,email
                else return "内部错误,请联系管理员";
            }else {
                return "验证码错误,请检查后再提交";
            }
        }else {
            return "请先请求一封验证码邮件";
        }
    }

    //仅验证,验证是否已经使用该邮箱登陆过
    @Override
    public String validateOnly(String email, String code, String sessionId) {
        String key = "email:" + sessionId + ":" + email + ":true";
        if(Boolean.TRUE.equals(template.hasKey(key))) {
            String s = template.opsForValue().get(key);
            if (s==null) return "验证码失效,请重新请求";
            if (s.equals(code)) {
                //如果redis里含有这个邮箱,则已经用过这个邮箱登陆过,需要删除它
                template.delete(key);
                return null;
            }else {
                return "验证码错误,请检查后再提交";
            }
        }else {
            return "请先请求一封验证码邮件";
        }
    }

    @Override
    public boolean resetPassword(String password, String email) {
        password = encoder.encode(password);//加密
        return mapper.resetPasswordByEmail(password,email) > 0;//数据库修改密码
    }


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

}






























