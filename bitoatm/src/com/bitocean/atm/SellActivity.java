package com.bitocean.atm;

import com.bitocean.atm.fragment.SellFlowDisplayFragment;

import android.os.Bundle;

/**
 * @author bing.liu
 * 
 */
public class SellActivity extends BaseTimerActivity {
	private SellFlowDisplayFragment sellFlowDisplayFragement;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sell);
		initUI();
	}

	private void initUI() {
		sellFlowDisplayFragement = (SellFlowDisplayFragment) getSupportFragmentManager()
				.findFragmentById(R.id.sell_flow_dislpay_fragment);
	}
}
