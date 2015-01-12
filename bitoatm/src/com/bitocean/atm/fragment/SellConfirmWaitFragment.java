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
import com.bitocean.atm.struct.SellBitcoinConfirmStruct;
import com.bitocean.atm.struct.SellBitcoinQRStruct;
import com.bitocean.atm.util.Util;

import de.greenrobot.event.EventBus;
/**
 * @author bing.liu
 * 
 */
public class SellConfirmWaitFragment extends NodeFragment {
	private String user_public_key = null;
	private int process_event = 0;
	private int currency_num = 0;
	private boolean isLoop = true;
	private ProgressDialog progressDialog = null;
	
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
		return;
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
			return;
		}
		
		Thread sellThread = new Thread() {
			public void run() {
				while (isLoop) {
					NetServiceManager.getInstance().SellBitcoin(user_public_key, AppManager.getUserId(), currency_num);
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
	
	public void onEventMainThread(ATMBroadCastEvent event) {
		switch (event.getType()) {
		case ATMBroadCastEvent.EVENT_GET_SELL_SUCCESS:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			SellBitcoinConfirmStruct struct = (SellBitcoinConfirmStruct) event
					.getObject();
			if ("success".equals(struct.resutlString)) {
				if(struct.currency_num == 0){
					//打印赎回码
				}else{
					//吐钞
				}
				
				ConfirmFragment fragment = new ConfirmFragment();
				Bundle b = new Bundle();
				b.putString("reason", getString(R.string.complete_tran));
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
					msgString = getString(R.string.sell_bitcoin_display_1);
					break;
				case 2:
					msgString = getString(R.string.sell_bitcoin_display_2);
					break;
				default:
					break;
				}
				new Util(getActivity()).showFeatureToast(msgString);
			}
			isLoop = false;
			break;
		case ATMBroadCastEvent.EVENT_GET_SELL_FAIL:
			new Util(getActivity())
					.showFeatureToast((String) event.getObject());
			break;
		default:
			break;
		}
	}
}
