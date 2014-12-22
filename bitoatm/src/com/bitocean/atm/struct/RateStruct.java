/**
 * 
 */
package com.bitocean.atm.struct;

import java.io.Serializable;

/**
 * @author bing.liu
 *
 */
public class RateStruct implements Serializable{
	public String bit_type;//虚拟货币币类型
	public String currency_type;//法币类型
	public String bit_rate;// 1比特币或火币与法币汇率  423.54 人民币
	public String currency_rate;// 最小阀值1000兑换多少比特币 0.00020456 比特币
	public String poundage_buy;// 买入费比率
	public String poundage_sell;// 卖出手续费比率
	public String threshold_min;// 最小阀值
	public String threshold_max;//每次对应币种交易法币最大阀值
}
