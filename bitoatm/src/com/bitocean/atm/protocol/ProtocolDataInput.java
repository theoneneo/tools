package com.bitocean.atm.protocol;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.struct.LoginAdminStruct;
import com.bitocean.atm.struct.LoginUserStruct;
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
	
	public static void parseRateListToJson(JSONObject obj)
			throws JSONException {
		if (obj == null) {
			return;
		}
		try {
			VerifyCodeStruct struct = new VerifyCodeStruct();
			struct.resutlString = obj.getString("result");
			struct.resonString = obj.getString("reson");
			return;
		} catch (JSONException ex) {
		} catch (Exception e) {

		}
		return;
	}
	
//	JSONTokener jsonParser = new JSONTokener(input);
//	JSONObject obj = (JSONObject) jsonParser.nextValue();
//	JSONArray arrays = obj.getJSONArray("receiver_list");
//	if (arrays == null)
//		return null;
//	PersonManager.getInstance().getReceiverList().clear();
//	for (int i = 0; i < arrays.length(); i++) {
//		UserInfo u = new UserInfo();
//		JSONObject item =  (JSONObject) arrays.opt(i);
//		u.user_id = Utf8Code.utf8Decode(item.getString("user_id"));
//		u.nick_name = Utf8Code.utf8Decode(item.getString("nick_name"));
//		if(u.user_id.equals(InfoCommApp.user_id))
//			continue;
//		PersonManager.getInstance().getReceiverList()
//				.add(u);
//	}
}