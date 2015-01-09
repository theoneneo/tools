/**
 * 
 */
package com.bitocean.atm.struct;

import java.io.Serializable;
/**
 * @author bing.liu
 *
 */
public class BuyBitcoinPrintWalletStruct implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3607895568741949939L;
	/**
	 * 
	 */
	public String resutlString;
	public int reason;
	public String wallet_public_key;
	public String wallet_private_key;
}
