package com.bitocean.atm.fragment;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitocean.atm.R;
import com.bitocean.atm.struct.SellBitcoinQRStruct;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * @author bing.liu
 * 
 */
public class SellScanQRCodeFragment extends NodeFragment {
	private SellBitcoinQRStruct struct = null;
	private String user_public_key = null;
	private int currency_num = 0;
	private int process_event = 0;
	private String path;
	private ImageView qr_image;

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

		qr_image = (ImageView) v.findViewById(R.id.qr_image);
		if (struct != null)
			qr_image.setImageBitmap(generateQRCode(struct.bitcoin_qr));

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

	private Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
		int w = matrix.getWidth();
		int h = matrix.getHeight();
		int[] rawData = new int[w * h];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int color = Color.WHITE;
				if (matrix.get(i, j)) {
					color = Color.BLACK;
				}
				rawData[i + (j * w)] = color;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
		bitmap.setPixels(rawData, 0, w, 0, 0, w, h);
		return bitmap;
	}

	private Bitmap generateQRCode(String content) {
		try {
			QRCodeWriter writer = new QRCodeWriter();
			// MultiFormatWriter writer = new MultiFormatWriter();
			BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE,
					500, 500);
			return bitMatrix2Bitmap(matrix);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}
}
