package com.bitocean.atm.controller;

import java.util.ArrayList;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;

import com.bitocean.atm.BitOceanATMApp;
import com.bitocean.atm.service.ATMReceiver;
import com.bitocean.atm.service.ATMService;
import com.bitocean.atm.struct.TypeRateStruct;


/**
 * @author bing.liu
 *
 */
public class AppManager extends BaseManager {
	public final static int LOOP_TIMER = 300;
	
	private static AppManager mInstance;
	public static boolean isAdmin = false;
	public static boolean isLoopTime = true;
	public static int loopTimer = LOOP_TIMER;
	public static String public_keyString = null;
	public static String exchangeString = "bitcocean";
	public static String currency_typeString = "JPY";
	public static int versionCode = 0;
	public static String versionNameString = null;
	public static String uuidString = null;
	public static ArrayList<String> bitType = new ArrayList<String>();
	public static TypeRateStruct typeRateStructs = new TypeRateStruct();
	//[bitcocean "btce","okcoin","huobi","btcc","bitstamp","bitfinex"]:
	
	public ATMReceiver atmReceiver;
	public ATMService mService;

	private AppManager(Application app) {
		super(app);
		// TODO Auto-generated constructor stub
		initManager();
	}

	public static AppManager getInstance() {
		AppManager instance;
		if (mInstance == null) {
			synchronized (AppManager.class) {
				if (mInstance == null) {
					instance = new AppManager(BitOceanATMApp.getApplication());
					mInstance = instance;
				}
			}
		}
		return mInstance;
	}

	@Override
	protected void initManager() {
		getAdminStauts();
		if(atmReceiver == null){
			atmReceiver = new ATMReceiver();
		}
		mContext.startService(new Intent(mContext, ATMService.class));
		bindService();
		
		PackageManager pm = BitOceanATMApp.getApplication().getPackageManager();
		PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo(BitOceanATMApp.getApplication().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(pi != null){
			versionCode = pi.versionCode;
			versionNameString = pi.versionName;
		}
		
		bitType.add("bitcoin");
	}

	@Override
	public void DestroyManager() {
		unbindService();
	}
	
	private void getAdminStauts(){
		isAdmin = mSharedPreferences.getBoolean("isadmin", false);
	}
	
	public void bindService(){
		Intent intent = new Intent().setClass(getContext() , ATMService.class ); 
		getContext().bindService(intent , mConnection, getContext().BIND_AUTO_CREATE);
    }
    
    public void unbindService(){
    	getContext().unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {  
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
	        mService = ((ATMService.LocalBinder)arg1).getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
	    	
		}
    };
}
