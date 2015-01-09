package com.bitocean.atm.protocol;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bitocean.atm.controller.AppManager;

/**
 * @author bing.liu
 * 
 */
public class ProtocolDataOutput {

	//管理员登陆
	public static JSONObject loginAdmin(String admin_id, String admin_password)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("admin_id", admin_id);
			output.put("admin_password", admin_password);
			output.put("dtm_uuid", AppManager.DTM_UUID);
			output.put("dtm_currency", AppManager.DTM_CURRENCY);
			output.put("dtm_state", AppManager.DTM_STATE);
			output.put("dtm_operators", AppManager.DTM_OPERATORS);
			output.put("versionCode", String.valueOf(AppManager.versionCode));
			output.put("versionName", AppManager.versionNameString);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	//用户登陆
	public static JSONObject loginUser(String user_id, String user_password)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("user_id", user_id);
			output.put("user_password", user_password);
			output.put("dtm_uuid", AppManager.DTM_UUID);
			output.put("dtm_currency", AppManager.DTM_CURRENCY);
			output.put("dtm_state", AppManager.DTM_STATE);
			output.put("dtm_operators", AppManager.DTM_OPERATORS);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	//获取验证码
	public static JSONObject verifyCode(String user_id) throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("user_id", user_id);
			output.put("dtm_uuid", AppManager.DTM_UUID);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	//获取汇率
	public static JSONObject getRateList(ArrayList<String> bitType)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			JSONArray rate_list = new JSONArray();
			for (String type : bitType) {
				rate_list.put(type);
			}
			output.put("rate_list", rate_list);
			output.put("dtm_uuid", AppManager.DTM_UUID);
			output.put("dtm_box_out_cash", AppManager.DTM_BOX_OUT_CASH);
			output.put("dtm_currency", AppManager.DTM_CURRENCY);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	//赎回确认码验证
	public static JSONObject redeemConfirm(String redeemCode)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("redeem_code", redeemCode);
			output.put("dtm_uuid", AppManager.DTM_UUID);
			output.put("dtm_currency", AppManager.DTM_CURRENCY);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//获取卖币二维码
	public static JSONObject getSellQRCode(String user_public_key, String user_id, int currency_num)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("user_public_key", user_public_key);
			output.put("dtm_uuid", AppManager.DTM_UUID);
			output.put("bit_type", "bitcoin");
			output.put("user_id", user_id);
			output.put("dtm_currency", AppManager.DTM_CURRENCY);
			output.put("currency_num", currency_num);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//卖币确认
	public static JSONObject getSellBitcoinMessage(String user_public_key, String bit_type, String user_id, int currency_num)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("user_public_key", user_public_key);
			output.put("dtm_uuid", AppManager.DTM_UUID);
			output.put("bit_type", bit_type);
			output.put("user_id", user_id);
			output.put("dtm_currency", AppManager.DTM_CURRENCY);
			output.put("currency_num", currency_num);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//二维码买币
	public static JSONObject buyBitcoinQR(String user_public_key, String bit_type, String user_id, int currency_num, String currency_type)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("user_public_key", user_public_key);
			output.put("dtm_uuid", AppManager.DTM_UUID);
			output.put("bit_type", bit_type);
			output.put("user_id", user_id);
			output.put("dtm_currency", AppManager.DTM_CURRENCY);
			output.put("currency_num", currency_num);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//纸钱包买币
	public static JSONObject buyBitcoinPrintWallet(String user_public_key, String bit_type, String user_id, int currency_num, String currency_type)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("user_public_key", user_public_key);
			output.put("dtm_uuid", AppManager.DTM_UUID);
			output.put("bit_type", bit_type);
			output.put("user_id", user_id);
			output.put("dtm_currency", AppManager.DTM_CURRENCY);
			output.put("currency_num", currency_num);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
