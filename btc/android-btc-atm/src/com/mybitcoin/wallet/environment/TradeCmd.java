/**
 *  AUTHOR: F
 *  DATE: 2014.6.10
 */

package com.mybitcoin.wallet.environment;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

public class TradeCmd {
    private static final String LOG_TAG = "TradeCmd";
    private static final boolean DEBUG_FLAG = false;

    private static final String INIT_FLAG = "init";                                               // 初始化标记
    private static final String TRADE_CMD_GET_PERIOD_SEC = "trade_cmd_get_period_sec";              // 获取TRADE_CMD周期
    private static final String SW_SHUTDOWN = "sw_shutdown";                                      // 交易开始/停止

    private static final String[] mKeyList = {SW_SHUTDOWN,
            TRADE_CMD_GET_PERIOD_SEC};

    private SharedPreferences mSharedPref;

    public TradeCmd(@Nonnull Context context) {
        mSharedPref = context.getSharedPreferences("trade_cmd", Context.MODE_APPEND);
        if (!mSharedPref.getBoolean(INIT_FLAG, false)) {
            setDefaultTradeCmd();
        }

        dLog("TradeCmd initialized");
    }

    public void setDefaultTradeCmd() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putBoolean(SW_SHUTDOWN, false);
        edit.putInt(TRADE_CMD_GET_PERIOD_SEC, 15);

        edit.putBoolean(INIT_FLAG, true);

        edit.commit();
    }

    public void setSwShutdown(boolean en) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putBoolean(SW_SHUTDOWN, en);

        edit.commit();
    }

    public boolean getSwShutdown() {
        return mSharedPref.getBoolean(SW_SHUTDOWN, false);
    }

    public void setTradeCmdGetPeriodSec(int sec) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(TRADE_CMD_GET_PERIOD_SEC, sec);

        edit.commit();
    }

    public int getTradeCmdGetPeriodSec() {
        return mSharedPref.getInt(TRADE_CMD_GET_PERIOD_SEC, -1);
    }

    public void setTradeCmdByJSON(@Nonnull JSONObject json) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        for (String key : mKeyList) {
            try {
                if (key.equals(SW_SHUTDOWN))
                    setSwShutdown(json.getBoolean(key));
                else if (key.equals(TRADE_CMD_GET_PERIOD_SEC))
                    setTradeCmdGetPeriodSec(json.getInt(key));

                dLog("put key: " + key + ", value: " + json.getString(key) + "into mSharedPref");
            } catch (JSONException e) {
                dLog("error when put the value of key: " + key + "into mSharedPref");
                continue;
            }
        }

        edit.commit();
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
            Log.d(LOG_TAG, logStr);
        }
    }
}
