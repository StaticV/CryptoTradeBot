package CryptoTrader;

public abstract class Tracker extends Thread {
	
	public int interval;
	
	public void run() {
		try {
			while (checkPrice()) sleep(interval);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
    }

	public abstract boolean checkPrice() throws InterruptedException;
}