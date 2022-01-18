package CryptoTrader;

import java.io.IOException;
import java.math.BigDecimal;

import CryptoExchange.ExchangeException;
import CryptoExchange.Order;

public interface PriceWatcher {

	public abstract BigDecimal coinUp(BigDecimal last, Order order) throws IOException, ExchangeException, InterruptedException;
	
	public abstract BigDecimal coinDown(BigDecimal last, Order order) throws IOException, ExchangeException, InterruptedException;
}
