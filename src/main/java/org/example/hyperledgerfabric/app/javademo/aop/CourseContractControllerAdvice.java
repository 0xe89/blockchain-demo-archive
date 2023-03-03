package org.example.hyperledgerfabric.app.javademo.aop;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Aspect //告诉spring这是一个切面类
@Component
public class CourseContractControllerAdvice {

    private Logger logger = LoggerFactory.getLogger(CourseContractControllerAdvice.class);

    //定义切点（连接点的集合）。告诉spring通知要织入到哪里
    //public： 方法的修饰符，可以不写，但不能为 *
    // *： 方法的返回值类型是什么都可以
    // org.example.hyperledgerfabric.app.javademo：包名
    // CourseContractController：类名
    // *：这个类下的哪些方法
    // (..)：参数是否做限制,..表示不做限制，任何参数都可以
    @Pointcut(value = "execution(public * org.example.hyperledgerfabric.app.javademo.CourseContractController.*(..))")
    public void myPointCut(){}

    //定义通知(advice)。告诉spring通知要织入哪些内容
    @Around(value = "myPointCut()")
    public Object myAdvice(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().toString();
        String methodName = pjp.getSignature().getName();
        Object[] argsArray = pjp.getArgs();
        ObjectMapper objectMapper = new ObjectMapper();

        logger.info("【调用前】类名："+className+"方法名："+methodName+"() 参数："+objectMapper.writeValueAsString(argsArray));
        Object obj = pjp.proceed();
        logger.info("【调用后】类名："+className+"方法名："+methodName+"() 返回值："+objectMapper.writeValueAsString(obj));
        return obj;
    }
}
