package com.bitocean.atm.protocol;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.struct.LoginAdminStruct;
import com.bitocean.atm.struct.LoginUserStruct;
import com.bitocean.atm.struct.RateStruct;
import com.bitocean.atm.struct.RedeemConfirmStruct;
import com.bitocean.atm.struct.VerifyCodeStruct;

/**
 * @author bing.liu
 * 
 */
public class ProtocolDataInput {

	public static LoginAdminStruct parseLoginAdminToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			LoginAdminStruct struct = new LoginAdminStruct();
			struct.resutlString = obj.getString("result");
			struct.resonString = obj.getString("reson");
			struct.update_infoString = obj.getString("info");
			struct.update_linkString = obj.getString("link");
			AppManager.public_keyString = obj.getString("public_key");
			return struct;
		} catch (JSONException ex) {
		} catch (Exception e) {

		}
		return null;
	}

	public static LoginUserStruct parseLoginUserToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			LoginUserStruct struct = new LoginUserStruct();
			struct.resutlString = obj.getString("result");
			struct.resonString = obj.getString("reson");
			struct.userTypeString = obj.getString("type");
			struct.levelString = obj.getString("level");
			return struct;
		} catch (JSONException ex) {
		} catch (Exception e) {

		}
		return null;
	}

	public static VerifyCodeStruct parseVerifyCodeToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			VerifyCodeStruct struct = new VerifyCodeStruct();
			struct.resutlString = obj.getString("result");
			struct.resonString = obj.getString("reson");
			return struct;
		} catch (JSONException ex) {
		} catch (Exception e) {

		}
		return null;
	}

	public static void parseRateListToJson(JSONObject obj) throws JSONException {
		if (obj == null) {
			return;
		}
		try {
			JSONArray arrays = obj.getJSONArray("result");
			if (arrays == null || arrays.length() == 0)
				return;

			AppManager.typeRateStructs.rateStructs.clear();
			for (int i = 0; i < arrays.length(); i++) {
				JSONObject item = (JSONObject) arrays.opt(i);
				RateStruct struct = new RateStruct();
				struct.bit_type = item.getString("bit_type");
				struct.currency_type = item.getString("currency_type");
				struct.bit_rate = item.getDouble("bit_rate");
				struct.currency_rate = item.getDouble("currency_rate");
				struct.poundage_buy = item.getDouble("poundage_buy");
				struct.poundage_sell = item.getDouble("poundage_sell");
				struct.type_limit = item.getDouble("type_limit_limit");
				struct.threshold_min = item.getDouble("threshold_min");
				struct.threshold_max = item.getDouble("threshold_max");
				AppManager.typeRateStructs.currency_typeString = struct.currency_type;
				AppManager.typeRateStructs.rateStructs.add(struct);
			}
			return;
		} catch (JSONException ex) {
		} catch (Exception e) {

		}
		return;
	}

	public static RedeemConfirmStruct parseRedeemConfirmToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			RedeemConfirmStruct struct = new RedeemConfirmStruct();
			struct.resutlString = obj.getString("result");
			struct.resonString = obj.getString("reson");
			struct.currency_type = obj.getString("currency_type");
			struct.cash_num = obj.getDouble("cash_num");
			return struct;
		} catch (JSONException ex) {
		} catch (Exception e) {

		}
		return null;
	}
}