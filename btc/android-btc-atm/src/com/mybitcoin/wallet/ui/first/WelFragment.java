package com.mybitcoin.wallet.ui.first;

import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class WelFragment extends BaseFragment {
	private Button stepBtn;
	private Button nextBtn;

	public WelFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_wel, container,
				false);
		stepBtn = (Button) rootView.findViewById(R.id.step);
		nextBtn = (Button) rootView.findViewById(R.id.next);

		stepBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).stepToWelcome();

			}
		});
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).switchFragment(3);

			}
		});
		
		stepBtn.setText(getString(UiInfo.appstart_skip_btn));
		nextBtn.setText(getString(UiInfo.appstart_next_btn));

		((TextView)rootView.findViewById(R.id.welcome_text)).setText(getString(UiInfo.appstart_welcometitle));
		
		return rootView;

	}
}
