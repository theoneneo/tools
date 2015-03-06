package com.mybitcoin.wallet.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.content.ComponentName;
import android_serialport_api.KS80TSDK;
import android_serialport_api.KS80TSDK.onPrintListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mybitcoin.wallet.Configuration;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.EnvironmentMonitor;
import com.mybitcoin.wallet.ExchangeRatesProvider;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.environment.SettingInfo;
import com.mybitcoin.wallet.environment.UiInfo;
//import com.mybitcoin.wallet.ui.CopyOfTransSuccessActivity.TransInfoThread;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.TransactionLog;
import com.mybitcoin.wallet.util.WalletUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nonnull;

/**
 * Created by zhuyun on 14-4-7.
 */
public class CopyOfTransFailedActivity extends WalletActivityTimeoutBase {
	 private static final String LOG_TAG = "CopyOfTransFailedActivity";
	    private static final boolean DEBUG_FLAG = true;
//    private TextView mTime;
    private static final int msgKey1 = 1;
    private Thread timerThread;
    private boolean timerFlag;
    private static Logger log = LoggerFactory.getLogger(CopyOfTransFailedActivity.class);
    private String strQRAddress;
    private String transType;
    private int coinAmount=0;
    private String bitcoinAmount="";
    String tele = "";
    private long beginTime;
    private WalletApplication application;
    private TextView  btnConfirm;
    
