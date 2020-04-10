package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping(path = "/user")
public class UserController implements CommunityConstant {
    private Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${community.path.domain}")
    private String domain;
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;
    @LoginRequired
    @RequestMapping(path="/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }
    @LoginRequired
    @RequestMapping(path="upload",method = RequestMethod.POST)
    public String uploadImage(MultipartFile headImage, Model model){
        if (headImage == null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
        //查看文件大小；单位是byte
        long size = headImage.getSize();
        size = size / 1024 / 1024; //将byte转为MB
        int maxSize = 3; // 表示最大文件大小，单位MB
        //如果文件大小超过3MB发出警告
        if (size > maxSize){
            model.addAttribute("error","文件大小不能超过"+maxSize+"MB");
            return "/site/setting";
        }
        //获取文件名
        String filename = headImage.getOriginalFilename();
        //提取文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        //判断文件格式是否正确
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID()+suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath+"/"+filename);
        try {
            //存储文件，将上传文件写到服务器指定文件
            headImage.transferTo(dest);
        } catch (IOException e) {
           logger.error("文件上传失败:"+e.getMessage());
           throw new RuntimeException("文件上传失败，服务器发生异常！"+e);
        }
        //更新当前用户的头像的路径(web访问路径)
        //http：//localhost：8080/community/user/header/xxxx.png
        //获取当前用户
        User user = hostHolder.getUser();
        String headerUrl = domain+contextPath+"/user/header/"+filename;
        //更新用户头像路径
        userService.updateHeader(user.getId(),headerUrl);
        //重定向到首页
        return "redirect:/index";
    }
    //获取头像
    @RequestMapping(path = "/header/{filename}",method = RequestMethod.GET)
    public void updateHeader(@PathVariable("filename") String filename, HttpServletResponse response){
        //文件存放的路径
        filename = uploadPath+"/"+filename;
        //获取后缀名
        String suffix = filename.substring(filename.lastIndexOf("."));
        //response.setContentType(MIME)的作用是使客户端浏览器，区分不同种类的数据，并根据不同的MIME调用浏览器内不同的程序嵌入模块来处理相应的数据。
        //响应图片
        response.setContentType("image/"+suffix);
        try (   OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename);
        ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败："+e.getMessage());
        }
    }
    //修改密码
    @RequestMapping(path = "/resetPassword",method = RequestMethod.POST)
    public String resetPassword(String oldPassword,String newPassword,String confirmPassword,Model model){
        //检查原密码是否正确
        User user = hostHolder.getUser();
        String password = CommunityUtil.md5(oldPassword+user.getSalt());
        if (!password.equals(user.getPassword())){
            model.addAttribute("oldPasswordMsg","原密码错误");
            return "/site/setting";
        }
        if (newPassword.length()<8){
            model.addAttribute("newPasswordMsg","新密码不足8位");
            return "/site/setting";
        }
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("confirmMsg","两次输入密码不一致");
            return "/site/setting";
        }
        userService.updatePassword(user.getId(),CommunityUtil.md5(newPassword+user.getSalt()));

        return "redirect:/logout";

    }
    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId,Model model){
        User user = userService.findUserById(userId);
        if (user==null){
            throw new RuntimeException("该用户不存在！");
        }
        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }














}
