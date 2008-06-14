package net.gnehzr.cct.scrambles;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public final class TimeoutJob {
	private TimeoutJob() {}
	
	public static final ScramblePluginClassLoader PLUGIN_LOADER = new ScramblePluginClassLoader();
	private static long timeout = 1000; //milliseconds
	private static class ThreadJob<T> extends Thread {
		private T result;
		private Throwable error;
		private Callable<T> callMe;
		public ThreadJob(Callable<T> callMe) {
			this.callMe = callMe;
		}
		public void run() {
			try {
				result = callMe.call();
			} catch (Throwable e) {				
				error = e;
			}
		}
	}
	
	//throws TimeoutException if the job timed out
	public static <T> T doWork(final Callable<T> callMe) throws Throwable {
		ThreadJob<T> t = new ThreadJob<T>(callMe);
		t.setContextClassLoader(PLUGIN_LOADER);
		t.start();
		long end = System.currentTimeMillis() + timeout;
		while(System.currentTimeMillis() < end && t.isAlive()) {
			try {
//				t.join(end - System.currentTimeMillis());
				t.join(0, 100); //apparently we get better performance if we're more aggressive
			} catch(InterruptedException e) {}
		}
		if(t.isAlive()) {
			t.stop();
			throw new TimeoutException("Job timed out after " + timeout + " milliseconds.");
		}
		if(t.error != null)
			throw t.error;
		return t.result;
	}

	public static void main(String[] args) {
		try { //exception
			System.out.println(TimeoutJob.doWork(new Callable<String>() {
				public String call() throws Exception {
					return ((String)null).intern();
				}
			}));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		try { //timeout
			System.out.println(TimeoutJob.doWork(new Callable<String>() {
				public String call() throws Exception {
					for(;;);
				}
			}));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		try { //correct
			System.out.println(TimeoutJob.doWork(new Callable<String>() {
				public String call() throws Exception {
					return "jeremy rocks!";
				}
			}));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
