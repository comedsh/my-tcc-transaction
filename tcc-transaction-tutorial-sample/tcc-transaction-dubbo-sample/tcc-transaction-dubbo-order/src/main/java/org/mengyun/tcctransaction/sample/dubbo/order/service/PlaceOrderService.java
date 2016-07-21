package org.mengyun.tcctransaction.sample.dubbo.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public interface PlaceOrderService {
	public void placeOrder(long payerUserId, long shopId, List<Pair<Long, Integer>> productQuantities,BigDecimal redPacketPayAmount);
}
