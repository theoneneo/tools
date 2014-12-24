package com.bitocean.atm.service;

import com.bitocean.atm.common.DrawWindowManager;
import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.controller.NetServiceManager;

import de.greenrobot.event.EventBus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;

/**
 * @author bing.liu
 * 
 */
public class ATMService extends Service {
	private IBinder mBinder = new LocalBinder();
	private DrawWindowManager drawWindowManager;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		if (null == mBinder)
			mBinder = new LocalBinder();
		return mBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	public class LocalBinder extends Binder {
		public ATMService getService() {
			return ATMService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		EventBus.getDefault().register(this, ATMBroadCastEvent.class);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		registerReceiver(AppManager.getInstance().atmReceiver, filter);

		Thread timerThread = new Thread() {
			public void run() {
				while (true) {
					if (!AppManager.isLoopTime) {
					} else {
						if (AppManager.loopTimer == 0){
							EventBus.getDefault()
									.post(new ATMBroadCastEvent(
											ATMBroadCastEvent.EVENT_GOHOME,
											null));
						}else{
							AppManager.loopTimer--;
							EventBus.getDefault()
							.post(new ATMBroadCastEvent(
									ATMBroadCastEvent.EVENT_UPDATE_TIMER,
									null));
						}
					}
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
//					NetServiceManager.getInstance().getRateList();
				}
			}
		};
		timerThread.start();
		
		Thread rateThread = new Thread() {
			public void run() {
				while (true) {
					NetServiceManager.getInstance().getRateList();
					try {
						sleep(60000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					

				}
			}
		};
		rateThread.start();
		
		checkNetworkStatus();
		checkShowTimer();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		EventBus.getDefault().unregister(this);
		this.unregisterReceiver(AppManager.getInstance().atmReceiver);
		if(drawWindowManager == null)
			drawWindowManager = new DrawWindowManager(this);
		drawWindowManager.removeNetWorkStatus();
		drawWindowManager.removeTimer();
		
		super.onDestroy();
		
		Intent localIntent = new Intent();
		localIntent.setClass(this, ATMService.class);
		startService(localIntent);
	}
	
	public void onEventMainThread(ATMBroadCastEvent event) {
		switch (event.getType()) {
		case ATMBroadCastEvent.EVENT_NETWORK_STATUS:
			if(drawWindowManager != null)
				drawWindowManager.updateNetworkStatus((NetworkInfo)event.getObject());
			break;
		case ATMBroadCastEvent.EVENT_UPDATE_TIMER:
			checkShowTimer();
			break;
		default:
			break;
		}
	}
	
	private void checkNetworkStatus() {
		ConnectivityManager connectivityManager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		
		if(drawWindowManager == null)
			drawWindowManager = new DrawWindowManager(this);
		drawWindowManager.updateNetworkStatus(info);
	}
	
	private void checkShowTimer(){
		if(drawWindowManager == null)
			drawWindowManager = new DrawWindowManager(this);
		drawWindowManager.showTimer(AppManager.isLoopTime);
	}

}
