package com.codeyyt.gulimall.order.listener;

import com.codeyyt.gulimall.order.entity.OrderEntity;
import com.codeyyt.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues="order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    // 订单超时未付款关闭
    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        try{
            System.out.println("收到订单关闭消息，" + message.getBody().toString());
            orderService.closeOrder(entity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
