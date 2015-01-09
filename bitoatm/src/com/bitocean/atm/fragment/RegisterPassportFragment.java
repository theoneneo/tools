package com.bitocean.atm.fragment;

import java.io.File;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitocean.atm.CameraActivity;
import com.bitocean.atm.R;
import com.bitocean.atm.controller.AppManager;
import com.bitocean.atm.controller.NetServiceManager;
import com.bitocean.atm.service.ATMBroadCastEvent;
import com.bitocean.atm.struct.LoginUserStruct;
import com.bitocean.atm.util.Util;

import de.greenrobot.event.EventBus;

/**
 * @author bing.liu
 * 
 */
public class RegisterPassportFragment extends NodeFragment {
	private ProgressDialog progressDialog = null;
	private LoginUserStruct struct;
	private ImageView photoImageView;
	private Bitmap bitmap = null;
	private String filepath;
	private String userIconString;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this, ATMBroadCastEvent.class);
		mContext = getActivity().getApplicationContext();
		Bundle b = getArguments();
		if (b == null)
			return;
		userIconString = (String) b.getSerializable("user_icon_url");
		struct = (LoginUserStruct) b.getSerializable("loginuserstruct");
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_register_passport, null);
		initView(v);
		return v;
	}

	public void onDestroy() {
		if (progressDialog != null)
			progressDialog.dismiss();
		EventBus.getDefault().unregister(this);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
		super.onDestroy();
	}

	private void initView(View v) {
		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.register_passport_prompt);

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
		nextButton.setText(R.string.next);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (bitmap != null) {
					if (struct != null) {
						if(!AppManager.isNetEnable){
							new Util(mContext).showFeatureToast(mContext
									.getString(R.string.network_error));
							return;
						}
						
						NetServiceManager.getInstance().registerUserKyc(
								struct.user_idString, userIconString, filepath);
						progressDialog = new Util(getActivity())
								.showProgressBar(getActivity().getString(
										R.string.wait));
					} else {
						RegisterPhoneFragment fragment = new RegisterPhoneFragment();
						Bundle b = new Bundle();
						b.putSerializable("user_icon_url", userIconString);
						b.putSerializable("passport_url", filepath);
						fragment.setArguments(b);
						getActivity()
								.getSupportFragmentManager()
								.beginTransaction()
								.setTransition(
										FragmentTransaction.TRANSIT_FRAGMENT_FADE)
								.add(R.id.container, fragment)
								.addToBackStack("registerpassportfragment")
								.commit();
					}
				} else {
					new Util(getActivity())
							.showFeatureToast(R.string.register_passport_error);
				}
			}
		});

		ImageButton photoButton = (ImageButton) v.findViewById(R.id.photo_btn);
		photoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startCameraActivity();
			}
		});

		photoImageView = (ImageView) v.findViewById(R.id.photo_image);
		photoImageView.setEnabled(true);
		photoImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startCameraActivity();
			}
		});
	}

	private void startCameraActivity() {
		Intent intent = new Intent(getActivity(), CameraActivity.class);
		intent.putExtra("fileType", "passport");
		startActivityForResult(intent, 1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				filepath = data.getStringExtra("url");
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Config.RGB_565;
				options.inPurgeable = false;

				File passport = new File(filepath);

				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
				bitmap = BitmapFactory.decodeFile(passport.getAbsolutePath(),
						options);
				photoImageView.setImageBitmap(bitmap);
				photoImageView.invalidate();
			}
		}
	}

	public void onEventMainThread(ATMBroadCastEvent event) {
		switch (event.getType()) {
		case ATMBroadCastEvent.EVENT_USER_REGISTER_KYC_SUCCESS:
			deleteUserIconPassport();
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}

			new Util(getActivity()).showFeatureToast(getActivity().getString(
					R.string.register_success));

			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("loginuserstruct", struct);
			resultIntent.putExtras(bundle);
			getActivity().setResult(getActivity().RESULT_OK, resultIntent);
			getActivity().finish();
			break;
		case ATMBroadCastEvent.EVENT_USER_REGISTER_KYC_FAIL:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}

			String msgString = (String) event.getObject();
			if ("1".equals(msgString)) {
				msgString = getString(R.string.register_fail_1);
			} else if ("2".equals(msgString)) {
				msgString = getString(R.string.register_fail_1);
			} else if ("3".equals(msgString)) {
				msgString = getString(R.string.register_fail_1);
			}

			ConfirmFragment fragment = new ConfirmFragment();
			Bundle b = new Bundle();
			b.putString("reason", msgString);
			fragment.setArguments(b);
			getActivity().getSupportFragmentManager().beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.add(R.id.container, fragment)
					.addToBackStack("redeemconfirmfragment").commit();
			break;
		default:
			break;
		}
	}

	private void deleteUserIconPassport() {
		new Thread(new deletePicture()).start();

	}

	class deletePicture implements Runnable {
		@Override
		public void run() {
			File userIcon = new File(userIconString);
			userIcon.delete();
			File passport = new File(filepath);
			passport.delete();
		}
	}
}
