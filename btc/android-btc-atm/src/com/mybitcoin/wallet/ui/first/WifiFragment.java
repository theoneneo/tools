package com.mybitcoin.wallet.ui.first;

import java.util.ArrayList;
import java.util.List;

import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class WifiFragment extends BaseFragment {
	private Button stepBtn;
	private Button nextBtn;
	private EditText pwEdit;
	private WifiAdmin wifiAdmin;
	private Spinner sp1;
	private List<String> ssidList = new ArrayList<String>();;
	private ArrayAdapter<String> ad;
	private Handler handler = new Handler();
	private LinearLayout progressBar;

	TextView titleView, pwView, ssidView; 
	
	public WifiFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_wifi, container,
				false);
		stepBtn = (Button) rootView.findViewById(R.id.step);
		nextBtn = (Button) rootView.findViewById(R.id.next);
		pwEdit = (EditText) rootView.findViewById(R.id.pwEdit);
		progressBar = (LinearLayout) rootView.findViewById(R.id.progressbar);

		wifiAdmin = new WifiAdmin(getActivity());
		wifiAdmin.openWifi();

		handler.postDelayed(scanRunnable, 0);
		sp1 = (Spinner) rootView.findViewById(R.id.spinner1);
		ad = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, ssidList);
		ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp1.setAdapter(ad);

		stepBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				handler.removeCallbacks(scanRunnable);
				((MainActivity) getActivity()).switchFragment(0);
			}
		});
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WifiTask wtask = new WifiTask();
				wtask.execute();
			}
		});

		stepBtn.setText(getString(UiInfo.appstart_skip_btn));
		nextBtn.setText(getString(UiInfo.appstart_next_btn));
		
		titleView = ((TextView) rootView.findViewById(R.id.title));
		pwView = ((TextView) rootView.findViewById(R.id.pwText));
		ssidView = ((TextView) rootView.findViewById(R.id.ssidText));
		titleView.setText(getString(UiInfo.appstart_pagetitle_wificonfig));
		pwView.setText(getString(UiInfo.appstart_wifipassword));
		ssidView.setText(getString(UiInfo.appstart_wifissid));
		return rootView;
	}


	private Runnable scanRunnable = new Runnable() {
		public void run() {
			wifiAdmin.startScan();
			ssidList.clear();
			ssidList.addAll(wifiAdmin.lookUpScan());
			ad.notifyDataSetChanged();
			handler.postDelayed(this, 2000);
		}
	};

	private class WifiTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
			stepBtn.setClickable(false);
			nextBtn.setClickable(false);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			String pw = pwEdit.getText().toString();
			String ssid = (String) sp1.getSelectedItem();
//			if (wifiAdmin.getSSID().equals(ssid))
//				return true;
			boolean success = wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(
					ssid, pw, 3));
			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressBar.setVisibility(View.GONE);
			stepBtn.setClickable(true);
			nextBtn.setClickable(true);
			if (result) {
				handler.removeCallbacks(scanRunnable);
				((MainActivity) getActivity()).switchFragment(0);
			} else {
				Toast.makeText(getActivity(), "WIFI连接失败", Toast.LENGTH_LONG)
						.show();
			}
		}

	}
}
