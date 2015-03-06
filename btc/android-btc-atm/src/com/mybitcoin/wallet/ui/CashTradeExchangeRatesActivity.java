/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.mybitcoin.wallet.*;
import com.mybitcoin.wallet.ExchangeRatesProvider.ExchangeRate;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.WalletUtils;
import com.umeng.analytics.MobclickAgent;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public class CashTradeExchangeRatesActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeExchangeRatesActivity";
    private static final boolean DEBUG_FLAG = true;

    private final long REFRESH_PERIOD_SEC = 10; //每隔10秒更新一次汇率

    private Activity activity;
    private Configuration config;
    private Wallet wallet;
    private LoaderManager loaderManager;

    private TextView mNxtBtn;
    private CurrencyTextView rateView;

    private static final int ID_RATE_LOADER = 1;

    public static final String CNY = "CNY";
    private BigInteger rateBase = GenericUtils.ONE_BTC;

    private BigInteger avaiableBitcoinAmount = null;
    private static final BigInteger handCNY = new BigInteger("10000000000000000");

    private ExchangeRateRefresherThread refresherThread;
    
    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_exchange_rate);

        rateView = (CurrencyTextView) findViewById(R.id.cashtrade_exchangerate_cash_txt);
        rateView.setPrecision(Constants.LOCAL_PRECISION, 0);

        mNxtBtn = (TextView) findViewById(R.id.cashtrade_exchangerate_nxt_btn);
        mNxtBtn.setVisibility(View.INVISIBLE); //初始化不显示“下一步”按钮
        mNxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String exchangeRateText = rateView.getAmountText();
                if (exchangeRateText != null) {
                    try {
                        if (Double.parseDouble(exchangeRateText) > 0) {
                            String tradeID = CashTradeInfoLog.genCashTradeID();
                            CashTradeInfoLog cti = new CashTradeInfoLog(CashTradeExchangeRatesActivity.this.getContentResolver(), tradeID);
                            cti.setInitingState(null, null, exchangeRateText, String.valueOf(getSettingInfo().getHandlingChargeProportion()));

                            dLog("trade id: " + tradeID + ", exchangerate: " + exchangeRateText);

                            cti.startActivityFromActivity(CashTradeExchangeRatesActivity.this, CashTradeInputActivity.class);
                        }
                    } catch (NumberFormatException e) {
                        dLog("error when parsing exchangerate on cliking");
                    }
                }
            }
        });

        TextView cnlBtn = (TextView) findViewById(R.id.cashtrade_exchangerate_cnl_btn);
        cnlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(TradeModeActivity.class);
            }
        });

        activity = this;
        config = getWalletApplication().getConfiguration();
        wallet = getWalletApplication().getWallet();
        loaderManager = getLoaderManager();

        loaderManager.initLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);

        avaiableBitcoinAmount = wallet.getBalance(BalanceType.AVAILABLE);
        dLog("local btc's origin is :" + avaiableBitcoinAmount);
    }

    private final LoaderCallbacks<Cursor> rateLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            dLog("loader oncreate.");
            String selection = ExchangeRatesProvider.KEY_CURRENCY_CODE
                    + "='CNY'";
            return new CursorLoader(
                    activity,
                    ExchangeRatesProvider.contentUri(activity.getPackageName()),
                    null, null, null, null);
        }

        @Override
        public void onLoadFinished(final Loader<Cursor> loader,
                                   final Cursor data) {
            dLog("onLoadFinished");
            if (data != null) {
                dLog("rate'count is :" + data.getCount());
                int currencyId = data
                        .getColumnIndexOrThrow(ExchangeRatesProvider.KEY_CURRENCY_CODE);
                int rateId = data
                        .getColumnIndexOrThrow(ExchangeRatesProvider.KEY_RATE);
                String rate;
                ExchangeRate exchangeRate;
                if (data.getCount() >= 1) {
                    data.moveToFirst();
                    String curCode = data.getString(currencyId);
                    rate = data.getString(rateId);
                    exchangeRate = ExchangeRatesProvider.getExchangeRate(data);
                    dLog("curCode  is :" + curCode);

                    if (curCode.equals(CNY)) {
                        rateView.setAmount(WalletUtils.localValue(rateBase,
                                exchangeRate.rate));
                        
                        rateView.setAmountText(false);
                        System.out.println(rateView.getAmount()+"-------------------------------------->"+rateView.getAmountText());
                        
                        config.setCachedExchangeRate(exchangeRate);
                        config.setExchangeCurrencyCode(exchangeRate.currencyCode);

                        WalletApplication.MinAvaiableBitcoinAmount = handCNY
                                .divide(WalletUtils.localValue(rateBase,
                                        exchangeRate.rate));
                        dLog("CNY rate is :"
                                + WalletUtils.localValue(rateBase,
                                exchangeRate.rate));
                        dLog("100 yan  bitcoins  is :"
                                + WalletApplication.MinAvaiableBitcoinAmount);
                        dLog("new rate is :"
                                + rateView.getAmount().floatValue());
                    } else {
                        dLog("curCode is :" + curCode);
                    }
                } else {
                    rateView.setText("");
                }
            } else {
                rateView.setText("");
            }

            // 如果正确获得交易汇率，则将“下一步”按钮设置为可见状态，反之设为不可见状态
            if(!rateView.getText().equals(""))
                mNxtBtn.setVisibility(View.VISIBLE);
            else
                mNxtBtn.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onLoaderReset(final Loader<Cursor> loader) {
            dLog("loader reset");
            loader.forceLoad();
        }

        private int findCurrencyCode(final Cursor cursor,
                                     final String currencyCode) {
            final int currencyCodeColumn = cursor
                    .getColumnIndexOrThrow(ExchangeRatesProvider.KEY_CURRENCY_CODE);

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                if (cursor.getString(currencyCodeColumn).equals(currencyCode))
                    return cursor.getPosition();
            }

            return -1;
        }
    };

    private class ExchangeRateRefresherThread extends Thread {
        @Override
        public void run() {
            while (loaderManager != null) {
                try {
                    Thread.sleep(REFRESH_PERIOD_SEC * 1000);

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (loaderManager != null) {
                                loaderManager.destroyLoader(ID_RATE_LOADER);
                                loaderManager.restartLoader(ID_RATE_LOADER,
                                        null, rateLoaderCallbacks);
                            }
                        }
                    });

                    dLog("Refresh exchangeRate.");
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

    }

    public void startRefresher() {
        if (refresherThread != null && !refresherThread.isInterrupted())
            refresherThread.interrupt();

        refresherThread = new ExchangeRateRefresherThread();
        refresherThread.start();
        dLog("ExchangeRateRefresherThread started in " + activity.toString());
    }

    public void endRefresher() {
        refresherThread.interrupt();
        dLog("ExchangeRateRefresherThread ended in " + activity.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        startRefresher();
        dLog("startRefresher is called in onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        endRefresher();
        dLog("endRefresher is called in onPause");
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_exchangerate_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_exchangerate_title);
        TextView hint = (TextView) findViewById(R.id.cashtrade_exchangerate_hint);
        TextView btc = (TextView) findViewById(R.id.cashtrade_exchangerate_btc_abb);
        TextView cash = (TextView) findViewById(R.id.cashtrade_exchangerate_cash_abb);
        TextView cancel = (TextView) findViewById(R.id.cashtrade_exchangerate_cnl_btn);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_EXCHANGERATE_TITLE));
        hint.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_EXCHANGERATE_HINT));
        btc.setText(getUiInfo().getTextByName(UiInfo.COMMON_BTC_ABB));
        cash.setText(getUiInfo().getTextByName(UiInfo.COMMON_CASH_ABB));
        cancel.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
        mNxtBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_NXT_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
