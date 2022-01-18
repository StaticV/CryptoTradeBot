package CryptoExchange;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Hashing {

	public static byte[] calculateHash(byte[] data, String algorithm) throws NoSuchAlgorithmException {
		return MessageDigest.getInstance(algorithm).digest(data);
	}

	public static byte[] calculateHMAC(byte[][] data, byte[] key, String algorithm) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = Mac.getInstance(algorithm);
		mac.init(new SecretKeySpec(key, algorithm));
		
		for (byte[] d : data) mac.update(d);
		
		return mac.doFinal();
	}
}