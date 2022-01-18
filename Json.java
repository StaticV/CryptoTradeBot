package CryptoExchange;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;

public class Json {
	
	protected static Object parseResults(JSONObject jo,String[] keys) {
		Object result = new Object();
		
		for (String key : keys) {
			result = jo.get(key);
					
			if (result instanceof JSONObject) jo = (JSONObject)result;
			else break;
		}
		
		return result;
	}
	
	public static String getString(Object o, String key) {
		return ((JSONObject)o).getString(key);
	}
	
	public static long getLong(Object o, String key) {
		return ((JSONObject)o).getLong(key);
	}
	
	public static BigDecimal getBigDecimal(Object o, String key) {
		return ((JSONObject)o).getBigDecimal(key);
	}

	public static Object getJSONData(String data,String[] keys, String[] errorKeys) throws ExchangeException {
		JSONObject jo = new JSONObject(data);
		
		String error = jo.get(errorKeys[0]).toString();
		if (!error.equals(errorKeys[1])) throw new ExchangeException(error);
		
		return parseResults(jo, keys);
	}
	
	public static Object getJSONData(String data,String[] keys) {
		JSONObject jo = new JSONObject(data);
		
		return parseResults(jo, keys);
	}
	
	public static Iterator<Object> getJSONArray(String data) {
		JSONObject jo = new JSONObject(data);
		
		Iterator<String> keys = jo.keys();
		
		ArrayList<Object> jos = new ArrayList<Object>();
		
		while(keys.hasNext()) jos.add(jo.getJSONObject(keys.next()));
		
		return jos.iterator();
	}
}