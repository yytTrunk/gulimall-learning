package com.codeyyt.gulimall.ware.listener;

import com.codeyyt.gulimall.common.to.mq.OrderTo;
import com.codeyyt.gulimall.common.to.mq.StockLockedTo;
import com.codeyyt.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;


    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        try {
            System.out.println("handleStockLockedRelease  处理release 消息 " + message.getBody());
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            // 失败将消息重新放回队列里
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

    // 订单过期，关闭订单，释放库存
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException {
        try {
            System.out.println("handleOrderCloseRelease  处理release 消息 " + message.getBody());
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
