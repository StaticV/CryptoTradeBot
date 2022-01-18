package CryptoExchange;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public class Http {
	
	public static String getData (String url) throws IOException, URISyntaxException, InterruptedException {
		return request(HttpRequest.newBuilder(new URI(url)).GET().build());
	}
	
	public static String postData (String[] headers, String data, String url) throws IOException, URISyntaxException, InterruptedException {
		return request(HttpRequest.newBuilder(new URI(url)).headers(headers).POST(HttpRequest.BodyPublishers.ofString(data)).build());
	}
	
	protected static String request(HttpRequest request) throws IOException, InterruptedException {
		return HttpClient.newHttpClient().send(request,BodyHandlers.ofString()).body();
	}
}