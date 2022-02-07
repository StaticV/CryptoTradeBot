package CryptoTrader;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import CryptoExchange.ExchangeException;

public abstract class PriceWatcher {
	
	public static final int scale = 8;
	public static final RoundingMode rm = RoundingMode.HALF_EVEN;

	public BigDecimal coinUp(BigDecimal last,BigDecimal orderPrice,BigDecimal orderQty) throws IOException,ExchangeException,InterruptedException {
		return processOrder(last,orderPrice,orderQty,-1,false,"  UP  ");
	}
	
	public BigDecimal coinDown(BigDecimal last,BigDecimal orderPrice,BigDecimal orderQty) throws IOException,ExchangeException,InterruptedException {
		return processOrder(last,orderPrice,orderQty,1,true," DOWN ");
	}
	
	protected abstract BigDecimal processOrder(BigDecimal last,BigDecimal orderPrice,BigDecimal orderQty,int avggtlt,Boolean buy,String updown)
			throws IOException,ExchangeException,InterruptedException;
	
	protected static BigDecimal priceDif(BigDecimal one,BigDecimal two) {
		return one.max(two).divide(two.min(one),scale,rm).subtract(BigDecimal.ONE).setScale(scale,rm);
	}
}
