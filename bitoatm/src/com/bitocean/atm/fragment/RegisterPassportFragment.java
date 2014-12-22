package com.bitocean.atm.fragment;

import java.io.File;
import android.app.Activity;
import android.app.FragmentTransaction;
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
import com.bitocean.atm.struct.LoginAdminStruct;
/**
 * @author bing.liu
 * 
 */
public class RegisterPassportFragment extends NodeFragment {
	private ImageView photoImageView;
	private Bitmap bitmap = null;
	private String filepath;
	private String userIconString;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		Bundle b = getArguments();
		if (b == null)
			return;
		userIconString = (String) b.getSerializable("user_icon_url");
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View v = mInflater.inflate(R.layout.fragment_register_passport, null);
		initView(v);
		return v;
	}

	public void onDestroy() {
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
				if(bitmap != null){
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
							.addToBackStack("registerpassportfragment").commit();
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
}
