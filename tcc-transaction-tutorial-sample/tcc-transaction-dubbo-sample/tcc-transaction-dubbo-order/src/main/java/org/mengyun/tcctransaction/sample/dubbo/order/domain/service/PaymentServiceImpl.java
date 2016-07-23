package org.mengyun.tcctransaction.sample.dubbo.order.domain.service;

import java.math.BigDecimal;

import org.mengyun.tcctransaction.Compensable;
import org.mengyun.tcctransaction.sample.dubbo.capital.api.CapitalTradeOrderService;
import org.mengyun.tcctransaction.sample.dubbo.capital.api.dto.CapitalTradeOrderDto;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.entity.Order;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.repository.OrderRepository;
import org.mengyun.tcctransaction.sample.dubbo.redpacket.api.RedPacketTradeOrderService;
import org.mengyun.tcctransaction.sample.dubbo.redpacket.api.dto.RedPacketTradeOrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by changming.xie on 4/1/16.
 */
@Service("paymentService")
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    CapitalTradeOrderService capitalTradeOrderService;

    @Autowired
    RedPacketTradeOrderService redPacketTradeOrderService;

    @Autowired
    OrderRepository orderRepository;

    @Compensable(confirmMethod = "confirmMakePayment",cancelMethod = "cancelMakePayment")
    public void makePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {
    	
        System.out.println("order try make payment called");

        order.pay(redPacketPayAmount, capitalPayAmount);
        // TODO 思考，若异常发生在这里，而且业务没这么简单, ORDER 会操作多个表执行多个业务操作，执行到一半，挂掉.. 
        // 然后进行实物补偿，是不是会被补多？我想，这里的答案就是肯定的罗。
        // 看来 TRY | CONFIRM / CANCEL 每一步，本地的单事务环境是必须的，这个得要好生考虑。
        orderRepository.updateOrder(order);
        
        // Scenario III, 在未执行任何远程调用之前抛出异常，看事务补偿是否会增加 Captial 和 Red Packet 的资金
        // 制造一个 RuntimeException，为什么是 除0 呢？因为保证后续代码可执行，不影响编译 
        // @SuppressWarnings("unused")
		// int i = 1 / 0;
        
        capitalTradeOrderService.record(null, buildCapitalTradeOrderDto(order));
        
        redPacketTradeOrderService.record(null, buildRedPacketTradeOrderDto(order));
        
        // Scenario II: 在主进程调用处抛出异常
        // throw new RuntimeException("Manull Runtime Exception - main Process - finally");
    }

    public void confirmMakePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {

        System.out.println("order confirm make payment called");
        order.confirm();

        orderRepository.updateOrder(order);
    }

    public void cancelMakePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {

        System.out.println("order cancel make payment called");

        order.cancelPayment();

        orderRepository.updateOrder(order);
    }


    private CapitalTradeOrderDto buildCapitalTradeOrderDto(Order order) {

        CapitalTradeOrderDto tradeOrderDto = new CapitalTradeOrderDto();
        tradeOrderDto.setAmount(order.getCapitalPayAmount());
        tradeOrderDto.setMerchantOrderNo(order.getMerchantOrderNo());
        tradeOrderDto.setSelfUserId(order.getPayerUserId());
        tradeOrderDto.setOppositeUserId(order.getPayeeUserId());
        tradeOrderDto.setOrderTitle(String.format("order no:%s", order.getMerchantOrderNo()));

        return tradeOrderDto;
    }

    private RedPacketTradeOrderDto buildRedPacketTradeOrderDto(Order order) {
        RedPacketTradeOrderDto tradeOrderDto = new RedPacketTradeOrderDto();
        tradeOrderDto.setAmount(order.getRedPacketPayAmount());
        tradeOrderDto.setMerchantOrderNo(order.getMerchantOrderNo());
        tradeOrderDto.setSelfUserId(order.getPayerUserId());
        tradeOrderDto.setOppositeUserId(order.getPayeeUserId());
        tradeOrderDto.setOrderTitle(String.format("order no:%s", order.getMerchantOrderNo()));

        return tradeOrderDto;
    }
}
