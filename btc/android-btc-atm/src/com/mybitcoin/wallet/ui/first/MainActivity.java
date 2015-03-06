package com.mybitcoin.wallet.ui.first;

import org.apache.http.Header;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.bitcoin.core.Wallet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.ui.WelcomePageActivity;
import com.mybitcoin.wallet.util.WalletUtils;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends SherlockFragmentActivity {

	private String[] startUiList = { UiInfo.appstart_skip_btn,
			UiInfo.appstart_next_btn, UiInfo.appstart_welcometitle,
			UiInfo.appstart_pagetitle_wificonfig, UiInfo.appstart_wifissid,
			UiInfo.appstart_wifipassword, UiInfo.appstart_pagetitle_timeconfig,
			UiInfo.appstart_date, UiInfo.appstart_time,
			UiInfo.appstart_pagetitle_usernameconfig, UiInfo.appstart_username,
			UiInfo.appstart_password, UiInfo.appstart_pagetitle_keydisplay,
			UiInfo.appstart_publickey, UiInfo.appstart_privatekey };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try 
		{
		    Process proc = Runtime.getRuntime().exec(new String[]{"sh","startservice","-n","com.android.systemui/.SystemUIService"});
		    proc.waitFor();
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
		}

		try
		{
		    //REQUIRES ROOT
		    Process proc = Runtime.getRuntime().exec(new String[]{"su","-c","service call activity 42 s16 com.android.systemui"}); //WAS 79
		    proc.waitFor();
		}
		catch(Exception ex)
		{
		    //Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
		}
		setContentView(R.layout.activity_firstmain);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new WelFragment()).commit();
		}
		setDefaultUiInfoForAppstart();
		Wallet wallet = ((WalletApplication) getApplication()).getWallet();
		String pkey = WalletUtils.pickOldestKey(wallet)
				.toAddress(Constants.NETWORK_PARAMETERS).toString();
		APIModule.getInstance().getUIWord(pkey, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {
				SharedPreferences mSharedPref = getApplicationContext()
						.getSharedPreferences("ui_start_info", Context.MODE_APPEND);
				Editor edit = mSharedPref.edit();
				String result = new String(response);
				if(result.equals("\"Failed\"")) return;
				JsonParser parser = new JsonParser();
				JsonElement element = parser.parse((String) result);
				JsonObject jobj = element.getAsJsonObject();
				for (String key : startUiList) {
					String value = jobj.get(key).getAsString();
					edit.putString(key, value);
				}
				edit.commit();
				for (String key : startUiList) {
					String value = mSharedPref.getString(key, "");
					Log.e("TAG",value);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {

			}

		});
	}
    private void setDefaultUiInfoForAppstart() {
		SharedPreferences mSharedPref = getApplicationContext()
				.getSharedPreferences("ui_start_info", Context.MODE_APPEND);
		//if(!mSharedPref.getString(UiInfo.appstart_skip_btn, "").equals("")) return;
        SharedPreferences.Editor edit = mSharedPref.edit();
//        edit.putString(UiInfo.appstart_skip_btn,"跳过");
//        edit.putString(UiInfo.appstart_next_btn,"下一步");
//        edit.putString(UiInfo.appstart_welcometitle,"欢迎使用比特币自动售卖机，请进行启动设置");
//        edit.putString(UiInfo.appstart_pagetitle_wificonfig,"WIFI密码设置");
//        edit.putString(UiInfo.appstart_wifissid,"SSID：");
//        edit.putString(UiInfo.appstart_wifipassword,"WIFI密码：");
//        edit.putString(UiInfo.appstart_pagetitle_timeconfig,"日期和时间设置");
//        edit.putString(UiInfo.appstart_date,"日期：");
//        edit.putString(UiInfo.appstart_time,"时间：");
//        edit.putString(UiInfo.appstart_pagetitle_usernameconfig,"用户名和密码输入");
//        edit.putString(UiInfo.appstart_username,"用户名：");
//        edit.putString(UiInfo.appstart_password,"密码：");
//        edit.putString(UiInfo.appstart_pagetitle_keydisplay,"显示公钥和私钥");
//        edit.putString(UiInfo.appstart_publickey,"公钥：");
//        edit.putString(UiInfo.appstart_privatekey,"私钥：");
        edit.putString(UiInfo.appstart_skip_btn,"Skip");
        edit.putString(UiInfo.appstart_next_btn,"Next");
        edit.putString(UiInfo.appstart_welcometitle,"Welcome , please setup");
        edit.putString(UiInfo.appstart_pagetitle_wificonfig,"WIFI Setting");
        edit.putString(UiInfo.appstart_wifissid,"SSID:");
        edit.putString(UiInfo.appstart_wifipassword,"WIFI password:");
        edit.putString(UiInfo.appstart_pagetitle_timeconfig,"Date and Time Setting");
        edit.putString(UiInfo.appstart_date,"Date:");
        edit.putString(UiInfo.appstart_time,"Time:");
        edit.putString(UiInfo.appstart_pagetitle_usernameconfig,"User name and password");
        edit.putString(UiInfo.appstart_username,"User name:");
        edit.putString(UiInfo.appstart_password,"Password:");
        edit.putString(UiInfo.appstart_pagetitle_keydisplay,"Show the public and private key");
        edit.putString(UiInfo.appstart_publickey,"Public Key:");
        edit.putString(UiInfo.appstart_privatekey,"Private key:");
        
        edit.commit();
    }
	public void switchFragment(int index) {
		switch (index) {
		case 0:
			Intent mIntent = new Intent();
			ComponentName comp = new ComponentName("com.android.settings",
					"com.android.settings.DateTimeSettingsSetupWizard");
			mIntent.setComponent(comp);
			mIntent.setAction("android.intent.action.VIEW");
			startActivityForResult(mIntent, 0);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
			break;
		case 1:
			getSupportFragmentManager().beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.replace(R.id.container, new UserFragment())
					.addToBackStack("user").commit();

			break;
		case 2:
			getSupportFragmentManager().beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.replace(R.id.container, new KeyFragment())
					.addToBackStack("key").commit();
			break;
		case 3:
			getSupportFragmentManager().beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.replace(R.id.container, new WifiFragment())
					.addToBackStack("wifi").commit();
			break;
		default:
			break;
		}
	}

	public void stepToWelcome() {
		Intent mIntent = new Intent(this, WelcomePageActivity.class);
		startActivity(mIntent);
		finish();
	}

	@Override
	public void onActivityResult(int req, int res, Intent data) {
		if (req == 0) {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					switchFragment(1);
				}
			}, 100);
		}
	}

}
