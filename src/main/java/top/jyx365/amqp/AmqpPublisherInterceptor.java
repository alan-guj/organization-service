package top.jyx365.amqp;


import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.bind.annotation.RequestMapping;

import top.jyx365.amqp.annotation.PublishMessage;

@Slf4j
@Aspect
public class AmqpPublisherInterceptor {

    @Autowired
    private AmqpSender sender;

    @Pointcut("@annotation(top.jyx365.amqp.annotation.PublishMessage)")
    public void publishMessageMethodPointcut() {}

    //@Around("publishMessageMethodPointcut() && @annotation(publishMessage)")
        //public Object publishMessageAround(ProceedingJoinPoint pjp, PublishMessage publishMessage) throws Throwable {
            //Object retVal = pjp.proceed();
            //log.info("Around:{}", retVal);
            //return retVal;
        //}

    @AfterReturning(
        pointcut = "publishMessageMethodPointcut() && @annotation(publishMessage)",
        returning = "retVal")
        public void publishMessage( JoinPoint pjp,Object retVal,
                PublishMessage publishMessage) {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod(); //获取被拦截的方法
            String methodName = publishMessage.method(); //获取被拦截的方法名
            if(methodName.equals("")) methodName = method.getName(); //获取被拦截的方法名
            if( retVal != null ){
                Map<String,Object> message = new HashMap<String,Object>();
                message.put("message",retVal);
                message.put("timestamp",new Date());
                message.put("method", methodName);
                sender.send(methodName+"."+publishMessage.key(), message);
                log.info("sendMessage:{}",message);
            }

            log.debug("AfterReturning:{};key:{};methodString:{};target:{};annotations:{}",
                    retVal, publishMessage.key(), pjp.toString(),pjp.getTarget(),method.getDeclaredAnnotations());
        }
}
