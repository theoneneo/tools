package com.bitocean.atm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
/**
 * @author bing.liu
 * 
 */
public class CameraActivity extends BaseTimerActivity {
	private boolean isCameraPicture = false;
	private Button submit, photo, cancel;
	private String fileType = "user_icon";
	private Camera camera;
	private Camera.Parameters parameters = null;
	private static String path;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_camera);

		if (getIntent() != null) {
			fileType = getIntent().getStringExtra("fileType");
		}

		submit = (Button) findViewById(R.id.submit);
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent data = new Intent();
				data.putExtra("url", path);
				CameraActivity.this.setResult(RESULT_OK, data);
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
					camera.startPreview();
					isCameraPicture = false;
				} else {
					CameraActivity.this.setResult(
							CameraActivity.this.RESULT_CANCELED, null);
					finish();
				}
			}
		});

		SurfaceView surfaceView = (SurfaceView) this
				.findViewById(R.id.surfaceView);
		surfaceView.getHolder()
				.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceView.getHolder().setKeepScreenOn(true);
		surfaceView.getHolder().addCallback(new SurfaceCallback());
	}

	private final class MyPictureCallback implements PictureCallback {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				Bundle bundle = new Bundle();
				bundle.putByteArray("bytes", data);
				saveToSDCard(data);
				photo.setVisibility(View.GONE);
				submit.setVisibility(View.VISIBLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void saveToSDCard(byte[] data) throws IOException {
		path = "/sdcard/" + fileType
				+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
				+ ".jpg";
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
				camera.release();
				camera = null;
			}
		}
	}
}