package CryptoTrader;

import java.io.File;
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
import CryptoExchange.LedgerItem;


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
				
				String coin = ct.l.items.get(0).pair.asset;
				LedgerItem last = ct.l.lastTrade(coin);
				System.out.println(new Date()+" Last Trade: "+last.time+" $"+last.price()+" Average $"+ct.l.avgPrice(coin));
				System.gc();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public CryptoTrader(Properties p) throws IOException, ExchangeException,InterruptedException,ClassNotFoundException  {
		if (p.getProperty("exchange").equalsIgnoreCase("kraken"))
			ex = new KrakenExchange(p.getProperty("apikey"),p.getProperty("privkey"));
		
		String currency = p.getProperty("currency");
		
		File f = new File(p.getProperty("ledger"),"ledger.ser");
		if (f.exists()) {
			l = Ledger.readFromFile(f);
			l.updateLedger(ex);
			l.buildTrades(currency);
		}
		else {
			l = new Ledger(ex,"trade",p.getProperty("first"),Integer.parseInt(p.getProperty("pages","10")),f);
			l.buildTrades(currency);
			l.writeToFile();
		}
		
		trackers = new ArrayList<Tracker>();
		for (String coin : p.getProperty("coins").split(" "))
			trackers.add(
					new OrderTracker(
							ex,
							coin,
							currency,
							l.lastTrade(coin).price(),
							Integer.parseInt(p.getProperty("interval","60"))*1000,
							true,
							new LastPriceWatcher(
									l,
									ex,
									Boolean.valueOf(p.getProperty("test","true")),
									new BigDecimal(p.getProperty("maxratio","0.5")).min(BigDecimal.ONE),
									new BigDecimal(p.getProperty("minratio","0.0026")),
									new BigDecimal(p.getProperty("traderatio","2")),
									ex.getMinOrder(coin,currency),
									coin,
									currency
							)
					)
			);
	}
}