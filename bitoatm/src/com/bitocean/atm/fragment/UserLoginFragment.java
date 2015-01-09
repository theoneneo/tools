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
import android.widget.EditText;
import android.widget.TextView;

import com.bitocean.atm.R;
import com.bitocean.atm.TradeModeActivity;
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
public class UserLoginFragment extends NodeFragment {
	private ProgressDialog progressDialog = null;
	private String user_idString;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this, ATMBroadCastEvent.class);
	}

	@Override
	public void onDestroy() {
		if(progressDialog != null)
			progressDialog.dismiss();
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_user_login, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		final EditText nameEditText = (EditText) v.findViewById(R.id.name_edit);
		final EditText passwordEditText = (EditText) v
				.findViewById(R.id.password_edit);
		Button loginButton = (Button) v.findViewById(R.id.login_btn);
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				user_idString = nameEditText.getText().toString();
				loginUser(nameEditText.getText().toString(), passwordEditText
						.getText().toString());
			}
		});
		
		Button registerButton = (Button) v.findViewById(R.id.register_btn);
		registerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(AppManager.isKycRegister){
					getActivity()
					.getSupportFragmentManager()
					.beginTransaction()
					.setTransition(
							FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.add(R.id.container, new RegisterPhotoFragment())
					.addToBackStack("userlogin").commit();
				}else{
					RegisterPhoneFragment fragment = new RegisterPhoneFragment();
					getActivity()
							.getSupportFragmentManager()
							.beginTransaction()
							.setTransition(
									FragmentTransaction.TRANSIT_FRAGMENT_FADE)
							.add(R.id.container, fragment)
							.addToBackStack("registerpassportfragment")
							.commit();
				}
			}
		});

		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.user_login_prompt);

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

		Button nextButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.right_btn);
		nextButton.setVisibility(View.INVISIBLE);
	}

	private void loginUser(String user_id, String user_password) {
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
		
		if(!AppManager.isNetEnable){
			new Util(mContext).showFeatureToast(mContext
					.getString(R.string.network_error));
			return;
		}
		
		NetServiceManager.getInstance().loginUser(user_id, user_password);
		progressDialog = new Util(getActivity()).showProgressBar(getActivity()
				.getString(R.string.wait));
	}

	public void onEventMainThread(ATMBroadCastEvent event) {
		switch (event.getType()) {
		case ATMBroadCastEvent.EVENT_USER_LOGIN_SUCCESS:
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			LoginUserStruct struct = (LoginUserStruct) event.getObject();
			if ("success".equals(struct.resutlString)) {
				goToTradeModeActivity(struct);
			} else if ("fail".equals(struct.resutlString)) {
				String msgString = null;
				switch (struct.reason) {
				case 1:
					msgString = getString(R.string.user_login_fail_1);
					break;
				case 2:
					msgString = getString(R.string.user_login_fail_2);
					break;
				case 3:
					msgString = getString(R.string.user_login_fail_3);
					break;
				default:
					break;
				}
				new Util(getActivity()).showFeatureToast(msgString);
			}
			break;
		case ATMBroadCastEvent.EVENT_USER_LOGIN_FAIL:
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
	
	private void goToTradeModeActivity(LoginUserStruct struct){
		if(("NO_KYC").equals(struct.userTypeString) && AppManager.isKycRegister){
			//如果用户没有kyc,但是当前需要kyc,走验证kyc流程
			KycConfirmFragment fragment = new KycConfirmFragment();
			Bundle b = new Bundle();
			b.putSerializable("loginuserstruct", struct);
			fragment.setArguments(b);
			
			getActivity()
			.getSupportFragmentManager()
			.beginTransaction()
			.setTransition(
					FragmentTransaction.TRANSIT_FRAGMENT_FADE)
			.add(R.id.container, fragment)
			.addToBackStack("userlogin").commit();
		}else{
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("loginuserstruct", struct);
			resultIntent.putExtras(bundle);
			getActivity().setResult(getActivity().RESULT_OK, resultIntent);
			getActivity().finish();	
		}
	}
}
