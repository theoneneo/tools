/*
 * Copyright 2011-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mybitcoin.wallet.ui;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.CheckForNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
//import android.provider.Telephony.Mms.Rate;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.mybitcoin.wallet.Configuration;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.ExchangeRatesProvider;
import com.mybitcoin.wallet.ExchangeRatesProvider.ExchangeRate;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.service.BlockchainService;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.WalletUtils;

/**
 * @author Andreas Schildbach
 */
public final class ExchangeRatesFragment extends SherlockFragment {
    private AbstractWalletActivity activity;
    private WalletApplication application;
    private Configuration config;
    private Wallet wallet;
    private LoaderManager loaderManager;

    private ExchangeRatesAdapter adapter;

    private BigInteger balance = null;
    private boolean replaying = false;
    @CheckForNull
    private String defaultCurrency = null;

    private static final int ID_BALANCE_LOADER = 0;
    private static final int ID_RATE_LOADER = 1;

    private boolean timerFlag;

    private static final Logger log = LoggerFactory.getLogger(ExchangeRatesFragment.class);

    public static final String CNY = "CNY";
    public static final String JPY = "JPY";
    public static final String USD = "USD";
    public static final String AUD = "AUD";
    private BigInteger rateBase = GenericUtils.ONE_BTC;

    CurrencyTextView rateViewCNY;//,rateViewUSD,rateViewJPY;
    
    private TextView btnCancel;
    private TextView btnNext;

    BigInteger avaiableBitcoinAmount = null;
    public static final BigInteger handYan = new BigInteger("10000000000000000");

    public static String rate;
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        this.activity = (AbstractWalletActivity) activity;
        this.application = (WalletApplication) activity.getApplication();
        this.config = application.getConfiguration();
        this.wallet = application.getWallet();
        this.loaderManager = getLoaderManager();
        this.timerFlag = true;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

//		adapter = new ExchangeRatesAdapter(activity);

        loaderManager.initLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);

        defaultCurrency = config.getExchangeCurrencyCode();

        avaiableBitcoinAmount = wallet.getBalance(BalanceType.AVAILABLE);
        log.info("local btc's origin is :" + avaiableBitcoinAmount);

//        log.info("local btc's amount is :"+GenericUtils.formatValue(avaiableBitcoinAmount, config.getBtcMaxPrecision(), config.getBtcShift()));

        //定时刷新汇率
        new RefreshExRateThread().start();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.exchange_rate_row, container, false);
       // LinearLayout cnyRow = (LinearLayout) view.findViewById(R.id.cny_rate_row);
//        log.info("cnyRow is :"+cnyRow);
//        cnyRow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                /*if( avaiableBitcoinAmount == null ||   (avaiableBitcoinAmount.compareTo(WalletApplication.MinAvaiableBitcoinAmount) <= 0)){
//                    Toast.makeText(activity,"此ATM机内的有效比特币数量不足，暂时不能交易！",Toast.LENGTH_SHORT).show();
//
//                    return;
//                }*/
//
//                if (rateViewCNY.getAmount() != null && rateViewCNY.getAmount().floatValue() > 0) {
////                    Intent intent = new Intent(activity,ShowNotesActivity.class);
//                    Intent intent = new Intent(activity, SelectOperationActivity.class);
//                    activity.startActivity(intent);
////                    activity.finish();
//                }
//            }
//        });
        
         btnCancel = (TextView)view.findViewById(R.id.btnCancel);
         btnNext = (TextView)view.findViewById(R.id.btnNext);
         btnNext.setVisibility(View.INVISIBLE);
        btnCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				activity.finish();
			}
        });
        btnNext.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if (rateViewCNY.getAmount() != null && rateViewCNY.getAmount().floatValue() > 0) {
					
					rate = rateViewCNY.getAmountText().toString();
					Intent intent = new Intent(activity, SelectOperationActivity.class);
					activity.startActivity(intent);  
				}
			}
        });

        rateViewCNY = (CurrencyTextView) view.findViewById(R.id.exchange_rate_row_cny);
        rateViewCNY.setPrecision(Constants.LOCAL_PRECISION, 0);
//        rateViewUSD = (CurrencyTextView) view.findViewById(R.id.exchange_rate_row_usd);
//        rateViewUSD.setPrecision(Constants.LOCAL_PRECISION, 0);

