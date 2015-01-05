/**
 * 
 */
package com.bitocean.atm.struct;

import java.io.Serializable;

/**
 * @author bing.liu
 *
 */
public class SellBitcoinMessageStruct implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9204694307884145556L;
	/**
	 * 
	 */
	public String resutlString;
	public String resonString;
	public String user_public_key;
	public String user_id;
	public String currency_codeString;
	public double currency_num;
}
