package com.codeyyt.gulimall.order.web;

import com.codeyyt.gulimall.common.exception.NoStockException;
import com.codeyyt.gulimall.order.service.OrderService;
import com.codeyyt.gulimall.order.vo.OrderConfirmVo;
import com.codeyyt.gulimall.order.vo.OrderSubmitVo;
import com.codeyyt.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        try {
            System.out.println("!!!!!!!!!!s");
            //下单
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            if(responseVo.getCode()==0){
                //成功
                model.addAttribute("submitOrderResp",responseVo);
                return "pay";
            }else{
                String msg = "下单失败:";
                //失败
                switch (responseVo.getCode()){
                    case 1: msg += "订单信息过期，请刷新提交"; break;
                    case 2: msg += "订单商品价格发生变动,请确认后再次提交";break;
                    case 3: msg += "库存锁定失败，商品库存不足";break;

                }
                redirectAttributes.addFlashAttribute("msg",msg);
                return "redirect:localhost:88/api/order/toTrade";
            }
        }catch (Exception e){
            if(e instanceof NoStockException){
                String msg = ((NoStockException) e).getMessage();
                redirectAttributes.addFlashAttribute("msg",msg);

            }
            e.printStackTrace();
            return "redirect:localhost:88/api/order/toTrade";
        }
    }
}
