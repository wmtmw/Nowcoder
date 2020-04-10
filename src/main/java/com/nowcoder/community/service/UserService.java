package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate redisTemplate;
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    public User findUserByUsername(String name){
        return userMapper.selectByName(name);
    }
    public User findUserById(int id){
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null){
            user = initCache(id);
        }
        return user;
    }
    public User findUserByEmail(String email){
        return userMapper.selectByEmail(email);
    }
    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    //2.取不到时初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    //3.数据变更时清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }


    public int updateHeader(int id,String headerUrl){
        int rows = userMapper.updateHeader(id,headerUrl);
        clearCache(id);
        return rows;
    }
    public int updatePassword(int id,String password){

        int rows = userMapper.updatePassword(id,password);
        clearCache(id);
        return rows;
    }
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u!=null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }
        //验证密码
        if(user.getPassword().length()<8){
            map.put("passwordMsg","密码不得小于8位");
            return map;
        }
        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u!=null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //给用户发激活邮件
        //通过context对象携带变量
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //激活路径：http://localhost:8080/community/activation/101/code
        String url = domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus()==1){
            return ACTIVATE_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATE_SUCCESS;
        }else{
            return ACTIVATE_FAILED;
        }
    }
    public Map<String,Object> login (String userName,String password,long expiredSecond){
        Map<String,Object> map = new HashMap<>();
        //空值判断
        if (StringUtils.isBlank(userName)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        //验证账号
        User user = userMapper.selectByName(userName);
        if (user==null){
            map.put("usernameMsg","账号不存在");
            return map;
        }
        if (user.getStatus()==0){
            map.put("usernameMsg","账号未激活");
            return map;
        }
        //验证密码
        if (!user.getPassword().equals(CommunityUtil.md5(password+user.getSalt()))){
            map.put("passwordMsg","密码错误");
            return map;
        }
        //生成凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSecond*1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }
    public void logout(String ticket){

//        loginTicketMapper.updateLoginTicket(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }
    public void modify(String email,String code){
        //给用户发验证邮件
        //通过context对象携带变量
        User user = userMapper.selectByEmail(email);
        Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("correctCode", CommunityUtil.generateUUID().substring(0, 6));
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(user.getEmail(), "忘记密码", content);
    }
//    //发送验证码之前的判断
//    public Map<String,Object> yanzheng (String email) {
//        Map<String, Object> map = new HashMap<>();
//        User user = userMapper.selectByEmail(email);
//        if (user == null) {
//            map.put("emailMsg", "该邮箱未注册");
//            return map;
//        }
//        return map;
//    }
//    //重设密码
//    public Map<String,Object> reset (User user,String password) {
//        Map<String, Object> map = new HashMap<>();
//        // 重置密码
//        password = CommunityUtil.md5(password + user.getSalt());
//        userMapper.updatePassword(user.getId(), password);
//        map.put("user", user);
//        return map;
//    }
// 重置密码
public Map<String, Object> resetPassword(String email, String password) {
    Map<String, Object> map = new HashMap<>();


    if (StringUtils.isBlank(password)) {
        map.put("passwordMsg", "密码不能为空!");
        return map;
    }

    User user = userMapper.selectByEmail(email);
    // 重置密码
    password = CommunityUtil.md5(password + user.getSalt());
    userMapper.updatePassword(user.getId(), password);

    map.put("user", user);
    return map;
}
//获取ticket
    public LoginTicket  findLoginTicket(String ticket){
//        return  loginTicketMapper.selectLoginTicket(ticket);
    String redisKey = RedisKeyUtil.getTicketKey(ticket);
    LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    return loginTicket;
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }

            }
        });
        return list;
    }
}
