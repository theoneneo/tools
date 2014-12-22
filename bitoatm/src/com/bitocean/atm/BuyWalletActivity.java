package com.bitocean.atm;

import com.bitocean.atm.fragment.UserLoginFragment;

import android.os.Bundle;
/**
 * @author bing.liu
 *
 */
public class BuyWalletActivity extends BaseTimerActivity {
	private UserLoginFragment UserLoginFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		initUI();
	}

	private void initUI() {
		UserLoginFragment = (UserLoginFragment) getSupportFragmentManager()
				.findFragmentById(R.id.user_login_fragment);
	}
}
