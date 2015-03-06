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
import android_serialport_api.L1000SDK;
import android_serialport_api.L1000SDK.onDispenseListener;

import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import javax.annotation.Nonnull;


public class CashTradeCashOutActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeCashOutActivity";
    private static final boolean DEBUG_FLAG = true;

    private CashTradeInfoLog mCashTradeInfoLog;

    private L1000SDK mL1000SDK;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_cash_out);

        // test
        TextView successBtn = (TextView) findViewById(R.id.cashtrade_cashout_success_btn);
        successBtn.setVisibility(View.INVISIBLE);
        successBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCashTradeInfoLog.startActivityFromActivity(CashTradeCashOutActivity.this, CashTradeSuccessActivity.class);
            }
        });

        TextView failureBtn = (TextView) findViewById(R.id.cashtrade_cashout_failure_btn);
        failureBtn.setVisibility(View.INVISIBLE);
        failureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCashTradeInfoLog.startActivityFromActivity(CashTradeCashOutActivity.this, CashTradeFailureActivity.class);
            }
        });

        mL1000SDK = new L1000SDK();                                //构造L1000SDK
        mL1000SDK.setOnDispenseListener(new onDispenseListener() {    //监听L1000SDK

            @Override
            public void onDispenseFinished() {
                dLog("CASH OUT SUCCEED");
                mL1000SDK.end(); // 关闭出钞模块
                mCashTradeInfoLog.startActivityFromActivity(CashTradeCashOutActivity.this, CashTradeSuccessActivity.class);
            }

            @Override
            public void onError(Exception e) {
                dLog("CASH OUT FAILED: " + e.toString());
                mL1000SDK.end(); // 关闭出钞模块
                mCashTradeInfoLog.startActivityFromActivity(CashTradeCashOutActivity.this, CashTradeFailureActivity.class);
            }

            @Override
            public void onComRead(byte[] rxbuffer, int size) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onNotesNearEnd() {
                // TODO Auto-generated method stub
                /**
                 * 继而在onNotesNearEnd()中加入对纸币nearend的处理方式
                 * L1000在dispense过程中，如果发现纸币过少，会提示notes near end，
                 * 表现为dispense的response的第10个byte，0x30表示纸币正常，0x31表示纸币near end
                 * */
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),
//                                "notes near end", Toast.LENGTH_SHORT).show();
//                    }
//                });
                dLog("notes near end ");
                mL1000SDK.end(); // 关闭出钞模块
                CashTradeSuccessActivity.statusLW=2;
//                mCashTradeInfoLog.startActivityFromActivity(CashTradeCashOutActivity.this, CashTradeSuccessActivity.class);
//                mCashTradeInfoLog.startActivityFromActivity(CashTradeCashOutActivity.this, TradePauseActivity.class);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        WalletActivityTimeoutController.getInstance().setTimeout(60);

        // 清空mCashTradeInfoLog
        mCashTradeInfoLog = null;

        String tradeID = CashTradeInfoLog.getTradeIdFromIntent(this);
        if (tradeID == null) {
            dLog("the intent does not contain INTENT_EXTRA_TRADE_ID, go back to WelcomePage");
            gotoActivity(WelcomePageActivity.class);
        } else {
            mCashTradeInfoLog = new CashTradeInfoLog(getContentResolver(), tradeID);

            // 统计钞票张数
            int cashNum = Integer.parseInt(mCashTradeInfoLog.getCashStr())/getSettingInfo().getCashDenomination();

            dLog("START CASH OUT, DENOMINATION: " + getSettingInfo().getCashDenomination() + ", NUMBER: " + cashNum);

            // 出钞，需要确保mCashTradeInfoLog非空
            mL1000SDK.begin(cashNum);
        }
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_cashout_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_cashout_title);
        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CASHOUT_TITLE));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
