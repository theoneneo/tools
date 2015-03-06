/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import javax.annotation.Nonnull;


public class CashTradeCheckPaymentActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "TradeModeActivity";
    private static final boolean DEBUG_FLAG = true;

    private static int TIMEOUT_SEC = 300; // 超时时间300s

    private static int MIN_WAIT_TIME_SEC = 3; // 最小等待时间，若在前一页面收到了blockchain的广播信息，则至少在此页面停留3秒，再进入下一页面
    private static int WAIT_PEROID_SEC = 1; // 交易状态变更检查周期

    private CashTradeInfoLog mCashTradeInfoLog;

    WalletActivityTimeoutController.ActivityTimeoutedEventListener mTimeoutedListener = new WalletActivityTimeoutController.ActivityTimeoutedEventListener() {
        @Override
        public void onTimeouted() {
            if (mCashTradeInfoLog != null)
                mCashTradeInfoLog.startActivityFromActivity(CashTradeCheckPaymentActivity.this, CashTradeCheckPaymentTimeoutActivity.class);
            else
                gotoActivity(CashTradeCheckPaymentTimeoutActivity.class); // 若mCashTradeInfoLog为空，则仍然切换至CashTradeCheckPaymentTimeoutActivity
        }
    };

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_check_payment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WalletActivityTimeoutController.getInstance().setTimeout(TIMEOUT_SEC);
        WalletActivityTimeoutController.getInstance().setTimeoutedEventListener(mTimeoutedListener);

        // 清空mCashTradeInfoLog
        mCashTradeInfoLog = null;

        final String tradeID = CashTradeInfoLog.getTradeIdFromIntent(this);
        if (tradeID == null) {
            dLog("the intent does not contain INTENT_EXTRA_TRADE_ID, go back to WelcomePage");
            gotoActivity(WelcomePageActivity.class);
        } else {
            mCashTradeInfoLog = new CashTradeInfoLog(getContentResolver(), tradeID);
            if (mCashTradeInfoLog.getCurrentState().equals(CashTradeInfoLog.STATE_PAYING)) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            dLog("PAYING state of tradeID: " + tradeID + " has been set before this activity, wait for 3s");
                            sleep(MIN_WAIT_TIME_SEC * 1000);
                            determinePaymentTypeAtPayingState(mCashTradeInfoLog);
                        } catch (InterruptedException e) {
                            // may not happen
                        }
                    }
                };
                thread.start();
            } else if (mCashTradeInfoLog.getCurrentState().equals(CashTradeInfoLog.STATE_INITED)) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        int timeout = TIMEOUT_SEC;
                        while (timeout >= 0) {
                            try {
                                sleep(WAIT_PEROID_SEC * 1000);
                                timeout -= WAIT_PEROID_SEC;
                                dLog("checking PAYING state for tradeID: " + tradeID);
                                if (mCashTradeInfoLog.getCurrentState().equals(CashTradeInfoLog.STATE_PAYING)) {
                                    dLog("PAYING state for tradeID: " + tradeID + "has been set at this activity");
                                    determinePaymentTypeAtPayingState(mCashTradeInfoLog);
                                    break;
                                }
                            } catch (InterruptedException e) {
                                // may not happen
                            }
                        }
                    }
                };
                thread.start();
            }
        }
    }

    private void determinePaymentTypeAtPayingState(final CashTradeInfoLog cashTradeInfoLog) {

		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Test");
		b.setMessage("交易金额："+ cashTradeInfoLog.getCashStr()+ "\n"+ "快速支付现金阈"+ String.valueOf(getSettingInfo().getQuickPaymentCashThreshold()));
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
		        // 如果交易金额小于QuickPaymentCashThreshold，则直接出钞（进入出钞页面）
		        if (Integer.parseInt(cashTradeInfoLog.getCashStr()) <= getSettingInfo().getQuickPaymentCashThreshold()) {
		            cashTradeInfoLog.setPayedState(); // 忽略confidence检查，直接设置为PAYED状态
		            cashTradeInfoLog.startActivityFromActivity(CashTradeCheckPaymentActivity.this, CashTradeCheckingoutActivity.class);
		        } else { // 否则产生提款二维码，延迟出钞（进入提款二维码显示页面）
		            cashTradeInfoLog.startActivityFromActivity(CashTradeCheckPaymentActivity.this, CashTradeShowCashQrActivity.class);
		        }
			}
		});
		b.show();
    	

    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_checkpayment_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_checkpayment_title);
        TextView hint = (TextView) findViewById(R.id.cashtrade_checkpayment_hint);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKPAYMENT_TITLE));
        hint.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKPAYMENT_HINT));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
