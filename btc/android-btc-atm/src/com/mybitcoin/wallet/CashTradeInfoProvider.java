/**
 *  AUTHOR: F
 *  DATE: 2014.6.3
 */

package com.mybitcoin.wallet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import javax.annotation.Nonnull;
import java.util.List;

public class CashTradeInfoProvider extends ContentProvider {
    private static final boolean DEBUG_FLAG = true;
    private static final String LOG_TAG = "CashTradeInfoProvider";

    private static final String AUTHORITY = "com.mybitcoin.wallet.cash_trade_info";

    public static final Uri BASIC_URI = Uri.parse("content://" + AUTHORITY); // URI地址

    private static final String DATABASE_TABLE = "cash_trade_info";

    // 数据库表结构
    private static final String KEY_ROWID = "_id";
    public static final String KEY_TRADEID = "trade_id";                        // 交易ID，用于标记本次交易，采用交易的起始时间计算
    public static final String KEY_TRADESTATE = "trade_state";                    // 交易目前状态
    public static final String KEY_BTC = "btc";                            // 交易比特币数额
    public static final String KEY_CASH = "cash";                            // 交易现金数额
    public static final String KEY_EXCHANGERATE = "exchange_rate";                    // 交易时汇率
    public static final String KEY_HCHARGEPROP = "handling_charge_proportion";     // 交易手续费率
    public static final String KEY_BTCQRSTR = "btc_string";                        // 比特币支付字符串
    public static final String KEY_TRANSHASH = "transaction_hash";                // 交易哈希码
    public static final String KEY_PAYERADDR = "payer_address";                    // 付款者地址
    public static final String KEY_CASHQRSTR = "cash_string";                    // 现金支付字符串
    public static final String KEY_CONFDEPTH = "confidence_depth";                // 矿机确认数
    public static final String KEY_CHECKOUTTIME = "checkedout_time";                // 提款时间


    public static final String STATE_ERROR = "ERROR";                    // 交易出错
    public static final String STATE_INITING = "INITING";                // 交易信息填充， CashTradeExchangeRateActivity和CashTradeInput产生
    public static final String STATE_INITED = "INITED";                    // 交易生成，等待用户支付比特币, CashTradeRequestCoinActivity产生
    public static final String STATE_PAYING = "PAYING";                    // 侦测到用户支付比特币通知, CashTradeShowCashQrActivity产生
    public static final String STATE_PAYED = "PAYED";                    // 侦测到矿机确认信息，确认比特币支付成功
    public static final String STATE_CHECKEDOUT = "CHECKEDOUT";                // 用户提款完成
    public static final String STATE_FAILED = "FAILED";                    // 交易失败

    private Helper helper;

    private static class Helper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "cash_trade_info.db";
        private static final int DATABASE_VERSION = 1;

        private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " (" //
                + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " //
                + KEY_TRADEID + " TEXT NOT NULL, " //
                + KEY_TRADESTATE + " TEXT, " //
                + KEY_BTC + " REAL, " //
                + KEY_CASH + " TEXT, " //
                + KEY_EXCHANGERATE + " TEXT, " //
                + KEY_HCHARGEPROP + " TEXT, " //
                + KEY_BTCQRSTR + " TEXT, " //
                + KEY_TRANSHASH + " TEXT, " //
                + KEY_PAYERADDR + " TEXT, " //
                + KEY_CASHQRSTR + " TEXT, " //
                + KEY_CONFDEPTH + " INTEGER DEFAULT 0, " //
                + KEY_CHECKOUTTIME + " TEXT);";

