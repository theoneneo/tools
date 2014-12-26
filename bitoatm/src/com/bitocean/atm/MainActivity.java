package com.bitocean.atm;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.bitocean.atm.service.ATMBroadCastEvent;
import de.greenrobot.event.EventBus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * @author bing.liu
 *
 */
public class MainActivity extends BaseTimerActivity {
	private TextView buyActionText, buyBitValueText, buyBitUnitText,
			buyMoneyValueText, buyMoneyUnitTextView, sellActionText,
			sellBitValueText, sellBitUnitText, sellMoneyValueText,
			sellMoneyUnitTextView;
	
	private boolean isClick = false;
	private int clickNum = 0;
	private Timer timer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initUI();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	private void initUI() {
		buyActionText = (TextView) findViewById(R.id.buy_rate_text)
				.findViewById(R.id.action_text);
		buyBitValueText = (TextView) findViewById(R.id.buy_rate_text)
				.findViewById(R.id.bit_value_text);
		buyBitUnitText = (TextView) findViewById(R.id.buy_rate_text)
				.findViewById(R.id.bit_unit_text);
		buyMoneyValueText = (TextView) findViewById(R.id.buy_rate_text)
				.findViewById(R.id.money_value_text);
		buyMoneyUnitTextView = (TextView) findViewById(R.id.buy_rate_text)
				.findViewById(R.id.money_unit_text);
		
		sellActionText = (TextView) findViewById(R.id.sell_rate_text)
				.findViewById(R.id.action_text);
		sellBitValueText = (TextView) findViewById(R.id.sell_rate_text)
				.findViewById(R.id.bit_value_text);
		sellBitUnitText = (TextView) findViewById(R.id.sell_rate_text)
				.findViewById(R.id.bit_unit_text);
		sellMoneyValueText = (TextView) findViewById(R.id.sell_rate_text)
				.findViewById(R.id.money_value_text);
		sellMoneyUnitTextView = (TextView) findViewById(R.id.sell_rate_text)
				.findViewById(R.id.money_unit_text);
		
		buyActionText.setText(R.string.buy);
		buyBitValueText.setText("1");
		buyBitUnitText.setText(R.string.unit_bit);
		buyMoneyValueText.setText("");
		buyMoneyUnitTextView.setText(R.string.unit_money);
		
		sellActionText.setText(R.string.sell);
		sellBitValueText.setText("1");
		sellBitUnitText.setText(R.string.unit_bit);
		sellMoneyValueText.setText("");
		sellMoneyUnitTextView.setText(R.string.unit_money);
		
		
		RelativeLayout clickLayout = (RelativeLayout)findViewById(R.id.click_layout);
		clickLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goToUserLoginActivity();
			}
		});
		
		View logo = (View)findViewById(R.id.logo);
		logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				clickLogoSet();
			}
		});
		
		Button language_btn = (Button)findViewById(R.id.language_btn);
		language_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				changeLanguage();
			}
		});
	}
	
	private void changeLanguage(){
		Intent intent = new Intent(this, LanguageActivity.class);
		startActivity(intent);
		finish();
	}
	
	private void clickLogoSet(){
		if(isClick){
			clickNum++;
			if(clickNum >= 7){
				timer.cancel();
				goToSettingActivity();
				return;
			}	
		}else{
			isClick = true;
	        timer = new Timer();
	        timer.schedule(new RemindTask(), 3*1000);
	        clickNum++;
		}
	}
	
    class RemindTask extends TimerTask {
        public void run() {
        	isClick = false;
            timer.cancel();
        }
    }
	
	private void goToUserLoginActivity(){
		Intent intent = new Intent(this, UserActivity.class);
		startActivity(intent);
	}
	
	private void goToSettingActivity(){
		Intent intent = new Intent(this, SettingActivity.class);
		startActivity(intent);
	}
}
