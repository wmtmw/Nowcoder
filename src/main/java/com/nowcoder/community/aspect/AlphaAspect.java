package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
    //定义切点
    //第一个*表示所有返回值，然后是包名（service包下的），所有类(*)的所有方法(*)的所有参数(..)
    @Pointcut("execution(* com.nowcoder.community.service..*.*(..))")
    public void pointcut(){

    }
    //在前面记日志
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }
    //在后面记日志
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }
    //在有返回值之后记日志
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }
    //在抛异常的时候织入代码
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }
    //前后都织入逻辑
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        //调用目标主键的方法
        System.out.println("around before");
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }

}
