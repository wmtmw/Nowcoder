package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);
        user = userMapper.selectByName("liubei");
        System.out.println(user);
        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }
    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("Sandy");
        user.setType(0);
        user.setStatus(1);
        user.setSalt("12345");
        user.setHeaderUrl("http://images.nowcoder.com/head/100t.png");
        user.setCreateTime(new Date());
        user.setPassword("1234");
        user.setEmail("623473995@qq.com");
        int id = userMapper.insertUser(user);
        System.out.println(id);
        System.out.println(user.getId());
    }
    @Test
    public void testUpdateStatus() {
        userMapper.updateStatus(150,2);
        userMapper.updateHeader(150,"www.nowcoder.com");
        userMapper.updatePassword(150,"qq12345");
    }
    @Test
    public void testSelectDiscussPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149,0,10,0);
        for (DiscussPost discussPost:list){
            System.out.println(discussPost);
        }
        int count = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(count);
    }
    @Test
    public void testSelectDiscussPostsRows(){
        int count = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(count);
    }
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60));
        int a = loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectLoginTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateLoginTicket("abc",1);
        loginTicket = loginTicketMapper.selectLoginTicket("abc");
        System.out.println(loginTicket);
    }
}
