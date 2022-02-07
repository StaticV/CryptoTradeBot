package CryptoExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

public interface Exchange {
	
	public abstract Date getTime() throws IOException, ExchangeException, InterruptedException;
	
	public abstract String getPrices(String coin, String currency) throws IOException, ExchangeException, InterruptedException;
	
	public abstract BigDecimal[] getHighLowPrice(String coin, String currency) throws IOException, ExchangeException, InterruptedException;
	
	public abstract String getAccountBalance() throws IOException, ExchangeException, InterruptedException;
	
	public abstract BigDecimal getCurrencyBalance(String curr) throws IOException, ExchangeException, InterruptedException;
	
	public abstract BigDecimal getCoinBalance(String coin) throws IOException, ExchangeException, InterruptedException;
	
	public abstract String orderMarketPrice(Boolean buy,String coin, String currency, String qty, Boolean validate) throws IOException, ExchangeException, InterruptedException;
	
	public abstract String getTradeableAssetPair(String coin, String currency) throws IOException, ExchangeException, InterruptedException;
	
	public abstract BigDecimal getMinOrder(String coin, String currency) throws IOException, ExchangeException, InterruptedException;
	
	public abstract Order[] getOrders(String coin, String currency) throws IOException, ExchangeException, InterruptedException;
	
	public abstract Ledger getLedger(String type,String start,int offset) throws IOException, ExchangeException, InterruptedException;
}