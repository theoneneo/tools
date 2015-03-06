package com.mybitcoin.wallet.ui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.mybitcoin.wallet.DeviceInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * 相机 层
 * 
 */
public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	Camera camera;
	Camera.Parameters p;
	SurfaceHolder mHolder;
	boolean bCameraPause;
	public int _width, _height;
	Context mContext;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context) {
		super(context);
		mContext = context;
		mHolder = getHolder();
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder.addCallback(this);
	}

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mHolder = getHolder();
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder.addCallback(this);
	}

	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mHolder = getHolder();
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHolder.addCallback(this);
	}

	private ArFragmentActivity arActivity;

	public void setActivity(ArFragmentActivity activity) {
		arActivity = activity;
	}

	private void upBtn() {
		arActivity.runOnUiThread(new Runnable() {
			public void run() {
				arActivity.isCameraPicture = false;
				arActivity.photo.setVisibility(View.GONE);
				arActivity.submit.setVisibility(View.VISIBLE);

			}

		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (is_preview) {
			camera.cancelAutoFocus();
			camera.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					// TODO Auto-generated method stub

				}
			});
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if (camera == null)
			return;
		try {
			camera.setPreviewDisplay(mHolder);
			p.setPreviewSize(_width, _height);

			p.setPictureSize(_width, _height);
			// p.setPictureSize(2560, 1440);

			camera.setParameters(p);
			camera.startPreview();
			is_preview = true;
			camera.autoFocus(null);
		} catch (Exception e) {
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	public void onDestoryed() {
		surfaceDestroyed(mHolder);
	}

	public void onResume() {
		if (camera == null) {
			try {
				camera = Camera.open();
				p = camera.getParameters();
				List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();

//				Camera.Size previewSize = getOptimalPreviewSize(previewSizes,
//						DeviceInfo.getInstance(mContext).getScreenWidth(),
//						DeviceInfo.getInstance(mContext).getScreenHeight());
				_width = 640;
				_height = 480;

//				surfaceCreated(mHolder);
			} catch (Exception e) {

			}
		} else {
			camera.startPreview();
			is_preview = true;
			camera.autoFocus(null);
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the
		// requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	private boolean is_preview = true;// 是否已经拍照了，拍照之后，取消触屏对焦

	public void takePhoto() {
		if (camera != null) {
			camera.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					// TODO Auto-generated method stub
					camera.takePicture(null, null, mPicture);
				}
			});

		}
	}

	public String filepath = "";// 照片保存路径

	private final PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				// 自定义文件保存路径 以拍摄时间区分命名
				filepath = "/sdcard/"+ArFragmentActivity.fileType
						+ new SimpleDateFormat("yyyyMMddHHmmss")
								.format(new Date()) + ".jpg";
				File file = new File(filepath);
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(file));
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);// 将图片压缩的流里面
				bos.flush();// 刷新此缓冲区的输出流
				bos.close();// 关闭此输出流并释放与此流有关的所有系统资源
				
				camera.stopPreview();//关闭预览 处理数据
				// camera.startPreview();//数据处理完后继续开始预览
				bitmap.recycle();//回收bitmap空间
				upBtn();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	public static Bitmap _preview;

}
