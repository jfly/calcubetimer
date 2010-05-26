package net.gnehzr.cct.scrambles;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

public final class TimeoutJob {
	private TimeoutJob() {}
	
	public static final ScramblePluginClassLoader PLUGIN_LOADER = new ScramblePluginClassLoader();
	private static class ThreadJob<T> extends Thread {
		T result;
		Throwable error;
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
		Integer timeout = null;
		try {
			timeout = Configuration.getInt(VariableKey.SCRAMBLE_PLUGIN_TIMEOUT, false);
		} catch(Throwable c) {} //we want to be able to handle no configuration at all
		if(timeout == null)
			timeout = 0;
		try {
			t.join(timeout);
		} catch(InterruptedException e) {}
		if(t.isAlive()) {
			t.stop();
			throw new TimeoutException("Job timed out after " + timeout + " milliseconds.");
		}
		if(t.error != null)
			throw t.error;
		return t.result;
	}

//	public static void main(String[] args) {
//		try { //exception
//			System.out.println(TimeoutJob.doWork(new Callable<String>() {
//				public String call() throws Exception {
//					return ((String)null).intern();
//				}
//			}));
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//		try { //timeout
//			System.out.println(TimeoutJob.doWork(new Callable<String>() {
//				public String call() throws Exception {
//					for(;;);
//				}
//			}));
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		
//		final PrintStream ps = System.out;
//		System.setOut(new PrintStream(new OutputStream() {
//			boolean stamp = true;
//			public void write(int b) throws IOException {
//				if(stamp) {
//					ps.print(Thread.currentThread().getName() + "\t");
//				}
//				ps.write(b);
//				stamp = (b == '\n');
//			}
//		}));
//		System.out.println("awesome");
//		System.out.println("Wowiiee!");
//		try { //correct
//			System.out.println(TimeoutJob.doWork(new Callable<String>() {
//				public String call() throws Exception {
//					return "jeremy rocks!";
//				}
//			}));
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
}
