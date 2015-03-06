package com.mybitcoin.wallet.ui.first;

import org.apache.http.Header;

import com.google.bitcoin.core.Wallet;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.WalletUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UserFragment extends BaseFragment {
	private Button stepBtn;
	private Button nextBtn;
	private EditText pwEdit;
	private LinearLayout progressBar;
	private EditText userEdit;
	private String pkey;
	private TextView pkText;

	public UserFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_user, container,
				false);
		stepBtn = (Button) rootView.findViewById(R.id.step);
		nextBtn = (Button) rootView.findViewById(R.id.next);
		pwEdit = (EditText) rootView.findViewById(R.id.pwEdit);
		userEdit = (EditText) rootView.findViewById(R.id.userEdit);
		pkText = (TextView) rootView.findViewById(R.id.pkContent);

		progressBar = (LinearLayout) rootView.findViewById(R.id.progressbar);

		Wallet wallet = ((WalletApplication) getActivity().getApplication())
				.getWallet();
		pkey = WalletUtils.pickOldestKey(wallet).toAddress(Constants.NETWORK_PARAMETERS).toString();

		stepBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).stepToWelcome();
			}
		});
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String pw = pwEdit.getText().toString();
				String user = userEdit.getText().toString();
				APIModule.getInstance().user(user, pw, pkey, new UserResponse());
			}
		});
		
		stepBtn.setText(getString(UiInfo.appstart_skip_btn));
		nextBtn.setText(getString(UiInfo.appstart_next_btn));
		pkText.setText(pkey);
		
		((TextView) rootView.findViewById(R.id.title)).setText(getString(UiInfo.appstart_pagetitle_usernameconfig));
		((TextView) rootView.findViewById(R.id.userText)).setText(getString(UiInfo.appstart_username));
		((TextView) rootView.findViewById(R.id.pwText)).setText(getString(UiInfo.appstart_password));
		((TextView) rootView.findViewById(R.id.pkText)).setText(getString(UiInfo.appstart_publickey));
		return rootView;
	}

	private class UserResponse extends AsyncHttpResponseHandler {
		@Override
		public void onStart() {
			progressBar.setVisibility(View.VISIBLE);
			stepBtn.setClickable(false);
			nextBtn.setClickable(false);
		}

		@Override
		public void onSuccess(int statusCode, Header[] headers, byte[] response) {
			String result = new String(response);
			progressBar.setVisibility(View.GONE);
			stepBtn.setClickable(true);
			nextBtn.setClickable(true);
			if (result.equals("OK")) {
				((MainActivity) getActivity()).switchFragment(2);
			} else {
				Toast.makeText(getActivity(), "验证失败", Toast.LENGTH_LONG).show();
				((MainActivity) getActivity()).stepToWelcome();
			}

		}

		@Override
		public void onFailure(int statusCode, Header[] headers,
				byte[] errorResponse, Throwable e) {
			stepBtn.setClickable(true);
			nextBtn.setClickable(true);
			Toast.makeText(getActivity(), "网络请求超时", Toast.LENGTH_SHORT).show();
			progressBar.setVisibility(View.GONE);
			((MainActivity) getActivity()).stepToWelcome();


		}

	}
}
