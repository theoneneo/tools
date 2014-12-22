package com.bitocean.atm;

import com.bitocean.atm.fragment.WifiSettingFragment;

import android.os.Bundle;
/**
 * @author bing.liu
 *
 */
public class SettingActivity extends BaseActivity {
	private WifiSettingFragment wifiFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		initUI();
	}

	private void initUI() {
		wifiFragment = (WifiSettingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.wifi_fragment);
	}
}
