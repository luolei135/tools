package com.ncs.dsf.core;

public class RequestContextHolder {
	
	private static ThreadLocal<DSFRequestContext> requestContextTL = new ThreadLocal<DSFRequestContext>();

	public static DSFRequestContext getDSFRequestContext() {
		DSFRequestContext context = (DSFRequestContext) requestContextTL.get();
		if (context == null) {
			context = new DSFRequestContext();
			requestContextTL.set(context);
		}
		return context;
	}

	/**
	 * set CafContext object
	 * 
	 * @param context
	 */
	public static void setMPARequestContext(DSFRequestContext context) {
		requestContextTL.set(context);
	}

	/**
	 * initial CafContext object
	 */
//	public static DSFRequestContext initContext() {
//		DSFRequestContext context = new DSFRequestContext();
//		requestContextTL.set(context);
//		return context;
//	}


}
