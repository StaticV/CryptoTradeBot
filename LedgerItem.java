package CryptoExchange;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
	
	public LedgerItem() {
		time = new Date(0);
	}
	
	public BigDecimal price() {
		return amount.divide(pair.amount,RoundingMode.HALF_EVEN).abs();
	}
}