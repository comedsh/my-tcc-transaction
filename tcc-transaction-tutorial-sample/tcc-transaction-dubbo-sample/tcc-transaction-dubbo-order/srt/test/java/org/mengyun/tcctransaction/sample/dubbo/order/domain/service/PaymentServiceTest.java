package org.mengyun.tcctransaction.sample.dubbo.order.domain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.entity.Order;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.entity.Shop;
import org.mengyun.tcctransaction.sample.dubbo.order.domain.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:config/spring/local/appcontext-*.xml", "classpath:tcc-transaction.xml"})
public class PaymentServiceTest {

	@Autowired
	PaymentService paymentService;
	
    @Autowired
    ShopRepository shopRepository;

    @Autowired
    OrderServiceImpl orderService;
	
	Order order;
	
	BigDecimal redPacketPayAmount = BigDecimal.TEN;
	
	@Before
	public void before(){
		
		Shop shop = shopRepository.findById( 1 );
		
		List<Pair<Long, Integer>> productQuantities = new ArrayList<Pair<Long, Integer>>();
		
		productQuantities.add(new ImmutablePair<Long, Integer>(1L, 1) );
				
        if( order == null ) 
        	order = orderService.createOrder( 1000, shop.getOwnerUserId(), productQuantities );
        
	}
	
	@Test
	public void testStart(){
		
		/*
		 *  用户 1000 总共转 60 块，50 块钱来源于它的 CAPTIAL，另外 10 块来源于 RED PACKET。
		 *  
		 *  用户 1000 CAPITAL 账户转 50 到用户 2000 CAPITAL 账户
		 *  用户 1000 RED PACKET 账户转 10 到用户 2000 RED PACKET 账户
		 */
		paymentService.makePayment(order, redPacketPayAmount, order.getTotalAmount().subtract(redPacketPayAmount) );
		
	}
	
	
}
