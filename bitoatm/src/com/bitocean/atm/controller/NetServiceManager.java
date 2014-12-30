package com.bitocean.atm.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bitocean.atm.BitOceanATMApp;
import com.bitocean.atm.protocol.ProtocolDataInput;
import com.bitocean.atm.protocol.ProtocolDataOutput;
import com.bitocean.atm.service.ATMBroadCastEvent;
import com.google.common.io.Files;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import de.greenrobot.event.EventBus;
/**
 * @author bing.liu
 * 
 */
public class NetServiceManager extends BaseManager {
	private static NetServiceManager mInstance;

	private static final String NET_SERVER = "http://infocomm.duapp.com/";
	private static final String NET_LOGIN_ADMIN = NET_SERVER + "";
	private static final String NET_VERIFY_CODE = NET_SERVER + "";
	private static final String NET_REGISTER_USER = NET_SERVER + "";
	private static final String NET_LOGIN_USER = NET_SERVER + "";
	private static final String NET_GET_RATE_LIST = NET_SERVER + "";
	private static final String NET_REDEEM_CONFIRM = NET_SERVER + "";

	private NetServiceManager(Application app) {
		super(app);
		// TODO Auto-generated constructor stub
		initManager();
	}

	public static NetServiceManager getInstance() {
		NetServiceManager instance;
		if (mInstance == null) {
			synchronized (NetServiceManager.class) {
				if (mInstance == null) {
					instance = new NetServiceManager(
							BitOceanATMApp.getApplication());
					mInstance = instance;
				}
			}
		}
		return mInstance;
	}

	@Override
	protected void initManager() {
		// TODO Auto-generated method stub

	}

	@Override
	public void DestroyManager() {
		// TODO Auto-generated method stub

	}

	public void loginAdmin(String admin_id, String admin_password) {
		RequestQueue mQueue = Volley.newRequestQueue(BitOceanATMApp
				.getContext());
		try {
			JSONObject obj = ProtocolDataOutput.loginAdmin(admin_id,
					admin_password);
			mQueue.add(new JsonObjectRequest(Method.POST, NET_LOGIN_ADMIN, obj,
					new Listener() {

						@Override
						public void onResponse(Object arg0) {
							// TODO Auto-generated method stub
							try {
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_ADMIN_LOGIN_SUCCESS,
												ProtocolDataInput
														.parseLoginAdminToJson((JSONObject) arg0)));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_ADMIN_LOGIN_FAIL,
												e.getMessage()));
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_ADMIN_LOGIN_FAIL,
											arg0.getMessage()));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}

	public void verifyCode(String user_id) {
		RequestQueue mQueue = Volley.newRequestQueue(BitOceanATMApp
				.getContext());
		try {
			JSONObject obj = ProtocolDataOutput.verifyCode(user_id);
			mQueue.add(new JsonObjectRequest(Method.POST, NET_VERIFY_CODE, obj,
					new Listener() {

						@Override
						public void onResponse(Object arg0) {
							// TODO Auto-generated method stub
							try {
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_VERIFY_CODE_SUCCESS,
												ProtocolDataInput
														.parseVerifyCodeToJson((JSONObject) arg0)));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_VERIFY_CODE_FAIL,
												e.getMessage()));
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_VERIFY_CODE_FAIL,
											arg0.getMessage()));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}

	public void registerUser(String user_id, String user_password,
			String verifyCode, String userIconString, String passportString) {
		new Thread(new RegisterThread(user_id, user_password, verifyCode,
				userIconString, passportString)).start();
	}

	public void loginUser(String user_id, String user_password) {
		RequestQueue mQueue = Volley.newRequestQueue(BitOceanATMApp
				.getContext());
		try {
			JSONObject obj = ProtocolDataOutput.loginUser(user_id,
					user_password);
			mQueue.add(new JsonObjectRequest(Method.POST, NET_LOGIN_USER, obj,
					new Listener() {

						@Override
						public void onResponse(Object arg0) {
							// TODO Auto-generated method stub
							try {
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_USER_LOGIN_SUCCESS,
												ProtocolDataInput
														.parseLoginUserToJson((JSONObject) arg0)));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_USER_LOGIN_FAIL,
												e.getMessage()));
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_USER_LOGIN_FAIL,
											arg0.getMessage()));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}

