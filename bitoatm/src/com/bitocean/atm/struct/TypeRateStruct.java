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
	public String typeString;
	public ArrayList<RateStruct> typeRateStructs = new ArrayList<RateStruct>();
}
