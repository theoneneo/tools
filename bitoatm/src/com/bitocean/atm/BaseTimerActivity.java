package com.bitocean.atm;

import android.os.Bundle;

import com.bitocean.atm.controller.AppManager;

/**
 * @author bing.liu
 *
 */
public class BaseTimerActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManager.loopTimer = AppManager.LOOP_TIMER;
		AppManager.isLoopTime = true;
	}
}
