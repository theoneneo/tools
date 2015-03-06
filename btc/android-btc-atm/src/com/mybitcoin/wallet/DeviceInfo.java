package com.mybitcoin.wallet;

import android.content.Context;
import android.util.DisplayMetrics;

public class DeviceInfo {
    private final int mWidth; // 屏幕宽度
    private final int mHeight; // 屏幕高度
    private final float mDensity; // 屏幕密度
    private final int mDensityDpi; // 密度dpi值

    private final Context mContext;

    private static DeviceInfo instance;

    /**
     * 采用懒汉式单利模式，双重加锁机制
     * 
     * @param context
     * @return
     */
    public synchronized static DeviceInfo getInstance(Context context) {
	if (instance == null) {
	    synchronized (DeviceInfo.class) {
		instance = new DeviceInfo(context);
	    }
	    return instance;
	}
	// 当横竖屏切换时，重新获取屏幕参数
	else {
	    if (context.getResources().getDisplayMetrics().widthPixels != instance
		    .getScreenWidth()) {
		synchronized (DeviceInfo.class) {
		    instance = new DeviceInfo(context);
		}
	    }
	}
	return instance;
    }

    private DeviceInfo(Context context) {
	mContext = context;
	DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
	mWidth = metrics.widthPixels;
	mHeight = metrics.heightPixels;
	mDensity = metrics.density;
	mDensityDpi = metrics.densityDpi;
    }

    /**
     * 获取屏幕宽度
     * 
     * @return
     */
    public int getScreenWidth() {
	return mWidth;
    }

    /**
     * 获取屏幕高度
     * 
     * @return
     */
    public int getScreenHeight() {
	return mHeight;
    }

    /**
     * 获取屏幕密度
     * 
     * @return
     */
    public float getScreenDensity() {
	return mDensity;
    }

    /**
     * 获取屏幕密度dpi值
     * 
     * @return
     */
    public int getScreenDensityDpi() {
	return mDensityDpi;
    }
}
