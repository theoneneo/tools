package com.bitocean.atm.fragment;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bitocean.atm.BuyQRActivity;
import com.bitocean.atm.BuyWalletActivity;
import com.bitocean.atm.R;
import com.bitocean.atm.TradeModeActivity;
import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.controller.NetServiceManager;
import com.bitocean.atm.service.ATMBroadCastEvent;
import com.bitocean.atm.service.ProcessEvent;
import com.bitocean.atm.struct.RedeemConfirmStruct;
import com.bitocean.atm.struct.SellBitcoinQRStruct;
import com.bitocean.atm.util.Util;

import de.greenrobot.event.EventBus;

/**
 * @author bing.liu
 * 
 */
public class CurrencyCountFragment extends NodeFragment {
	private String user_public_key = null;
	private int process_event = 0;
	private ProgressDialog progressDialog = null;
	private int currency_num;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this, ATMBroadCastEvent.class);
		mContext = getActivity().getApplicationContext();
		Bundle b = getArguments();
		if (b == null)
			return;
		user_public_key = (String) b.getString("user_public_key");
		process_event = (int) b.getInt("process_event", 0);
	}

	@Override
	public void onDestroy() {
		if (progressDialog != null)
			progressDialog.dismiss();
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_currency_count, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.buy_prompt);

		Button cancelButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.left_btn);
		cancelButton.setText(R.string.back);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getActivity().getSupportFragmentManager().popBackStack();
			}
		});

		Button nextButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.right_btn);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				checkProcessEvent();
			}
		});
	}

	private void checkProcessEvent() {
		if (process_event == ProcessEvent.EVENT_SELL) {
			// 卖币
			// 获取卖币码
			if(!AppManager.isNetEnable){
				new Util(mContext).showFeatureToast(mContext
						.getString(R.string.network_error));
				return;
			}
			
			NetServiceManager.getInstance().getSellQRCode(user_public_key,
					AppManager.getUserId(), currency_num);
			progressDialog = new Util(getActivity())
					.showProgressBar(getActivity().getString(R.string.wait));
		} else {
			// 二维码买币
			// 纸钱包
			PayConfirmFragment fragment = new PayConfirmFragment();
			Bundle b = new Bundle();
			b.putString("user_public_key", user_public_key);
			b.putInt("currency_num", currency_num);
			b.putInt("process_event", process_event);
			fragment.setArguments(b);
			getActivity()
					.getSupportFragmentManager()
					.beginTransaction()
					.setTransition(
							FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.add(R.id.container, fragment)
					.addToBackStack("currenycountfragment").commit();
		}
	}

	public void onEventMainThread(ATMBroadCastEvent event) {
		switch (event.getType()) {
		case ATMBroadCastEvent.EVENT_GET_SELL_QR_CODE_SUCCESS:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			SellBitcoinQRStruct struct = (SellBitcoinQRStruct) event
					.getObject();
			if ("success".equals(struct.resutlString)) {
				SellScanQRCodeFragment fragment = new SellScanQRCodeFragment();
				Bundle b = new Bundle();
				b.putSerializable("sellbitcoinqrstruct", struct);
				b.putString("user_public_key", user_public_key);
				b.putInt("currency_num", currency_num);
				b.putInt("process_event", process_event);
				fragment.setArguments(b);
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.add(R.id.container, fragment)
						.addToBackStack("currenycountfragment").commit();
			} else if ("fail".equals(struct.resutlString)) {
				String msgString = null;
				switch (struct.reason) {
				case 1:
					msgString = getString(R.string.sell_qr_code_display_1);
					break;
				case 2:
					msgString = getString(R.string.sell_qr_code_display_2);
					break;
				default:
					break;
				}
				new Util(getActivity()).showFeatureToast(msgString);
			}
			break;
		case ATMBroadCastEvent.EVENT_GET_SELL_QR_CODE_FAIL:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			new Util(getActivity())
					.showFeatureToast((String) event.getObject());
			break;
		default:
			break;
		}
	}
}
