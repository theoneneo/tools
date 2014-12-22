package com.bitocean.atm.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
/**
 * @author bing.liu
 * 
 */
public abstract class NodeFragment extends Fragment {
	protected Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
	}
}