//        rateViewJPY = (CurrencyTextView) view.findViewById(R.id.exchange_rate_row_jpy);
//        rateViewJPY.setPrecision(Constants.LOCAL_PRECISION, 0);

        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        activity.registerReceiver(broadcastReceiver, new IntentFilter(BlockchainService.ACTION_BLOCKCHAIN_STATE));

        loaderManager.initLoader(ID_BALANCE_LOADER, null, balanceLoaderCallbacks);

        updateView();
        this.timerFlag = true;
    }

    @Override
    public void onPause() {
        loaderManager.destroyLoader(ID_BALANCE_LOADER);

        activity.unregisterReceiver(broadcastReceiver);
        this.timerFlag = false;

        super.onPause();
    }

    @Override
    public void onDestroy() {
        loaderManager.destroyLoader(ID_RATE_LOADER);
        this.timerFlag = false;


        super.onDestroy();
    }

    //@Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final Cursor cursor = (Cursor) adapter.getItem(position);
        final ExchangeRate exchangeRate = ExchangeRatesProvider.getExchangeRate(cursor);
        config.setCachedExchangeRate(exchangeRate);
        config.setExchangeCurrencyCode(exchangeRate.currencyCode);
        Intent intent = new Intent(activity, ShowNotesActivity.class);
        activity.startActivity(intent);
//        activity.finish();

    }

    //exchange rate refresh thread
    public class RefreshExRateThread extends Thread {
        @Override
        public void run() {

            while (timerFlag && (loaderManager != null)) {
                try {
                    Thread.sleep(15000);

                    activity.runOnUiThread(new Runnable() {
                        public void run() {

                            if (loaderManager != null) {
                                loaderManager.destroyLoader(ID_RATE_LOADER);
                                loaderManager.restartLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);
                            }
                        }
                    });

                    log.info("Refresh echangerate.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //	@Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (Configuration.PREFS_KEY_EXCHANGE_CURRENCY.equals(key) || Configuration.PREFS_KEY_BTC_PRECISION.equals(key)) {
            defaultCurrency = config.getExchangeCurrencyCode();

            updateView();
        }
    }

    private void updateView() {
        balance = application.getWallet().getBalance(BalanceType.ESTIMATED);

        if (adapter != null) {
            final int btcShift = config.getBtcShift();

            final BigInteger base = btcShift == 0 ? GenericUtils.ONE_BTC : GenericUtils.ONE_MBTC;

            adapter.setRateBase(base);
        }
    }

    private final BlockchainBroadcastReceiver broadcastReceiver = new BlockchainBroadcastReceiver();

    private final class BlockchainBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            replaying = intent.getBooleanExtra(BlockchainService.ACTION_BLOCKCHAIN_STATE_REPLAYING, false);

            updateView();
        }
    }

    private final LoaderCallbacks<Cursor> rateLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            log.info("loader oncreate.");
            String selection = ExchangeRatesProvider.KEY_CURRENCY_CODE + "='CNY'";
//            String selection = ExchangeRatesProvider.KEY_CURRENCY_CODE + "='AUD'";
            return new CursorLoader(activity, ExchangeRatesProvider.contentUri(activity.getPackageName()), null, null, null, null);
        }

        @Override
        public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
            log.info("onLoadFinished");
//            String loaderData = loader.dataToString(data);

