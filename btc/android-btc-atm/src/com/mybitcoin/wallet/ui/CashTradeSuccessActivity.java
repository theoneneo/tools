/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.KS80TSDK;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lw.db.FileUtils;
import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.EnvironmentMonitor;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.WalletUtils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CashTradeSuccessActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeSuccessActivity";
    private static final boolean DEBUG_FLAG = true;

    private CashTradeInfoLog mCashTradeInfoLog;

    private TextView mBtcContent;
    private TextView mCashContent;
    private TextView mExchangeRateContent;
    private TextView mCfmBtn;

    private String mAtmAddr;
    private long mTime;
    private String mBtcAmount;
    private String mCashAmount;
    private String mExchangeRate;
    private String mHandlingChargeProportion;
    private String mPayerAddr;

    private KS80TSDK mKS80TSDK;

    public static int statusLW = 0;
    
    Response.Listener<JSONObject> mRspListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            dLog("CpuInfoThread: Response is " + response.toString());
        }
    };

    Response.ErrorListener mErrListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            dLog("CpuInfoThread: " + error.toString());
        }
    };

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_success);

        mAtmAddr = WalletUtils.pickOldestKey(getWalletApplication().getWallet()).toAddress(Constants.NETWORK_PARAMETERS).toString();

        mBtcContent = (TextView) findViewById(R.id.cashtrade_success_btc_content);

        mCashContent = (TextView) findViewById(R.id.cashtrade_success_cash_content);

        mExchangeRateContent = (TextView) findViewById(R.id.cashtrade_success_rate_content);

        mCfmBtn = (TextView) findViewById(R.id.cashtrade_success_cfm_btn);
        mCfmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(WelcomePageActivity.class);
            }
        });

        mKS80TSDK = new KS80TSDK();
        mKS80TSDK.setOnPrintListener(new KS80TSDK.onPrintListener() {
            @Override
            public void onFinished() {
                dLog("PRINT CASH OUT SLIP SUCCEED");
                mKS80TSDK.end();
            }

            @Override
            public void onError(Exception e) {
                // 打印出钞凭条失败，跳转至交易失败页面
                dLog("PRINT CASH OUT SLIP FAILED, ERROR: " + e.toString());
                mKS80TSDK.end();
                mCashTradeInfoLog.startActivityFromActivity(CashTradeSuccessActivity.this, CashTradeFailureActivity.class);
            }

			@Override
			public void onComRead(byte[] rxbuffer, int size) {
				// TODO Auto-generated method stub
				
			}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        WalletActivityTimeoutController.getInstance().setTimeout(60);

        // 清空各变量和各Views
        mTime = -1;
        mBtcAmount = null;
        mCashAmount = null;
        mExchangeRate = null;
        mHandlingChargeProportion = null;
        mPayerAddr = null;

        // 清空mCashTradeInfoLog
        mCashTradeInfoLog = null;

        mBtcContent.setText("");
        mCashContent.setText("");
        mExchangeRateContent.setText("");

        String tradeID = CashTradeInfoLog.getTradeIdFromIntent(this);
        if (tradeID == null) {
            dLog("the intent does not contain INTENT_EXTRA_TRADE_ID, go back to WelcomePage");
            gotoActivity(WelcomePageActivity.class);
        } else {
            mCashTradeInfoLog = new CashTradeInfoLog(getContentResolver(), tradeID);
            dLog("the intent does not contain INTENT_EXTRA_TRADE_ID, jack's id!!");
            mCashTradeInfoLog.setCheckedOutState();

            mTime = System.currentTimeMillis();
            mBtcAmount = mCashTradeInfoLog.getBtcStr();
            mCashAmount = mCashTradeInfoLog.getCashStr();
            mExchangeRate = mCashTradeInfoLog.getExchangeRateStr();
            mHandlingChargeProportion = mCashTradeInfoLog.getHandlingChargeProportionStr();
            mPayerAddr = mCashTradeInfoLog.getPayerAddrStr();

            mBtcContent.setText(mBtcAmount);
            mCashContent.setText(mCashAmount);
            mExchangeRateContent.setText(mExchangeRate);

            // 打印出钞成功凭条
            mKS80TSDK.printCashOutSlip(mCashTradeInfoLog.getCashStr(), true);
            mKS80TSDK.begin();

            SucceedTransInfoThread succeedTransInfoThread = new SucceedTransInfoThread();
            succeedTransInfoThread.start();

//            sendTradeSuccessSms();
        }
    }

    private class SucceedTransInfoThread extends Thread {
        
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
        private static final String DIRECTION_SELL_COINS = "sell_coins";

        private static final int TRADE_STATUS_SUCCESS = 0;
        private static final int TRADE_STATUS_FAILURE = 1;

        private RequestQueue mQueue;
        
        public SucceedTransInfoThread() {
            mQueue = Volley.newRequestQueue(getApplicationContext());
        }

        Response.Listener<JSONObject> rspListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dLog("SucceedTransInfoThread: Response is " + response.toString());
            }
        };

        Response.ErrorListener errListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dLog("SucceedTransInfoThread: " + error.toString());
            }
        };

        @Override
        public void run() {

            try {
                JSONObject postBody = new JSONObject();
                postBody.put(ATM_NAME, mAtmAddr);
                postBody.put(PAYER_ADDR, mPayerAddr);
                postBody.put(TIME, getCurrentDateAndTimeStr());
                postBody.put(TRANS_DIRECTION, DIRECTION_SELL_COINS);
                postBody.put(BTC_AMOUNT, mBtcAmount);
                postBody.put(CASH_AMOUNT, mCashAmount);
                postBody.put(EXCHANGE_RATE, mExchangeRate);
                postBody.put(HANDLING_CHARGE_PROPORTION, mHandlingChargeProportion);
                if(statusLW == 2){
                    postBody.put(TRADE_STATUS, 2);
//                    
//                    FileUtils.append("STATUS.txt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\n");
//                    FileUtils.append("STATUS.txt", "ATM_NAME:"+mAtmAddr+"\n");
//                    FileUtils.append("STATUS.txt", "PAYER_ADDR:"+mPayerAddr+"\n");
//                    FileUtils.append("STATUS.txt", "TRANS_DIRECTION:"+DIRECTION_BUY_COINS+"\n");
//                    FileUtils.append("STATUS.txt", "BTC_AMOUNT:"+mBtcAmount+"\n");
//                    FileUtils.append("STATUS.txt", "CASH_AMOUNT:"+mCashAmount+"\n");
//                    FileUtils.append("STATUS.txt", "EXCHANGE_RATE:"+ExchangeRatesFragment.rate+"\n");
//                    FileUtils.append("STATUS.txt", "HANDLING_CHARGE_PROPORTION:"+mHandlingChargeProportion+"\n");
//                    FileUtils.append("STATUS.txt", "TRADE_STATUS:"+2+"\n");
//                    FileUtils.append("STATUS.txt", "\n");
                    statusLW =0;
                }
                else{
                       postBody.put(TRADE_STATUS, TRADE_STATUS_SUCCESS);
//                       
//                       FileUtils.append("STATUS.txt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\n");
//                       FileUtils.append("STATUS.txt", "ATM_NAME:"+mAtmAddr+"\n");
//                       FileUtils.append("STATUS.txt", "PAYER_ADDR:"+mPayerAddr+"\n");
//                       FileUtils.append("STATUS.txt", "TRANS_DIRECTION:"+DIRECTION_BUY_COINS+"\n");
//                       FileUtils.append("STATUS.txt", "BTC_AMOUNT:"+mBtcAmount+"\n");
//                       FileUtils.append("STATUS.txt", "CASH_AMOUNT:"+mCashAmount+"\n");
//                       FileUtils.append("STATUS.txt", "EXCHANGE_RATE:"+ExchangeRatesFragment.rate+"\n");
//                       FileUtils.append("STATUS.txt", "HANDLING_CHARGE_PROPORTION:"+mHandlingChargeProportion+"\n");
//                       FileUtils.append("STATUS.txt", "TRADE_STATUS:"+TRADE_STATUS_SUCCESS+"\n");
//                       FileUtils.append("STATUS.txt", "\n");
                }
//                Toast.makeText(CashTradeSuccessActivity.this, "TRADE_STATUS:"+statusLW, Toast.LENGTH_LONG).show();
                
                
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, postBody, rspListener, errListener);

                dLog("SucceedTransInfoThread: POST is preparing to send to: " + URL + ", JSON: " + postBody.toString());
                mQueue.add(request);
            } catch (JSONException e) {
                dLog("SucceedTransInfoThread: error when handling json");
            }
        }

        private String getCurrentDateAndTimeStr() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(mTime);
            return formatter.format(date);
        }
    }

    public void sendTradeSuccessSms() {
        String baseUrl = getSettingInfo().getSmsPlatformUrl();
        String smsTag = "&smsText=";

        /**
         * 短信模板：
         *
         * 贵平台<atm_name>于<time>发生一笔卖出比特币交易，交易额为卖出比特币<btc_amount>，兑换人民币<cash_amount>，即时汇率为<exchange_rate>，手续费率为<handling_charge_proportion>。
         * 其中：
         *   atm_public_key:                ATM机的公钥
         *   time:                          交易发生的时间，格式为：2014-06-01 12:00:00
         *   direction:                     “卖出比特币“或“买入比特币”，二选一
         *   amount:                        交易总额，以比特币为单位，精确到小数点后6位
         *   exchange_rate:                 交易汇率，法币：比特币，精确到小数点后2位
         *   handling_charge_proportion:    手续费率，1.05表示手续费为5%
         */

        // 将模板中的<xxx>标签替换为实际数值
        String content = getUiInfo().getTextByName(UiInfo.cointrade_tradesuccess_sms_template);
        content = content.replaceAll("<atm_name>", mAtmAddr);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"); // 注意冒号在URL中为非法字符，故使用“-”代替冒号
        Date date = new Date(mTime);
        String dateStr = formatter.format(date);
        content = content.replaceAll("<time>", dateStr);

        content = content.replaceAll("<btc_amount>", mBtcAmount);
        content = content.replaceAll("<cash_amount>", mCashAmount);
        content = content.replaceAll("<exchange_rate>", mExchangeRate);
        content = content.replaceAll("<handling_charge_proportion>", mHandlingChargeProportion);

        String smsMobileArrStr = getSettingInfo().getSmsMobileArrStr();
        String[] smsMobileArr = smsMobileArrStr.split(",");
        if (smsMobileArr == null) {
            dLog("sending sms error: smsMobileArr is null");
            return;
        }

        for (String mobile : smsMobileArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(baseUrl).append(mobile).append(smsTag).append(content);

            String sms = sb.toString();

            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(sms);

            try {
                client.execute(httpPost);
                dLog("Sending SMS via HTTP: " + sms + ", destination: " + mobile);
            } catch (IOException e) {
                dLog("Error when sending SMS via HTTP: " + sms + ", destination: " + mobile + ", error: " + e.toString());
            }
        }
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_success_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_success_title);
        TextView btcTitle = (TextView) findViewById(R.id.cashtrade_success_btc_title);
        TextView cashTitle = (TextView) findViewById(R.id.cashtrade_success_cash_title);
        TextView rateTitle = (TextView) findViewById(R.id.cashtrade_success_rate_title);
        TextView btcAbb = (TextView) findViewById(R.id.cashtrade_success_btc_abb);
        TextView cashAbb = (TextView) findViewById(R.id.cashtrade_success_cash_abb);
        TextView rateAbb = (TextView) findViewById(R.id.cashtrade_success_rate_abb);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SUCCESS_TITLE));
        btcTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SUCCESS_BTC_TITLE));
        cashTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SUCCESS_CASH_TITLE));
        rateTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SUCCESS_RATE_TITLE));
        btcAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_BTC_ABB));
        cashAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_CASH_ABB));
        rateAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_RATE_ABB));
        mCfmBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CFM_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
