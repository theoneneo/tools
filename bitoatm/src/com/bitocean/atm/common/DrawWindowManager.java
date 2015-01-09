/**
 * 
 */
package com.bitocean.atm.common;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitocean.atm.R;
import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.util.Util;

/**
 * @author bing.liu
 * 
 */
public class DrawWindowManager {
	private Context mContext;
	private WindowManager mWManager;
	private ImageView wifiImageView;
	private TextView timerTextView;

	public DrawWindowManager(Context context) {
		mContext = context;
		mWManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
	}

	public void updateNetworkStatus(NetworkInfo info) {
		if (info != null && info.isAvailable()) {
			AppManager.isNetEnable = true;
			showNetWorkStatus(info.isAvailable());
		} else {
			AppManager.isNetEnable = false;
			if (info != null)
				showNetWorkStatus(info.isAvailable());
			new Util(mContext).showFeatureToast(mContext
					.getString(R.string.network_error));
		}
	}

	public void showNetWorkStatus(boolean isAvailable) {
		if (wifiImageView != null) {
			if (isAvailable)
				wifiImageView.setImageResource(R.drawable.logo);
			else
				wifiImageView.setImageBitmap(null);
			wifiImageView.invalidate();
		} else {
			wifiImageView = new ImageView(mContext);
			wifiImageView.setImageResource(R.drawable.logo);
			wifiImageView.invalidate();
			WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
			wmParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

			wmParams.type = LayoutParams.TYPE_PHONE;

			wmParams.format = PixelFormat.RGBA_8888;

			wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;

			wmParams.gravity = Gravity.RIGHT | Gravity.TOP;

			wmParams.x = 100;//Util.dip2px(mContext, 5);
			wmParams.y = 100;//Util.dip2px(mContext, 5);

			wmParams.width = 80;//Util.dip2px(mContext, 30);
			wmParams.height = 80;//Util.dip2px(mContext, 30);
			mWManager.addView(wifiImageView, wmParams);
		}
	}

	public void removeNetWorkStatus() {
		if (mWManager != null && wifiImageView != null)
			mWManager.removeView(wifiImageView);
	}

	public void showTimer(boolean isAvailable) {
		if (timerTextView != null) {
			if (isAvailable) {
				timerTextView.setVisibility(View.VISIBLE);
				timerTextView.setText(AppManager.loopTimer + "");
			} else {
				timerTextView.setVisibility(View.INVISIBLE);
			}
			timerTextView.invalidate();
		} else {
			timerTextView = new TextView(mContext);
			if (isAvailable) {
				timerTextView.setVisibility(View.VISIBLE);
				timerTextView.setText(AppManager.loopTimer + "");
			} else {
				timerTextView.setVisibility(View.INVISIBLE);
			}
			timerTextView.invalidate();
			WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
			wmParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

			wmParams.type = LayoutParams.TYPE_PHONE;

			wmParams.format = PixelFormat.RGBA_8888;

			wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;

			wmParams.gravity = Gravity.RIGHT | Gravity.TOP;

			wmParams.x = Util.dip2px(mContext, 20);
			wmParams.y = Util.dip2px(mContext, 20);

			wmParams.width = Util.dip2px(mContext, 10);
			wmParams.height = Util.dip2px(mContext, 10);
			mWManager.addView(timerTextView, wmParams);
		}
	}

	public void removeTimer() {
		if (mWManager != null && timerTextView != null)
			mWManager.removeView(timerTextView);
	}
}
