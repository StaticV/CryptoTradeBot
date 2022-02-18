package CryptoExchange;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class LedgerItem implements Serializable {
	
	public static final long serialVersionUID = 1;

	public String refid,type,subtype,aclass,asset;
	public Instant time;
	public BigDecimal amount,fee, balance;
	public LedgerItem pair = null;
	
	
	public LedgerItem(Object jo) {
		refid = Json.getString(jo,"refid");
		type = Json.getString(jo,"type");
		subtype = Json.getString(jo,"subtype");
		aclass = Json.getString(jo,"aclass");
		asset = Json.getString(jo,"asset");
		time = Instant.ofEpochSecond(Json.getLong(jo,"time"));
		amount = Json.getBigDecimal(jo,"amount");
		fee = Json.getBigDecimal(jo,"fee");
		balance = Json.getBigDecimal(jo,"balance");
	}
	
	public LedgerItem(String[] strings) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
		
		refid = strings[1].substring(1,strings[1].length()-1);
		type = strings[3].substring(1,strings[3].length()-1);
		subtype = strings[4].substring(1,strings[4].length()-1);
		aclass = strings[5].substring(1,strings[5].length()-1);
		asset = strings[6].substring(1,strings[6].length()-1);
		time = Instant.from(formatter.parse(strings[2].substring(1,strings[2].length()-1)));
		amount = new BigDecimal(strings[7]);
		fee = new BigDecimal(strings[8]);
		balance = new BigDecimal(strings[9]);
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
		if (arePair(l)) {
			pair = l;
			l.pair = this;
			return true;
		}
		else return false;
	}
	
	public String toString() {
		String r = refid+" "+time+" "+asset+" "+amount;
		if (pair != null) r += " "+pair.asset+" "+pair.amount;
		return r;
	}
}