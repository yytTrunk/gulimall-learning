package com.codeyyt.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@EnableRabbit
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter(){
        // 配置rabbite使用json进行接收转换
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue stockDelayQueue(){
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release");
        arguments.put("x-message-ttl",120000);
        return new Queue("stock.delay.queue",true,false,false,arguments);
    }

    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue("stock.release.stock.queue",true,false,false);
    }

    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange",true, false);
    }

    @Bean
    public Binding stockLockBinding(){
        Map<String,Object> arguments = new HashMap<>();
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange","stock.locked",arguments);
    }

    @Bean
    public Binding stockReleaseBinding(){
        Map<String,Object> arguments = new HashMap<>();
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",arguments);
    }

    //创建初始化RabbitAdmin对象
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        // 只有设置为 true，spring 才会加载 RabbitAdmin 这个类
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    //创建交换机和对列
    @Bean
    public void createExchangeQueue (){
        rabbitAdmin.declareExchange(stockEventExchange());
        rabbitAdmin.declareBinding(stockLockBinding());
        rabbitAdmin.declareBinding(stockReleaseBinding());
        rabbitAdmin.declareQueue(stockReleaseStockQueue());
        rabbitAdmin.declareQueue(stockDelayQueue());
    }

    @PostConstruct
    public void initRabbitTemplate(){
        rabbitTemplate.setConfirmCallback(
                new RabbitTemplate.ConfirmCallback() {
                    @Override
                    public void confirm(CorrelationData correlationData, boolean b, String s) {

                    }
                }
        );
    }
}
