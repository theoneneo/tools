package com.bitocean.atm.fragment;

import java.io.File;

import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitocean.atm.CameraActivity;
import com.bitocean.atm.QrCaptureActivity;
import com.bitocean.atm.R;
import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.controller.NetServiceManager;
import com.bitocean.atm.protocol.ProtocolDataInput;
import com.bitocean.atm.service.ATMBroadCastEvent;
import com.bitocean.atm.struct.LoginAdminStruct;
import com.bitocean.atm.struct.RedeemConfirmStruct;
import com.bitocean.atm.util.Util;

import de.greenrobot.event.EventBus;

/**
 * @author bing.liu
 * 
 */
public class RedeemScanQRFragment extends NodeFragment {
	private ProgressDialog progressDialog = null;
	private ImageView qr_image;
	private TextView key_text;
	private String redeemInfoString = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this, ATMBroadCastEvent.class);
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
		View v = mInflater.inflate(R.layout.fragment_redeem_qr, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.redeem_prompt);

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

		qr_image = (ImageView) v.findViewById(R.id.qr_image);
		qr_image.setEnabled(true);
		qr_image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				goToQrCaptureActivity();
			}
		});
		
		key_text = (TextView)v.findViewById(R.id.key_text);	

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
				redeemConfirm(redeemInfoString);
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
				redeemInfoString = bundle.getString("result");
				// 显示
				qr_image.setImageBitmap((Bitmap) data
						.getParcelableExtra("bitmap"));
				key_text.setText(redeemInfoString);
			}
		}
	}

	private void redeemConfirm(String redeemString) {
		if(!AppManager.isNetEnable){
			new Util(mContext).showFeatureToast(mContext
					.getString(R.string.network_error));
			return;
		}
		
		if(redeemString == null){
			new Util(getActivity()).showFeatureToast(getString(R.string.check_redeem_code));
			return;
		}
		
		NetServiceManager.getInstance().redeemConfirm(redeemString);
		progressDialog = new Util(getActivity()).showProgressBar(getActivity()
				.getString(R.string.wait));
	}

	public void onEventMainThread(ATMBroadCastEvent event) {
		switch (event.getType()) {
		case ATMBroadCastEvent.EVENT_REDEEM_CONFIRM_SUCCESS:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			RedeemConfirmStruct struct = (RedeemConfirmStruct) event
					.getObject();
			if ("success".equals(struct.resutlString)) {
				RedeemSuccessFragment fragment = new RedeemSuccessFragment();
				Bundle b = new Bundle();
				b.putSerializable("redeemconfirmstruct", struct);
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
					msgString = getString(R.string.redeem_fail_1);
					break;
				case 2:
					msgString = getString(R.string.redeem_fail_2);
					break;
				case 3:
					msgString = getString(R.string.redeem_fail_3);
					break;
				default:
					break;
				}
				ConfirmFragment fragment = new ConfirmFragment();
				Bundle b = new Bundle();
				b.putString("reason", msgString);
				fragment.setArguments(b);
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.add(R.id.container, fragment)
						.addToBackStack("redeemconfirmfragment").commit();
			}
			break;
		case ATMBroadCastEvent.EVENT_REDEEM_CONFIRM_FAIL:
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
