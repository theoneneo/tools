package com.bitocean.atm.fragment;

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
import com.bitocean.atm.util.Util;
/**
 * @author bing.liu
 * 
 */
public class SellConfirmWaitFragment extends NodeFragment {

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_confirm, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.confirm);
		
		TextView textView = (TextView) v.findViewById(R.id.text);
//		textView.setText(reasonString);

		Button cancelButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.left_btn);
		cancelButton.setVisibility(View.INVISIBLE);

		Button nextButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.right_btn);
		nextButton.setVisibility(View.INVISIBLE);
		
		Button confirmButton = (Button) v.findViewById(R.id.confirm_btn);
		confirmButton.setVisibility(View.GONE);
		
		checkSellResult();
	}
	
	private void checkSellResult(){
		if(!AppManager.isNetEnable){
			new Util(mContext).showFeatureToast(mContext
					.getString(R.string.network_error));
		}
		
		Thread sellThread = new Thread() {
			public void run() {
				while (true) {
					NetServiceManager.getInstance().SellBitcoin
					try {
						sleep(6000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					

				}
			}
		};
		sellThread.start();
	}
}
