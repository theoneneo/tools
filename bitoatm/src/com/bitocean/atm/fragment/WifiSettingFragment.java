package com.bitocean.atm.fragment;

import java.util.Calendar;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bitocean.atm.R;

/**
 * @author bing.liu
 * 
 */
public class WifiSettingFragment extends NodeFragment {
	private TextView wifiTextView;
	private TextView dateTextView;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_wifi, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		wifiTextView = (TextView) v.findViewById(R.id.wifi_info);
		updateWifiInfo();
		dateTextView = (TextView) v.findViewById(R.id.date_info);
		updateDateInfo();
		Button wifiButton = (Button) v.findViewById(R.id.wifi_btn);
		wifiButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				startActivityForResult(mIntent, 0);
			}
		});
		Button dateButton = (Button) v.findViewById(R.id.date_btn);
		dateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent mIntent = new Intent();
				ComponentName comp = new ComponentName("com.android.settings",
						"com.android.settings.DateTimeSettingsSetupWizard");
				mIntent.setComponent(comp);
				mIntent.setAction("android.intent.action.VIEW");
				startActivityForResult(mIntent, 0);
			}
		});

		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.wifi_prompt);

		Button cancelButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.left_btn);
		cancelButton.setText(R.string.back);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getActivity().finish();
			}
		});

		Button nextButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.right_btn);
		nextButton.setText(R.string.administrator);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.add(R.id.container, new AdminLoginFragment())
						.addToBackStack("wifiset").commit();
			}
		});
	}

	private void updateWifiInfo() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable())
			wifiTextView.setText(info.getExtraInfo());
		else
			wifiTextView.setText(R.string.wifi_default);
	}

	private void updateDateInfo() {
		dateTextView.setText(Calendar.getInstance().getTime().toLocaleString());
	}

	@Override
	public void onActivityResult(int req, int res, Intent data) {
		updateWifiInfo();
		updateDateInfo();
	}
}
