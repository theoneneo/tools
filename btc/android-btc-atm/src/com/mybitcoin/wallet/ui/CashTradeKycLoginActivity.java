/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.io.Files;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.ui.first.MainActivity;
import com.mybitcoin.wallet.util.TimeUtil;
import com.mybitcoin.wallet.util.Tools;

public class CashTradeKycLoginActivity extends WalletActivityTimeoutBase {
	private static final String LOG_TAG = "CashTradeKycLoginActivity";

	private static final boolean DEBUG_FLAG = true;

	EditText phoneNoText;
	EditText veriCode;

	TextView mCnlBtn;
	TextView mLoginBtn;
	TextView mRegBtn;
	TextView mSendMessage;

	private static final int PROCESS_LOGIN = 1;
	Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			super.dispatchMessage(msg);
			switch (msg.what) {
			case PROCESS_LOGIN:
				break;
			}
		}
	};

	// 用AsyncHttp发验证码请求
	class LoginThread2 implements Runnable {
		AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				dLog("Start");
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {
				if (response != null) {
					String responseString = new String(response);
					dLog(responseString);
					if (responseString.equals("Checked")) {
						gotoActivity(CashTradeKycSuccessActivity.class);
					} else if (responseString.equals("UnChecked")) {
						gotoActivity(CashTradeKycFailureActivity.class);
					}
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {
				if (errorResponse != null)
					dLog(new String(errorResponse));

			}

		};

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String URL = "http://dashboard.bitocean.com:8081/kyc_login/";
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams postBody = new RequestParams();
			postBody.put("phone_num", phoneNoText.getText().toString().trim());
			postBody.put("atm_name", publicKey);
			postBody.put("verify_code", veriCode.getText().toString().trim());
			postBody.put("time", Tools.getTime());

			try {
				client.post(URL, postBody, responseHandler);
			} catch (Exception e) {
				dLog("Something wrong with post");
			}

		}

	}

	// 用AsyncHttp发验证码请求
	class SendMsgThread2 implements Runnable {
		AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {
				dLog("Start");
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {
				if (response != null)
					dLog(new String(response));

		        TextView text = new TextView(CashTradeKycLoginActivity.this);
		        text.setText("verify code send ok!");
		        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		        text.setBackgroundColor(Color.BLACK);
		        Toast toast = new Toast(CashTradeKycLoginActivity.this);
		        toast.setView(text);
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();	 
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {
				if (errorResponse != null)
					dLog(new String(errorResponse));

		        TextView text = new TextView(CashTradeKycLoginActivity.this);
		        text.setText("verify code send error!");
		        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		        text.setBackgroundColor(Color.BLACK);
		        Toast toast = new Toast(CashTradeKycLoginActivity.this);
		        toast.setView(text);
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();
			}

		};

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String URL = "http://dashboard.bitocean.com:8081/kyc_verificationcode/";
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams postBody = new RequestParams();
			postBody.put("phone_num", phoneNoText.getText().toString().trim());
			postBody.put("atm_name", publicKey);
			postBody.put("time", Tools.getTime());

			client.post(URL, postBody, responseHandler);

		}

	}

	private RequestQueue mQueue;

	@Override
	protected void onCreate(final Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);

		setLayout(R.layout.cash_trade_kyc_login);
		mQueue = Volley.newRequestQueue(getWalletApplication()
				.getApplicationContext());

		mCnlBtn = (TextView) findViewById(R.id.cashtrade_kyclogin_cnl_btn);
		mCnlBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				gotoActivity(WelcomePageActivity.class);
				// new Thread(new LoginThread()).start();
			}
		});

		mLoginBtn = (TextView) findViewById(R.id.cashtrade_kyclogin_login_btn);
		mLoginBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// gotoActivity(CashTradeKycSuccessActivity.class);
				new Thread(new LoginThread2()).start();
			}
		});

		mRegBtn = (TextView) findViewById(R.id.cashtrade_kyclogin_register_btn);
		mRegBtn.requestFocus();
		mRegBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				gotoActivity(CashTradeKycRegister1Activity.class);
			}
		});
		mSendMessage = (TextView) findViewById(R.id.cashtrade_kyclogin_sendmsg_btn);
		mSendMessage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new Thread(new SendMsgThread2()).start();
			}
		});

		// Test
		TextView failedBtn = (TextView) findViewById(R.id.cashtrade_kyclogin_failed_btn);
		failedBtn.setVisibility(View.INVISIBLE);
		failedBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				gotoActivity(CashTradeKycFailureActivity.class);
			}
		});

		veriCode = (EditText) findViewById(R.id.cashtrade_kyclogin_veri_txt);
		veriCode.clearFocus();
		veriCode.setInputType(InputType.TYPE_NULL);
		veriCode.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(CashTradeKycLoginActivity.this,
						KeyboardActivity.class);
				intent.putExtra("index", 2);
				intent.putExtra("num", veriCode.getText().toString());
				startActivityForResult(intent, 2);
			}

		});
		veriCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if (arg1) {
					Intent intent = new Intent(CashTradeKycLoginActivity.this,
							KeyboardActivity.class);
					intent.putExtra("index", 2);
					intent.putExtra("num", veriCode.getText().toString());
					startActivityForResult(intent, 2);
				}
			}
		});

		phoneNoText = (EditText) findViewById(R.id.cashtrade_kyclogin_phoneno);
		phoneNoText.clearFocus();
		phoneNoText.setInputType(InputType.TYPE_NULL);
		phoneNoText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(CashTradeKycLoginActivity.this,
						KeyboardActivity.class);
				intent.putExtra("index", 1);
				intent.putExtra("num", phoneNoText.getText().toString());
				startActivityForResult(intent, 1);
			}

		});
		phoneNoText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if (arg1) {
					Intent intent = new Intent(CashTradeKycLoginActivity.this,
							KeyboardActivity.class);
					intent.putExtra("index", 1);
					intent.putExtra("num", phoneNoText.getText().toString());
					startActivityForResult(intent, 1);
				}
			}
		});
		phoneNoText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				String inputStr = s.toString();

				if (inputStr == null) {
					dLog("input is null");
					return;
				} else
					dLog("input is: " + inputStr);

				// 尽管onCreate中设置amountText.setInputType(EditorInfo.TYPE_CLASS_PHONE)，设置数字键盘
				// 但仍然会出现非正整数的情况，故在此进行额外判断
				long input;
				try {
					input = Long.parseLong(inputStr);
				} catch (NumberFormatException e) { // 输入非整数
					dLog("error when calling Integer.parseInt(inputStr): "
							+ e.toString() + ", and delete the last char");

					// 删掉最后一个违法字符
					if (s.length() == 0)
						s.delete(s.length(), s.length());
					else
						s.delete(s.length() - 1, s.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		WalletActivityTimeoutController.getInstance().setTimeout(300);

	}

	@Override
	public void updateUiInfo() {
		super.updateUiInfo();

		TextView tradeType = (TextView) findViewById(R.id.cashtrade_kyclogin_tradetype);
		// tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));
		tradeType.setText("KYC Verify");

		TextView newuser_title = (TextView) findViewById(R.id.cashtrade_kyclogin_newuser_title);
		TextView title = (TextView) findViewById(R.id.cashtrade_kyclogin_title);
		TextView hint = (TextView) findViewById(R.id.cashtrade_kyclogin_newuser_hint);
		TextView phoneTitle = (TextView) findViewById(R.id.cashtrade_kyclogin_phone_title);
		TextView sendMsgBtn = (TextView) findViewById(R.id.cashtrade_kyclogin_sendmsg_btn);
		TextView veriTitle = (TextView) findViewById(R.id.cashtrade_kyclogin_veri_title);

		newuser_title.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCLOGIN_NEWUSER_TITLE));
		title.setText(getUiInfo()
				.getTextByName(UiInfo.CASHTRADE_KYCLOGIN_TITLE));
		hint.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCLOGIN_NEWUSER_HINT));
		phoneTitle.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCLOGIN_PHONE_TITLE));
		sendMsgBtn.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCLOGIN_SENDMSG_BTN));
		veriTitle.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCLOGIN_VERI_TITLE));
		mCnlBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
		mRegBtn.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCLOGIN_REG_BTN));
		mLoginBtn.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCLOGIN_LOGIN_BTN));
	}

	private static void dLog(@Nonnull String logStr) {
		if (DEBUG_FLAG) {
//			Log.d(LOG_TAG, logStr);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			String str = data.getStringExtra("num");
			if (requestCode == 1) {
				phoneNoText.setText(str);
			}else if(requestCode ==2){
				veriCode.setText(str);
			}
		}
	}
}
