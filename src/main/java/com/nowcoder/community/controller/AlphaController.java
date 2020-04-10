package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return"Hello Spring Boot.";
    }
    @RequestMapping("/data")
    @ResponseBody
    public String sayData(){
        return alphaService.find();
    }
    @RequestMapping("http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name+" : "+value);
        }
        System.out.println(request.getParameter("code"));
        //返回数据
        response.setContentType("text/html;charset=utf-8");
        try (
                PrintWriter writer = response.getWriter()
        ){

            writer.write("<h1>牛客网</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //获取请求参数的方式
    //Get请求处理方式
    ///students?current=1&limit=10
    @RequestMapping(path="/students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@RequestParam(name = "current",required = false,defaultValue = "1") int current,
                              @RequestParam(name = "limit",required = false,defaultValue = "10")int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }
    ///student/123
    @RequestMapping(path="/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }
    //post请求处理方式
    @RequestMapping(path="/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return "Success";
    }
    //响应html数据
    @RequestMapping(path="/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","Sandy");
        mav.addObject("age",25);
        mav.setViewName("/demo/view");
        return mav;
    }
    @RequestMapping(path="/school",method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","清华大学");
        model.addAttribute("age",100);

        return "/demo/view";
    }
    //响应json数据（异步请求）
    //JAVA对象->JSON字符串 ->JS对象
    @RequestMapping(path="/emp",method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getEmp(){
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","张三");
        emp.put("age",24);
        emp.put("salary",8000.00);
        return emp;
    }
    @RequestMapping(path="/emps",method = RequestMethod.GET)
    @ResponseBody
    //会自动转换为json格式发送给浏览器
    public List<Map<String,Object>> getEmps(){
        List< Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","张三");
        emp.put("age",24);
        emp.put("salary",8000.00);
        list.add(emp);
        emp.put("name","李四");
        emp.put("age",25);
        emp.put("salary",9000.00);
        list.add(emp);
        emp.put("name","王五");
        emp.put("age",26);
        emp.put("salary",10000.00);
        list.add(emp);
        return list;
    }
    //cookie示例
    @RequestMapping(path="/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //返回值是什么不重要，因为最终都是通过response去做响应，cookie是要存到response的头中
        //创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie生效的范围,即指定访问哪些路径时需要发送cookie
        cookie.setPath("/community/alpha");
        //设置cookie生存时间（默认是存到内存中，关闭浏览器就消失了，但是设置其生效时间,cookie会存到硬盘里，直到超过生效时间才会失效）
        cookie.setMaxAge(60*10);//十分钟
        //发送cookie
        response.addCookie(cookie);
        return "set cookie";
    }
    @RequestMapping(path="/cookie/get",method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        System.out.println(code);
        return "get Cookie" ;
    }
    //session示例
    @RequestMapping(path="/session/set",method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","Test");
        return "set session";
    }
    @RequestMapping(path="/session/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println( session.getAttribute("name"));
        return "get session";
    }
}
