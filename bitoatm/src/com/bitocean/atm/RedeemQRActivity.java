package com.bitocean.atm;

import com.bitocean.atm.fragment.RedeemScanQRFragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

/**
 * @author bing.liu
 * 
 */
public class RedeemQRActivity extends BaseTimerActivity {
	private RedeemScanQRFragment redeemQRFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_redeem_qr);
		initUI();
	}

	private void initUI() {
		redeemQRFragment = (RedeemScanQRFragment) getSupportFragmentManager()
				.findFragmentById(R.id.redeem_qr_fragment);
	}
}
