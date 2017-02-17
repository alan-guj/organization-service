package top.jyx365.amqp;


import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ConditionalOnClass({ RabbitTemplate.class })
@AutoConfigureAfter(RabbitAutoConfiguration.class)
@EnableConfigurationProperties(AmqpProperties.class)
//@Import(AmqpWebAdapter.class)
public class AmqpConfig {

    @Autowired
    AmqpProperties config;

    @Bean
    public RabbitTemplate template(ConnectionFactory connectionFactory, ObjectMapper mapper) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setJsonObjectMapper(mapper);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public TopicExchange topic() {
        return new TopicExchange(config.getTopic());
    }

    @Bean
    public AmqpSender amqpSender() {
        return new AmqpSender();
    }

    @Bean
    public AmqpPublisherInterceptor interceptor() {
        return new AmqpPublisherInterceptor();
    }


}

@Configuration
class AmqpWebAdapter extends WebMvcConfigurerAdapter {

    @Autowired
    private AmqpSender sender;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AmqpWebInterceptor(sender)).addPathPatterns("/api/**");
        super.addInterceptors(registry);
    }

}

@ConfigurationProperties(prefix = "amqp")
class AmqpProperties {
    private String topic;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}

@Slf4j
class AmqpWebInterceptor implements HandlerInterceptor {

    private AmqpSender sender;

    public AmqpWebInterceptor(AmqpSender sender){
        this.sender = sender;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        log.debug(">>>MyInterceptor1>>>>>>>在请求处理之前进行调用（Controller方法调用之前）");
        return true;// 只有返回true才会继续向下执行，返回false取消当前请求
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        log.debug(">>>MyInterceptor1>>>>>>>请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）");
        if(response.getStatus()==200 || response.getStatus()==201) {
            String key = request.getRequestURI();
            log.debug("path:{}",request.getRequestURI());
            log.debug("key:{}",key);
            sender.send("restful:"+key,"test_message");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception
    {
        log.debug(">>>MyInterceptor1>>>>>>>在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）");
    }
}


