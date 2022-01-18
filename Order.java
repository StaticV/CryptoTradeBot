package CryptoExchange;

import java.math.BigDecimal;

public class Order {

	public String coin;
	public String currency;
	public BigDecimal price;
	public BigDecimal qty;
	
	public Order(String coin, String currency,String price, String qty) throws NumberFormatException {
		this.coin = coin;
		this.currency = currency;
		this.price = new BigDecimal(price);
		this.qty = new BigDecimal(qty);
	}
}