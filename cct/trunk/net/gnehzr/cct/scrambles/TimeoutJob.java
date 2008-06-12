package net.gnehzr.cct.scrambles;

import java.util.concurrent.Callable;

public abstract class TimeoutJob<T> implements Callable<T> {
	@SuppressWarnings("serial")
	public static class TimeoutException extends Exception {}
	
	private T result;
	private Exception error;
	public abstract T call() throws Exception;
	
	private long timeout = 1000; //milliseconds
	//throws TimeoutException if the job timed out
	public T doWork() throws Exception {
		Thread t = new Thread() {
			public void run() {
				try {
					result = call();
				} catch (Exception e) {
					error = e;
				}
			}
		};
		t.start();
		long end = System.currentTimeMillis() + timeout;
		while(System.currentTimeMillis() < end) {
			try {
				t.join(end - System.currentTimeMillis());
			} catch(InterruptedException e) {}
		}
		if(t.isAlive()) {
			t.stop();
			throw new TimeoutException();
		}
		if(error != null)
			throw error;
		return result;
	}

	public static void main(String[] args) {
		try { //exception
			System.out.println(new TimeoutJob<String>() {
				public String call() throws Exception {
					return ((String)null).intern();
				}
			}.doWork());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try { //timeout
			System.out.println(new TimeoutJob<String>() {
				public String call() throws Exception {
					for(;;);
				}
			}.doWork());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try { //correct
			System.out.println(new TimeoutJob<String>() {
				public String call() throws Exception {
					return "jeremy rocks!";
				}
			}.doWork());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
