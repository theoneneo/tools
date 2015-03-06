package com.mybitcoin.wallet.ui.first;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KeyFragment extends BaseFragment {
	private Button stepBtn;
	private Button nextBtn;
	private TextView secretKey;
	private LinearLayout progressBar;
	private TextView pubKey;

	public KeyFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_key, container,
				false);
		stepBtn = (Button) rootView.findViewById(R.id.step);
		nextBtn = (Button) rootView.findViewById(R.id.next);
		secretKey = (TextView) rootView.findViewById(R.id.skContent);
		pubKey = (TextView) rootView.findViewById(R.id.pkContent);

		progressBar = (LinearLayout) rootView.findViewById(R.id.progressbar);

		Wallet wallet = ((WalletApplication) getActivity().getApplication())
				.getWallet();

		for (final ECKey key : wallet.getKeys()) {
			if (key.getPrivKeyBytes() != null && !wallet.isKeyRotating(key)) {
				String skey = key.getPrivateKeyEncoded(
						Constants.NETWORK_PARAMETERS).toString();

				String pkey = WalletUtils.pickOldestKey(wallet)
						.toAddress(Constants.NETWORK_PARAMETERS).toString();

				secretKey.setText(skey);
				pubKey.setText(pkey);
				break;
			}
		}
		stepBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).stepToWelcome();
			}
		});
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).stepToWelcome();

			}
		});

		stepBtn.setText(getString(UiInfo.appstart_skip_btn));
		nextBtn.setText(getString(UiInfo.appstart_next_btn));

		((TextView) rootView.findViewById(R.id.title)).setText(getString(UiInfo.appstart_pagetitle_keydisplay));
		((TextView) rootView.findViewById(R.id.pkText)).setText(getString(UiInfo.appstart_publickey));
		((TextView) rootView.findViewById(R.id.skText)).setText(getString(UiInfo.appstart_privatekey));

		return rootView;
	}

	private class KeyResponse extends AsyncHttpResponseHandler {
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
			if (!result.equals("")) {
			} else {
				Toast.makeText(getActivity(), "上传失败", Toast.LENGTH_LONG).show();
			}
			((MainActivity) getActivity()).stepToWelcome();

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
