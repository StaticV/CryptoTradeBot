package CryptoExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Ledger implements Serializable {
	
	public static final int ipg = 50;
	public static final long serialVersionUID = 1;

	public ArrayList<LedgerItem> items;
	protected File f;
	public Date last;
	
	
	protected Ledger(int capacity) {
		items = new ArrayList<LedgerItem>(capacity);
		last = new Date(0);
	}
	
	protected Ledger(Iterator<Object> it) {
		this(ipg);
		
		while (it.hasNext()) {
			LedgerItem l = new LedgerItem(it.next());
			items.add(l);
			if (l.time.after(last)) last = l.time;
		}
	}
	
	public Ledger(String json) {
		this(Json.getJSONArray(json));
	}
	
	public Ledger(Exchange ex, String type, String first,int pages,File f) throws IOException,ExchangeException,InterruptedException {
		this(ipg*pages);
		
		this.f = f;
		
		for (int i = 0; i < pages; ++i) {
			Ledger l = ex.getLedger(type,first,i*ipg);
			
			if (l.items.isEmpty()) break;
			else mergeLedgers(l);
		}
	}
	
	public void buildTrades(String asset) {
		for (Iterator<LedgerItem> it = items.iterator();it.hasNext();) {
			LedgerItem cur = it.next();
			
			if (cur.pair == null)
				for (LedgerItem curPair : items)
					if (cur.pair(curPair)) break;
			
			if (!cur.asset.equals(asset)) it.remove();
		}
		
		for (Iterator<LedgerItem> it = items.iterator();it.hasNext();) {
			LedgerItem cur = it.next();
			if (cur.pair == null) it.remove();
		}
		
		items.trimToSize();
	}
	
	public BigDecimal avgPrice(String coin) {
		BigDecimal result = new BigDecimal("0.00");
		BigDecimal qty = new BigDecimal("0.00");
		
		for (LedgerItem cur : items)
			if (cur.pair.asset.equals(coin)) {
				qty = qty.add(cur.pair.amount);
				result = result.add(cur.amount);
			}
		
		return result.divide(qty,RoundingMode.HALF_EVEN).abs();
	}
	
	public boolean mergeLedgers(Ledger l) {
		if (last.before(l.last)) last = l.last;
		
		return items.addAll(l.items);
	}
	
	public void updateLedger(Exchange ex) throws IOException,ExchangeException,InterruptedException {
		mergeLedgers(ex.getLedger("trade",String.valueOf(last.getTime()/1000),0));
	}
	
	public LedgerItem lastTrade(String coin) {
		LedgerItem result = new LedgerItem();
		
		for (LedgerItem cur : items)
			if (cur.pair.asset.equals(coin) && cur.time.after(result.time))
				result = cur;
		
		return result;
	}
	
	public void writeToFile() throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
		out.writeObject(this);
		out.close();
	}
	
	public static Ledger readFromFile(File f) throws ClassNotFoundException,IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
		Ledger l = (Ledger)in.readObject();
		in.close();
		l.f = f;
		return l;
	}
}