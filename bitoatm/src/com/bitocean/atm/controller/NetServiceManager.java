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
	private static final String NET_LOGIN_ADMIN = NET_SERVER
			+ "loginadmin_bit.py";
	private static final String NET_VERIFY_CODE = NET_SERVER + "verify_bit.py";
	private static final String NET_REGISTER_USER = NET_SERVER
			+ "register_bit.py";
	private static final String NET_REGISTER_KYC = NET_SERVER + "kyc_bit.py";
	private static final String NET_LOGIN_USER = NET_SERVER
			+ "loginuser_bit.py";
	private static final String NET_GET_RATE_LIST = NET_SERVER
			+ "getrate_bit.py";
	private static final String NET_REDEEM_CONFIRM = NET_SERVER
			+ "redeemconfirm_bit.py";
	private static final String NET_SELL_QR_CODE = NET_SERVER + "sellqr_bit.py";
	private static final String NET_SELL_BITCOIN = NET_SERVER + "sell_bit.py";
	private static final String NET_BUY_QR_BITCOIN = NET_SERVER + "buy_qr_bit.py";
	private static final String NET_BUY_WALLET_BITCOIN = NET_SERVER + "buy_wallet_bit.py";

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

	// 管理员登录
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
							String msg = null;
							if (arg0.getMessage() != null)
								msg = arg0.getMessage();
							else
								msg = arg0.getLocalizedMessage();
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_ADMIN_LOGIN_FAIL,
											msg));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}

	// 获取注册验证码
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
							String msg = null;
							if (arg0.getMessage() != null)
								msg = arg0.getMessage();
							else
								msg = arg0.getLocalizedMessage();
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_VERIFY_CODE_FAIL,
											msg));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}

	// 用户注册
	public void registerUser(String user_id, String user_password,
			String verifyCode, String userIconString, String passportString) {
		new Thread(new RegisterThread(user_id, user_password, verifyCode,
				userIconString, passportString)).start();
	}

	// 注册kyc
	public void registerUserKyc(String user_id, String userIconString,
			String passportString) {
		new Thread(new RegisterThread(user_id, null, null, userIconString,
				passportString)).start();
	}

	// 用户登录
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
							String msg = null;
							if (arg0.getMessage() != null)
								msg = arg0.getMessage();
							else
								msg = arg0.getLocalizedMessage();
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_USER_LOGIN_FAIL,
											msg));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}

	// 获取汇率
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
							String msg = null;
							if (arg0.getMessage() != null)
								msg = arg0.getMessage();
							else
								msg = arg0.getLocalizedMessage();
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_GET_RATE_LIST_FAIL,
											msg));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}

	// 赎回
	public void redeemConfirm(String redeemString) {
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

								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_REDEEM_CONFIRM_SUCCESS,
												ProtocolDataInput
														.parseRedeemConfirmToJson((JSONObject) arg0)));
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
							String msg = null;
							if (arg0.getMessage() != null)
								msg = arg0.getMessage();
							else
								msg = arg0.getLocalizedMessage();
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_REDEEM_CONFIRM_FAIL,
											msg));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}

	class RegisterThread implements Runnable {
		public String user_id = null;
		public String user_password = null;
		public String verifyCode = null;
		public String userIconString = null;
		public String passportString = null;

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
					if (user_password != null) {
						EventBus.getDefault()
								.post(new ATMBroadCastEvent(
										ATMBroadCastEvent.EVENT_USER_REGISTER_SUCCESS,
										responseStr));
					} else {
						EventBus.getDefault()
								.post(new ATMBroadCastEvent(
										ATMBroadCastEvent.EVENT_USER_REGISTER_KYC_SUCCESS,
										responseStr));
					}
				} else {
					if (user_password != null) {
						EventBus.getDefault()
								.post(new ATMBroadCastEvent(
										ATMBroadCastEvent.EVENT_USER_REGISTER_FAIL,
										responseStr));
					} else {
						EventBus.getDefault()
								.post(new ATMBroadCastEvent(
										ATMBroadCastEvent.EVENT_USER_REGISTER_KYC_FAIL,
										responseStr));
					}
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
			if (user_password != null)
				postBody.put("register_password", user_password);
			if (verifyCode != null)
				postBody.put("verify_code", verifyCode);
			postBody.put("dtm_uuid", AppManager.DTM_UUID);
			try {
				// 根据是否有userIconString 判断是否kyc注册
				if (userIconString != null) {
					File userIcon = new File(userIconString);
					byte[] byteUserIconData = Files.toByteArray(userIcon);
					ByteArrayInputStream bsUserIcon = new ByteArrayInputStream(
							byteUserIconData);
					postBody.put("user_icon", bsUserIcon, userIconString);
				}

				if (passportString != null) {
					File passport = new File(passportString);
					byte[] bytepassportData = Files.toByteArray(passport);
					ByteArrayInputStream bspassport = new ByteArrayInputStream(
							bytepassportData);
					postBody.put("passport", bspassport, passportString);
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (user_password != null)
				client.post(NET_REGISTER_USER, postBody, responseHandler);
			else
				client.post(NET_REGISTER_KYC, postBody, responseHandler);
		}
	}

	// 获取卖币二维码
	public void getSellQRCode(String user_public_key, String user_id,
			int currency_num) {
		RequestQueue mQueue = Volley.newRequestQueue(BitOceanATMApp
				.getContext());
		try {
			JSONObject obj = ProtocolDataOutput.getSellQRCode(user_public_key,
					user_id, currency_num);
			mQueue.add(new JsonObjectRequest(Method.POST, NET_REDEEM_CONFIRM,
					obj, new Listener() {

						@Override
						public void onResponse(Object arg0) {
							// TODO Auto-generated method stub
							try {

								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_GET_SELL_QR_CODE_SUCCESS,
												ProtocolDataInput
														.parseSellQRCodeToJson((JSONObject) arg0)));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_GET_SELL_QR_CODE_FAIL,
												e.getMessage()));
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							String msg = null;
							if (arg0.getMessage() != null)
								msg = arg0.getMessage();
							else
								msg = arg0.getLocalizedMessage();
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_GET_SELL_QR_CODE_FAIL,
											msg));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}
	
	// 卖币
	public void SellBitcoin(String user_public_key, String user_id,
			int currency_num) {
		RequestQueue mQueue = Volley.newRequestQueue(BitOceanATMApp
				.getContext());
		try {
			JSONObject obj = ProtocolDataOutput.getSellBitcoinConfirm(user_public_key,
					user_id, currency_num);
			mQueue.add(new JsonObjectRequest(Method.POST, NET_SELL_BITCOIN,
					obj, new Listener() {

						@Override
						public void onResponse(Object arg0) {
							// TODO Auto-generated method stub
							try {

								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_GET_SELL_SUCCESS,
												ProtocolDataInput
														.parseSellBitcoinConfirmToJson((JSONObject) arg0)));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_GET_SELL_FAIL,
												e.getMessage()));
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							String msg = null;
							if (arg0.getMessage() != null)
								msg = arg0.getMessage();
							else
								msg = arg0.getLocalizedMessage();
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_GET_SELL_FAIL,
											msg));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}
	
	//二维码买币
	public void BuyQRBitcoin(String user_public_key, String user_id,
			int currency_num) {
		RequestQueue mQueue = Volley.newRequestQueue(BitOceanATMApp
				.getContext());
		try {
			JSONObject obj = ProtocolDataOutput.buyBitcoinQR(user_public_key,
					user_id, currency_num);
			mQueue.add(new JsonObjectRequest(Method.POST, NET_BUY_QR_BITCOIN,
					obj, new Listener() {

						@Override
						public void onResponse(Object arg0) {
							// TODO Auto-generated method stub
							try {

								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_BUY_QR_SUCCESS,
												ProtocolDataInput
														.parseBuyBitcoinQRToJson((JSONObject) arg0)));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_BUY_QR_FAIL,
												e.getMessage()));
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							String msg = null;
							if (arg0.getMessage() != null)
								msg = arg0.getMessage();
							else
								msg = arg0.getLocalizedMessage();
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_BUY_QR_FAIL,
											msg));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}
	
	//纸钱包买币
	public void BuyWalletBitcoin(String user_id,
			int currency_num) {
		RequestQueue mQueue = Volley.newRequestQueue(BitOceanATMApp
				.getContext());
		try {
			JSONObject obj = ProtocolDataOutput.buyBitcoinPrintWallet(
					user_id, currency_num);
			mQueue.add(new JsonObjectRequest(Method.POST, NET_BUY_WALLET_BITCOIN,
					obj, new Listener() {

						@Override
						public void onResponse(Object arg0) {
							// TODO Auto-generated method stub
							try {

								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_BUY_WALLET_SUCCESS,
												ProtocolDataInput
														.parseBuyBitcoinPrintWalletToJson((JSONObject) arg0)));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								EventBus.getDefault()
										.post(new ATMBroadCastEvent(
												ATMBroadCastEvent.EVENT_BUY_WALLET_FAIL,
												e.getMessage()));
							}
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							// TODO Auto-generated method stub
							String msg = null;
							if (arg0.getMessage() != null)
								msg = arg0.getMessage();
							else
								msg = arg0.getLocalizedMessage();
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_BUY_WALLET_FAIL,
											msg));
						}
					}));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mQueue.start();
	}
}
