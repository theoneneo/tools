package com.bitocean.atm.fragment;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bitocean.atm.R;
import com.bitocean.atm.RedeemQRActivity;
import com.bitocean.atm.SellQRActivity;
import com.bitocean.atm.UserActivity;
import com.bitocean.atm.controller.AppManager;

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
		Button buyModeButton = (Button) v.findViewById(R.id.buy_mode);
		buyModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (AppManager.DTM_STATE.equals("JAPAN")) {
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
					startActivity(intent);
				}
			}
		});

		Button sellModeButton = (Button) v.findViewById(R.id.sell_mode);
		sellModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goToSellModeActivity();
			}
		});

		Button qrModeButton = (Button) v.findViewById(R.id.qr_mode);
		qrModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goToQRModeActivity();
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

	private void goToSellModeActivity() {
		if (AppManager.DTM_STATE.equals("JAPAN")) {
			Intent intent = new Intent(getActivity(), SellQRActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(getActivity(), UserActivity.class);
			startActivity(intent);
		}
	}

	private void goToQRModeActivity() {
		Intent intent = new Intent(getActivity(), RedeemQRActivity.class);
		startActivity(intent);
	}
}