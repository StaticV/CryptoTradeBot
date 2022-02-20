package CryptoExchange;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;


public class Ledger implements Serializable {
	
	public static final int ipg = 50;
	public static final long serialVersionUID = 1;

	public ArrayList<LedgerItem> items;
	public File f;
	public Instant last;
	
	
	protected Ledger(int capacity) {
		items = new ArrayList<LedgerItem>(capacity);
		last = Instant.MIN;
	}
	
	public Ledger(Iterator<Object> it,Exchange ex) {
		this(ipg);
		
		while (it.hasNext()) {
			LedgerItem l = ex.buildLedgerItem(it.next());
			items.add(l);
			if (l.time.isAfter(last)) last = l.time;
		}
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
	
	public Ledger(File f,Exchange ex) throws IOException,ParseException {
		this(ipg);
		
		BufferedReader in = new BufferedReader(new FileReader(f));
		String line = in.readLine();
		
		while ((line = in.readLine()) != null) {
			String[] item = line.split(",");
			if (item[3].equals("\"trade\"")) { 
				LedgerItem li = ex.buildLedgerItem(item);
				if (li.time.isAfter(last))
					last = li.time;
				items.add(li);
			}
				
		}
		
		in.close();
	}
	
	public synchronized void buildTrades(String asset) {
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
	
	public synchronized boolean mergeLedgers(Ledger l) {
		if (last.isBefore(l.last)) last = l.last;
		
		for (LedgerItem cur : items) {
			for (Iterator<LedgerItem> it = l.items.iterator();it.hasNext();) {
				LedgerItem i = it.next();
				
				if (i.refid.equals(cur.refid))
					it.remove();
			}
		}
		
		return items.addAll(l.items);
	}
	
	public boolean updateLedger(Exchange ex) throws IOException,ExchangeException,InterruptedException {
		return mergeLedgers(ex.getLedger("trade",String.valueOf(last.toEpochMilli()/1000),0));
	}
	
	public LedgerItem lastTrade(String coin) {
		LedgerItem result = new LedgerItem();
		
		for (LedgerItem cur : items)
			if (cur.pair.asset.equals(coin) && cur.time.isAfter(result.time))
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
	
	public String toString() {
		String result = "";
		
		for (LedgerItem cur : items)
			result += cur+"\n";
		
		return result;
	}
	
	public static void main(String[] args) {
		try {
			Ledger l = readFromFile(new File(args[0]));
			
			System.out.println(l);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}