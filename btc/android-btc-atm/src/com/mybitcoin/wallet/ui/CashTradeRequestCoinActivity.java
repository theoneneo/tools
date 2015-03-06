/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.uri.BitcoinURI;
import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.Qr;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public class CashTradeRequestCoinActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeRequestCoinActivity";
    private static final boolean DEBUG_FLAG = true;

    private static int TIMEOUT_SEC = 150; // 超时时间150s

    private String mCashInputStr;
    private String mBtcCaculatedStr;

    private TextView cnlBtn;
    private TextView cfmBtn;
    private TextView cashView;
    private TextView btcView;
    private ImageView qrView;

    private CashTradeInfoLog mCashTradeInfoLog;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_request_coin);

        cnlBtn = (TextView) findViewById(R.id.cashtrade_requestcoin_cnl_btn);
        cnlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(TradeModeActivity.class);
            }
        });

        cfmBtn = (TextView) findViewById(R.id.cashtrade_requestcoin_cfm_btn);
        cfmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCashTradeInfoLog.startActivityFromActivity(CashTradeRequestCoinActivity.this, CashTradeCheckPaymentActivity.class);
            }
        });

        cashView = (TextView) findViewById(R.id.cashtrade_requestcoin_cash_amount);
        btcView = (TextView) findViewById(R.id.cashtrade_requestcoin_btc_amount);
        qrView = (ImageView) findViewById(R.id.cashtrade_requestcoin_qr);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WalletActivityTimeoutController.getInstance().setTimeout(TIMEOUT_SEC);

        cfmBtn.setVisibility(View.INVISIBLE); // 初始化不显示“下一步”按钮

        // 在此进入界面时，清空状态输入
        mCashInputStr = null;
        mBtcCaculatedStr = null;

        // 清空mCashTradeInfoLog
        mCashTradeInfoLog = null;

        String tradeID = CashTradeInfoLog.getTradeIdFromIntent(this);
        if (tradeID == null) {
            dLog("the intent does not contain INTENT_EXTRA_TRADE_ID, go back to WelcomePage");
            gotoActivity(WelcomePageActivity.class);
        } else {
            mCashTradeInfoLog = new CashTradeInfoLog(getContentResolver(), tradeID);
            mCashInputStr = mCashTradeInfoLog.getCashStr();
            mBtcCaculatedStr = mCashTradeInfoLog.getBtcStr();

            dLog("CashTradeInput: " + mCashInputStr
                    + ", BtcCaculated: " + mBtcCaculatedStr + " received from CashTradeInputActivity");

            updateViews();
        }
    }

    private void updateViews() {
        btcView.setText(mBtcCaculatedStr);
        cashView.setText(mCashInputStr);

        final String bitcoinRequest = determineBitcoinRequestStr();

        // 生成二维码
        final int size = (int) (256 * getResources().getDisplayMetrics().density);
        dLog("set QR bitmap:" + bitcoinRequest);
        Bitmap qrCodeBitmap = Qr.bitmap(bitcoinRequest, size);
        qrView.setImageBitmap(qrCodeBitmap);

        mCashTradeInfoLog.setInitedState(bitcoinRequest);

        cfmBtn.setVisibility(View.VISIBLE); //生成二维码后显示“确定”按钮
    }

    // 产生比特币付款字符串
    private String determineBitcoinRequestStr() {
        final Address address = getWalletApplication().determineSelectedAddress();
        final BigInteger amount = getAmount();

        final StringBuilder uri = new StringBuilder(BitcoinURI.convertToBitcoinURI(address, amount, null, null));

        return uri.toString();
    }

    // 将String类型的mBtcCaculated内容转化为BigInteger
    private BigInteger getAmount() {
        return GenericUtils.toNanoCoins(mBtcCaculatedStr.trim(), 0);
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_requestcoin_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_requestcoin_title);
        TextView hint = (TextView) findViewById(R.id.cashtrade_requestcoin_hint);
        TextView btcTitle = (TextView) findViewById(R.id.cashtrade_requestcoin_btc_title);
        TextView cashTitle = (TextView) findViewById(R.id.cashtrade_requestcoin_cash_title);
        TextView btcAbb = (TextView) findViewById(R.id.cashtrade_requestcoin_btc_abb);
        TextView cashAbb = (TextView) findViewById(R.id.cashtrade_requestcoin_cash_abb);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_REQUESTCOIN_TITLE));
        hint.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_REQUESTCOIN_HINT));
        btcTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_REQUESTCOIN_BTC_TITLE));
        cashTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_REQUESTCOIN_CASH_TITLE));
        btcAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_BTC_ABB));
        cashAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_CASH_ABB));
        cnlBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
        cfmBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CFM_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
