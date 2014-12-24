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
	public String currency_typeString;
	public ArrayList<RateStruct> rateStructs = new ArrayList<RateStruct>();
}
