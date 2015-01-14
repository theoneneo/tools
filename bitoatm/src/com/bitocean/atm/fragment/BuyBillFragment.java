package com.bitocean.atm.fragment;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bitocean.atm.R;
import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.controller.NetServiceManager;
import com.bitocean.atm.service.ATMBroadCastEvent;
import com.bitocean.atm.service.ProcessEvent;
import com.bitocean.atm.struct.BuyBitcoinPrintWalletStruct;
import com.bitocean.atm.struct.BuyBitcoinQRStruct;
import com.bitocean.atm.util.Util;

import de.greenrobot.event.EventBus;
/**
 * @author bing.liu
 * 
 */
public class BuyBillFragment extends NodeFragment {
	private String user_public_key = null;
	private int process_event = 0;
	private ProgressDialog progressDialog = null;
	private int currency_num;
	private int cash_num;
	
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
		currency_num = (int) b.getInt("currency_num", 0);
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
		View v = mInflater.inflate(R.layout.fragment_buy_bill, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.buy_bill_prompt);

		Button cancelButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.left_btn);
		cancelButton.setText(R.string.back);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getActivity().getSupportFragmentManager().popBackStack("currenycountfragment", 0);
			}
		});

		Button nextButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.right_btn);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				checkBoxInCash();
			}
		});
	}
	
	private void checkBoxInCash(){
		if(cash_num == 0){
			new Util(mContext).showFeatureToast(mContext
					.getString(R.string.buy_bill_prompt));
		}
		
		if(!AppManager.isNetEnable){
			new Util(mContext).showFeatureToast(mContext
					.getString(R.string.network_error));
		}
		
		//关闭钱箱
		if (process_event == ProcessEvent.EVENT_BUY_QR) {
			NetServiceManager.getInstance().BuyQRBitcoin(user_public_key,
					AppManager.getUserId(), cash_num);
			progressDialog = new Util(getActivity())
					.showProgressBar(getActivity().getString(R.string.wait));
		} else if (process_event == ProcessEvent.EVENT_BUY_WALLET){
			NetServiceManager.getInstance().BuyWalletBitcoin(
					AppManager.getUserId(), cash_num);
			progressDialog = new Util(getActivity())
					.showProgressBar(getActivity().getString(R.string.wait));
		}
	}
	
	public void onEventMainThread(ATMBroadCastEvent event) {
		switch (event.getType()) {
		case ATMBroadCastEvent.EVENT_BUY_QR_SUCCESS:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			BuyBitcoinQRStruct qrStruct = (BuyBitcoinQRStruct) event
					.getObject();
			if ("success".equals(qrStruct.resutlString)) {
				ConfirmFragment fragment = new ConfirmFragment();
				Bundle b = new Bundle();
				b.putString("reason", qrStruct.bitcoin_num+"");
				fragment.setArguments(b);
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.add(R.id.container, fragment)
						.addToBackStack("redeemconfirmfragment").commit();
			} else if ("fail".equals(qrStruct.resutlString)) {
				String msgString = null;
				switch (qrStruct.reason) {
				case 1:
					msgString = getString(R.string.buy_fail_reason_1);
					break;
				case 2:
					msgString = getString(R.string.buy_fail_reason_2);
					break;
				default:
					break;
				}
				new Util(getActivity()).showFeatureToast(msgString);
			}
			break;
		case ATMBroadCastEvent.EVENT_BUY_WALLET_SUCCESS:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			BuyBitcoinPrintWalletStruct struct = (BuyBitcoinPrintWalletStruct) event
					.getObject();
			if ("success".equals(struct.resutlString)) {
				//打印二维码
				ConfirmFragment fragment = new ConfirmFragment();
				Bundle b = new Bundle();
				b.putString("reason", struct.bitcoin_num+"");
				fragment.setArguments(b);
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.add(R.id.container, fragment)
						.addToBackStack("redeemconfirmfragment").commit();
			} else if ("fail".equals(struct.resutlString)) {
				String msgString = null;
				switch (struct.reason) {
				case 1:
					msgString = getString(R.string.buy_fail_reason_1);
					break;
				case 2:
					msgString = getString(R.string.buy_fail_reason_2);
					break;
				default:
					break;
				}
				new Util(getActivity()).showFeatureToast(msgString);
			}
			break;
		case ATMBroadCastEvent.EVENT_BUY_WALLET_FAIL:	
		case ATMBroadCastEvent.EVENT_BUY_QR_FAIL:
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
