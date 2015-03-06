/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import javax.annotation.Nonnull;


public class CashTradeKycFailureActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeKycFailureActivity";
    private static final boolean DEBUG_FLAG = true;

    TextView mCnlBtn;
    TextView mRetryBtn;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_kyc_failure);

        mCnlBtn = (TextView) findViewById(R.id.cashtrade_kycfailure_cnl_btn);
        mCnlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(WelcomePageActivity.class);
            }
        });

        mRetryBtn = (TextView) findViewById(R.id.cashtrade_kycfailure_retry_btn);
        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(CashTradeKycLoginActivity.class);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

//        WalletActivityTimeoutController.getInstance().setTimeout(30);
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_kycfailure_tradetype);
//        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));
        tradeType.setText("KYC Verify");
        
        TextView title = (TextView) findViewById(R.id.cashtrade_kycfailure_title);
        TextView retryPrefix = (TextView) findViewById(R.id.cashtrade_kycfailure_retry_prefix);
        retryPrefix.setVisibility(View.INVISIBLE);
        TextView retrySuffix = (TextView) findViewById(R.id.cashtrade_kycfailure_retry_suffix);
        retrySuffix.setVisibility(View.INVISIBLE);
        TextView retryBtn = (TextView) findViewById(R.id.cashtrade_kycfailure_retry_btn);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCFAILURE_TITLE));
//        retryPrefix.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCFAILURE_RETRY_PREFIX));
//        retrySuffix.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCFAILURE_RETRY_SUFFIX));
        mCnlBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
        retryBtn.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCFAILURE_RETRY_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
