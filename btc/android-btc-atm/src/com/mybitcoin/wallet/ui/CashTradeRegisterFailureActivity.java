/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import javax.annotation.Nonnull;


public class CashTradeRegisterFailureActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeRegisterFailureActivity";
    private static final boolean DEBUG_FLAG = true;

    TextView mCfmBtn;
    TextView mRetryBtn;
    int regresult;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_kyc_register_fail);
 
        mCfmBtn = (TextView) findViewById(R.id.cashtrade_kycregresult_cfm_btn);
        mCfmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(WelcomePageActivity.class);
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

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_kycregresult_tradetype);
//        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));
        tradeType.setText("KYC Verify");
        
        TextView success_title = (TextView) findViewById(R.id.cashtrade_kycregresult_fail_title);
        TextView waiting_title = (TextView) findViewById(R.id.cashtrade_kycregresult_waiting_title);
        success_title.setText("Registeration failed! ");
        waiting_title.setText("Please register again!");
        mCfmBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CFM_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
