/**
 * 
 */
package com.bitocean.atm.struct;

import java.io.Serializable;

/**
 * @author bing.liu
 *
 */
public class LoginUserStruct implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6287966543127633662L;
	public String resutlString;
	public int reason;
	public String userTypeString;
	public String levelString;
	public String user_idString;
	public String sourceString;
	public double date_remaining_quota;
}
