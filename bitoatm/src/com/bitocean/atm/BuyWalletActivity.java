package com.bitocean.atm;

import com.bitocean.atm.fragment.BuyCountFragment;
import com.bitocean.atm.fragment.BuyQRFragment;
import com.bitocean.atm.fragment.UserLoginFragment;

import android.os.Bundle;
/**
 * @author bing.liu
 *
 */
public class BuyWalletActivity extends BaseTimerActivity {
	private BuyCountFragment buyCountFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buy_wallet);
		initUI();
	}

	private void initUI() {
		buyCountFragment = (BuyCountFragment) getSupportFragmentManager()
				.findFragmentById(R.id.buy_count_fragment);
	}
}
