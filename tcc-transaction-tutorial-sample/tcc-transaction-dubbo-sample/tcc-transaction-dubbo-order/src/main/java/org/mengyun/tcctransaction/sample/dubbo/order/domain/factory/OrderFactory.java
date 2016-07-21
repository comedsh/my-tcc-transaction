package org.mengyun.tcctransaction.sample.dubbo.order.domain.factory;

import org.apache.commons.lang3.tuple.Pair;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.entity.Order;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.entity.OrderLine;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by changming.xie on 4/1/16.
 */
public class OrderFactory {

	// 作者这里又添加了 fake data，初始化了一个 OrderLine.. 什么意思？生产线？OK 从表结构上来看，是为某个商品进行定价..
    public static Order buildOrder(long payerUserId, long payeeUserId, List<Pair<Long, Integer>> productQuantities) {

        Order order = new Order(payerUserId, payeeUserId);

        for (Pair<Long, Integer> pair : productQuantities) {
            order.addOrderLine(new OrderLine(pair.getLeft(), pair.getRight(), BigDecimal.valueOf(60)));
        }

        return order;
    }
}
