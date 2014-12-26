package com.bitocean.atm;

import java.util.Locale;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author bing.liu
 * 
 */
public class LanguageActivity extends BaseTimerActivity implements
		OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_language);
		initUI();
	}

	private void initUI() {
		Button englishButton = (Button) findViewById(R.id.language_english_btn);
		englishButton.setOnClickListener(this);
		Button japanButton = (Button) findViewById(R.id.language_japan_btn);
		japanButton.setOnClickListener(this);
		Button chinaButton = (Button) findViewById(R.id.language_china_btn);
		chinaButton.setOnClickListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Resources resources = getResources();// 获得res资源对象
		Configuration config = resources.getConfiguration();// 获得设置对象

		switch (arg0.getId()) {
		case R.id.language_english_btn:
			config.locale = Locale.ENGLISH;
			break;
		case R.id.language_japan_btn:
			config.locale = Locale.JAPAN;
			break;
		case R.id.language_china_btn:
			config.locale = Locale.SIMPLIFIED_CHINESE;
			break;
		default:
			break;
		}
		resources.updateConfiguration(config, null);

		Intent intent = new Intent();
		intent.setClass(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
		finish();
	}
}
