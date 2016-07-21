package org.mengyun.tcctransaction.sample.dubbo.order.domain.service;

import java.math.BigDecimal;

import org.mengyun.tcctransaction.sample.dubbo.order.domain.entity.Order;

public interface PaymentService {

	public void makePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount);
	
	public void confirmMakePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount);
	
	public void cancelMakePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount);

}
