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

import javax.annotation.Nonnull;
import android.content.pm.ActivityInfo;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.TextView;

//import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.mybitcoin.wallet.PaymentIntent;
import com.mybitcoin.wallet.R;

/**
 * @author Andreas Schildbach
 */
public final class SendCoinsActivity extends AbstractBindServiceActivity
{
	public static final String INTENT_EXTRA_PAYMENT_INTENT = "payment_intent";

    private TextView mTime;
    private static final int msgKey1 = 1;
    private Thread timerThread;
    private boolean timerFlag;

    public static void start(final Context context, @Nonnull PaymentIntent paymentIntent)
	{
		final Intent intent = new Intent(context, SendCoinsActivity.class);
		intent.putExtra(INTENT_EXTRA_PAYMENT_INTENT, paymentIntent);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.send_coins_content);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);

        getWalletApplication().startBlockchainService(false);

        mTime = (TextView)findViewById(R.id.timer);
        long sysTime = System.currentTimeMillis();
        CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",sysTime);
        mTime.setText(sysTimerStr);
        timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();
        log.info("timerThread start.");
		/*final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);*/
	}
    public class TimeThread extends Thread{
        @Override
        public void run(){
            while(timerFlag){
                try{
                    Thread.sleep(1000);
                    Message message = new Message();
                    message.what = msgKey1;
                    mHandler.sendMessage(message);
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
                case msgKey1:
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",sysTime);
                    mTime.setText(sysTimerStr);
                    log.info("time2："+sysTimerStr);
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
        log.info("timerThread stop.");
    }

    @Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.send_coins_activity_options, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;

			/*case R.id.send_coins_options_help:
				HelpDialogFragment.page(getSupportFragmentManager(), R.string.help_send_coins);
				return true;*/
		}

		return super.onOptionsItemSelected(item);
	}
}
