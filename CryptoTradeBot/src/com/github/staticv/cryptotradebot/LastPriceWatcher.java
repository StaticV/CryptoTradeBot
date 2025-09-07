package com.github.staticv.cryptotradebot;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import com.github.staticv.cryptoexchange.Exchange;
import com.github.staticv.cryptoexchange.ExchangeException;
import com.github.staticv.cryptoexchange.Ledger;

public class LastPriceWatcher extends PriceWatcher {
	
	protected Ledger l;
	protected Exchange ex;
	protected Boolean test;
	protected BigDecimal maxratio,traderatio,minratio,minOrder,coinBal,currBal;
	protected String coin,curr;
	
	
	public LastPriceWatcher(Ledger l,Exchange ex,Boolean test,BigDecimal maxratio,BigDecimal minratio,BigDecimal traderatio,String coin,String curr) 
			throws IOException,ExchangeException,InterruptedException {
		this.l = l;
		this.ex = ex;
		this.test = test;
		this.maxratio = maxratio;
		this.minratio = minratio;
		this.traderatio = traderatio;
		minOrder = ex.getMinOrder(coin,curr);
		this.coin = coin;
		this.curr = curr;
		updateBalance();
	}
	
	public void updateBalance() throws IOException,ExchangeException,InterruptedException {
		coinBal = ex.getCoinBalance(coin);
		currBal = ex.getCurrencyBalance(curr);
	}
	
	protected BigDecimal processOrder(BigDecimal last,BigDecimal orderPrice,BigDecimal orderQty,int avggtlt,Boolean buy,String updown) 
			throws IOException,ExchangeException,InterruptedException {
		BigDecimal avg = l.avgPrice(coin);
		
		if (orderPrice.compareTo(avg) == avggtlt || priceDif(avg,orderPrice).compareTo(minratio) == -1) return last;
		
		BigDecimal qty,dif;
		
		if (avggtlt == -1) { // UP
			dif = priceDif(last.max(avg),orderPrice);
			qty = coinBal.multiply(dif.multiply(traderatio).min(maxratio)).min(orderQty);
		}
		else if (avggtlt == 1) { // DOWN
			dif = priceDif(last.min(avg),orderPrice);
			qty = currBal.multiply(dif.multiply(traderatio).min(maxratio)).divide(orderPrice,scale,rm).min(orderQty);
		} 
		else return last;
			
		if (qty.compareTo(minOrder) == -1) return last;
		
		String order = ex.orderMarketPrice(buy,coin,curr,qty.toPlainString(),test);
		System.out.print(new Date() + updown + dif.floatValue()*100 + "% $" + orderPrice+" "+order);
		
		l.updateLedger(ex);
		l.buildTrades(curr);
		l.writeToFile();
		
		System.out.println(" New Average Price: "+l.avgPrice(coin));
		
		updateBalance();
		
		return orderPrice;
	}
}