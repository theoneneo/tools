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


public class CashTradeKycSuccessActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeKycSuccessActivity";
    private static final boolean DEBUG_FLAG = true;

    private TextView mCnlBtn;
    private TextView mCfmBtn;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_kyc_success);

        mCnlBtn = (TextView) findViewById(R.id.cashtrade_kycsuccess_cnl_btn);
        mCnlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(WelcomePageActivity.class);
            }
        });

        mCfmBtn = (TextView) findViewById(R.id.cashtrade_kycsuccess_cfm_btn);
        mCfmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(TradeModeActivity.class);
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

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_kycsuccess_tradetype);
//        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));
        tradeType.setText("KYC Verify");

        TextView title = (TextView) findViewById(R.id.cashtrade_kycsuccess_title);
        TextView infoTitle = (TextView) findViewById(R.id.cashtrade_kycsuccess_info_title);
        infoTitle.setVisibility(View.INVISIBLE);
        TextView nameTitle = (TextView) findViewById(R.id.cashtrade_kycsuccess_name_title);
        nameTitle.setVisibility(View.INVISIBLE);
        
        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCSUCCESS_TITLE));
        infoTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCSUCCESS_INFO_TITLE));
        nameTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_KYCSUCCESS_NAME_TITLE));
        mCnlBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
        mCfmBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CFM_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
