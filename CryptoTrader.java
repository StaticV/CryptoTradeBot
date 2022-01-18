package CryptoTrader;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import CryptoExchange.Exchange;
import CryptoExchange.ExchangeException;
import CryptoExchange.KrakenExchange;
import CryptoExchange.Ledger;


public class CryptoTrader {
	
	protected ArrayList<Tracker> trackers;
	protected Exchange ex;
	protected Ledger l;


	public static void main(String[] args) {
		for (String arg : args) {
			try {
				Properties p = new Properties();
				p.load(new FileReader(arg));
				CryptoTrader ct = new CryptoTrader(p);
				
				System.out.println(new Date()+" Last Trade: "+ct.l.last);
				System.gc();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public CryptoTrader(Properties p) throws IOException, ExchangeException, InterruptedException  {
		if (p.getProperty("exchange").equalsIgnoreCase("kraken"))
			ex = new KrakenExchange(p.getProperty("apikey"),p.getProperty("privkey"));
		
		l = new Ledger(ex,"trade",p.getProperty("first"),Integer.parseInt(p.getProperty("pages","10")));
		l.buildTrades(p.getProperty("currency"));
		
		trackers = new ArrayList<Tracker>();
		for (String coin : p.getProperty("coins").split(" ")) {
			PriceWatcher pw;
			pw = new LastPriceWatcher(
					l,
					ex,
					Boolean.valueOf(p.getProperty("test","true")),
					new BigDecimal(p.getProperty("maxratio","0.5")).min(BigDecimal.ONE),
					new BigDecimal(p.getProperty("minratio","0.0026")), 
					new BigDecimal(p.getProperty("traderatio","2")),
					ex.getMinOrder(coin, p.getProperty("currency"))
				);
			
			trackers.add(new OrderTracker(ex,coin,p.getProperty("currency"),l.avgPrice(coin),Integer.parseInt(p.getProperty("interval","60")) * 1000,true,pw));
		}
	}
}