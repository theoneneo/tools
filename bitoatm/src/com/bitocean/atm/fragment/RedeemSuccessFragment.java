package com.bitocean.atm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bitocean.atm.R;
import com.bitocean.atm.struct.RedeemConfirmStruct;
/**
 * @author bing.liu
 * 
 */
public class RedeemSuccessFragment extends NodeFragment {
	private RedeemConfirmStruct struct;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		Bundle b = getArguments();
		if (b == null)
			return;
		struct = (RedeemConfirmStruct)b.getSerializable("redeemconfirmstruct");
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
		confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getActivity().finish();
			}
		});
		out_cash();
	}
	
	private void out_cash(){
		//TODO 出钞，发送出钞通知，用户
	}
}
