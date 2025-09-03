package com.hmall.trade.listener;


import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("trade.pay.success.queue"),
            exchange = @Exchange(value = "pay.direct",type = ExchangeTypes.DIRECT),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId){
        Order order = orderService.getById(orderId);
        if (order == null || order.getStatus() !=1){
            return;
        }
        orderService.markOrderPaySuccess(orderId);
    }
}
