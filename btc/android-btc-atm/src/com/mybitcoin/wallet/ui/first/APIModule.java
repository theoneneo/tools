package com.mybitcoin.wallet.ui.first;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.util.TimeUtil;
import com.mybitcoin.wallet.util.WalletUtils;

public class APIModule {

	private static APIModule apiModule;

	public static APIModule getInstance() {
		if (apiModule == null) {
			apiModule = new APIModule();
		}
		return apiModule;
	}

	public void user(String user, String password, String pkey,
			AsyncHttpResponseHandler asyncHttpResponseHandler) {

		RequestParams params = new RequestParams();
		params.put("atmuser_name", user);
		params.put("atmuser_password", password);
		params.put("time", TimeUtil.getDate());
		params.put("atm_name", pkey);
		BaseHttpModule.post("/atmuser_reg/", params, asyncHttpResponseHandler);
	}

	public void key(String pKey, String sKey,
			AsyncHttpResponseHandler asyncHttpResponseHandler) {
		RequestParams params = new RequestParams();
		params.put("atm_publickey", pKey);
		params.put("atm_privatekey", sKey);
		params.put("time", TimeUtil.getDate());
		BaseHttpModule.get("/atmuser_key/", params, asyncHttpResponseHandler);
	}
	
	public void getUIWord(String pKey, AsyncHttpResponseHandler asyncHttpResponseHandler) {
		BaseHttpModule.get("/ui_info/"+pKey,new RequestParams(), asyncHttpResponseHandler);
	}
}
