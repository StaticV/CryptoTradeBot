package CryptoExchange;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;


public class LedgerItem implements Serializable {
	
	public static final long serialVersionUID = 1;

	public String refid,type,subtype,aclass,asset;
	public Instant time;
	public BigDecimal amount,fee,balance;
	public LedgerItem pair = null;
	
	
	public LedgerItem(String refid,String type,String subtype,String aclass,String asset,Instant time,BigDecimal amount,BigDecimal fee,BigDecimal balance) {
		this.refid = refid;
		this.type = type;
		this.subtype = subtype;
		this.aclass = aclass;
		this.asset = asset;
		this.time = time;
		this.amount = amount;
		this.fee = fee;
		this.balance = balance;
	}
	
	public LedgerItem() {
		time = Instant.MIN;
	}
	
	public BigDecimal price() {
		return amount.divide(pair.amount,RoundingMode.HALF_EVEN).abs();
	}
	
	public boolean arePair(LedgerItem l) {
		return refid.equals(l.refid) && !asset.equals(l.asset);
	}
	
	public boolean pair(LedgerItem l) {
		if (!arePair(l)) return false;
		
		pair = l;
		l.pair = this;
		return true;
	}
	
	public String toString() {
		String r = refid+" "+time+" "+asset+" "+amount;
		if (pair != null) r += " "+pair.asset+" "+pair.amount+" price "+price();
		return r;
	}
}