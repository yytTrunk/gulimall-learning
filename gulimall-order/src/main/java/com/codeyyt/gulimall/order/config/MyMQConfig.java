package com.codeyyt.gulimall.order.config;

import com.codeyyt.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {

    @Autowired
    RabbitAdmin rabbitAdmin;

//    @RabbitListener(queues = "order.release.order.queue")
//    public void listener(OrderEntity orderEntity){
//        System.out.println("收到订单实体，准备关闭订单 " + orderEntity.getOrderSn());
//    }

    @Bean
    public MessageConverter messageConverter(){
        // 配置rabbite使用json进行接收转换
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue orderDelayQueue(){
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        return new Queue("order.delay.queue",true,false,false,arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        return new Queue("order.release.order.queue",true,false,false);
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
        Map<String,Object> arguments = new HashMap<>();
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.create.order",arguments);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        Map<String,Object> arguments = new HashMap<>();
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.order",arguments);
    }

    // 绑定订单释放和库存释放
    @Bean
    public Binding orderReleaseOtherBinding(){
        Map<String,Object> arguments = new HashMap<>();
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,"order-event-exchange",
                "order.release.other.#",arguments);
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
        rabbitAdmin.declareExchange(orderEventExchange());
        rabbitAdmin.declareBinding(orderCreateOrderBinding());
        rabbitAdmin.declareBinding(orderReleaseOrderBinding());
        rabbitAdmin.declareBinding(orderReleaseOtherBinding());
        rabbitAdmin.declareQueue(orderDelayQueue());
        rabbitAdmin.declareQueue(orderReleaseOrderQueue());
    }

}