        public Helper(final Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.beginTransaction();
            try {
                for (int v = oldVersion; v < newVersion; v++)
                    upgrade(db, v);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        private void upgrade(final SQLiteDatabase db, final int oldVersion) {
            if (oldVersion == 1) {
                // future
            } else {
                throw new UnsupportedOperationException("old=" + oldVersion);
            }
        }
    }

    @Override
    public boolean onCreate() {
        helper = new Helper(this.getContext());
        dLog("database: " + DATABASE_TABLE + " is created");
        return true;
    }

    @Override
    public String getType(final Uri uri) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Uri insert(@Nonnull final Uri uri, final ContentValues values) {
        if (uri.getPathSegments().size() != 1)
            throw new IllegalArgumentException(uri.toString());

        final String id = uri.getLastPathSegment();

        // uri重复检查
        Cursor c = query(uri, null, null, null, null);
        if (c.getCount() != 0) {
            dLog("insert: find duplicated tradeID: " + id);
            c.close();
            return null;
        }
        c.close();

        values.put(KEY_TRADEID, id);

        long rowId = helper.getWritableDatabase().insertOrThrow(DATABASE_TABLE, null, values);

        // 插入失败
        if (rowId < 0)
            throw new SQLException("insert: error when inserting " + uri.toString());

        final Uri rowUri = uri.buildUpon().appendPath(Long.toString(rowId)).build();

        dLog("insert input: " + uri.toString() + ", values: " + getValueString(values));
        dLog("insert return: " + rowUri.toString());

        return rowUri;
    }

    @Override
    public int update(@Nonnull final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        if (uri.getPathSegments().size() != 1)
            throw new IllegalArgumentException(uri.toString());

        final String id = uri.getLastPathSegment();

        Cursor c = query(uri, null, null, null, null);
        if (c.getCount() > 1) {
            c.close();
            throw new SQLException("update: find duplicated tradeID: " + id);
        } else if (c.getCount() == 0) {
            c.close();
            throw new SQLException("update: find no matched tradeID: " + id);
        }

        // 只能在未赋值的条目上进行更新
        ContentValues cv = new ContentValues();
        c.moveToNext();

        String state = values.getAsString(CashTradeInfoProvider.KEY_TRADESTATE);
        if (state != null)
            cv.put(CashTradeInfoProvider.KEY_TRADESTATE, state);

        if (c.isNull(c.getColumnIndex(CashTradeInfoProvider.KEY_BTC))) {
            Double temp = values.getAsDouble(CashTradeInfoProvider.KEY_BTC);
            if (temp != null)
                cv.put(CashTradeInfoProvider.KEY_BTC, temp);
        }
        if (c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_CASH)) == null) {
            String temp = values.getAsString(CashTradeInfoProvider.KEY_CASH);
            if (temp != null)
                cv.put(CashTradeInfoProvider.KEY_CASH, temp);
        }
        if (c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_EXCHANGERATE)) == null) {
            String temp = values.getAsString(CashTradeInfoProvider.KEY_EXCHANGERATE);
            if (temp != null)
                cv.put(CashTradeInfoProvider.KEY_EXCHANGERATE, temp);
        }
        if (c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_HCHARGEPROP)) == null) {
            String temp = values.getAsString(CashTradeInfoProvider.KEY_HCHARGEPROP);
            if (temp != null)
                cv.put(CashTradeInfoProvider.KEY_HCHARGEPROP, temp);
        }
        if (c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_BTCQRSTR)) == null) {
            String temp = values.getAsString(CashTradeInfoProvider.KEY_BTCQRSTR);
            if (temp != null)
                cv.put(CashTradeInfoProvider.KEY_BTCQRSTR, temp);
        }
        if (c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_CASHQRSTR)) == null) {
            String temp = values.getAsString(CashTradeInfoProvider.KEY_CASHQRSTR);
            if (temp != null)
                cv.put(CashTradeInfoProvider.KEY_CASHQRSTR, temp);
        }
        if (c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_PAYERADDR)) == null) {
            String temp = values.getAsString(CashTradeInfoProvider.KEY_PAYERADDR);
            if (temp != null)
                cv.put(CashTradeInfoProvider.KEY_PAYERADDR, temp);
        }
        if (c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_TRANSHASH)) == null) {
            String temp = values.getAsString(CashTradeInfoProvider.KEY_TRANSHASH);
            if (temp != null)
                cv.put(CashTradeInfoProvider.KEY_TRANSHASH, temp);
        }
        if (c.getString(c.getColumnIndex(CashTradeInfoProvider.KEY_CHECKOUTTIME)) == null) {
            String temp = values.getAsString(CashTradeInfoProvider.KEY_CHECKOUTTIME);
            if (temp != null)
                cv.put(CashTradeInfoProvider.KEY_CHECKOUTTIME, temp);
        }
        if (values.containsKey(CashTradeInfoProvider.KEY_CONFDEPTH)) {
            int newCommit = values.getAsInteger(CashTradeInfoProvider.KEY_CONFDEPTH).intValue();
            int oldCommit = c.getInt(c.getColumnIndex(CashTradeInfoProvider.KEY_CONFDEPTH));
            if (newCommit > oldCommit) // 所更新的commit值需要比已有的大，否则不更新
                cv.put(CashTradeInfoProvider.KEY_CONFDEPTH, values.getAsInteger(CashTradeInfoProvider.KEY_CONFDEPTH));
        }

        c.close();

        if (cv.size() == 0) {
            dLog("update return: 0");
            return 0;
        }

        // 忽略selection和selectionArgs
        final int count = helper.getWritableDatabase().update(DATABASE_TABLE, cv, KEY_TRADEID + "=?", new String[]{id});

        if (count < 0)
            throw new SQLException("updating " + uri.toString() + " error");

        dLog("update input: " + uri.toString() + ", values: " + getValueString(values));
        dLog("update return: " + count);

        return count;
    }

    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        throw new UnsupportedOperationException(); //不允许删除交易记录
    }

    @Override
    public Cursor query(@Nonnull final Uri uri, final String[] projection, final String originalSelection, final String[] originalSelectionArgs,
                        final String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DATABASE_TABLE);

        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() > 1)
            throw new IllegalArgumentException(uri.toString());

        String selection = null;
        String[] selectionArgs = null;

        if (pathSegments.size() == 1) // 使用按TRADEID查询方式时，selection和selectionArgs无效
        {
            final String tradeID = uri.getLastPathSegment();

            qb.appendWhere(KEY_TRADEID + "=");
            qb.appendWhereEscapeString(tradeID);

            dLog("query input: " + uri.toString());
        } else {
            selection = originalSelection;
            selectionArgs = originalSelectionArgs;

            dLog("query input: " + uri.toString());
        }

        final Cursor cursor = qb.query(helper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

        if (cursor == null) {
            dLog("query return: cursor == null");
        } else {
            dLog("query return: cursor != null");
        }

        return cursor;
    }

    private static String getValueString(@Nonnull ContentValues values) {
        StringBuilder sb = new StringBuilder();

        if (values.containsKey(KEY_TRADEID))
            sb.append(values.getAsString(KEY_TRADEID)).append(", ");
        else if (values.containsKey(KEY_TRADESTATE))
            sb.append(values.getAsString(KEY_TRADESTATE)).append(", ");
        else if (values.containsKey(KEY_BTC))
            sb.append(values.getAsString(KEY_BTC)).append(", ");
        else if (values.containsKey(KEY_CASH))
            sb.append(values.getAsString(KEY_CASH)).append(", ");
        else if (values.containsKey(KEY_EXCHANGERATE))
            sb.append(values.getAsString(KEY_EXCHANGERATE)).append(", ");
        else if (values.containsKey(KEY_BTCQRSTR))
            sb.append(values.getAsString(KEY_BTCQRSTR)).append(", ");
        else if (values.containsKey(KEY_CASHQRSTR))
            sb.append(values.getAsString(KEY_CASHQRSTR)).append(", ");
        else if (values.containsKey(KEY_PAYERADDR))
            sb.append(values.getAsString(KEY_PAYERADDR)).append(", ");
        else if (values.containsKey(KEY_TRANSHASH))
            sb.append(values.getAsString(KEY_TRANSHASH)).append(", ");
        else if (values.containsKey(KEY_TRANSHASH))
            sb.append(values.getAsString(KEY_TRANSHASH)).append(", ");
        else if (values.containsKey(KEY_CONFDEPTH))
            sb.append(values.getAsString(KEY_CONFDEPTH)).append(", ");
        else if (values.containsKey(KEY_CHECKOUTTIME))
            sb.append(values.getAsString(KEY_CHECKOUTTIME)).append(", ");

        return sb.toString();
    }


    private static String getStringfromStringArray(@Nonnull String[] StrArr) {
        StringBuilder sb = new StringBuilder();

        for (String str : StrArr) {
            sb.append(str).append(":");
        }

        return sb.toString();
    }


    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
