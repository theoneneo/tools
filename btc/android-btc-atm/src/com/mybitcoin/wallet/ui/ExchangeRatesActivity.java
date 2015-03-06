/*
 * Copyright 2012-2014 the original author or authors.
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

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.google.bitcoin.core.Address;

import com.actionbarsherlock.view.MenuItem;
import com.mybitcoin.wallet.Configuration;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.environment.UiInfo;

import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.ECKey;
import com.mybitcoin.wallet.util.WalletUtils;


/**
 * @author Andreas Schildbach
 */
public final class ExchangeRatesActivity extends AbstractWalletActivity
{

    private static final int DIALOG_CHANGELOG = 2;
    private static final int DIALOG_TIMESKEW_ALERT = 3;
    private static final int DIALOG_VERSION_ALERT = 4;
    private static final int DIALOG_LOW_STORAGE_ALERT = 5;




    private boolean timerFlag;

    private WalletApplication application;
    private Configuration config;
    private Wallet wallet;

    @Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();

        setLayout(R.layout.exchange_rates_content);

        config.touchLastUsed();

        final Address localAddress = WalletUtils.pickOldestKey(wallet).toAddress(Constants.NETWORK_PARAMETERS);
        log.info("The address of ATM  is :"+localAddress.toString());

        for (final ECKey key : wallet.getKeys())
            if (key.getPrivKeyBytes() == null)
                throw new Error("found read-only key, but wallet is likely an encrypted wallet from the future");
            else if (!wallet.isKeyRotating(key))
                log.info("private key is "+key.getPrivateKeyEncoded(Constants.NETWORK_PARAMETERS).toString());

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
    public class TimeThread extends Thread{
        @Override
        public void run(){
            while(timerFlag){
                try{
                    Thread.sleep(3000);
                    /*Message message = new Message();
                    message.what = msgKey1;
                    mHandler.sendMessage(message);*/
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case 555:
                	init();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onStop(){
        super.onStop();
        timerFlag = false;
//        log.info("timerThread stop.");
    }
    @Override
    protected void onResume()
    {
        super.onResume();

        application.startBlockchainService(false);

//        checkLowStorageAlert();
        timerFlag = true;
        
        init();
        new Thread(new UpdateText()).start();
    }
    
    public void init(){
    	super.init();
    	UiInfo uiInfo = new UiInfo(this);
    	
    	TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
    	
    	TextView tvTitle = (TextView)findViewById(R.id.exchangerate_title);
    	TextView tvHint = (TextView)findViewById(R.id.exchangerate_hint);
    	
    	TextView exchangerate_btc_abb = (TextView)findViewById(R.id.exchangerate_btc_abb);
    	TextView exchangerate_cash_abb = (TextView)findViewById(R.id.exchangerate_cash_abb);
    	
    	TextView tvCancel = (TextView)findViewById(R.id.btnCancel) ;
    	TextView tvNext = (TextView)findViewById(R.id.btnNext);
    	
    	
    	txtTitle.setText(uiInfo.getTextByName(UiInfo.TRADEMODE_BTC_BTN));
    	tvTitle.setText(uiInfo.getTextByName(UiInfo.CASHTRADE_EXCHANGERATE_TITLE));
    	tvHint.setText(uiInfo.getTextByName(UiInfo.CASHTRADE_EXCHANGERATE_HINT));
    	exchangerate_btc_abb.setText(uiInfo.getTextByName(UiInfo.COMMON_BTC_ABB));
    	exchangerate_cash_abb.setText(uiInfo.getTextByName(UiInfo.COMMON_CASH_ABB));
    	tvCancel.setText(uiInfo.getTextByName(UiInfo.COMMON_CNL_BTN));
    	tvNext.setText(uiInfo.getTextByName(UiInfo.COMMON_NXT_BTN));
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	flag = false;
    }
    boolean flag = true;
    class UpdateText implements Runnable{
		@Override
		public void run() {
			while(flag){
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(555);
			}
		}
    }

    @Override
    protected Dialog onCreateDialog(final int id, final Bundle args)
    {

         if (id == DIALOG_CHANGELOG)
            return createChangeLogDialog();
        else if (id == DIALOG_TIMESKEW_ALERT)
            return createTimeskewAlertDialog(args.getLong("diff_minutes"));
        else if (id == DIALOG_VERSION_ALERT)
            return createVersionAlertDialog();
        else if (id == DIALOG_LOW_STORAGE_ALERT)
            return createLowStorageAlertDialog();
        else
            throw new IllegalArgumentException();
    }
    private Dialog createChangeLogDialog()
    {
        final DialogBuilder dialog = DialogBuilder.warn(this, R.string.wallet_precision_warning_dialog_title);
        dialog.setMessage(R.string.wallet_precision_warning_dialog_msg);
        dialog.setPositiveButton(R.string.button_dismiss, null);
        dialog.setNegativeButton(R.string.button_settings, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, final int id)
            {
                startActivity(new Intent(ExchangeRatesActivity.this, PreferencesActivity.class));
            }
        });
        return dialog.create();
    }

    private Dialog createLowStorageAlertDialog()
    {
        final DialogBuilder dialog = DialogBuilder.warn(this, R.string.wallet_low_storage_dialog_title);
        dialog.setMessage(R.string.wallet_low_storage_dialog_msg);
        dialog.setPositiveButton(R.string.wallet_low_storage_dialog_button_apps, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(final DialogInterface dialog, final int id)
            {
                startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                finish();
            }
        });
        dialog.setNegativeButton(R.string.button_dismiss, null);
        return dialog.create();
    }
    private Dialog createTimeskewAlertDialog(final long diffMinutes)
    {
        final PackageManager pm = getPackageManager();
        final Intent settingsIntent = new Intent(android.provider.Settings.ACTION_DATE_SETTINGS);

        final DialogBuilder dialog = DialogBuilder.warn(this, R.string.wallet_timeskew_dialog_title);
        dialog.setMessage(getString(R.string.wallet_timeskew_dialog_msg, diffMinutes));

        if (pm.resolveActivity(settingsIntent, 0) != null)
        {
            dialog.setPositiveButton(R.string.button_settings, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    startActivity(settingsIntent);
                    finish();
                }
            });
        }

        dialog.setNegativeButton(R.string.button_dismiss, null);
        return dialog.create();
    }

    private Dialog createVersionAlertDialog()
    {
       /* final PackageManager pm = getPackageManager();
        final Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Constants.MARKET_APP_URL, getPackageName())));
        final Intent binaryIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BINARY_URL));

        final DialogBuilder dialog = DialogBuilder.warn(this, R.string.wallet_version_dialog_title);
        final StringBuilder message = new StringBuilder(getString(R.string.wallet_version_dialog_msg));
        if (Build.VERSION.SDK_INT < Constants.SDK_DEPRECATED_BELOW)
            message.append("\n\n").append(getString(R.string.wallet_version_dialog_msg_deprecated));
        dialog.setMessage(message);

        if (pm.resolveActivity(marketIntent, 0) != null)
        {
            dialog.setPositiveButton(R.string.wallet_version_dialog_button_market, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    startActivity(marketIntent);
                    finish();
                }
            });
        }

        if (pm.resolveActivity(binaryIntent, 0) != null)
        {
            dialog.setNeutralButton(R.string.wallet_version_dialog_button_binary, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    startActivity(binaryIntent);
                    finish();
                }
            });
        }

        dialog.setNegativeButton(R.string.button_dismiss, null);
        return dialog.create();*/
        return null;
    }

}
