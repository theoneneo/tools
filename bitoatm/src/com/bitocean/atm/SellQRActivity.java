package com.bitocean.atm;

import com.bitocean.atm.fragment.SellCountFragment;
import com.bitocean.atm.fragment.TradeModeFragment;

import android.os.Bundle;

/**
 * @author bing.liu
 * 
 */
public class SellQRActivity extends BaseTimerActivity {
	private SellCountFragment sellCountFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sell);
		initUI();
	}

	private void initUI() {
		sellCountFragment = (SellCountFragment) getSupportFragmentManager()
				.findFragmentById(R.id.sell_count_fragment);
	}
}
