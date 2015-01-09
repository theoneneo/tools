package com.bitocean.atm.protocol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.struct.BuyBitcoinPrintWalletStruct;
import com.bitocean.atm.struct.BuyBitcoinQRStruct;
import com.bitocean.atm.struct.LoginAdminStruct;
import com.bitocean.atm.struct.LoginUserStruct;
import com.bitocean.atm.struct.RateStruct;
import com.bitocean.atm.struct.RedeemConfirmStruct;
import com.bitocean.atm.struct.SellBitcoinMessageStruct;
import com.bitocean.atm.struct.SellBitcoinQRStruct;
import com.bitocean.atm.struct.VerifyCodeStruct;

/**
 * @author bing.liu
 * 
 */
public class ProtocolDataInput {
	// 管理员登陆
	public static LoginAdminStruct parseLoginAdminToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			LoginAdminStruct struct = new LoginAdminStruct();
			struct.resutlString = obj.getString("result");
			struct.reason = obj.getInt("reason");
			struct.update_infoString = obj.getString("info");
			struct.update_linkString = obj.getString("link");
			struct.public_keyString = obj.getString("public_key");
			AppManager.public_keyString = obj.getString("public_key");
			return struct;
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 用户登陆
	public static LoginUserStruct parseLoginUserToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			LoginUserStruct struct = new LoginUserStruct();
			struct.resutlString = obj.getString("result");
			struct.reason = obj.getInt("reason");
			struct.userTypeString = obj.getString("type");
			struct.levelString = obj.getString("level");
			struct.user_idString = obj.getString("user_id");
			struct.sourceString = obj.getString("source");
			struct.date_remaining_quota = obj.getDouble("date_remaining_quota");
			return struct;
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 验证码
	public static VerifyCodeStruct parseVerifyCodeToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			VerifyCodeStruct struct = new VerifyCodeStruct();
			struct.resutlString = obj.getString("result");
			struct.reason = obj.getInt("reason");
			return struct;
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 汇率列表
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
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	// 赎回确认码
	public static RedeemConfirmStruct parseRedeemConfirmToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			RedeemConfirmStruct struct = new RedeemConfirmStruct();
			struct.resutlString = obj.getString("result");
			struct.reason = obj.getInt("reason");
			struct.currency_type = obj.getString("dtm_currency");
			struct.currency_num = obj.getDouble("currency_num");
			return struct;
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 获取blockchain码
	public static SellBitcoinQRStruct parseSellQRCodeToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			SellBitcoinQRStruct struct = new SellBitcoinQRStruct();
			struct.resutlString = obj.getString("result");
			struct.reason = obj.getInt("reason");
			struct.user_public_key = obj.getString("user_public_key");
			struct.bitcoin_qr = obj.getString("bitcoin_qr");
			struct.quota_num = obj.getDouble("quota_num");
			return struct;
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 获取卖币确认 需要轮询获取
	public static SellBitcoinMessageStruct parseSellBitcoinMessageToJson(
			JSONObject obj) throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			SellBitcoinMessageStruct struct = new SellBitcoinMessageStruct();
			struct.resutlString = obj.getString("result");
			struct.reason = obj.getInt("reason");
			struct.user_public_key = obj.getString("user_public_key");
			struct.currency_codeString = obj.getString("currency_code");
			struct.currency_num = obj.getDouble("currency_num");
			return struct;
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 二维码买币
	public static BuyBitcoinQRStruct parseBuyBitcoinQRToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			BuyBitcoinQRStruct struct = new BuyBitcoinQRStruct();
			struct.resutlString = obj.getString("result");
			struct.reason = obj.getInt("reason");
			struct.user_public_key = obj.getString("user_public_key");
			struct.bitcoin_num = obj.getDouble("bitcoin_num");
			return struct;
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 纸钱包买币
	public static BuyBitcoinPrintWalletStruct parseBuyBitcoinPrintWalletToJson(
			JSONObject obj) throws JSONException {
		if (obj == null) {
			return null;
		}
		try {
			BuyBitcoinPrintWalletStruct struct = new BuyBitcoinPrintWalletStruct();
			struct.resutlString = obj.getString("result");
			struct.reason = obj.getInt("reason");
			struct.wallet_public_key = obj.getString("wallet_public_key");
			struct.wallet_private_key = obj.getString("wallet_private_key");
			return struct;
		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}