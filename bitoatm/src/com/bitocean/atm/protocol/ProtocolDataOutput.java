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

	public static JSONObject loginAdmin(String admin_id, String admin_password)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("admin_id", admin_id);
			output.put("admin_password", admin_password);
			output.put("uuid", AppManager.uuidString);
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

	public static JSONObject loginUser(String user_id, String user_password)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("user_id", user_id);
			output.put("user_password", user_password);
			output.put("uuid", AppManager.uuidString);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject verifyCode(String user_id) throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("user_id", user_id);
			output.put("uuid", AppManager.uuidString);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject getRateList(ArrayList<String> bitType)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			JSONArray rate_list = new JSONArray();
			for (String type : bitType) {
				rate_list.put(type);
			}
			output.put("get_rate_list", rate_list);
			output.put("uuid", AppManager.uuidString);
			output.put("exchange", AppManager.exchangeString);
			output.put("currency_type", AppManager.currency_typeString);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject redeemConfirm(String redeemCode)
			throws JSONException {
		try {
			JSONObject output = new JSONObject();
			output.put("redeem_code", redeemCode);
			output.put("uuid", AppManager.uuidString);
			return output;
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
