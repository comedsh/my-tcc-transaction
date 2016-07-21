package org.mengyun.tcctransaction.sample.dubbo.order.domain.service;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.entity.Order;

public interface OrderService {
	
	public Order createOrder(long payerUserId, long payeeUserId, List<Pair<Long, Integer>> productQuantities); 

}
