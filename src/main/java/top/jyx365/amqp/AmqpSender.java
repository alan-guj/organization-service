package top.jyx365.amqp;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.TopicExchange;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
public class AmqpSender {

    @Autowired
    private RabbitTemplate template;
    @Autowired
    private TopicExchange topic;

    private String key;

    public void send(String key ,Object message) {
        template.convertAndSend(topic.getName(),key, message);
    }

}

