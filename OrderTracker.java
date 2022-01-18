package CryptoTrader;

import java.io.IOException;
import java.math.BigDecimal;

import CryptoExchange.Exchange;
import CryptoExchange.ExchangeException;
import CryptoExchange.Order;

public class OrderTracker extends Tracker {
	
	public String currency, coin;
	public Exchange ex;
	public BigDecimal lastPrice;
	protected PriceWatcher pw;
	
	public OrderTracker(Exchange ex, String coin, String currency,BigDecimal startPrice, int interval, boolean start, PriceWatcher pw) {
		this.coin = coin;
		this.currency = currency;
		lastPrice = startPrice;
		this.ex = ex;
		this.interval = interval;
		this.pw = pw;
		
		if (start) start();
	}
	
	public boolean checkPrice() throws InterruptedException {
		try {
			Order[] orders = ex.getOrders(coin, currency);
			if (lastPrice.compareTo(orders[0].price) == 1) lastPrice = pw.coinDown(lastPrice,orders[0]);
			else if (lastPrice.compareTo(orders[1].price) == -1) lastPrice = pw.coinUp(lastPrice,orders[1]);
		} catch(IOException e) {
			System.out.println(e);
		} catch(ExchangeException e) {
			System.out.println(e);
		}
		return true;
	}
}