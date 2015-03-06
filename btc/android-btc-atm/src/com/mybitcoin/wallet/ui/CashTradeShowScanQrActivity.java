/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import javax.annotation.Nonnull;

public class CashTradeShowScanQrActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeShowScanQrActivity";
    private static final boolean DEBUG_FLAG = true;


    private static final int REQUEST_CODE_SCAN = 0;

    private TextView mHintView;
    private TextView mCnlBtn;
    private TextView mRescanBtn;

    private String mHintDefault;
    private String mHintUnconfirmed;
    private String mHintUnknown;
    private String mHintForbidden;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        startActivityForResult(new Intent(this, CashTradeScanQrActivity.class), REQUEST_CODE_SCAN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setLayout(R.layout.cash_trade_show_scan_qr);

        mCnlBtn = (TextView) findViewById(R.id.cashtrade_showscanqr_cnl_btn);
        mCnlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(TradeModeActivity.class);
            }
        });

        mRescanBtn = (TextView) findViewById(R.id.cashtrade_showscanqr_rescan_btn);
        mRescanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(CashTradeShowScanQrActivity.this, CashTradeScanQrActivity.class), REQUEST_CODE_SCAN);
            }
        });

        mHintView = (TextView) findViewById(R.id.cashtrade_showscanqr_hint);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        mHintView.setText(mHintDefault);

        if (requestCode == REQUEST_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            final String input = intent.getStringExtra(CashTradeScanQrActivity.INTENT_EXTRA_RESULT);
            dLog("Scanned Qr Str is: " + input);

            String tradeID = CashTradeInfoLog.queryMatchedTradeIdByCashQr(getContentResolver(), input);
            if (tradeID == null) {
                // 启动错误页面
                dLog("Scanned Qr: " + input + " is not matched with any PAYING or PAYED state trades");
                mHintView.setText(mHintUnknown);
            } else { // 找到匹配的TradeID
                CashTradeInfoLog info = new CashTradeInfoLog(getContentResolver(), tradeID);
                if (info.getCurrentState().equals(CashTradeInfoLog.STATE_PAYED)) {
                    // 启动提款页面
                    info.startActivityFromActivity(CashTradeShowScanQrActivity.this, CashTradeCheckingoutActivity.class);
                } else if (info.getCurrentState().equals(CashTradeInfoLog.STATE_PAYING)) {
                    // 交易尚未确认
                    mHintView.setText(mHintUnconfirmed);
                } else {
                    // 启动错误页面
                    mHintView.setText(mHintForbidden);
                }
            }
        } else {
            // 未扫描二维码，回到欢迎页面
            gotoActivity(WelcomePageActivity.class);
        }
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_showscanqr_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_showscanqr_title);
        mHintDefault = getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWSCANQR_HINT_DEFAULT);
        mHintUnconfirmed = getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWSCANQR_HINT_UNCONFIRMED);
        mHintUnknown = getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWSCANQR_HINT_UNKNOWN);
        mHintForbidden = getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWSCANQR_HINT_FORBIDDEN);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWSCANQR_TITLE));
        mCnlBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
        mRescanBtn.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWSCANQR_RESCAN_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
