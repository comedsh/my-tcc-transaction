package org.mengyun.tcctransaction.sample.dubbo.capital.service;

import java.math.BigDecimal;

import org.mengyun.tcctransaction.Compensable;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.sample.dubbo.capital.api.CapitalTradeOrderService;
import org.mengyun.tcctransaction.sample.dubbo.capital.api.dto.CapitalTradeOrderDto;
import org.mengyun.tcctransaction.sample.dubbo.capital.domain.entity.CapitalAccount;
import org.mengyun.tcctransaction.sample.dubbo.capital.infrastructure.dao.CapitalAccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by changming.xie on 4/2/16.
 */
@Service("capitalTradeOrderService")
public class CapitalTradeOrderServiceImpl implements CapitalTradeOrderService {
    
    @Autowired
    CapitalAccountDao capitalAccountDao;    

    /**
     * #1 Trying step
     *    “未达款” - 将要被扣除的款项，先暂扣 -> 临时锁定
     */
    @Compensable(confirmMethod = "confirmRecord", cancelMethod = "cancelRecord")
    public void record(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
        System.out.println("capital try record called");

        // presetCapitalAccount( tradeOrderDto.getSelfUserId() );
        
        CapitalAccount transferFromAccount = capitalAccountDao.findByUserId(tradeOrderDto.getSelfUserId());
        
        transferFromAccount.transferFrom(tradeOrderDto.getAmount());

        capitalAccountDao.update(transferFromAccount);
    }
    
    /*
     * fixes the null pointer errors, need to re-set some account for the current user, or else errors thrown up
     */
    @SuppressWarnings("unused")
	private void presetCapitalAccount(long userid) {
    	
    	CapitalAccount account = capitalAccountDao.findByUserId( userid );
    	
    	if( account == null ){
    		
    		capitalAccountDao.insert(BigDecimal.valueOf(1000L), userid);
    		
    	}
		
	}
    
    /**
     * #2.1 CONFIRM
     * 		确认事务完成，将 "未达款" 最终转移给另外一个用户，使其成为“已达款”
     */
	public void confirmRecord(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
        System.out.println("capital confirm record called");

        CapitalAccount transferToAccount = capitalAccountDao.findByUserId(tradeOrderDto.getOppositeUserId());

        transferToAccount.transferTo(tradeOrderDto.getAmount());

        capitalAccountDao.update(transferToAccount);
    }
	
	/**
	 * #2.2 CANCEL
	 * 	   TCC 事务失败，将“未达款”返还给用户。
	 */
    public void cancelRecord(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
        System.out.println("capital cancel record called");

        CapitalAccount capitalAccount = capitalAccountDao.findByUserId(tradeOrderDto.getSelfUserId());

        capitalAccount.cancelTransfer(tradeOrderDto.getAmount());

        capitalAccountDao.update(capitalAccount);
    }
}
