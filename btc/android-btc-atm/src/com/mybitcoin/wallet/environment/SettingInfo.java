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

public class SettingInfo {
    private static final String LOG_TAG = "SettingInfo";
    private static final boolean DEBUG_FLAG = false;

    private static final String INIT_FLAG = "init";                                               // 初始化标�
    private static final String DEFAULT_TIMEOUT_SEC = "default_timeout";                          // 默认超时时间
    private static final String BITCOIN_TRADING_LIMIT = "bitcoin_trading_limit";                  // 当日交易限额
    private static final String CASH_MIN_AMOUNT = "cash_min_amount";                              // 最小现金交易量，必须为出钞机钞票面额的整数�
    private static final String CASH_DENOMINATION = "cash_denomination";                          // 出钞机钞票面�
    private static final String CASH_OUT_LIMIT = "cash_out_limit";                                // 最高自动出钞额�
    private static final String BALANCE_WARNING_THRESHOLD = "balance_warning_threshold";          // 余额告警阈�
    private static final String EXCHANGE_RATE_URL = "exchange_rate_api_url";                      // 汇mv率接口API地址
    private static final String SMS_PLATFORM_URL = "sms_platform_url";                            // 短信通知平台URL
    private static final String SMS_MOBILE = "sms_mobile";                                        // 短信发送号�
    private static final String KYC_ENABLE = "kyc_enable";                                        // 启动KYC
    private static final String HANDLING_CHARGE_PROPORTION = "handling_charge_proportion";        // 手续费比�
    private static final String QUICK_PAYMENT_CASH_THRESHOLD = "quick_payment_cash_threshold";    // 快速支付现金阈�
    private static final String NETWORK_INTERFACE = "network_interface";                          // 网络接口
    private static final String CPU_INFO_POST_PERIOD_SEC = "cpu_info_post_period_sec";            // 上传CPU_INFO周期
    private static final String MEMORY_INFO_POST_PERIOD_SEC = "memory_info_post_period_sec";      // 上传MEMORY_INFO周期
    private static final String NETWORK_INFO_POST_PERIOD_SEC = "network_info_post_period_sec";    // 上传NETWORK_INFO周期
    private static final String SETTING_INFO_GET_PERIOD_SEC = "setting_info_get_period_sec";      // 获取SETTING_INFO周期
    private static final String UI_INFO_GET_PERIOD_SEC = "ui_info_get_period_sec";                // 获取UI_INFO周期

    private static final String[] mKeyList = {DEFAULT_TIMEOUT_SEC,
            BITCOIN_TRADING_LIMIT,
            CASH_OUT_LIMIT,
            CASH_MIN_AMOUNT,
            CASH_DENOMINATION,
            BALANCE_WARNING_THRESHOLD,
            EXCHANGE_RATE_URL,
            SMS_PLATFORM_URL,
            SMS_MOBILE,
            KYC_ENABLE,
            HANDLING_CHARGE_PROPORTION,
            QUICK_PAYMENT_CASH_THRESHOLD,
            NETWORK_INTERFACE,
            CPU_INFO_POST_PERIOD_SEC,
            MEMORY_INFO_POST_PERIOD_SEC,
            NETWORK_INFO_POST_PERIOD_SEC,
            SETTING_INFO_GET_PERIOD_SEC,
            UI_INFO_GET_PERIOD_SEC};

    private SharedPreferences mSharedPref;

    public SettingInfo(@Nonnull Context context) {
        mSharedPref = context.getSharedPreferences("setting_info", Context.MODE_APPEND);
        if (!mSharedPref.getBoolean(INIT_FLAG, false)) {
            setDefaultSettingInfo();
        }

        dLog("SettingInfo initialized");
    }

