package com.bitocean.atm;

import com.bitocean.atm.fragment.TradeModeFragment;

import android.os.Bundle;

/**
 * @author bing.liu
 * 
 */
public class TradeModeActivity extends BaseTimerActivity {
	private TradeModeFragment tradeModeFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trade);
		initUI();
	}

	private void initUI() {
		tradeModeFragment = (TradeModeFragment) getSupportFragmentManager()
				.findFragmentById(R.id.trade_mode_fragment);
	}
}
