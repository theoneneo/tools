/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.app.Activity;
import android.util.Log;
import com.mybitcoin.wallet.environment.SettingInfo;

import javax.annotation.Nonnull;

public class WalletActivityTimeoutController {
    private static final String LOG_TAG = "WalletActivityTimeoutController";
    private static final boolean DEBUG_FLAG = true;

    private static final WalletActivityTimeoutController mController = new WalletActivityTimeoutController();

    private TimerThread mTimerThread;
    private long mTimeoutSec;
    private Activity mActivity;

    private WalletActivityTimeoutController() {
        dLog("WalletActivityTimeoutController initialized");
    }

    static WalletActivityTimeoutController getInstance() {
        return mController;
    }

    interface ActivityCountingDownEventListener {
        public void onCountingDown(long timeoutSec);
    }

    interface ActivityTimeoutedEventListener {
        public void onTimeouted();
    }

    private ActivityCountingDownEventListener mCountingDownListener;
    private ActivityTimeoutedEventListener mTimeoutedListener;

    void setmCountingDownEventListener(@Nonnull WalletActivityTimeoutController.ActivityCountingDownEventListener listener) {
        mCountingDownListener = listener;
    }

    void setTimeoutedEventListener(@Nonnull WalletActivityTimeoutController.ActivityTimeoutedEventListener listener) {
        mTimeoutedListener = listener;
    }

    void setTimeout(int timeoutSec) {
        mTimeoutSec = timeoutSec;
    }

    void start(@Nonnull Activity activity) {
        mActivity = activity;

        SettingInfo settingInfo = new SettingInfo(activity);

        mTimeoutSec = settingInfo.getDefaultTimeOut();

        if (mTimerThread != null && !mTimerThread.isInterrupted())
            mTimerThread.interrupt();

        mTimerThread = new TimerThread();
        mTimerThread.start();
        dLog("timerThread started in " + activity.toString());
    }

    void end() {
        mTimerThread.interrupt();
        dLog("timerThread ended in " + mActivity.toString());
    }

    private class TimerThread extends Thread {
        @Override
        public void run() {
            if (mCountingDownListener != null) {
                mCountingDownListener.onCountingDown(mTimeoutSec);
            }

            while (true) {
                try {
                    Thread.sleep(1000);
                    --mTimeoutSec;

                    if (mCountingDownListener != null) {
                        mCountingDownListener.onCountingDown(mTimeoutSec);
                    }

                    if (mTimeoutSec <= 0) {
                        dLog("timeout in " + mActivity.toString());
                        if (mTimeoutedListener != null) {
                            mTimeoutedListener.onTimeouted();
                        }
                        mActivity.finish();
                        return;
                    }
                } catch (InterruptedException e) {
                    // 调用end()方法后产生该异常，退出循环，结束线程，停止计时
                    dLog("timerThread is interupted by " + mActivity.toString());
                    return;
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
