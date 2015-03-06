/**
 *  AUTHOR: F
 *  DATE: 2014.6.6
 */

package com.mybitcoin.wallet.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import javax.annotation.Nonnull;

public class CashTradeCheckingoutActivity extends WalletActivityBase {
    private static final String LOG_TAG = "CashTradeCheckingoutActivity";
    private static final boolean DEBUG_FLAG = true;

    CashTradeInfoLog mCashTradeInfoLog;

    TextView mBtcAmount;
    TextView mCashAmount;
    TextView mRateAmount;
    TextView mCheckoutBtn;
    TextView cnlBtn;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.cash_trade_checkingout);

        mCheckoutBtn = (TextView) findViewById(R.id.cashtrade_checkingout_checkout_btn);
        mCheckoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 切换至出钞页面
                mCashTradeInfoLog.startActivityFromActivity(CashTradeCheckingoutActivity.this, CashTradeCashOutActivity.class);
            }
        });

        cnlBtn = (TextView) findViewById(R.id.cashtrade_checkingout_cnl_btn);
        cnlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(TradeModeActivity.class);
            }
        });

        mBtcAmount = (TextView) findViewById(R.id.cashtrade_checkingout_btc_amount);
        mCashAmount = (TextView) findViewById(R.id.cashtrade_checkingout_cash_amount);
        mRateAmount = (TextView) findViewById(R.id.cashtrade_checkingout_rate_amount);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 清空mCashTradeInfoLog
        mCashTradeInfoLog = null;
        mCheckoutBtn.setVisibility(View.INVISIBLE);

        String tradeID = CashTradeInfoLog.getTradeIdFromIntent(this);
        if (tradeID == null) {
            dLog("the intent does not contain INTENT_EXTRA_TRADE_ID, go back to WelcomePage");
            gotoActivity(WelcomePageActivity.class);
        } else {
            mCashTradeInfoLog = new CashTradeInfoLog(getContentResolver(), tradeID);

            String cashAmountStr = mCashTradeInfoLog.getCashStr();
            String btcAmountStr = mCashTradeInfoLog.getBtcStr();
            String rateAmountStr = mCashTradeInfoLog.getExchangeRateStr();

            mCashAmount.setText(cashAmountStr);
            mBtcAmount.setText(btcAmountStr);
            mRateAmount.setText(rateAmountStr);

            dLog("Cash: " + cashAmountStr
                    + ", Btc: " + btcAmountStr + ", Exchangerate: " + rateAmountStr + " received from CashTradeShowScanQrResultActivity");

            mCheckoutBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateUiInfo() {
        TextView tradeType = (TextView) findViewById(R.id.cashtrade_checkingout_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_checkingout_title);
        TextView hint = (TextView) findViewById(R.id.cashtrade_checkingout_hint);
        TextView btcTitle = (TextView) findViewById(R.id.cashtrade_checkingout_btc_title);
        TextView cashTitle = (TextView) findViewById(R.id.cashtrade_checkingout_cash_title);
        TextView rateTitle = (TextView) findViewById(R.id.cashtrade_checkingout_rate_title);
        TextView btcAbb = (TextView) findViewById(R.id.cashtrade_checkingout_btc_abb);
        TextView cashAbb = (TextView) findViewById(R.id.cashtrade_checkingout_cash_abb);
        TextView rateAbb = (TextView) findViewById(R.id.cashtrade_checkingout_rate_abb);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKINGOUT_TITLE));
        hint.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKINGOUT_HINT));
        btcTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKINGOUT_BTC_TITLE));
        cashTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKINGOUT_CASH_TITLE));
        rateTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKINGOUT_RATE_TITLE));
        btcAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_BTC_ABB));
        cashAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_CASH_ABB));
        rateAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_RATE_ABB));
        mCheckoutBtn.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKINGOUT_CHECKOUT));
        cnlBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
