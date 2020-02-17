package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes  = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testMail(){
        mailClient.sendMail("mingwei_tang@bupt.edu.cn","test","加油");
    }

    @Test
    public void testTemplate(){
        Context context = new Context();
        context.setVariable("username","Sandy");
        //调用模板生成动态网页
        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);
        mailClient.sendMail("mingwei_tang@bupt.edu.cn","template",content);
    }
}
