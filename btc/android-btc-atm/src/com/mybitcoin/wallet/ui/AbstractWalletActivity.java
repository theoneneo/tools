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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

/**
 * @author Andreas Schildbach
 */
public abstract class AbstractWalletActivity extends SherlockFragmentActivity
{
	private WalletApplication application;

	protected static final Logger log = LoggerFactory.getLogger(AbstractWalletActivity.class);

    /*private TextView mTime;
    private static final int msgKey1 = 1;*/

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		application = (WalletApplication) getApplication();

		super.onCreate(savedInstanceState);
       /* requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);
        final View view = getLayoutInflater().inflate(R.layout.customtitle, null);

        mTime = (TextView)view.findViewById(R.id.timer);
        mTime.setText("adfasfasfafa");
        log.info("mTime is :"+mTime);
        new TimeThread().start();*/


    }
    /*public class TimeThread extends Thread{
        @Override
        public void run(){
            while(true){
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
                    CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss",sysTime);
                    mTime.setText(sysTimerStr);
                    mTime.setTextColor(R.color.fg_significant);
                    log.info("当前时间："+sysTimerStr);
                    break;
                default:
                    break;
            }
        }
    };*/
	protected WalletApplication getWalletApplication()
	{
		return application;
	}

	protected final void toast(@Nonnull final String text, final Object... formatArgs)
	{
		toast(text, 0, Toast.LENGTH_SHORT, formatArgs);
	}

	protected final void longToast(@Nonnull final String text, final Object... formatArgs)
	{
		toast(text, 0, Toast.LENGTH_LONG, formatArgs);
	}

	protected final void toast(@Nonnull final String text, final int imageResId, final int duration, final Object... formatArgs)
	{
		final View view = getLayoutInflater().inflate(R.layout.transient_notification, null);
		TextView tv = (TextView) view.findViewById(R.id.transient_notification_text);
		tv.setText(String.format(text, formatArgs));
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~liuwei: "+tv.getText());
		tv.setCompoundDrawablesWithIntrinsicBounds(imageResId, 0, 0, 0);

		final Toast toast = new Toast(this);
		toast.setView(view);
		toast.setDuration(duration);
		toast.show();
	}

	protected final void toast(final int textResId, final Object... formatArgs)
	{
		toast(textResId, 0, Toast.LENGTH_SHORT, formatArgs);
	}

	protected final void longToast(final int textResId, final Object... formatArgs)
	{
		toast(textResId, 0, Toast.LENGTH_LONG, formatArgs);
	}

	protected final void toast(final int textResId, final int imageResId, final int duration, final Object... formatArgs)
	{
		final View view = getLayoutInflater().inflate(R.layout.transient_notification, null);
		TextView tv = (TextView) view.findViewById(R.id.transient_notification_text);
		tv.setText(getString(textResId, formatArgs));
		tv.setCompoundDrawablesWithIntrinsicBounds(imageResId, 0, 0, 0);

		final Toast toast = new Toast(this);
		toast.setView(view);
		toast.setDuration(duration);
		toast.show();
	}
	
	/////////////////////////////////////
	
    private static final String LOG_TAG = "WalletActivityBase";
    private static final boolean DEBUG_FLAG = true;

    private TextView mCounterView;

    private WalletActivityTimeoutController.ActivityCountingDownEventListener mDefaultCountingDownListener = new WalletActivityTimeoutController.ActivityCountingDownEventListener() {
        @Override
        public void onCountingDown(final long timeoutSec) {
            if (mCounterView != null) {
            	AbstractWalletActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mCounterView.setText(String.valueOf(timeoutSec));
                    }
                });
            }
        }
    };

    private WalletActivityTimeoutController.ActivityTimeoutedEventListener mDefaultTimeoutedListener = new WalletActivityTimeoutController.ActivityTimeoutedEventListener() {
        @Override
        public void onTimeouted() {
            //父类默认的超时
            gotoActivity(WelcomePageActivity.class);
        }
    };
    protected void gotoActivity(@Nonnull Class<?> activityCls) {
        startActivity(new Intent(this, activityCls));
    }
    @Override
    protected void onResume() {
        super.onResume();

        mCounterView = (TextView) findViewById(R.id.timeoutbase_counter);

        WalletActivityTimeoutController.getInstance().setmCountingDownEventListener(mDefaultCountingDownListener);
        WalletActivityTimeoutController.getInstance().setTimeoutedEventListener(mDefaultTimeoutedListener);

        WalletActivityTimeoutController.getInstance().start(this);
        dLog("ActivityTimeoutController.getInstance.start is called in onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        WalletActivityTimeoutController.getInstance().end();
        dLog("ActivityTimeoutController.getInstance.end is called in onPause");
    }

    // 将超类的layout加入到R.layout.timeout_base中的timeout_base_content中
    // 使得timeout_base_counter部分由该类托管，并由ActivityTimeoutController进行超时管理
    private LinearLayout addLinearLayout(int layoutResID) {
        LinearLayout layoutBase = (LinearLayout) getLayoutInflater().inflate(R.layout.timeout_base, null);
        LinearLayout layoutBaseContent = (LinearLayout) layoutBase.findViewById(R.id.timeoutbase_content);
       

        layoutBaseContent.addView(getLayoutInflater().inflate(layoutResID, null));

        dLog("addLinearLayout add " + layoutResID + " to R.id.timeout_base_content");

        return layoutBase;
    }

    // 将组合好的layout设置为当前activity的layout
    protected void setLayout(int LayoutResID) {
        LinearLayout layout = addLinearLayout(LayoutResID);

        setContentView(layout);
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
    
    public void init(){
    	UiInfo uiInfo = new UiInfo(this);
    	 TextView timeoutBaseTitle = (TextView) findViewById(R.id.timeoutbase_title);
         timeoutBaseTitle.setText(uiInfo.getTextByName(UiInfo.COMMON_TIMEOUT_TITLE));
    }

}