    public void setDefaultSettingInfo() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(DEFAULT_TIMEOUT_SEC, 60);
        edit.putInt(BITCOIN_TRADING_LIMIT, 1000);
        edit.putInt(CASH_MIN_AMOUNT, 1000);
        edit.putInt(CASH_DENOMINATION, 1000);
        edit.putInt(CASH_OUT_LIMIT, 5000);
        edit.putInt(BALANCE_WARNING_THRESHOLD, 3);
        edit.putString(EXCHANGE_RATE_URL, "https://www.okcoin.cn/api/ticker.do,https://blockchain.info/ticker");
        edit.putString(SMS_PLATFORM_URL, "http://utf8.sms.webchinese.cn/?Uid=btcatm&Key=dae55ec097981a932a87&smsMob=");
        edit.putString(SMS_MOBILE, "18611147179,18611121112");
        edit.putBoolean(KYC_ENABLE, false);
        edit.putFloat(HANDLING_CHARGE_PROPORTION, 0.05f);
        edit.putInt(QUICK_PAYMENT_CASH_THRESHOLD, 100);
        edit.putString(NETWORK_INTERFACE, "wlan0");
        edit.putInt(CPU_INFO_POST_PERIOD_SEC, 15);
        edit.putInt(MEMORY_INFO_POST_PERIOD_SEC, 15);
        edit.putInt(NETWORK_INFO_POST_PERIOD_SEC, 15);
        edit.putInt(SETTING_INFO_GET_PERIOD_SEC, 15);
        edit.putInt(UI_INFO_GET_PERIOD_SEC, 15);

        edit.putBoolean(INIT_FLAG, true);

