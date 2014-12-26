package com.bitocean.atm;

import com.bitocean.atm.fragment.RedeemQRFragment;
import com.bitocean.atm.fragment.TradeModeFragment;

import android.os.Bundle;

/**
 * @author bing.liu
 * 
 */
public class RedeemQRActivity extends BaseTimerActivity {
	private RedeemQRFragment redeemQRFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_redeem_qr);
		initUI();
	}

	private void initUI() {
		redeemQRFragment = (RedeemQRFragment) getSupportFragmentManager()
				.findFragmentById(R.id.redeem_qr_fragment);
	}
}