    KS80TSDK mks80t;
    String mAtmAddr;
    private Configuration config;
    private BigInteger rateBase = GenericUtils.ONE_BTC;
    @Override
    protected void onCreate(final Bundle saveInstanceState){
        
        super.onCreate(saveInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
       
        mAtmAddr = WalletUtils.pickOldestKey(getWalletApplication().getWallet()).toAddress(Constants.NETWORK_PARAMETERS).toString();
//     	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        //setContentView(R.layout.transfail);
        
        
        setLayout(R.layout.transfail);
        
//     	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);

        application = (WalletApplication) getApplication();
        config = application.getConfiguration();
        ExchangeRatesProvider.ExchangeRate exchangeRate = config.getCachedExchangeRate();
        WalletUtils.localValue(rateBase, exchangeRate.rate);
        
//    	mTime = (TextView)findViewById(R.id.timer);
        beginTime = System.currentTimeMillis();
//     	CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",beginTime);
//     	mTime.setText(sysTimerStr);
        timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();
//     	log.info("timerThread start.");
        tele  = getResources().getString(R.string.server_telephone);

        //获取交易类型和二维码地址
        Intent intent = getIntent();
        transType = intent.getStringExtra("transType");
//        log.info("transType is :"+transType);
        strQRAddress = intent.getStringExtra("qrAddress");
//        log.info("strQRAddress is :"+strQRAddress);

        coinAmount = intent.getIntExtra("coinAmount",0);
//        log.info("coinAmount is :"+coinAmount);
        bitcoinAmount = intent.getStringExtra("bitcoinAmount");
        log.info("bitcoinAmount is :"+bitcoinAmount);

        //输出到日志文件
        TransactionLog.writeLogtoFile(transType, strQRAddress, String.valueOf(coinAmount), bitcoinAmount, "失败");

        //发送短信
        application.sendMessage("失败");


//        TextView btnPrintInvoice = (TextView)findViewById(R.id.btn_printinvoice);
        btnConfirm = (TextView)findViewById(R.id.btn_confirm);
        btnConfirm.setVisibility(View.INVISIBLE);

       /* btnPrintInvoice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                    printTransInvoice(strQRAddress,bitcoinAmount,String.valueOf(coinAmount));

            }
        });*/
        btnConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(CopyOfTransFailedActivity.this, WelcomePageActivity.class));
                finish();

            }
        });
        
        //new KS80TSDK 
        mks80t = new KS80TSDK();
        
		mks80t.setOnPrintListener(new onPrintListener(){		//监听KS80TSDK

			@Override
			public void onFinished() {
				// 这里写监听到出钞结束后的处理
				//byte[] finishflag = {(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff};
				//onDataReceived(finishflag,4);
				mHandler.sendEmptyMessage(100);
			}

			@Override
			public void onError(Exception e) {
				// 这里写监听到初始化串口出错后的处理
				//DisplayError(e.toString());
			}

			@Override
			public void onComRead(byte[] rxbuffer, int size) {
				// TODO Auto-generated method stub
				
			}

		});

        printTransInvoice(strQRAddress,bitcoinAmount,String.valueOf(coinAmount));
        mks80t.begin();
    }


        /*
     * print invoice of  transaction
     *
     */
    private void printTransInvoice(String address,String bitcoinAmount,String coinAmount){
    	log.info("print invoice of  transaction");
    	mks80t.printTransInvoice(address, bitcoinAmount, coinAmount,tele,false) ;               
    }

    @Override
    protected void onStop(){

        timerFlag = false;
        mks80t.end();
        super.onStop();


    }

    public class TimeThread extends Thread{
        @Override
        public void run(){
            while(timerFlag){
                try{
                    Thread.sleep(1000);
                    Message message = new Message();
                    message.what = msgKey1;
                    mHandler.sendMessage(message);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case msgKey1:
                    long sysTime = System.currentTimeMillis();
//                    CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",sysTime);
//                    mTime.setText(sysTimerStr);
//                    log.info("time2："+sysTimerStr);
                    if((sysTime - beginTime) > 60000){
                        startActivity(new Intent(CopyOfTransFailedActivity.this,WelcomePageActivity.class));
                        CopyOfTransFailedActivity.this.finish();
                    }
                    break;
                case 555:
                	init();
                	break;
                case 100:
                	btnConfirm.setVisibility(View.VISIBLE);
                	FailedTransInfoThread failedtransInfoThread = new FailedTransInfoThread();
                    failedtransInfoThread.start();                	
                	break;
                default:
                    break;
            }
        }
    };


    protected void onResume() {
		super.onResume();
		init();
		new Thread(new UpdateText()).start();
	};
	private void init(){
		UiInfo uiInfo = new UiInfo(this);
		
		TextView tvTitle = (TextView)findViewById(R.id.tvTitle);
		TextView tvHint = (TextView)findViewById(R.id.tvHint);
		tvTitle.setText(uiInfo.getTextByName(UiInfo.cointrade_failure_title));
		tvHint.setText(uiInfo.getTextByName(UiInfo.cointrade_failure_hint));
	
		TextView tvOk = (TextView)findViewById(R.id.btn_confirm);
		
		tvOk.setText(uiInfo.getTextByName(UiInfo.COMMON_CFM_BTN));
	}
	 @Override
	    protected void onDestroy() {
	
	    	super.onDestroy();
	    	flag1 = false;
	    }
	    boolean flag1 = true;
	    class UpdateText implements Runnable{
			@Override
			public void run() {
				while(flag1){
					try {
						Thread.sleep(10*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mHandler.sendEmptyMessage(555);
				}
			}
	    }
		@Override
		public void updateUiInfo() {
			// TODO Auto-generated method stub
			
		}

		private class FailedTransInfoThread extends Thread {
	        private static final String URL = EnvironmentMonitor.URL_BASE + "/transaction/";
	        private static final String ATM_NAME = "atm_name";
	        private static final String TIME = "time";
	        private static final String TRANS_DIRECTION = "direction";
	        private static final String BTC_AMOUNT = "btc_amount";
	        private static final String CASH_AMOUNT = "cash_amount";
	        private static final String EXCHANGE_RATE = "exchange_rate";
	        private static final String HANDLING_CHARGE_PROPORTION = "handling_charge_proportion";
	        private static final String PAYER_ADDR = "payer_addr";
	        private static final String TRADE_STATUS = "trade_status";


	        private static final String DIRECTION_BUY_COINS = "buy_coins";

	        private static final int TRADE_STATUS_FAILURE = 1;

	        private RequestQueue mQueue;

	        public FailedTransInfoThread() {
	            mQueue = Volley.newRequestQueue(getApplicationContext());
	        }

	        Response.Listener<JSONObject> rspListener = new Response.Listener<JSONObject>() {
	            @Override
	            public void onResponse(JSONObject response) {
	                dLog("FailedTransInfoThread: Response is " + response.toString());
	            }
	        };

	        Response.ErrorListener errListener = new Response.ErrorListener() {
	            @Override
	            public void onErrorResponse(VolleyError error) {
	               dLog("FailedTransInfoThread: " + error.toString());
	            }
	        };

	        @Override
	        public void run() {
	            try {
	                JSONObject postBody = new JSONObject();
	                postBody.put(ATM_NAME, mAtmAddr);
	                postBody.put(PAYER_ADDR, strQRAddress);
	                postBody.put(TIME, getCurrentDateAndTimeStr());
	                postBody.put(TRANS_DIRECTION, DIRECTION_BUY_COINS);
	                
	                String amount = bitcoinAmount.substring(4);
	                Double amountD = Double.valueOf(amount);	                
	                amountD = amountD/1000;
	                postBody.put(BTC_AMOUNT, String.valueOf(amountD));
	                postBody.put(CASH_AMOUNT, String.valueOf(coinAmount));
	                postBody.put(EXCHANGE_RATE,  ExchangeRatesFragment.rate);
	                SettingInfo setting = new SettingInfo(CopyOfTransFailedActivity.this);
	                postBody.put(HANDLING_CHARGE_PROPORTION, String.valueOf(setting.getHandlingChargeProportion()));
	                postBody.put(TRADE_STATUS, TRADE_STATUS_FAILURE);

	                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, postBody, rspListener, errListener);

	                dLog("FailedTransInfoThread: POST is preparing to send to: " + URL + ", JSON: " + postBody.toString());
	                mQueue.add(request);
	            } catch (JSONException e) {
	                dLog("FailedTransInfoThread: error when handling json");
	            }
	        }

	        private String getCurrentDateAndTimeStr() {
	            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            Date date = new Date();
	            return formatter.format(date);
	        }
	    }
		
		  private static void dLog(@Nonnull String logStr) {
		        if (DEBUG_FLAG == true) {
//		            Log.d(LOG_TAG, logStr);
		        }
		    }
}
