package CryptoExchange;

import java.io.IOException;

public class KrakenWebSocketsExchange extends KrakenExchange {

	
	public KrakenWebSocketsExchange(String apiKey,String privKey) {
		super(apiKey,privKey);
	}

	protected String getWebSocketsToken() throws IOException,ExchangeException,InterruptedException {
		String[] keys = {JSONKey,"token"};
		
		return getJSONString(signedRequest(privUrl+"GetWebSocketsToken",""),keys);
	}
}
