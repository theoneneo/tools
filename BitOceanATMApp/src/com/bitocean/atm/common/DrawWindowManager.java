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
			// TODO 显示网络状态图标
			showNetWorkStatus(info.isAvailable());
		} else {
			// TODO 显示网络状态图标
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
			// 设置window type
			wmParams.type = LayoutParams.TYPE_PHONE;
			// 设置图片格式，效果为背景透明
			wmParams.format = PixelFormat.RGBA_8888;
			// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
			wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
			// 调整悬浮窗显示的停靠位置为左侧置顶
			wmParams.gravity = Gravity.RIGHT | Gravity.TOP;
			// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
			wmParams.x = Util.dip2px(mContext, 5);
			wmParams.y = Util.dip2px(mContext, 5);

			// 设置悬浮窗口长宽数据
			wmParams.width = Util.dip2px(mContext, 10);
			wmParams.height = Util.dip2px(mContext, 10);
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
				timerTextView.setText(AppManager.loopTimer+"");
			} else {
				timerTextView.setVisibility(View.INVISIBLE);
			}
			timerTextView.invalidate();
		} else {
			timerTextView = new TextView(mContext);
			if (isAvailable) {
				timerTextView.setVisibility(View.VISIBLE);
				timerTextView.setText(AppManager.loopTimer+"");
			} else {
				timerTextView.setVisibility(View.INVISIBLE);
			}
			timerTextView.invalidate();
			WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
			wmParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			// 设置window type
			wmParams.type = LayoutParams.TYPE_PHONE;
			// 设置图片格式，效果为背景透明
			wmParams.format = PixelFormat.RGBA_8888;
			// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
			wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
			// 调整悬浮窗显示的停靠位置为左侧置顶
			wmParams.gravity = Gravity.RIGHT | Gravity.TOP;
			// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
			wmParams.x = Util.dip2px(mContext, 20);
			wmParams.y = Util.dip2px(mContext, 20);

			// 设置悬浮窗口长宽数据
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
