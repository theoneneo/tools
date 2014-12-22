package com.bitocean.atm.fragment;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bitocean.atm.R;
import com.bitocean.atm.TradeModeActivity;
import com.bitocean.atm.controller.NetServiceManager;
import com.bitocean.atm.service.ATMBroadCastEvent;
import com.bitocean.atm.struct.LoginUserStruct;
import com.bitocean.atm.struct.VerifyCodeStruct;
import com.bitocean.atm.util.Util;

import de.greenrobot.event.EventBus;
/**
 * @author bing.liu
 * 
 */
public class RegisterPhoneFragment extends NodeFragment {
	private ProgressDialog progressDialog = null;
	private String userIconString;
	private String passportString;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this, ATMBroadCastEvent.class);
		Bundle b = getArguments();
		if (b == null)
			return;
		userIconString = (String) b.getSerializable("user_icon_url");
		passportString = (String) b.getSerializable("passport_url");
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_register_phone, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		final EditText nameEditText = (EditText) v.findViewById(R.id.name_edit);
		final EditText passwordEditText = (EditText) v
				.findViewById(R.id.password_edit);
		final EditText verifyEditText = (EditText) v
				.findViewById(R.id.verify_edit);
		Button registerButton = (Button) v.findViewById(R.id.register_btn);
		registerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				registerUser(nameEditText.getText().toString(),
						passwordEditText.getText().toString(), verifyEditText
								.getText().toString());
			}
		});

		Button verifyButton = (Button) v.findViewById(R.id.verify_btn);
		verifyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getVerifyCode(nameEditText.getText().toString());
			}
		});

		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.register_phone_prompt);

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
		nextButton.setText(R.string.out);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				deleteUserIconPassport();
				getActivity()
						.getSupportFragmentManager()
						.popBackStack(
								"registerphotofragment",
								android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}
		});
	}

	private void deleteUserIconPassport() {
		new Thread(new deletePicture()).start();

	}

	class deletePicture implements Runnable {
		@Override
		public void run() {
			File userIcon = new File(userIconString);
			userIcon.delete();
			File passport = new File(passportString);
			passport.delete();
		}
	}

	private void registerUser(String user_id, String user_password,
			String verifyCode) {
		if (user_id == null || user_id.equals("")) {
			new Util(getActivity()).showFeatureToast(getActivity().getString(
					R.string.admin_name_error));
			return;
		}

		if (user_password == null || user_password.equals("")) {
			new Util(getActivity()).showFeatureToast(getActivity().getString(
					R.string.admin_password_error));
			return;
		}

		if (verifyCode == null || verifyCode.equals("")) {
			new Util(getActivity()).showFeatureToast(getActivity().getString(
					R.string.verify_error));
			return;
		}

		NetServiceManager.getInstance().registerUser(user_id, user_password,
				verifyCode, userIconString, passportString);
		progressDialog = new Util(getActivity()).showProgressBar(getActivity()
				.getString(R.string.wait));
	}

	private void getVerifyCode(String user_id) {
		if (user_id == null || user_id.equals("")) {
			new Util(getActivity()).showFeatureToast(getActivity().getString(
					R.string.admin_name_error));
			return;
		}

		NetServiceManager.getInstance().verifyCode(user_id);
		progressDialog = new Util(getActivity()).showProgressBar(getActivity()
				.getString(R.string.wait));
	}

	public void onEventMainThread(ATMBroadCastEvent event) {
		switch (event.getType()) {
		case ATMBroadCastEvent.EVENT_USER_REGISTER_SUCCESS:
			deleteUserIconPassport();
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			new Util(getActivity()).showFeatureToast(getActivity().getString(
					R.string.register_success));
			goToTradeModeActivity();
			break;
		case ATMBroadCastEvent.EVENT_USER_REGISTER_FAIL:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			new Util(getActivity())
					.showFeatureToast((String) event.getObject());
			break;
		case ATMBroadCastEvent.EVENT_VERIFY_CODE_SUCCESS:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			VerifyCodeStruct struct = (VerifyCodeStruct) event.getObject();
			if ("success".equals(struct.resutlString)) {
				new Util(getActivity()).showFeatureToast(getActivity()
						.getString(R.string.send_verify_success));
			} else if ("fail".equals(struct.resutlString)) {
				new Util(getActivity()).showFeatureToast(struct.resonString);
			}
			break;
		case ATMBroadCastEvent.EVENT_VERIFY_CODE_FAIL:
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

	private void goToTradeModeActivity() {
		Intent intent = new Intent(getActivity(), TradeModeActivity.class);
		startActivity(intent);
		getActivity().finish();
	}
}
