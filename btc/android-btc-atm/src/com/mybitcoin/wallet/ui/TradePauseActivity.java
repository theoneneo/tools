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
import com.mybitcoin.wallet.environment.TradeCmd;
import com.mybitcoin.wallet.environment.UiInfo;

import javax.annotation.Nonnull;


public class TradePauseActivity extends WalletActivityBase {
    private static final String LOG_TAG = "TradePauseActivity";
    private static final boolean DEBUG_FLAG = true;

    private TradeCmd mTradeCmd;
    private TradeCmdController mTradeCmdController;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setContentView(R.layout.cash_trade_failure);

        mTradeCmd = new TradeCmd(getWalletApplication().getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 开启TradeCmdController线程，监视服务器对ATM机的开启/关闭交易指令
        if(mTradeCmdController == null) { // 线程对象为空
            mTradeCmdController = new TradeCmdController();
            mTradeCmdController.start();
        } else if (mTradeCmdController.isInterrupted() || !mTradeCmdController.isAlive()) { // 线程已被中断或停止
            mTradeCmdController = new TradeCmdController();
            mTradeCmdController.start();
        }
    }

    @Override
    public void updateUiInfo() {
        TextView title = (TextView) findViewById(R.id.cashtrade_failure_title);
        TextView confirmdata = (TextView) findViewById(R.id.cashtrade_failure_cfm_btn);
        TextView failurehint = (TextView) findViewById(R.id.cashtrade_failure_hint);

        title.setText(getUiInfo().getTextByName(UiInfo.TRADE_PAUSE_TITLE));
        confirmdata.setText(getUiInfo().getTextByName(UiInfo.COMMON_CFM_BTN));
        failurehint.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_CHECKPAYMENTTIMEOUT_HINT));
    }

    private class TradeCmdController extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    sleep(5000); // 每5s检查
                } catch (InterruptedException e) {
                    dLog("TradeCmdController: thread is interrupted");
                }

                if (!mTradeCmd.getSwShutdown()) {
                    dLog("SW_SHUTDOWN is false, go to WelcomePageActivity from TradePauseActivity");
                    Thread gotoActivityThread = new Thread() {
                        @Override
                        public void run() {
                           gotoActivity(WelcomePageActivity.class);
                        }
                    };
                    gotoActivityThread.start();

                    break; // 结束TradeCmdController线程
                }
            }
        }
    }


    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
