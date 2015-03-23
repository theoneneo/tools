/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.Files;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.R.color;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.environment.UiInfo;

public class CashTradeKycRegister1Activity extends WalletActivityTimeoutBase {
	private static final String LOG_TAG = "CashTradeKycRegister1Activity";
	private static final boolean DEBUG_FLAG = true;

	TextView mRescanBtn;
	TextView mNxtBtn;
	TextView mCnlBtn;

	ImageView mImage;

	public static byte[] byteData;
	public static File userIcon;

	@Override
	protected void onCreate(final Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);

		setLayout(R.layout.cash_trade_kyc_register1);
		hasImage = false;

		mImage = (ImageView) findViewById(R.id.cashtrade_kycregister1_img);
		mImage.setEnabled(true);
		mImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO
				Intent intent = new Intent(CashTradeKycRegister1Activity.this,
						ArFragmentActivity.class);
				intent.putExtra("file", "user_icon");
				startActivityForResult(intent, 1);
			}
		});
		mCnlBtn = (TextView) findViewById(R.id.cashtrade_kycregister1_cnl_btn);
		mCnlBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				gotoActivity(WelcomePageActivity.class);
			}
		});

		mRescanBtn = (TextView) findViewById(R.id.cashtrade_kycregister1_rescan_btn);
		mRescanBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO

				Intent intent = new Intent(CashTradeKycRegister1Activity.this,
						ArFragmentActivity.class);
				intent.putExtra("file", "user_icon");
				startActivityForResult(intent, 1);
			}
		});

		mNxtBtn = (TextView) findViewById(R.id.cashtrade_kycregister1_nxt_btn);
		mNxtBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (hasImage) {
					gotoActivity(CashTradeKycRegister2Activity.class);
				} else {
					TextView text = new TextView(
							CashTradeKycRegister1Activity.this);
					text.setText("please take photo!");
					text.setBackgroundColor(Color.BLACK);
					text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
					Toast toast = new Toast(CashTradeKycRegister1Activity.this);
					toast.setView(text);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		});
	}

	private Bitmap bitmap;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				String filepath = data.getStringExtra("url");
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = null;
				userIcon = new File(filepath);

				if (bitmap != null && !bitmap.isRecycled()) {
					// 回收并且置为null
					bitmap.recycle();
					bitmap = null;
				}
				bitmap = BitmapFactory.decodeFile(userIcon.getAbsolutePath(),
						options);
				mImage.setImageBitmap(bitmap);
				mImage.invalidate();
				hasImage = true;
				try {
					byteData = Files.toByteArray(userIcon);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (bitmap != null && !bitmap.isRecycled()) {
			// 回收并且置为null
			bitmap.recycle();
			bitmap = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		WalletActivityTimeoutController.getInstance().setTimeout(150);
	}

	@Override
	public void updateUiInfo() {
		super.updateUiInfo();

		TextView tradeType = (TextView) findViewById(R.id.cashtrade_kycregister1_tradetype);
		// tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));
		tradeType.setText("KYC Verify");

		TextView step = (TextView) findViewById(R.id.cashtrade_kycregister1_step);
		TextView step1 = (TextView) findViewById(R.id.cashtrade_kycregister1_step1);
		TextView step2 = (TextView) findViewById(R.id.cashtrade_kycregister1_step2);
		TextView step3 = (TextView) findViewById(R.id.cashtrade_kycregister1_step3);
		TextView title = (TextView) findViewById(R.id.cashtrade_kycregister1_title);

		step.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCREGISTER_STEP));
		step1.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCREGISTER_STEP1));
		step2.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCREGISTER_STEP2));
		step3.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCREGISTER_STEP3));
		title.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCREGISTER1_TITLE));
		mRescanBtn.setText(getUiInfo().getTextByName(
				UiInfo.CASHTRADE_KYCREGISTER1_RESCAN_BTN));
		mNxtBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_NXT_BTN));
		mCnlBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
	}

	private static void dLog(@Nonnull String logStr) {
		if (DEBUG_FLAG) {
			// Log.d(LOG_TAG, logStr);
		}
	}
}