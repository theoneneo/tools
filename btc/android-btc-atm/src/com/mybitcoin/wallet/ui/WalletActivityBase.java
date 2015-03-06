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

import com.google.bitcoin.core.Wallet;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.environment.SettingInfo;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.WalletUtils;

import javax.annotation.Nonnull;

public abstract class WalletActivityBase extends Activity {
    private static final String LOG_TAG = "WalletActivityBase";
    private static final boolean DEBUG_FLAG = true;

    private WalletApplication mApplication;

    private SettingInfo mSettingInfo;
    private UiInfo mUiInfo;
    
    protected String publicKey;
    public static String publiKeyForOuter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mApplication = (WalletApplication) getApplication();

        mSettingInfo = new SettingInfo(mApplication);

        mUiInfo = new UiInfo(this);
        
        Wallet wallet = getWalletApplication().getWallet();
        publicKey = WalletUtils.pickOldestKey(wallet).toAddress(Constants.NETWORK_PARAMETERS).toString();
        publiKeyForOuter = publicKey;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 设置当前Activity为本Activity
        UiInfo.setCurrActivity(this);

        // 判断是否需要立即更新UiInfo
        if (getUiInfo().needUpdateUiInfo()) {
            updateUiInfo();
            getUiInfo().updateUiInfoTag();
        }
    }

    protected WalletApplication getWalletApplication() {
        return mApplication;
    }

    protected SettingInfo getSettingInfo() {
        return mSettingInfo;
    }

    public UiInfo getUiInfo() {
        return mUiInfo;
    }

    protected void gotoActivity(@Nonnull Class<?> activityCls) {
        startActivity(new Intent(this, activityCls));
    }

    public abstract void updateUiInfo();

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
