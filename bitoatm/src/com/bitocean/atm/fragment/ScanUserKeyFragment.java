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
import android.widget.ImageView;
import android.widget.TextView;

import com.bitocean.atm.QrCaptureActivity;
import com.bitocean.atm.R;
import com.bitocean.atm.controller.ProcessEvent;
import com.bitocean.atm.struct.LoginAdminStruct;
import com.bitocean.atm.util.Util;

/**
 * @author bing.liu
 * 
 */
public class ScanUserKeyFragment extends NodeFragment {
	private ImageView qr_image;
	private TextView key_text;
	private String user_public_key = null;
	private int process_event = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		Bundle b = getArguments();
		if (b == null)
			return;
		process_event = (int) b.getInt("process_event", 0);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_scan_user_key, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.scan_user_key_prompt);
		
		key_text = (TextView)v.findViewById(R.id.key_text);
		
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

		qr_image = (ImageView) v.findViewById(R.id.qr_image);
		qr_image.setEnabled(true);
		qr_image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goToQrCaptureActivity();
			}
		});

		Button qrButton = (Button) v.findViewById(R.id.qr_btn);
		qrButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goToQrCaptureActivity();
			}
		});

		Button nextButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.right_btn);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (user_public_key == null) {
					new Util(getActivity())
							.showFeatureToast(getString(R.string.check_scan_user_public_key));
					return;
				}

				CurrencyCountFragment fragment = new CurrencyCountFragment();
				Bundle b = new Bundle();
				b.putString("user_public_key", user_public_key);
				b.putInt("process_event", process_event);
				fragment.setArguments(b);
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.add(R.id.container, fragment)
						.addToBackStack("scanuserkeyfragment").commit();
			}
		});
	}

	private void goToQrCaptureActivity() {
		Intent intent = new Intent(getActivity(), QrCaptureActivity.class);
		startActivityForResult(intent, 1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				Bundle bundle = data.getExtras();
				user_public_key = bundle.getString("result");
				// 显示
				qr_image.setImageBitmap((Bitmap) data
						.getParcelableExtra("bitmap"));
				key_text.setText(user_public_key);
			}
		}
	}
}
