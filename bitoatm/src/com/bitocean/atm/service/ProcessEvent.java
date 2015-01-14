/**
 * 
 */
package com.bitocean.atm.service;

/**
 * @author bing.liu
 *
 */
public class ProcessEvent {
	public final static int BASE_EVENT = 0;

	public final static int EVENT_REDEEM = BASE_EVENT + 1;
	public final static int EVENT_SELL = BASE_EVENT + 2;
	public final static int EVENT_BUY_QR = BASE_EVENT + 3;
	public final static int EVENT_BUY_WALLET = BASE_EVENT + 4;
}
