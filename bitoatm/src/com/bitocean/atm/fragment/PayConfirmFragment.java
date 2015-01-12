package com.bitocean.atm.fragment;

import u.aly.cv;
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
/**
 * @author bing.liu
 * 
 */
public class PayConfirmFragment extends NodeFragment {
	private String user_public_key = null;
	private int process_event = 0;
	private int currency_num;
	private TextView currency_num_text;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		Bundle b = getArguments();
		if (b == null)
			return;
		user_public_key = (String) b.getString("user_public_key");
		process_event = (int) b.getInt("process_event", 0);
		currency_num = (int) b.getInt("currency_num", 0);
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
		textView.setText(R.string.buy_pay_confirm_prompt);
		
		currency_num_text = (TextView)v.findViewById(R.id.currency_num_text);

		Button cancelButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.left_btn);
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
				BuyBillFragment fragment = new BuyBillFragment();
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
						.addToBackStack("payconfirmfragment").commit();
			}
		});
		
		Button confirmButton = (Button) v.findViewById(R.id.confirm_btn);
		confirmButton.setVisibility(View.INVISIBLE);
	}
}
