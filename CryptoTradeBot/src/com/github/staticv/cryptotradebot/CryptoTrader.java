package com.github.staticv.cryptotradebot;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import com.github.staticv.cryptoexchange.Exchange;
import com.github.staticv.cryptoexchange.ExchangeException;
import com.github.staticv.cryptoexchange.KrakenExchange;
import com.github.staticv.cryptoexchange.Ledger;
import com.github.staticv.cryptoexchange.LedgerItem;

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
				
				System.gc();
				
				String coin = ct.l.items.get(0).pair.asset;
				LedgerItem last = ct.l.lastTrade(coin);
				System.out.println(new Date()+" Last Trade: "+last.time+" $"+last.price()+" Average $"+ct.l.avgPrice(coin)+" Profit: $"+profit(ct.l,coin));
			} catch(Exception e) {
				System.err.print(new Date() + " ");
				e.printStackTrace();
			}
		}
	}
	
	public CryptoTrader(Properties p) throws IOException,ExchangeException,InterruptedException,ClassNotFoundException,ParseException {
		if (p.getProperty("exchange").equalsIgnoreCase("kraken"))
			ex = new KrakenExchange(p.getProperty("apikey"),p.getProperty("privkey"));
		
		String currency = p.getProperty("currency");
		
		File f = new File(p.getProperty("ledger"),"ledgers.ser");
		if (f.exists()) {
			l = Ledger.readFromFile(f);
			l.updateLedger(ex);
			l.buildTrades(currency);
			l.f = f;
			l.writeToFile();
		} else {
			File csv = new File(p.getProperty("export"),"ledgers.csv");
			if (csv.exists()) {
				l = new Ledger(csv,ex);
				l.updateLedger(ex);
				l.buildTrades(currency);
				l.f = f;
				l.writeToFile();
			} else {
				l = new Ledger(ex,"trade",p.getProperty("first"),Integer.parseInt(p.getProperty("pages","10")),f);
				l.buildTrades(currency);
				l.writeToFile();
			}
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
									coin,
									currency
							)
					)
			);
	}
	
	public static BigDecimal profit(Ledger l, String coin) {
        BigDecimal totalQty = l.items.stream()
                .map(item -> item.pair.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = l.items.stream()
                .map(item -> item.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAmount.multiply(totalQty).negate();
    }
}
