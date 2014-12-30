/**
 * 
 */
package com.bitocean.atm.struct;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author bing.liu
 *
 */
public class TypeRateStruct implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6129413418524975053L;
	public String currency_typeString;
	public ArrayList<RateStruct> rateStructs = new ArrayList<RateStruct>();
}
