/**
 *  AUTHOR: F
 *  DATE: 2014.6.3
 */

package com.mybitcoin.wallet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

import javax.annotation.Nonnull;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CashTradeInfoLog {
    private static final boolean DEBUG_FLAG = true;
    private static final String LOG_TAG = "CashTradeInfoLog";

    public static final String INTENT_EXTRA_TRADE_ID = "intent_tradeID";

    public static final String STATE_ERROR = CashTradeInfoProvider.STATE_ERROR;            // 交易出错
    public static final String STATE_EMPTY = "EMPTY";                                        // 空状态，CashTradeInfoLog刚被创建时为该状态
    public static final String STATE_INITING = CashTradeInfoProvider.STATE_INITING;            // 交易初始化，构造交易信息，由 CashTradeExchangeRateActivity和CashTradeInput构造
    public static final String STATE_INITED = CashTradeInfoProvider.STATE_INITED;            // 交易生成，等待用户支付比特币。 CashTradeRequestCoinActivity产生
    public static final String STATE_PAYING = CashTradeInfoProvider.STATE_PAYING;            // 侦测到用户支付比特币通知, BlockchainServiceImpl产生
    public static final String STATE_PAYED = CashTradeInfoProvider.STATE_PAYED;            // 侦测到确认信息，确认比特币支付成功
    public static final String STATE_CHECKEDOUT = CashTradeInfoProvider.STATE_CHECKEDOUT;        // 用户提款完成
    public static final String STATE_FAILED = CashTradeInfoProvider.STATE_FAILED;            // 交易失败

    public static final int BTC_FRACTION_PRECISION = 6; // 换算精度，小数点后位数

    private static final Uri BASIC_URI = CashTradeInfoProvider.BASIC_URI;

    private ContentResolver mCr;
    private String mTradeID;
    private Uri mUriWithTradeID;

    private String mCurrState = STATE_EMPTY; // 当前状态

    public CashTradeInfoLog(@Nonnull ContentResolver cr, @Nonnull String tradeID) {
        mCr = cr; // 绑定当前Activity的ContentResolver
        mTradeID = tradeID;

        mUriWithTradeID = BASIC_URI.buildUpon().appendPath(mTradeID).build();

        updateState();
    }

    public static String genCashTradeID() {
        long sysTime = System.currentTimeMillis();
        return String.valueOf(sysTime);
    }

    public String getCurrentState() {
        updateState();

        return mCurrState;
    }

    // 读取数据库，更新当前状态
    private void updateState() {
        dLog("current uri is " + mUriWithTradeID.toString());

        Cursor c = mCr.query(mUriWithTradeID, null, null, null, null);
        int count = c.getCount();
        if (count < 1) {
            dLog("set current state as EMPTY");
            mCurrState = STATE_EMPTY;
        } else if (count == 1) {
            // 查找该条记录中记载的交易状态
            c.moveToNext();
            String state = c.getString(c.getColumnIndexOrThrow(CashTradeInfoProvider.KEY_TRADESTATE));
            if (state.equals(STATE_EMPTY)) // EMPTY状态不记录入数据库
                throw new IllegalArgumentException("find the tradeID with EMPTY state, tradeID: " + mTradeID);
            mCurrState = state;
        }
        // count > 1， 条目重复情况由CashTradeInfoProvider处理

        c.close();
    }

    // 交易出错
    public void setErrorState() {
        dLog("setting STATE_ERROR of tradeID: " + mTradeID);
        // 在EMPTY状态下不能能进入ERROR状态
        if (getCurrentState().equals(CashTradeInfoLog.STATE_EMPTY))
            throw new IllegalArgumentException("can not set to ERROR state when in: " + getCurrentState() + ", tradeID: " + mTradeID);

        ContentValues cv = new ContentValues();
        cv.put(CashTradeInfoProvider.KEY_TRADESTATE, CashTradeInfoLog.STATE_ERROR);

        mCr.update(mUriWithTradeID, cv, null, null);

        updateState();
    }

    // 交易初始化，构造交易信息，由 CashTradeExchangeRateActivity和CashTradeInput构造
    public void setInitingState(String amountBTC,
                                String amountCash,
                                String exchangeRate,
                                String handlingChargeProportion) {
        dLog("setting STATE_INITING of tradeID: " + mTradeID);
        // 只有在EMPTY或INITING状态下才能进入INITING状态
        if (!getCurrentState().equals(CashTradeInfoLog.STATE_EMPTY) && !getCurrentState().equals(CashTradeInfoLog.STATE_INITING))
            throw new IllegalArgumentException("can not set to INITING state when in: " + getCurrentState() + ", tradeID: " + mTradeID);

        ContentValues cv = new ContentValues();
        cv.put(CashTradeInfoProvider.KEY_TRADESTATE, CashTradeInfoProvider.STATE_INITING);
        if (amountBTC != null) {
            // 格式化btc金额
            dLog("setInitingState: amountBTC = " + amountBTC);
            double btc = Double.parseDouble(amountBTC);
            DecimalFormat formater = new DecimalFormat();
            formater.setMaximumFractionDigits(BTC_FRACTION_PRECISION); // 调整最大小数位数
            formater.setGroupingSize(0);
            formater.setRoundingMode(RoundingMode.FLOOR);
            String btcStr = formater.format(btc);
            btc = Double.parseDouble(btcStr);
            dLog("setInitingState: btc= " + btc);

            cv.put(CashTradeInfoProvider.KEY_BTC, btc);
        }

        if (amountCash != null)
            cv.put(CashTradeInfoProvider.KEY_CASH, amountCash);
        if (exchangeRate != null)
            cv.put(CashTradeInfoProvider.KEY_EXCHANGERATE, exchangeRate);
        if (exchangeRate != null)
            cv.put(CashTradeInfoProvider.KEY_HCHARGEPROP, handlingChargeProportion);

        if (getCurrentState().equals(CashTradeInfoLog.STATE_EMPTY))
            mCr.insert(mUriWithTradeID, cv);
        else
            mCr.update(mUriWithTradeID, cv, null, null);

        updateState();
    }

    // 交易生成，等待用户支付比特币。 CashTradeRequestCoinActivity产生
    public void setInitedState(@Nonnull String btcQrStr) {
        dLog("setting STATE_INITED of tradeID: " + mTradeID);
        // 只有在INITING或INIT状态下才能进入INIT状态
        if (!getCurrentState().equals(CashTradeInfoLog.STATE_INITING) && !getCurrentState().equals(CashTradeInfoLog.STATE_INITED))
            throw new IllegalArgumentException("can not set to INITED state when in: " + getCurrentState() + ", tradeID: " + mTradeID);

        ContentValues cv = new ContentValues();
        cv.put(CashTradeInfoProvider.KEY_TRADESTATE, CashTradeInfoProvider.STATE_INITED);
        cv.put(CashTradeInfoProvider.KEY_BTCQRSTR, btcQrStr);

        mCr.update(mUriWithTradeID, cv, null, null);

        updateState();
    }

    // 侦测到用户支付比特币通知, BlockchainServiceImpl产生
    public void setPayingState(String transHash,
                               String payerAddress,
                               String cashPaymentRequestStr) {
        dLog("setting STATE_PAYING of tradeID: " + mTradeID);
        // 只有在INIT或PAYING状态下才能进入PAYING状态
        if (!getCurrentState().equals(CashTradeInfoLog.STATE_INITED) && !getCurrentState().equals(CashTradeInfoLog.STATE_PAYING))
            throw new IllegalArgumentException("can not set to PAYING state when in: " + getCurrentState() + ", tradeID: " + mTradeID);

        ContentValues cv = new ContentValues();
        cv.put(CashTradeInfoProvider.KEY_TRADESTATE, CashTradeInfoProvider.STATE_PAYING);
        cv.put(CashTradeInfoProvider.KEY_TRANSHASH, transHash);
        cv.put(CashTradeInfoProvider.KEY_PAYERADDR, payerAddress);
        cv.put(CashTradeInfoProvider.KEY_CASHQRSTR, cashPaymentRequestStr);

        mCr.update(mUriWithTradeID, cv, null, null);

        updateState();
    }

    // 侦测到用户支付比特币通知, CashTradeShowCashQrActivity产生
    public void setPayedState() {
        dLog("setting STATE_PAYED of tradeID: " + mTradeID);
        // 只有在PAYING或PAYED状态下才能进入PAYED状态
        if (!getCurrentState().equals(CashTradeInfoLog.STATE_PAYING) && !getCurrentState().equals(CashTradeInfoLog.STATE_PAYED))
            throw new IllegalArgumentException("can not set to PAYED state when in: " + getCurrentState() + ", tradeID: " + mTradeID);

        ContentValues cv = new ContentValues();
        cv.put(CashTradeInfoProvider.KEY_TRADESTATE, CashTradeInfoProvider.STATE_PAYED);

        mCr.update(mUriWithTradeID, cv, null, null);

        updateState();
    }

    // 更新Confidence Depth
    public void updateConfidenceDepth(int confirmCount) {
        dLog("updating confidence depth of tradeID: " + mTradeID);
        // 只有在PAYING及之后的状态下才能更新
        if (getCurrentState().equals(CashTradeInfoLog.STATE_EMPTY) || getCurrentState().equals(CashTradeInfoLog.STATE_INITING) || getCurrentState().equals(CashTradeInfoLog.STATE_INITED))
            throw new IllegalArgumentException("can not update confidence depth  when in: " + getCurrentState() + ", tradeID: " + mTradeID);

        ContentValues cv = new ContentValues();
        cv.put(CashTradeInfoProvider.KEY_CONFDEPTH, Integer.valueOf(confirmCount));

        mCr.update(mUriWithTradeID, cv, null, null);
    }

    // 用户提款完成
    public void setCheckedOutState() {
        dLog("setting STATE_CHECKEDOUT of tradeID: " + mTradeID);
        // 只有在PAYED或CHECKEDOUT状态下才能进入CHECKEDOUT状态
        if (!getCurrentState().equals(CashTradeInfoLog.STATE_PAYED) && !getCurrentState().equals(CashTradeInfoLog.STATE_CHECKEDOUT))
            throw new IllegalArgumentException("can not set to CHECKEDOUT state when in: " + getCurrentState() + ", tradeID: " + mTradeID);

        ContentValues cv = new ContentValues();
        cv.put(CashTradeInfoProvider.KEY_TRADESTATE, CashTradeInfoProvider.STATE_CHECKEDOUT);

        mCr.update(mUriWithTradeID, cv, null, null);

        updateState();
    }

    // 交易失败
    public void setFailedState() {
        dLog("setting STATE_FAILED of tradeID: " + mTradeID);
        // 在EMPTY状态下不能能进入FAILED状态
        if (getCurrentState().equals(CashTradeInfoLog.STATE_EMPTY))
            throw new IllegalArgumentException("can not set to FAILD state when in: " + getCurrentState() + ", tradeID: " + mTradeID);

        ContentValues cv = new ContentValues();
        cv.put(CashTradeInfoProvider.KEY_TRADESTATE, CashTradeInfoLog.STATE_FAILED);

        mCr.update(mUriWithTradeID, cv, null, null);

        updateState();
    }

    public void startActivityFromActivity(@Nonnull Context from, @Nonnull Class<?> to) {
        Intent intent = new Intent(from, to);

        intent.putExtra(CashTradeInfoLog.INTENT_EXTRA_TRADE_ID, mTradeID);

        from.startActivity(intent);
    }

    public void startActivityFromService(@Nonnull Context from, @Nonnull Class<?> to) {
        Intent intent = new Intent(from, to);

        intent.putExtra(CashTradeInfoLog.INTENT_EXTRA_TRADE_ID, mTradeID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        from.startActivity(intent);
    }

    public static String getTradeIdFromIntent(@Nonnull Activity activity) {
        Intent intent = activity.getIntent();

        if (intent != null && intent.hasExtra(CashTradeInfoLog.INTENT_EXTRA_TRADE_ID)) {
            String tradeID = intent.getStringExtra(CashTradeInfoLog.INTENT_EXTRA_TRADE_ID);

            intent.removeExtra(CashTradeInfoLog.INTENT_EXTRA_TRADE_ID);

            return tradeID;
        }

        return null;
    }

    // 获取属性值函数的基函数
    private String getXXXStr(String[] states_exclude, @Nonnull String key) {
        // 检查不支持查询的交易状态，states_exclude为null时不检查
        if(states_exclude != null) {
            for(String state : states_exclude) {
                if(getCurrentState().equals(state)) {
                    throw new UnsupportedOperationException("can not get BTC in STATE " + states_exclude);
                }
            }
        }

        Cursor c = mCr.query(mUriWithTradeID, null, null, null, null);

        validateQueryResult(c);

        c.moveToNext();

        String str = c.getString(c.getColumnIndexOrThrow(key));
        c.close();

        return str;
    }

    // 检查查询有效性，排除空结果与多重结果
    private void validateQueryResult(@Nonnull Cursor c) {
        int count = c.getCount();

        if (count < 1) {
            throw new SQLException("find non tradeID: " + mTradeID + " when validateQueryResult");
        } else if (count > 1) {
            // 重复的tradeID
            throw new SQLException("find duplicated tradeID: " + mTradeID + " when validateQueryResult");
        }
    }

    // 获取当前交易的汇率
    public String getExchangeRateStr() {
        String[] stateArr = {STATE_EMPTY};
        return getXXXStr(stateArr, CashTradeInfoProvider.KEY_EXCHANGERATE);
    }

    // 获取当前交易的现金数额
    public String getCashStr() {
        String[] stateArr = {STATE_EMPTY};
        return getXXXStr(stateArr, CashTradeInfoProvider.KEY_CASH);
    }

    // 获取当前交易的比特币数额
    public String getBtcStr() {
        String[] stateArr = {STATE_EMPTY};
        return getXXXStr(stateArr, CashTradeInfoProvider.KEY_BTC);
    }

    // 获取当前交易的手续费比例
    public String getHandlingChargeProportionStr() {
        String[] stateArr = {STATE_EMPTY};
        return getXXXStr(stateArr, CashTradeInfoProvider.KEY_HCHARGEPROP);
    }

    // 获取当前交易的用户地址（公钥）
    public String getPayerAddrStr() {
        String[] stateArr = {STATE_EMPTY, STATE_INITING, STATE_INITED};
        return getXXXStr(stateArr, CashTradeInfoProvider.KEY_PAYERADDR);
    }


    public static String queryMatchedTradeIdByBTC(@Nonnull ContentResolver cr, @Nonnull String btcStr) {
        dLog("queryMatchedTradeIdByBTC: btcStr = " + btcStr);
        // 格式化btc金额
        double btc = Double.parseDouble(btcStr);
        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(BTC_FRACTION_PRECISION); // 调整最大小数位数
        formater.setGroupingSize(0);
        formater.setRoundingMode(RoundingMode.FLOOR);
        String formatedBtcStr = formater.format(btc);
        btc = Double.parseDouble(formatedBtcStr);
        dLog("queryMatchedTradeIdByBTC: btc = " + btc);

        // 状态为INITED，且KEY_BTC值与btc匹配
        // 注意BTC为String类型，因此匹配时必需完全一致（例如0.123450不等于0.12345）
        String selection = CashTradeInfoProvider.KEY_BTC                //
                + "="                                                  //
                + btc                                                   //
                + " AND "                                              //
                + CashTradeInfoProvider.KEY_TRADESTATE                  //
                + "='"                                                  //
                + CashTradeInfoLog.STATE_INITED                         //
                + "'";

        String sortOrder = CashTradeInfoProvider.KEY_TRADEID + " desc";
        String[] projection = {CashTradeInfoProvider.KEY_TRADEID};

        Cursor c = cr.query(BASIC_URI, projection, selection, null, sortOrder);
        if (c == null) {
            dLog("Cursor is null");
            return null;
        }

        dLog("count of the Cursor: " + c.getCount());

        if (c.getCount() == 0) {
            c.close();
            return null;
        }

        // 如果查询结果个数大于1，说明处于INITED状态且具有相同金额的支付多于1个，系统选择最近的交易进行处理
        // TODO 此处应该为系统错误，将在未来处理

        c.moveToNext();
        String id = c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_TRADEID));
        c.close();

        return id;
    }

    public static String queryMatchedTradeIdByCashQr(@Nonnull ContentResolver cr, @Nonnull String cashQr) {
        // 状态为PAYING或PAYED，且KEY_CASHQR值与cash匹配
        String selection = CashTradeInfoProvider.KEY_CASHQRSTR        //
                + "='"                                    //
                + cashQr                                //
                + "' AND ("                                //
                + CashTradeInfoProvider.KEY_TRADESTATE    //
                + "='"                                    //
                + CashTradeInfoLog.STATE_PAYING            //
                + "' OR "                                //
                + CashTradeInfoProvider.KEY_TRADESTATE    //
                + "='"                                    //
                + CashTradeInfoLog.STATE_PAYED            //
                + "')";

        String sortOrder = CashTradeInfoProvider.KEY_TRADEID + " desc";
        String[] projection = {CashTradeInfoProvider.KEY_TRADEID};

        Cursor c = cr.query(BASIC_URI, projection, selection, null, sortOrder);
        if (c == null) {
            dLog("Cursor is null");
            return null;
        }

        dLog("count of the Cursor:" + c.getCount());

        if (c.getCount() == 0) {
            c.close();
            return null;
        } else if (c.getCount() > 1) { //此处检查结果大于1的情况，因为CashQr全局唯一
            c.close();
            return null;
        }

        c.moveToNext();
        String id = c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_TRADEID));
        c.close();

        return id;
    }

    public static String queryMatchedTradeIdByTransHash(@Nonnull ContentResolver cr, @Nonnull String transHashStr) {
        // 状态为PAYING，且KEY_CASHQR值与cash匹配
        String selection = CashTradeInfoProvider.KEY_TRANSHASH        //
                + "='"                                    //
                + transHashStr                            //
                + "' AND "                                //
                + CashTradeInfoProvider.KEY_TRADESTATE    //
                + "='"                                    //
                + CashTradeInfoLog.STATE_PAYING            //
                + "'";

        String sortOrder = CashTradeInfoProvider.KEY_TRADEID + " desc";
        String[] projection = {CashTradeInfoProvider.KEY_TRADEID};

        Cursor c = cr.query(BASIC_URI, projection, selection, null, sortOrder);
        if (c == null) {
            dLog("Cursor is null");
            return null;
        }

        dLog("count of the Cursor:" + c.getCount());

        if (c.getCount() == 0) {
            c.close();
            return null;
        } else if (c.getCount() > 1) { //此处检查结果大于1的情况，因为TransHash全局唯一
            c.close();
            return null;
        }

        c.moveToNext();
        String id = c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_TRADEID));
        c.close();

        return id;
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
