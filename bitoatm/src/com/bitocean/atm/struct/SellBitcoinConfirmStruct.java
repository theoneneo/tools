/**
 * 
 */
package com.bitocean.atm.struct;

import java.io.Serializable;

/**
 * @author bing.liu
 *
 */
public class SellBitcoinConfirmStruct implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9204694307884145556L;
	/**
	 * 
	 */
	public String resutlString;
	public int reason;
	public String user_public_key;
	public String dtm_currency;
	public String redeem_code;
	public double currency_num;
}
