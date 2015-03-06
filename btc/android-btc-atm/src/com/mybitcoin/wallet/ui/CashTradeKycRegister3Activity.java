/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import javax.annotation.Nonnull;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.Tools;


public class CashTradeKycRegister3Activity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeKycRegister3Activity";
    private static final boolean DEBUG_FLAG = true;
    private EditText phoneNoText;
//    private String veriCode;

    TextView mLoginBtn;
    TextView mCnlBtn;

    TextView cashtrade_kycregister3_sendmsg_btn;
    
    EditText veriNoText;
    
    
    private RequestQueue mQueue;
    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_kyc_register3);
        mQueue = Volley.newRequestQueue(getWalletApplication().getApplicationContext());
        
        veriNoText = (EditText)findViewById(R.id.cashtrade_kycregister3_veri_txt);
		veriNoText.setInputType(InputType.TYPE_NULL);
		veriNoText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(CashTradeKycRegister3Activity.this,
						KeyboardActivity.class);
				intent.putExtra("index", 2);
				intent.putExtra("num", veriNoText.getText().toString());
				startActivityForResult(intent, 2);
			}

		});
		veriNoText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if (arg1) {
					Intent intent = new Intent(CashTradeKycRegister3Activity.this,
							KeyboardActivity.class);
					intent.putExtra("index", 2);
					intent.putExtra("num", veriNoText.getText().toString());
					startActivityForResult(intent, 2);
				}
			}
		});        
        mCnlBtn = (TextView) findViewById(R.id.cashtrade_kycregister3_cnl_btn);
        mCnlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(WelcomePageActivity.class);
            }
        });

        cashtrade_kycregister3_sendmsg_btn = (TextView)findViewById(R.id.cashtrade_kycregister3_sendmsg_btn);
        cashtrade_kycregister3_sendmsg_btn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new Thread(new SendMsgThread2()).start();
			}
        });
        
        mLoginBtn = (TextView) findViewById(R.id.cashtrade_kycregister3_login_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gotoActivity(CashTradeKycSuccessActivity.class);
            	//不验证，直接上传
//            	if (veriNoText.getText().toString().trim().equals(veriCode)){
            		new Thread(new RegisterThread2()).start();
//            	} else {
            		//验证码错误
            		
//            	}
            }
        });

        phoneNoText = (EditText) findViewById(R.id.cashtrade_kycregister3_phoneno);
		phoneNoText.setInputType(InputType.TYPE_NULL);
		phoneNoText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(CashTradeKycRegister3Activity.this,
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
					Intent intent = new Intent(CashTradeKycRegister3Activity.this,
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
                    dLog("error when calling Integer.parseInt(inputStr): " + e.toString() + ", and delete the last char");

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

    
    private static final int PROCESS_REGISTER = 1;
    Handler mHandler = new Handler(){
    	@Override
    	public void dispatchMessage(Message msg) {
    		// TODO Auto-generated method stub
    		super.dispatchMessage(msg);
    		switch(msg.what){
    		case PROCESS_REGISTER:
    			
    			break;
    		}
    	}
    };
    //发送注册请求
    class RegisterThread2 implements Runnable{
    	AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler(){
    		@Override
    		public void onStart() {
    			dLog("Start");
    		}

    		@Override
    		public void onSuccess(int statusCode, Header[] headers, byte[] response) {
    			if(response != null)
    				dLog(new String(response));
    			if (CashTradeKycRegister1Activity.userIcon != null){
    				CashTradeKycRegister1Activity.userIcon.delete();
    			}
    			if (CashTradeKycRegister2Activity.userCard != null){
    				CashTradeKycRegister2Activity.userCard.delete();
    			}
    			String responseStr = new String(response);
    			if (responseStr.equals("OK")){
    				gotoActivity(CashTradeRegisterSuccessActivity.class);
    			}else{
    				gotoActivity(CashTradeRegisterFailureActivity.class);
    			}

    		}

    		@Override
    		public void onFailure(int statusCode, Header[] headers,
    				byte[] errorResponse, Throwable e) {
    			if(errorResponse != null)
    				dLog(new String(errorResponse));


    		}
    		
    	};

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String URL = "http://dashboard.bitocean.com:8081/kyc_register/";
			AsyncHttpClient client = new AsyncHttpClient();
			RequestParams postBody = new RequestParams();
			postBody.put("phone_num", phoneNoText.getText().toString().trim());
            postBody.put("atm_name", publicKey);
            postBody.put("time", Tools.getTime());
            postBody.put("verify_code", veriNoText.getText().toString().trim());
            try {
            	//没有办法，两个File就是不行，一个File，一个Stream，搞定
            	ByteArrayInputStream bs = new ByteArrayInputStream(CashTradeKycRegister1Activity.byteData);
//            	String fileName = picDir + "user_icon" + System.currentTimeMillis() + ".jpg";
            	postBody.put("user_icon", bs,CashTradeKycRegister1Activity.userIcon.getAbsolutePath());
            	dLog("user_icon:" + String.valueOf(CashTradeKycRegister1Activity.userIcon.length()));
            	
            	postBody.put("user_card", CashTradeKycRegister2Activity.userCard, "image/jpeg");

				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            client.post(URL,postBody,responseHandler);
           
		}
    	
    }
   
  //用AsyncHttp发验证码请求
    class SendMsgThread2 implements Runnable{
    	AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler(){
    		@Override
    		public void onStart() {
    			dLog("Start");
    		}

    		@Override
    		public void onSuccess(int statusCode, Header[] headers, byte[] response) {
    			if(response != null)
    				dLog(new String(response));
		        TextView text = new TextView(CashTradeKycRegister3Activity.this);
		        text.setText("verify code send ok!");
		        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		        text.setBackgroundColor(Color.BLACK);
		        Toast toast = new Toast(CashTradeKycRegister3Activity.this);
		        toast.setView(text);
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();	    			
    		}

    		@Override
    		public void onFailure(int statusCode, Header[] headers,
    				byte[] errorResponse, Throwable e) {
    			if(errorResponse != null)
    				dLog(new String(errorResponse));
		        TextView text = new TextView(CashTradeKycRegister3Activity.this);
		        text.setText("verify code send error!");
		        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		        text.setBackgroundColor(Color.BLACK);
		        Toast toast = new Toast(CashTradeKycRegister3Activity.this);
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
			try{
			client.post(URL,postBody,responseHandler);
			}catch(Exception e){
				dLog("Something wrong with post");
			}
			
		}
    	
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();

        WalletActivityTimeoutController.getInstance().setTimeout(300);
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_kycregister3_tradetype);
//        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));
        tradeType.setText("KYC Verify");        

        TextView step = (TextView) findViewById(R.id.cashtrade_kycregister3_step);
        TextView step1 = (TextView) findViewById(R.id.cashtrade_kycregister3_step1);
        TextView step2 = (TextView) findViewById(R.id.cashtrade_kycregister3_step2);
        TextView step3 = (TextView) findViewById(R.id.cashtrade_kycregister3_step3);
        TextView title = (TextView) findViewById(R.id.cashtrade_kycregister3_title);
        TextView phoneTitle = (TextView) findViewById(R.id.cashtrade_kycregister3_phone_title);
        TextView sendMsgBtn = (TextView) findViewById(R.id.cashtrade_kycregister3_sendmsg_btn);
        TextView veriTitle = (TextView) findViewById(R.id.cashtrade_kycregister3_veri_title);

        step.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCREGISTER_STEP));
        step1.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCREGISTER_STEP1));
        step2.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCREGISTER_STEP2));
        step3.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCREGISTER_STEP3));
        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCREGISTER3_TITLE));
        phoneTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCREGISTER3_PHONE_TITLE));
        sendMsgBtn.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCREGISTER3_SENDMSG_BTN));
        veriTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCREGISTER3_VERI_TITLE));
        mLoginBtn.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCREGISTER3_LOGIN_BTN));
        mCnlBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
	}

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
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
				veriNoText.setText(str);
			}
		}
	}
}