//            log.info("loaderData is :"+loaderData);
            if (data != null) {
                log.info("rate'count is :" + data.getCount());
                int currencyId = data.getColumnIndexOrThrow(ExchangeRatesProvider.KEY_CURRENCY_CODE);
                int rateId = data.getColumnIndexOrThrow(ExchangeRatesProvider.KEY_RATE);
                String rate;
                ExchangeRate exchangeRate;
                log.info("");
                if (data.getCount() >= 1) {
                    data.moveToFirst();
//                while(data.moveToNext()){
                    String curCode = data.getString(currencyId);
                    rate = data.getString(rateId);
                    exchangeRate = ExchangeRatesProvider.getExchangeRate(data);
                    log.info("curCode  is :" + curCode);

                    if (curCode.equals(CNY)) {
                        rateViewCNY.setAmount(WalletUtils.localValue(rateBase, exchangeRate.rate));
                        System.out.println(rateViewCNY.getAmount()+"----------------------------------------->"+rateViewCNY.getAmountText());
                        
                       rateViewCNY.setAmountText(true);
                        System.out.println(rateViewCNY.getAmount()+"----------------------------------------->"+rateViewCNY.getAmountText());
                        /*
                         * 
                         * 
                         */
                        
                        btnNext.setVisibility(View.VISIBLE);
                        
                        config.setCachedExchangeRate(exchangeRate);
                        config.setExchangeCurrencyCode(exchangeRate.currencyCode);

                        WalletApplication.MinAvaiableBitcoinAmount = handYan.divide(WalletUtils.localValue(rateBase, exchangeRate.rate));
                        log.info("CNY rate is :" + WalletUtils.localValue(rateBase, exchangeRate.rate));
                        log.info("100 yan  bitcoins  is :" + WalletApplication.MinAvaiableBitcoinAmount);
                        log.info("new rate is :" + rateViewCNY.getAmount().floatValue());
                    } else {
                        log.info("curCode is :" + curCode);
                    }
                }
            }


        }

        @Override
        public void onLoaderReset(final Loader<Cursor> loader) {
            log.info("loader reset");
//            loader.abandon();//loader.reset();
            loader.forceLoad();
        }

        private int findCurrencyCode(final Cursor cursor, final String currencyCode) {
            final int currencyCodeColumn = cursor.getColumnIndexOrThrow(ExchangeRatesProvider.KEY_CURRENCY_CODE);

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                if (cursor.getString(currencyCodeColumn).equals(currencyCode))
                    return cursor.getPosition();
            }

            return -1;
        }
    };

    private final LoaderCallbacks<BigInteger> balanceLoaderCallbacks = new LoaderManager.LoaderCallbacks<BigInteger>() {
        @Override
        public Loader<BigInteger> onCreateLoader(final int id, final Bundle args) {
            return new WalletBalanceLoader(activity, wallet);
        }

        @Override
        public void onLoadFinished(final Loader<BigInteger> loader, final BigInteger balance) {
            ExchangeRatesFragment.this.balance = balance;

            updateView();
        }

        @Override
        public void onLoaderReset(final Loader<BigInteger> loader) {
        }
    };

    private final class ExchangeRatesAdapter extends ResourceCursorAdapter {
        private BigInteger rateBase = GenericUtils.ONE_BTC;

        private ExchangeRatesAdapter(final Context context) {
            super(context, R.layout.exchange_rate_row, null, true);
        }

        public void setRateBase(final BigInteger rateBase) {
            this.rateBase = rateBase;

            notifyDataSetChanged();
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            final ExchangeRate exchangeRate = ExchangeRatesProvider.getExchangeRate(cursor);
            final boolean isDefaultCurrency = exchangeRate.currencyCode.equals(defaultCurrency);

//			view.setBackgroundResource(isDefaultCurrency ? R.color.bg_list_selected : R.color.bg_list);
//            view.setBackgroundResource(isDefaultCurrency ? R.color.bg_list_selected : R.color.bg_list);

			/*final View defaultView = view.findViewById(R.id.exchange_rate_row_default);
                defaultView.setVisibility(isDefaultCurrency ? View.VISIBLE : View.INVISIBLE);
            */
			/*final TextView currencyCodeView = (TextView) view.findViewById(R.id.exchange_rate_row_currency_code);
			currencyCodeView.setText(exchangeRate.currencyCode);

			final CurrencyTextView rateView = (CurrencyTextView) view.findViewById(R.id.exchange_rate_row_rate);
			rateView.setPrecision(Constants.LOCAL_PRECISION, 0);
			rateView.setAmount(WalletUtils.localValue(rateBase, exchangeRate.rate));*/

			/*final CurrencyTextView walletView = (CurrencyTextView) view.findViewById(R.id.exchange_rate_row_balance);
			walletView.setPrecision(Constants.LOCAL_PRECISION, 0);
			if (!replaying)
			{
				walletView.setAmount(WalletUtils.localValue(balance, exchangeRate.rate));
				walletView.setStrikeThru(Constants.TEST);
			}
			else
			{
				walletView.setText("n/a");
				walletView.setStrikeThru(false);
			}
			walletView.setTextColor(getResources().getColor(R.color.fg_less_significant));*/
        }
    }
}
