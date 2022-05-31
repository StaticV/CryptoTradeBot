package CryptoTrader;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import CryptoExchange.Exchange;
import CryptoExchange.ExchangeException;
import CryptoExchange.Order;

public class OrderTracker extends Tracker {
	
	public String currency, coin;
	public Exchange ex;
	public BigDecimal lastPrice;
	protected PriceWatcher pw;
	
	
	public OrderTracker(Exchange ex,String coin,String currency,BigDecimal lastPrice,int interval,boolean start,PriceWatcher pw) {
		this.coin = coin;
		this.currency = currency;
		this.lastPrice = lastPrice;
		this.ex = ex;
		this.interval = interval;
		this.pw = pw;
		
		if (start) start();
	}
	
	public boolean checkPrice() throws InterruptedException {
		try {
			Order[] orders = ex.getOrders(coin,currency);
			if (lastPrice.compareTo(orders[0].price) == 1) lastPrice = pw.coinDown(lastPrice,orders[0].price,orders[0].qty);
			else if (lastPrice.compareTo(orders[1].price) == -1) lastPrice = pw.coinUp(lastPrice,orders[1].price,orders[1].qty);
		} catch(IOException e) {
			System.err.println(new Date() + " " + e);
		} catch(ExchangeException e) {
			System.err.println(new Date() + " " + e);
		} catch(Exception e) {
			System.err.print(new Date() + " ");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}