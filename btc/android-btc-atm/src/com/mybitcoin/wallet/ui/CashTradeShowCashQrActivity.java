/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.KS80TSDK;
import com.google.bitcoin.core.Address;
import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.Qr;

import javax.annotation.Nonnull;

public class CashTradeShowCashQrActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeShowCashQrActivity";
    private static final boolean DEBUG_FLAG = true;

    private String mCashInputStr;
    private String mBtcCaculatedStr;

    private TextView fnsBtn;
    private TextView cashView;
    private TextView btcView;
    private ImageView qrView;

    private CashTradeInfoLog mCashTradeInfoLog;

    private KS80TSDK mKS80TSDK;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_show_cash_qr);

        fnsBtn = (TextView) findViewById(R.id.cashtrade_showcashqr_fns_btn);
        fnsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(TradeModeActivity.class);
            }
        });

        cashView = (TextView) findViewById(R.id.cashtrade_showcashqr_cash_amount);
        btcView = (TextView) findViewById(R.id.cashtrade_showcashqr_btc_amount);
        qrView = (ImageView) findViewById(R.id.cashtrade_showcashqr_qr);

        mKS80TSDK = new KS80TSDK();
        mKS80TSDK.setOnPrintListener(new KS80TSDK.onPrintListener() {
            @Override
            public void onFinished() {
                dLog("PRINT CASH QR AND TRANSACTION SLIP SUCCEED");
                mKS80TSDK.end();
                CashTradeShowCashQrActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        fnsBtn.setVisibility(View.VISIBLE); //生成二维码并打印二维码凭条后显示“完成”按钮，在CashTradeShowCashQrActivity的UI线程中执行
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // 打印交易二维码凭条失败，跳转至交易失败页面
                dLog("PRINT CASH QR AND TRANSACTION SLIP FAILED, ERROR: " + e.toString());
                mKS80TSDK.end();
                mCashTradeInfoLog.startActivityFromActivity(CashTradeShowCashQrActivity.this, CashTradeFailureActivity.class);
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
        WalletActivityTimeoutController.getInstance().setTimeout(120);

        fnsBtn.setVisibility(View.INVISIBLE); // 初始化不显示“完成”按钮

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

            dLog("CashTradeShowCashQr: " + mCashInputStr
                    + ", BtcCaculated: " + mBtcCaculatedStr + " received from BlockchainServiceImpl");

            btcView.setText(mBtcCaculatedStr);
            cashView.setText(mCashInputStr);

            // 生成提款请求字符串
            final String cashCheckoutRequestStr = determineCashCheckoutRequestStr();

            // 基于提款请求字符串，生成二维码
            final int size = (int) (256 * getResources().getDisplayMetrics().density);
            dLog("set QR bitmap:" + cashCheckoutRequestStr);
            Bitmap qrCodeBitmap = Qr.bitmap(cashCheckoutRequestStr, size);
            qrView.setImageBitmap(qrCodeBitmap);

            mCashTradeInfoLog.setPayingState(null, null, cashCheckoutRequestStr);
            dLog("CashTradeShowCashQr set Paying state, Cash string:" + cashCheckoutRequestStr
                    + ", Cash QR: " + qrCodeBitmap.toString());

            // qrCodeBitmap默认为256*256
            if(mBtcCaculatedStr == null){
		        TextView text = new TextView(this);
		        text.setText("btc amount get fail");
		        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		        text.setBackgroundColor(Color.BLACK);
		        Toast toast = new Toast(this);
		        toast.setView(text);
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();
            	return;
            }
            mKS80TSDK.printCashQRSlip(mCashInputStr, cashCheckoutRequestStr, qrCodeBitmap, mBtcCaculatedStr);
            mKS80TSDK.printTransactionSlip(mCashInputStr, mBtcCaculatedStr, true);
            mKS80TSDK.begin();
        }
    }


    // 产生提款字符串
    private String determineCashCheckoutRequestStr() {
        final Address address = getWalletApplication().determineSelectedAddress();
        final String currTimeStr = String.valueOf(System.currentTimeMillis());

        final String content = currTimeStr;
        dLog("CashWithdrawalRequest: " + content);

        final String qrContent = Qr.encodeBinary(content.getBytes());

        return qrContent;
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_showcashqr_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_showcashqr_title);
        TextView hint1 = (TextView) findViewById(R.id.cashtrade_showcashqr_hint1);
        TextView hint2 = (TextView) findViewById(R.id.cashtrade_showcashqr_hint2);
        TextView btcTitle = (TextView) findViewById(R.id.cashtrade_showcashqr_btc_title);
        TextView cashTitle = (TextView) findViewById(R.id.cashtrade_showcashqr_cash_title);
        TextView btcunit = (TextView) findViewById(R.id.cashtrade_showcashqr_btc_abb);
        TextView cashunit = (TextView) findViewById(R.id.cashtrade_showcashqr_cash_abb);
        

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWCASHQR_TITLE));
        hint1.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWCASHQR_HINT1));
        hint2.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWCASHQR_HINT2));
        btcTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWCASHQR_BTC_TITLE));
        cashTitle.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_SHOWCASHQR_CASH_TITLE));
        fnsBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_FNS_BTN));
        btcunit.setText(getUiInfo().getTextByName(UiInfo.COMMON_BTC_ABB));
        cashunit.setText(getUiInfo().getTextByName(UiInfo.COMMON_CASH_ABB));
   }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
