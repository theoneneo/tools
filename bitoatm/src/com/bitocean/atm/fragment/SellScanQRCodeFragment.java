package com.bitocean.atm.fragment;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitocean.atm.R;
import com.bitocean.atm.controller.ProcessEvent;
import com.bitocean.atm.service.ATMBroadCastEvent;
import com.bitocean.atm.struct.SellBitcoinQRStruct;

import de.greenrobot.event.EventBus;

/**
 * @author bing.liu
 * 
 */
public class SellScanQRCodeFragment extends NodeFragment {
	private SellBitcoinQRStruct struct = null;
	private String user_public_key = null;
	private int currency_num = 0;
	private int process_event = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		Bundle b = getArguments();
		if (b == null)
			return;
		struct = (SellBitcoinQRStruct) b.getSerializable("sellbitcoinqrstruct");
		user_public_key = (String) b.getString("user_public_key");
		currency_num = (int) b.getInt("currency_num", 0);
		process_event = (int) b.getInt("process_event", 0);
		return;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_sell_scan_qr_code, null);
		initView(v);
		return v;
	}

	private void initView(View v) {
		if (struct == null)
			return;

		TextView titleTextView = (TextView) v.findViewById(R.id.title_text)
				.findViewById(R.id.view_text);
		titleTextView.setText(R.string.sell_flow_display);

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

		createCode();
		ImageView qr_image = (ImageView) v.findViewById(R.id.qr_image);
//		qr_image.setImageBitmap(bm);

		if (struct.quota_num == 0) {
			TextView large_prompt_text = (TextView) v
					.findViewById(R.id.large_prompt_text);
			large_prompt_text.setVisibility(View.VISIBLE);
		}

		Button nextButton = (Button) v.findViewById(R.id.bottom_button)
				.findViewById(R.id.right_btn);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SellConfirmWaitFragment fragment = new SellConfirmWaitFragment();
				Bundle b = new Bundle();
				b.putString("user_public_key", user_public_key);
				b.putInt("currency_num", currency_num);
				b.putInt("process_event", process_event);
				fragment.setArguments(b);
				getActivity()
						.getSupportFragmentManager()
						.beginTransaction()
						.setTransition(
								FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.add(R.id.container, fragment)
						.addToBackStack("sellscanqrcodefragment").commit();
			}
		});
	}

	private void createCode() {
		String text = "http://blog.csdn.net/gao36951";
		// int width = 300;
		// int height = 300;
		// // 二维码的图片格式
		// String format = "png";
		// /**
		// * 设置二维码的参数
		// */
		// Hashtable<EncodeHintType, Object> hints = new
		// Hashtable<EncodeHintType, Object>();
		// // 内容所使用编码
		// hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		// try {
		// BitMatrix bitMatrix = new
		// MultiFormatWriter().encode(text,BarcodeFormat.QR_CODE,width,height,hints);
		// // 生成二维码
		// File outputFile = new File("");
		// MatrixToImageWriter.writeToFile(bitMatrix, format, outputFile);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

	}
}
