package org.mengyun.tcctransaction.sample.dubbo.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.entity.Order;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.entity.Shop;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.repository.ShopRepository;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.service.OrderService;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by changming.xie on 4/1/16.
 */
@Service
public class PlaceOrderServiceImpl implements PlaceOrderService{

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    public void placeOrder(long payerUserId, long shopId, List<Pair<Long, Integer>> productQuantities,BigDecimal redPacketPayAmount) {
        Shop shop = shopRepository.findById(shopId);
        Order order = orderService.createOrder(payerUserId,shop.getOwnerUserId(),productQuantities);
        paymentService.makePayment(order,redPacketPayAmount,order.getTotalAmount().subtract(redPacketPayAmount));
    }
}