	public void getRateList(ArrayList<String> bitType) {
		RequestQueue mQueue = Volley.newRequestQueue(BitOceanATMApp
				.getContext());
		try {
			JSONObject obj = ProtocolDataOutput.getRateList(bitType);
			mQueue.add(new JsonObjectRequest(Method.POST, NET_GET_RATE_LIST,
					obj, new Listener() {

						@Override
						public void onResponse(Object arg0) {
							// TODO Auto-generated method stub
							try {
								ProtocolDataInput
										.parseRateListToJson((JSONObject) arg0);
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_GET_RATE_LIST_SUCCESS));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_GET_RATE_LIST_FAIL,
												e.getMessage()));
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_GET_RATE_LIST_FAIL,
											arg0.getMessage()));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}
	
	public void redeemConfirm(String redeemString){
		RequestQueue mQueue = Volley.newRequestQueue(BitOceanATMApp
				.getContext());
		try {
			JSONObject obj = ProtocolDataOutput.redeemConfirm(redeemString);
			mQueue.add(new JsonObjectRequest(Method.POST, NET_REDEEM_CONFIRM,
					obj, new Listener() {

						@Override
						public void onResponse(Object arg0) {
							// TODO Auto-generated method stub
							try {
								ProtocolDataInput
										.parseRedeemConfirmToJson((JSONObject) arg0);
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_REDEEM_CONFIRM_SUCCESS));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_REDEEM_CONFIRM_FAIL,
												e.getMessage()));
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_REDEEM_CONFIRM_FAIL,
											arg0.getMessage()));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}

	class RegisterThread implements Runnable {
		final private String user_id;
		final private String user_password;
		final private String verifyCode;
		final private String userIconString;
		final private String passportString;

		public RegisterThread(String user_id, String user_password,
				String verifyCode, String userIconString, String passportString) {
			this.user_id = user_id;
			this.user_password = user_password;
			this.verifyCode = verifyCode;
			this.userIconString = userIconString;
			this.passportString = passportString;
		}

		AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {
				String responseStr = new String(response);
				if (responseStr.equals("OK")) {
					EventBus.getDefault()
							.post(new ATMBroadCastEvent(
									ATMBroadCastEvent.EVENT_USER_REGISTER_SUCCESS,
									new String(response)));
				} else {
					EventBus.getDefault().post(
							new ATMBroadCastEvent(
									ATMBroadCastEvent.EVENT_USER_REGISTER_FAIL,
									new String(response)));
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {
				if (errorResponse != null) {
					EventBus.getDefault().post(
							new ATMBroadCastEvent(
									ATMBroadCastEvent.EVENT_USER_REGISTER_FAIL,
									new String(errorResponse)));
				}
			}
		};

		@Override
		public void run() {
			// TODO Auto-generated method stub
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams postBody = new RequestParams();
			postBody.put("register_id", user_id);
			postBody.put("register_password", user_password);
			postBody.put("verify_code", verifyCode);
			postBody.put("uuid", AppManager.uuidString);
			try {
				File userIcon = new File(userIconString);
				byte[] byteUserIconData = Files.toByteArray(userIcon);
				ByteArrayInputStream bsUserIcon = new ByteArrayInputStream(
						byteUserIconData);
				postBody.put("user_icon", bsUserIcon, userIconString);

				File passport = new File(passportString);
				byte[] bytepassportData = Files.toByteArray(passport);
				ByteArrayInputStream bspassport = new ByteArrayInputStream(
						bytepassportData);
				postBody.put("passport", bspassport, passportString);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			client.post(NET_REGISTER_USER, postBody, responseHandler);
		}
	}
}
