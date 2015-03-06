package com.mybitcoin.wallet.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mybitcoin.wallet.R;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ArFragmentActivity extends FragmentActivity {

	// public CameraPreview mPreview;
	public static boolean isCameraPicture = false;
	public Button submit, photo, cancel;
	public TextView timer;
	public static String fileType;
	private Camera camera;
	private Camera.Parameters parameters = null;
	private static String path;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (getIntent() != null) {
			fileType = getIntent().getStringExtra("file");
		}

		setContentView(R.layout.activity_ar_activity);
		submit = (Button) findViewById(R.id.submit);
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent data = new Intent();
				data.putExtra("url", path);
				ArFragmentActivity.this.setResult(RESULT_OK, data);
				finish();
			}

		});
		photo = (Button) findViewById(R.id.photo);
		photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isCameraPicture = true;
				camera.autoFocus(new AutoFocusCallback() {

					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						// TODO Auto-generated method stub
						camera.takePicture(null, null, new MyPictureCallback());
					}
				});
			}

		});
		cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (isCameraPicture) {
					photo.setVisibility(View.VISIBLE);
					submit.setVisibility(View.GONE);
					time = 300;
					camera.startPreview();
					isCameraPicture = false;
				} else {
					ArFragmentActivity.this.setResult(
							ArFragmentActivity.this.RESULT_CANCELED, null);
					finish();
				}
			}

		});

		SurfaceView surfaceView = (SurfaceView) this
				.findViewById(R.id.surfaceView);
		surfaceView.getHolder()
				.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//		surfaceView.getHolder().setFixedSize(176, 144);
		surfaceView.getHolder().setKeepScreenOn(true);
		surfaceView.getHolder().addCallback(new SurfaceCallback());

		timer = (TextView) findViewById(R.id.timer);
		timer.setText(time + "");

		new Thread(new ClassCut()).start();
	}

	private int time = 300;
	private Handler mHandler = new Handler();

	class ClassCut implements Runnable {
		@Override
		public void run() {
			while (time > 0) {
				time--;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						timer.setText(time + "");
					}
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					ArFragmentActivity.this.finish();
				}
			});
		}
	}

	Bundle bundle = null;

	private final class MyPictureCallback implements PictureCallback {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				bundle = new Bundle();
				bundle.putByteArray("bytes", data);
				saveToSDCard(data);

				photo.setVisibility(View.GONE);
				submit.setVisibility(View.VISIBLE);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void saveToSDCard(byte[] data) throws IOException {

		path = "/sdcard/" + fileType

		+ new SimpleDateFormat("yyyyMMddHHmmss")

		.format(new Date()) + ".jpg";

		File file = new File(path);

		FileOutputStream outputStream = new FileOutputStream(file);

		outputStream.write(data);

		outputStream.close();

	}

	private final class SurfaceCallback implements Callback {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			parameters = camera.getParameters();
			parameters.setPictureFormat(PixelFormat.JPEG);
			parameters.setPreviewSize(640, 480);
			parameters.setPreviewFrameRate(5);
			parameters.setPictureSize(640, 480);
//			parameters.setJpegQuality(80);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera = Camera.open();
				camera.setPreviewDisplay(holder);
				camera.setDisplayOrientation(Surface.ROTATION_0);
				camera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (camera != null) {
				camera.release(); // ?????剧?х?告??
				camera = null;
			}
		}
	}
}
