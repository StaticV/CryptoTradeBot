package CryptoExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Ledger {
	
	public static final int ipg = 50;

	protected ArrayList<LedgerItem> items;
	public Date last;
	
	public Ledger(int capacity) {
		items = new ArrayList<LedgerItem>(capacity);
		last = new Date(0);
	}
	
	public Ledger(Iterator<Object> it) {
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
	
	public Ledger(Exchange ex, String type, String first,int pages) throws IOException, ExchangeException, InterruptedException {
		this(ipg*pages);
		
		for (int i = 0; i < pages; ++i) {
			Ledger l = ex.getLedger(type,first,i*ipg);
			
			if (l.items.isEmpty()) break;
			else mergeLedgers(l);
		}
	}
	
	public Iterator<LedgerItem> iterator() {
		return items.iterator();
	}
	
	public void buildTrades(String asset) {
		Iterator<LedgerItem> it = iterator();
		
		while(it.hasNext()) {
			LedgerItem cur = it.next();
			
			if (cur.pair == null) {
				Iterator<LedgerItem> nested = iterator();
				
				while (nested.hasNext()) {
					LedgerItem curPair = nested.next();
					
					if (cur.refid.equals(curPair.refid) && !cur.asset.equals(curPair.asset)) {
						cur.pair = curPair;
						curPair.pair = cur;
						break;
					}
				}
			}
			
			if (!cur.asset.equals(asset)) it.remove();
		}
		
		it = iterator();
		while(it.hasNext()) {
			LedgerItem cur = it.next();
			
			if (cur.pair == null) it.remove();
		}
		
		items.trimToSize();
	}
	
	public BigDecimal avgPrice(String coin) {
		BigDecimal result = new BigDecimal("0.00");
		BigDecimal qty = new BigDecimal("0.00");
		
		Iterator<LedgerItem> it = iterator();
		
		while (it.hasNext()) {
			LedgerItem cur = it.next();
			
			if (cur.pair.asset.equals(coin)) {
				qty = qty.add(cur.pair.amount);
				result = result.add(cur.amount);
			}
		}
		
		return result.divide(qty,RoundingMode.HALF_EVEN).abs();
	}
	
	public boolean mergeLedgers(Ledger l) {
		if (last.before(l.last)) last = l.last;
		
		return items.addAll(l.items);
	}
	
	public void updateLedger(Exchange ex) throws IOException, ExchangeException, InterruptedException {
		mergeLedgers(ex.getLedger("trade",String.valueOf(last.getTime()/1000),0));
	}
}