package com.bitocean.atm;

import com.bitocean.atm.fragment.BuyCountFragment;
import com.bitocean.atm.fragment.BuyQRFragment;
import com.bitocean.atm.fragment.UserLoginFragment;

import android.os.Bundle;
/**
 * @author bing.liu
 *
 */
public class BuyQRActivity extends BaseTimerActivity {
	private BuyQRFragment buyQRFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buy_qr);
		initUI();
	}

	private void initUI() {
		buyQRFragment = (BuyQRFragment) getSupportFragmentManager()
				.findFragmentById(R.id.buy_qr_fragment);
	}
}