        edit.commit();
    }

    public void setDefaultTimeOut(int timeout_sec) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(DEFAULT_TIMEOUT_SEC, timeout_sec);

        edit.commit();
    }

    public int getDefaultTimeOut() {
        return mSharedPref.getInt(DEFAULT_TIMEOUT_SEC, -1);
    }

    
    
    public void setBitcoinTradingLimit(float limit) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putFloat(BITCOIN_TRADING_LIMIT, limit);

        edit.commit();
    }

    public float getBitcoinTradingLimit() {
        return mSharedPref.getFloat(BITCOIN_TRADING_LIMIT, -1f);
    }

    public void setCashMinAmount(int amount) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(CASH_MIN_AMOUNT, amount);

        edit.commit();
    }

    public int getCashMinAmount() {
        return mSharedPref.getInt(CASH_MIN_AMOUNT, -1);
    }

    public void setCashDenomination(int denomination) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(CASH_DENOMINATION, denomination);

        edit.commit();
    }

    public int getCashDenomination() {
        return mSharedPref.getInt(CASH_DENOMINATION, -1);
    }

    public void setCashOutLimit(int limit) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(CASH_OUT_LIMIT, limit);

        edit.commit();
    }

    public int getCashOutUpperLimit() {
        return mSharedPref.getInt(CASH_OUT_LIMIT, -1);
    }

    public void setBalanceWarningThreshold(float limit) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putFloat(BALANCE_WARNING_THRESHOLD, limit);

        edit.commit();
    }

    public float getBalanceWarningThreshold() {
        return mSharedPref.getFloat(BALANCE_WARNING_THRESHOLD, -1f);
    }

    public void setExchangeRateUrl(@Nonnull String url) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(EXCHANGE_RATE_URL, url);

        edit.commit();
    }

    public String getExchangeRateUrl() {
        return mSharedPref.getString(EXCHANGE_RATE_URL, null);
    }

    public void setSmsPlatformUrl(@Nonnull String url) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(SMS_PLATFORM_URL, url);

        edit.commit();
    }

    public String getSmsPlatformUrl() {
        return mSharedPref.getString(SMS_PLATFORM_URL, null);
    }

    public void setSmsMobileArr(@Nonnull String mobileArrStr) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(SMS_MOBILE, mobileArrStr);

        edit.commit();
    }

    public String getSmsMobileArrStr() {
        return mSharedPref.getString(SMS_MOBILE, null);
    }


    public void setKycEnable(boolean en) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putBoolean(KYC_ENABLE, en);

        edit.commit();
    }

    public boolean getKycEnable() {
        return mSharedPref.getBoolean(KYC_ENABLE, false);
    }

    public void setHandlingChargeProportion(float proportion) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putFloat(HANDLING_CHARGE_PROPORTION, proportion);

        edit.commit();
    }

    public float getHandlingChargeProportion() {
        return mSharedPref.getFloat(HANDLING_CHARGE_PROPORTION, -1f);
    }

    public void setQuickPaymentCashThreshold(int threshold) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(QUICK_PAYMENT_CASH_THRESHOLD, threshold);

        edit.commit();
    }

    public int getQuickPaymentCashThreshold() {
        return mSharedPref.getInt(QUICK_PAYMENT_CASH_THRESHOLD, -1);
    }

    public void setNetworkInterface(String type) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(NETWORK_INTERFACE, type);

        edit.commit();
    }

    public String getNetworkInterface() {
        return mSharedPref.getString(NETWORK_INTERFACE, null);
    }

    public void setCpuInfoPostPeriodSec(int sec) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(CPU_INFO_POST_PERIOD_SEC, sec);

        edit.commit();
    }

    public int getCpuInfoPostPeriodSec() {
        return mSharedPref.getInt(CPU_INFO_POST_PERIOD_SEC, -1);
    }

    public void setMemoryInfoPostPeriodSec(int sec) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(MEMORY_INFO_POST_PERIOD_SEC, sec);

        edit.commit();
    }

    public int getMemoryInfoPostPeriodSec() {
        return mSharedPref.getInt(MEMORY_INFO_POST_PERIOD_SEC, -1);
    }

    public void setNetworkInfoPostPeriodSec(int sec) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(NETWORK_INFO_POST_PERIOD_SEC, sec);

        edit.commit();
    }

    public int getNetworkInfoPostPeriodSec() {
        return mSharedPref.getInt(NETWORK_INFO_POST_PERIOD_SEC, -1);
    }

    public void setSettingInfoGetPeriodSec(int sec) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(SETTING_INFO_GET_PERIOD_SEC, sec);

        edit.commit();
    }

    public int getSettingInfoGetPeriodSec() {
        return mSharedPref.getInt(SETTING_INFO_GET_PERIOD_SEC, -1);
    }

    public void setUiInfoGetPeriodSec(int sec) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putInt(UI_INFO_GET_PERIOD_SEC, sec);

        edit.commit();
    }

    public int getUiInfoGetPeriodSec() {
        return mSharedPref.getInt(UI_INFO_GET_PERIOD_SEC, -1);
    }

    public void setSettingInfoByJSON(@Nonnull JSONObject json) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        for (String key : mKeyList) {
            try {
                if (key.equals(DEFAULT_TIMEOUT_SEC))
                    setDefaultTimeOut(json.getInt(key));
                else if (key.equals(BITCOIN_TRADING_LIMIT))
                    setBitcoinTradingLimit(Float.parseFloat(String.valueOf(json.getDouble(key)))); // 将double转换为float
                else if (key.equals(CASH_OUT_LIMIT))
                    setCashOutLimit(json.getInt(key));
                else if (key.equals(BALANCE_WARNING_THRESHOLD))
                    setBalanceWarningThreshold(Float.parseFloat(String.valueOf(json.getDouble(key)))); // 将double转换为float
                else if (key.equals(EXCHANGE_RATE_URL))
                    setExchangeRateUrl(json.getString(key));
                else if (key.equals(SMS_PLATFORM_URL))
                    setSmsPlatformUrl(json.getString(key));
                else if (key.equals(SMS_MOBILE))
                    setSmsMobileArr(json.getString(key));
                else if (key.equals(KYC_ENABLE))
                    setKycEnable(json.getBoolean(key));
                else if (key.equals(HANDLING_CHARGE_PROPORTION))
                    setHandlingChargeProportion(Float.parseFloat(String.valueOf(json.getDouble(key)))); // 将double转换为float
                else if (key.equals(QUICK_PAYMENT_CASH_THRESHOLD))
                    setQuickPaymentCashThreshold(json.getInt(QUICK_PAYMENT_CASH_THRESHOLD));
                else if (key.equals(NETWORK_INTERFACE))
                    setNetworkInterface(json.getString(key));
                else if (key.equals(CPU_INFO_POST_PERIOD_SEC))
                    setCpuInfoPostPeriodSec(json.getInt(key));
                else if (key.equals(MEMORY_INFO_POST_PERIOD_SEC))
                    setMemoryInfoPostPeriodSec(json.getInt(key));
                else if (key.equals(NETWORK_INFO_POST_PERIOD_SEC))
                    setNetworkInfoPostPeriodSec(json.getInt(key));
                else if (key.equals(SETTING_INFO_GET_PERIOD_SEC))
                    setSettingInfoGetPeriodSec(json.getInt(key));
                else if (key.equals(UI_INFO_GET_PERIOD_SEC))
                    setUiInfoGetPeriodSec(json.getInt(key));

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
