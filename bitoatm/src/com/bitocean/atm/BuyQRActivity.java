package com.bitocean.atm;

import com.bitocean.atm.fragment.BuyQRFlowDisplayFragment;

import android.os.Bundle;
/**
 * @author bing.liu
 *
 */
public class BuyQRActivity extends BaseTimerActivity {
	private BuyQRFlowDisplayFragment buyQRFlowDisplayFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buy_qr);
		initUI();
	}

	private void initUI() {
		buyQRFlowDisplayFragment = (BuyQRFlowDisplayFragment) getSupportFragmentManager()
				.findFragmentById(R.id.buy_qr_flow_display_fragment);
	}
}
