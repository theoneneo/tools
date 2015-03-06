package com.mybitcoin.wallet.ui.first;

import com.mybitcoin.wallet.environment.UiInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
	private UiInfo mUiInfo;
	private SharedPreferences mSharedPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUiInfo = new UiInfo(getActivity());
		mSharedPref = getActivity().getApplicationContext()
				.getSharedPreferences("ui_start_info", Context.MODE_APPEND);
	}


	public UiInfo getUiInfo() {
		if (mUiInfo == null)
			mUiInfo = new UiInfo(getActivity());
		return mUiInfo;
	}

	public String getString(String key) {
		return mSharedPref.getString(key, "");
	}
}
