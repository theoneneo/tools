package com.bitocean.atm.fragment;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bitocean.atm.R;
import com.bitocean.atm.RedeemQRActivity;
import com.bitocean.atm.SellActivity;
import com.bitocean.atm.UserActivity;
import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.struct.LoginUserStruct;
import com.bitocean.atm.util.Util;

/**
 * @author bing.liu
 * 
 */
public class TradeModeFragment extends NodeFragment {
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_trade_mode, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		ImageButton buyModeButton = (ImageButton) v.findViewById(R.id.buy_mode_btn);
		buyModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goToBuyModeActivity();
			}
		});

		ImageButton sellModeButton = (ImageButton) v.findViewById(R.id.sell_mode_btn);
		sellModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goToSellModeActivity();
			}
		});

		ImageButton qrModeButton = (ImageButton) v.findViewById(R.id.redeem_mode_btn);
		qrModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goToRedeemModeActivity();
			}
		});

		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.trade_mode);

		Button cancelButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.left_btn);
		cancelButton.setText(R.string.back);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getActivity().finish();
			}
		});

		Button nextButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.right_btn);
		nextButton.setVisibility(View.INVISIBLE);
	}
	
	private void goToBuyModeActivity(){
		if(!AppManager.isNetEnable){
			new Util(mContext).showFeatureToast(mContext
					.getString(R.string.network_error));
		}
		
		if(!getBoxInCashState()){
			return;
		}
		
		if(!getPrintState()){
			return;
		}
		
		if (!AppManager.isLogined) {
			getActivity()
					.getSupportFragmentManager()
					.beginTransaction()
					.setTransition(
							FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.add(R.id.container, new BuyModeFragment())
					.addToBackStack("trademode").commit();
		} else {
			Intent intent = new Intent(getActivity(),
					UserActivity.class);
			startActivityForResult(intent, 1);
		}
	}

	private void goToSellModeActivity() {
		if(!AppManager.isNetEnable){
			new Util(mContext).showFeatureToast(mContext
					.getString(R.string.network_error));
		}
		
		if(!getBoxOutCashState()){
			return;
		}
		
		if(!getPrintState()){
			return;
		}
		
		if (!AppManager.isLogined) {
			Intent intent = new Intent(getActivity(), SellActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(getActivity(), UserActivity.class);
			startActivityForResult(intent, 1);
		}
	}

	private void goToRedeemModeActivity() {
		if(!AppManager.isNetEnable){
			new Util(mContext).showFeatureToast(mContext
					.getString(R.string.network_error));
		}
		
		if(!getBoxOutCashState()){
			return;
		}
		
		Intent intent = new Intent(getActivity(), RedeemQRActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				Bundle bundle = data.getExtras();
				LoginUserStruct struct = (LoginUserStruct)bundle.getSerializable("loginuserstruct");
			}
		}
	}
	
	private boolean getBoxInCashState(){
		//判断钱箱状态
		//弹出提示
		//发送硬件信息
		//关闭入钞箱
		return true;
	}
	
	private boolean getBoxOutCashState(){
		return true;
	}
	
	private boolean getPrintState(){
		return true;
	}
}