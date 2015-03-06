/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android_serialport_api.KS80TSDK;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.EnvironmentMonitor;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.WalletUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CashTradeFailureActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeFailureActivity";
    private static final boolean DEBUG_FLAG = true;

    private CashTradeInfoLog mCashTradeInfoLog;

    private TextView mCfmBtn;

    private String mAtmAddr;
    private long mTime;
    private String mBtcAmount;
    private String mCashAmount;
    private String mExchangeRate;
    private String mHandlingChargeProportion;
    private String mPayerAddr;

    private KS80TSDK mKS80TSDK;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_failure);

        mCfmBtn = (TextView) findViewById(R.id.cashtrade_failure_cfm_btn);
        mCfmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(WelcomePageActivity.class);
            }
        });

        mAtmAddr = WalletUtils.pickOldestKey(getWalletApplication().getWallet()).toAddress(Constants.NETWORK_PARAMETERS).toString();

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

        // 清空mCashTradeInfoLog
        mCashTradeInfoLog = null;

        String tradeID = CashTradeInfoLog.getTradeIdFromIntent(this);
        if (tradeID == null) {
            dLog("the intent does not contain INTENT_EXTRA_TRADE_ID, go back to WelcomePage");
            gotoActivity(WelcomePageActivity.class);
        } else {
            mCashTradeInfoLog = new CashTradeInfoLog(getContentResolver(), tradeID);
            mCashTradeInfoLog.setFailedState();

            mTime = System.currentTimeMillis();

            // 由于交易失败可能发生在不同阶段，因此下列字段可能为空，当发生时使用NULL代替
            if(mBtcAmount == null)
                mBtcAmount = "NULL";
            else
                mBtcAmount = mCashTradeInfoLog.getBtcStr();

            if(mCashAmount == null)
                mCashAmount = "NULL";
            else
                mCashAmount = mCashTradeInfoLog.getCashStr();

            if(mExchangeRate == null)
                mExchangeRate = "NULL";
            else
                mExchangeRate = mCashTradeInfoLog.getExchangeRateStr();

            if(mHandlingChargeProportion == null)
                mHandlingChargeProportion = "NULL";
            else
                mHandlingChargeProportion = mCashTradeInfoLog.getHandlingChargeProportionStr();

            if(mPayerAddr == null)
                mPayerAddr = "NULL";
            else
                mPayerAddr = mCashTradeInfoLog.getPayerAddrStr();

            // 打印出钞失败凭条
            mKS80TSDK.printCashOutSlip(mCashTradeInfoLog.getCashStr(), false);
            mKS80TSDK.begin();

            FailedTransInfoThread failedTransInfoThread = new FailedTransInfoThread();
            failedTransInfoThread.start();
        }
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
        private static final String DIRECTION_SELL_COINS = "sell_coins";

        private static final int TRADE_STATUS_SUCCESS = 0;
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
                postBody.put(PAYER_ADDR, mPayerAddr);
                postBody.put(TIME, getCurrentDateAndTimeStr());
                postBody.put(TRANS_DIRECTION, DIRECTION_SELL_COINS);
                postBody.put(BTC_AMOUNT, mBtcAmount);
                postBody.put(CASH_AMOUNT, mCashAmount);
                postBody.put(EXCHANGE_RATE, mExchangeRate);
                postBody.put(HANDLING_CHARGE_PROPORTION, mHandlingChargeProportion);
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
            Date date = new Date(mTime);
            return formatter.format(date);
        }
    }

    @Override
    public void updateUiInfo() {
        TextView tradeType = (TextView) findViewById(R.id.cashtrade_failure_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_failure_title);
        TextView hint = (TextView) findViewById(R.id.cashtrade_failure_hint);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_FAILURE_TITLE));
        hint.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_FAILURE_HINT));
        mCfmBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CFM_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
