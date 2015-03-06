/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import javax.annotation.Nonnull;


public class CashTradeCheckPaymentTimeoutActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeCheckPaymentTimeoutActivity";
    private static final boolean DEBUG_FLAG = true;

    private CashTradeInfoLog mCashTradeInfoLog;

    private TextView cfmBtn;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_check_payment_timeout);

        cfmBtn = (TextView) findViewById(R.id.cashtrade_checkpaymenttimeout_cfm_btn);
        cfmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(WelcomePageActivity.class);
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
        }
    }

    @Override
    public void updateUiInfo() {
        TextView tradeType = (TextView) findViewById(R.id.cashtrade_checkpaymenttimeout_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_checkpaymenttimeout_title);
        TextView hint = (TextView) findViewById(R.id.cashtrade_checkpaymenttimeout_hint);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKPAYMENTTIMEOUT_TITLE));
        hint.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKPAYMENTTIMEOUT_HINT));

        cfmBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CFM_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
