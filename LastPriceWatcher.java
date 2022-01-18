package CryptoTrader;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import CryptoExchange.Exchange;
import CryptoExchange.ExchangeException;
import CryptoExchange.Ledger;
import CryptoExchange.Order;

public class LastPriceWatcher implements PriceWatcher {

	public static final int scale = 8;
	public static final RoundingMode rm = RoundingMode.HALF_EVEN;
	
	protected Ledger l;
	protected Exchange ex;
	protected Boolean test;
	protected BigDecimal maxratio,traderatio,minratio,minOrder;
	
	public LastPriceWatcher(Ledger l,Exchange ex,Boolean test,BigDecimal maxratio,BigDecimal minratio, BigDecimal traderatio,BigDecimal minOrder) {
		this.l = l;
		this.ex = ex;
		this.test = test;
		this.maxratio = maxratio;
		this.minratio = minratio;
		this.traderatio = traderatio;
		this.minOrder = minOrder;
	}
	
	public BigDecimal coinUp(BigDecimal last,Order order) throws IOException, ExchangeException, InterruptedException {
		return processOrder(last,order,-1,"sell","  UP  ");
	}
	
	public BigDecimal coinDown(BigDecimal last,Order order) throws IOException, ExchangeException, InterruptedException {
		return processOrder(last,order,1,"buy"," DOWN ");
	}
	
	protected BigDecimal processOrder(BigDecimal last,Order order,int avggtlt,String type,String updown) throws IOException, ExchangeException, InterruptedException {
		BigDecimal avg = l.avgPrice(order.coin);
		
		if (order.price.compareTo(avg) == avggtlt || priceDif(avg,order.price).compareTo(minratio) == -1) return last;
		
		BigDecimal qty,dif = priceDif(last,order.price);
		
		if (avggtlt == -1)
			qty = ex.getCoinBalance(order.coin).multiply(dif.multiply(traderatio).min(maxratio));
		else
			qty = ex.getCurrencyBalance(order.currency).multiply(dif.multiply(traderatio).min(maxratio)).divide(order.price,scale,rm);
		
		if (qty.compareTo(minOrder) == -1) return last;
		
		System.out.print(new Date() + updown + dif.floatValue()*100 + "% $" + order.price);
		System.out.print(" " + ex.orderMarketPrice(type,order.coin,order.currency,qty.min(order.qty).toPlainString(),test.toString()));
		
		l.updateLedger(ex);
		l.buildTrades(order.currency);
		
		System.out.println(" New Average Price: "+l.avgPrice(order.coin));
		
		return order.price;
	}
	
	protected static BigDecimal priceDif(BigDecimal one, BigDecimal two) {
		return one.max(two).divide(two.min(one),scale,rm).subtract(BigDecimal.ONE).setScale(scale,rm);
	}
}