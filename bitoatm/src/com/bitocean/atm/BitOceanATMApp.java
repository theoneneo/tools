package com.bitocean.atm;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.service.ATMBroadCastEvent;

import de.greenrobot.event.EventBus;

/**
 * @author bing.liu
 * 
 */
public class BitOceanATMApp extends Application {
	private static Application app;
	private static ArrayList<Activity> list = new ArrayList<Activity>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		AppManager.getInstance();
		EventBus.getDefault().register(this, ATMBroadCastEvent.class);
	}

	public void onTerminate() {
		EventBus.getDefault().unregister(this);
		super.onTerminate();
	}

	public static Application getApplication() {
		return app;
	}

	public static Context getContext() {
		return getApplication().getApplicationContext();
	}

	public static void addActivity(Activity activity) {
		list.add(activity);
	}

	public static void removeActivity(Activity activity) {
		for (Activity ac : list) {
			if (ac == activity) {
				list.remove(ac);
				return;
			}
		}
	}

	public static void exit() {
		for (Activity activity : list) {
			activity.finish();
		}
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void onEventMainThread(ATMBroadCastEvent event) {
		switch (event.getType()) {
		case ATMBroadCastEvent.EVENT_GOHOME:
			for (int i = list.size() - 1; i >= 0; i--) {
				if (i == 0)
					return;
				list.get(i).finish();
			}
			break;
		default:
			break;
		}
	}
}
