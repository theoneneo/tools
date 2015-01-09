/**
 * 
 */
package com.bitocean.atm.struct;

import java.io.Serializable;
/**
 * @author bing.liu
 *
 */
public class SellBitcoinQRStruct implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7441791379703418621L;
	/**
	 * 
	 */
	public String resutlString;
	public int reason;
	public String user_public_key;
	public String bitcoin_qr;
	public double quota_num;
}
