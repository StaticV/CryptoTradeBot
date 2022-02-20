package CryptoExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;


public class KrakenExchange implements Exchange {
	
	public static final String pubUrl = "/0/public/";
	public static final String privUrl = "/0/private/";
	public static final String apiUrl = "https://api.kraken.com";
	public static final String hashType = "SHA-256";
	public static final String signType = "HmacSHA512";
	public static final String JSONKey = "result";
	public static final String[] errorKeys = {"error","[]"};

	private String apiKey;
	private byte[] privKey;
	private long nonce;
	
	
	public static void main(String[] args) {
		try {
			KrakenExchange test = new KrakenExchange("","kQH5HW/8p1uGOVjbgWA7FunAmGO8lsSUXNsu3eow76sz84Q18fWxnyRzBHCd3pd5nE9qa99HAZtuZuj6F1huXg==");
			
			System.out.println(test.getTime());
			System.out.println("Nonce: "+test.nonce);
			System.out.println(test.getPrices(args[0],args[1]));
			System.out.println(test.getOrderBook(args[0],args[1],1));
			System.out.println(test.getTradeableAssetPair(args[0],args[1]));
			System.out.println("Calculated: "+test.apiSign("/0/private/AddOrder","&ordertype=limit&pair=XBTUSD&price=37500&type=buy&volume=1.25","1616492376594"));
			System.out.println("Expected:   4/dpxb3iT4tp/ZCVEwSnEsLxx0bqyhLpdfOpc6fn7OR8+UClSV5n9E6aSS8MPtnRfp32bAb0nmbRn6H8ndwLUQ==");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public KrakenExchange(String apiKey,String privKey) {
		this.apiKey = apiKey;
		this.privKey = Base64.getDecoder().decode(privKey);
		nonce = System.currentTimeMillis();
	}
	
	protected static String getJSONString(String data,String[] keys) throws ExchangeException {
		return Json.getJSONData(data,keys,errorKeys).toString();
	}
	
	protected static String getJSONSubString(String data,String[] keys) throws ExchangeException {
		return Json.getJSONData(data,keys).toString();
	}
	
	protected static String getData(String path) throws IOException, InterruptedException {
		try {
			return Http.getData(apiUrl + path);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected static String postData(String[] header,String data, String path) throws IOException, InterruptedException {
		try {
			return Http.postData(header,data,apiUrl + path);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String apiSign(String path, String payload, String nonce) {
		try {
			byte[][] encPayload = {path.getBytes(),Hashing.calculateHash((nonce+"nonce="+nonce+payload).getBytes(),hashType)};
			
			return Base64.getEncoder().encodeToString(Hashing.calculateHMAC(encPayload,privKey,signType));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch(InvalidKeyException e) {
			e.printStackTrace();
		} catch(SignatureException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected synchronized String signedRequest(String path, String payload) throws IOException, ExchangeException, InterruptedException {
		String nonce =  String.valueOf(this.nonce++);
		
		String[] header = {"API-Key",apiKey,"API-Sign",apiSign(path,payload,nonce)};
		
		return postData(header,"nonce="+nonce+payload,path);
	}
	
	public Instant getTime() throws IOException, ExchangeException, InterruptedException {
		String[] keys = {JSONKey,"unixtime"};
		
		return Instant.ofEpochSecond(Long.parseUnsignedLong(getJSONString(getData(pubUrl+"Time"),keys)));
	}
	
	public String getPrices(String coin, String currency) throws IOException, ExchangeException, InterruptedException {
		String[] keys = {JSONKey,coin+currency};
		
		return getJSONString(getData(pubUrl+"Ticker?pair="+coin+currency),keys);
	}
	
	public BigDecimal[] getHighLowPrice(String coin, String currency) throws IOException, ExchangeException, InterruptedException {
		String[] keys = {"p"};

		String[] prices = getJSONSubString(getPrices(coin,currency),keys).split("\"");

		BigDecimal[] out = {new BigDecimal(prices[1]),new BigDecimal(prices[3])};
		
		return out;
	}
	
	public String getAccountBalance() throws IOException, ExchangeException, InterruptedException {
		String[] keys = {JSONKey};
		
		return getJSONString(signedRequest(privUrl + "Balance",""),keys);
	}
	
	public BigDecimal getCurrencyBalance(String curr) throws IOException, ExchangeException, InterruptedException {
		String[] keys = {curr};
		
		return new BigDecimal(getJSONSubString(getAccountBalance(),keys));
	}
	
	public BigDecimal getCoinBalance(String coin) throws IOException, ExchangeException, InterruptedException {
		String[] keys = {coin};
		
		return new BigDecimal(getJSONSubString(getAccountBalance(),keys));
	}
	
	public String orderMarketPrice(Boolean buy,String coin,String currency,String qty,Boolean validate) throws IOException, ExchangeException, InterruptedException {
		String[] keys = {JSONKey,"descr","order"};
		
		String type;
		if (buy) type = "buy";
		else type = "sell";
		
		return getJSONString(signedRequest(privUrl + "AddOrder","&ordertype=market&type="+type+"&volume="+qty+"&pair="+coin+currency+"&validate="+validate),keys);
	}
	
	public String getTradeableAssetPair(String coin, String currency) throws IOException, ExchangeException, InterruptedException {
		String[] keys = {JSONKey,coin+currency};
		
		return getJSONString(getData(pubUrl+"AssetPairs?pair="+coin+currency),keys);
	}
	
	public BigDecimal getMinOrder(String coin, String currency) throws IOException, ExchangeException, InterruptedException {
		String[] keys = {"ordermin"};
		
		return new BigDecimal(getJSONSubString(getTradeableAssetPair(coin,currency),keys));
	}
	
	public String getOrderBook(String coin, String currency, int count) throws IOException, ExchangeException, InterruptedException {
		String[] keys = {JSONKey,coin+currency};
		
		return getJSONString(getData(pubUrl+"Depth?pair="+coin+currency+"&count="+count),keys);
	}
	
	public Order[] getOrders(String coin, String currency) throws IOException, ExchangeException, InterruptedException {
		String[][] keys = {{"asks"},{"bids"}};
		
		String orders = getOrderBook(coin,currency,1);
		
		String[] asks = getJSONSubString(orders,keys[0]).split("\"");
		String[] bids = getJSONSubString(orders,keys[1]).split("\"");
		
		Order[] result = new Order[2];
		result[0] = new Order(coin,currency,asks[1],asks[3]);
		result[1] = new Order(coin,currency,bids[1],bids[3]);
		
		return result;
	}
	
	public Ledger getLedger(String type,String start, int offset) throws IOException, ExchangeException, InterruptedException {
		String keys[] = {JSONKey,"ledger"};
		
		String payload = "";
		if (type != null) payload += "&type="+type;
		if (start != null) payload += "&start="+start;
		if (offset > 1) payload += "&ofs="+offset;
		
		return new Ledger(Json.getJSONArray(getJSONString(signedRequest(privUrl + "Ledgers",payload),keys)),this);
	}
	
	public LedgerItem buildLedgerItem(Object o) {
		String refid = Json.getString(o,"refid");
		String type = Json.getString(o,"type");
		String subtype = Json.getString(o,"subtype");
		String aclass = Json.getString(o,"aclass");
		String asset = Json.getString(o,"asset");
		Instant time = Instant.ofEpochSecond(Json.getLong(o,"time"));
		BigDecimal amount = Json.getBigDecimal(o,"amount");
		BigDecimal fee = Json.getBigDecimal(o,"fee");
		BigDecimal balance = Json.getBigDecimal(o,"balance");
		
		return new LedgerItem(refid,type,subtype,aclass,asset,time,amount,fee,balance);
	}
	
	public LedgerItem buildLedgerItem(String[] strings) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
		
		String refid = strings[1].substring(1,strings[1].length()-1);
		String type = strings[3].substring(1,strings[3].length()-1);
		String subtype = strings[4].substring(1,strings[4].length()-1);
		String aclass = strings[5].substring(1,strings[5].length()-1);
		String asset = strings[6].substring(1,strings[6].length()-1);
		Instant time = Instant.from(formatter.parse(strings[2].substring(1,strings[2].length()-1)));
		BigDecimal amount = new BigDecimal(strings[7]);
		BigDecimal fee = new BigDecimal(strings[8]);
		BigDecimal balance = new BigDecimal(strings[9]);
		
		return new LedgerItem(refid,type,subtype,aclass,asset,time,amount,fee,balance);
	}
}