package CryptoExchange;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LedgerItem implements Serializable {
	
	public static final long serialVersionUID = 1;

	public String refid,type,subtype,aclass,asset;
	public Date time;
	public BigDecimal amount,fee, balance;
	public LedgerItem pair;
	
	
	public LedgerItem(Object jo) {
		refid = Json.getString(jo,"refid");
		type = Json.getString(jo,"type");
		subtype = Json.getString(jo,"subtype");
		aclass = Json.getString(jo,"aclass");
		asset = Json.getString(jo,"asset");
		time = new Date(Json.getLong(jo,"time")*1000);
		amount = Json.getBigDecimal(jo,"amount");
		fee = Json.getBigDecimal(jo,"fee");
		balance = Json.getBigDecimal(jo,"balance");
		pair = null;
	}
	
	public LedgerItem(String[] strings) throws ParseException {
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		refid = strings[1].substring(1,strings[1].length()-1);
		type = strings[3].substring(1,strings[3].length()-1);
		subtype = strings[4].substring(1,strings[4].length()-1);
		aclass = strings[5].substring(1,strings[5].length()-1);
		asset = strings[6].substring(1,strings[6].length()-1);
		time = sd.parse(strings[2].substring(1,strings[2].length()-1));
		amount = new BigDecimal(strings[7]);
		fee = new BigDecimal(strings[8]);
		balance = new BigDecimal(strings[9]);
		pair = null;
	}
	
	public LedgerItem() {
		time = new Date(0);
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
		return refid+" "+time+" "+asset+" "+amount+" "+pair.asset+" "+pair.amount;
	}
}