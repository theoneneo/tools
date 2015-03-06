package com.mybitcoin.wallet.net;
import com.mybitcoin.wallet.util.Tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public abstract class AbstractNet {

	private Handler mHandler = null;
	private PostNetRunnableComment mPostCommentNet = null;
	private PostNetRunnable mPostNet = null;
	private GetNetRunnable mGetNet = null;

	public void requestGet(Bundle parameter, Handler handler, int requestId,
			int requestDataType) {
		this.mHandler = handler;
		
		Bundle par = setRequestParameter(parameter);

		if (Tools.checkNetStatus() == ConstNet.LOCAL_NETWORK_NOT_CONNECT) {
			Message msg = mHandler.obtainMessage();
			msg.what = ConstNet.LOCAL_NETWORK_NOT_CONNECT;
			mHandler.sendMessage(msg);
			return;
		}

		par.putInt(ConstNet.NETWORK_REQUEST_DATA_TYPE, requestDataType);
		par.putInt(ConstNet.NETWORK_REQUEST_ID, requestId);

		mGetNet = new GetNetRunnable(par);
		mGetNet.start();

	}

	private class GetNetRunnable extends Thread {

		private HttpNet mHttp = null;
		private Bundle mParameter = null;
		private boolean mIsStop = false;

		public GetNetRunnable(Bundle parameter) {
			this.mParameter = parameter;
		}

		public void stopNet() {
			if (!mIsStop) {
				if (mHttp != null) {
					mHttp.stop();
				}
			}
		}

		public void run() {
			Bundle response = null;
			mHttp = new HttpNet();
			response = mHttp.requestGet(
					mParameter.getString(ConstNet.NETWORK_REQUEST_URL),
					mParameter.getInt(ConstNet.NETWORK_REQUEST_ID),
					mParameter.getInt(ConstNet.NETWORK_REQUEST_DATA_TYPE));

			Message msg = mHandler.obtainMessage();
			msg.what = ConstNet.NETWORK_RETURN_RESULT;
			msg.setData(response);
			mHandler.sendMessage(msg);

		}
	}

	public void requestPost(Bundle parameter, Handler handler, int requestId,
			int requestDataType) {
		this.mHandler = handler;

		Bundle par = setRequestParameter(parameter);

		if (Tools.checkNetStatus() == ConstNet.LOCAL_NETWORK_NOT_CONNECT) {
			Message msg = mHandler.obtainMessage();
			msg.what = ConstNet.LOCAL_NETWORK_NOT_CONNECT;
			mHandler.sendMessage(msg);
			return;
		}
		par.putInt(ConstNet.NETWORK_REQUEST_ID, requestId);
		par.putInt(ConstNet.NETWORK_REQUEST_DATA_TYPE, requestDataType);

		mPostNet = new PostNetRunnable(par);
		mPostNet.start();
	}

	public void cancel() {
		if (mPostNet != null) {
			mPostNet.stopNet();
		}
		if (mGetNet != null) {
			mGetNet.stopNet();
		}
	}

	private class PostNetRunnable extends Thread {
		private HttpNet httpNet = null;
		private Bundle parameter = null;
		private boolean isStop = false;

		public PostNetRunnable(Bundle parameter) {
			this.parameter = parameter;
		}

		@Override
		public void run() {
			super.run();

			Bundle response = null;
			httpNet = new HttpNet();

			response = httpNet.requestPost(
					parameter.getString(ConstNet.NETWORK_REQUEST_URL),
					parameter.getString(ConstNet.NETWORK_REQUEST_DATA),
					parameter.getInt(ConstNet.NETWORK_REQUEST_ID),
					parameter.getInt(ConstNet.NETWORK_REQUEST_DATA_TYPE));

			Message msg = mHandler.obtainMessage();
			msg.what = ConstNet.NETWORK_RETURN_RESULT;
			msg.setData(response);
			mHandler.sendMessage(msg);
		}

		public void stopNet() {
			if (isStop) {
				if (httpNet != null) {
					httpNet.stop();
				}
			}
		}
	};

	protected abstract Bundle setRequestParameter(Bundle parameter);

	public void requestPostComment(Bundle parameter, Handler handler,
			int requestId, int requestDataType) {
		this.mHandler = handler;

		if (Tools.checkNetStatus() == ConstNet.LOCAL_NETWORK_NOT_CONNECT) {
			Message msg = mHandler.obtainMessage();
			msg.what = ConstNet.LOCAL_NETWORK_NOT_CONNECT;
			mHandler.sendMessage(msg);
			return;
		}

		Bundle par = new Bundle();// "http://api.oopsdata.com:8000/upload");
		par.putString(ConstNet.NETWORK_REQUEST_URL,
				"http://api.oopsdata.com:8000");// Global.url+"/interviewer/materials.json");
		par.putBundle("bundle", parameter);
		par.putInt(ConstNet.NETWORK_REQUEST_ID, requestId);
		par.putInt(ConstNet.NETWORK_REQUEST_DATA_TYPE, requestDataType);

		mPostCommentNet = new PostNetRunnableComment(par);
		mPostCommentNet.start();
	}

	class PostNetRunnableComment extends Thread {
		private HttpNet httpNet = null;
		private Bundle parameter = null;
		private boolean isStop = false;

		public PostNetRunnableComment(Bundle parameter) {
			this.parameter = parameter;
		}

		@Override
		public void run() {
			super.run();

			Bundle response = null;
			httpNet = new HttpNet();

			response = httpNet.requestPostComment(
					parameter.getBundle("bundle"),
					parameter.getString(ConstNet.NETWORK_REQUEST_URL),
					parameter.getInt(ConstNet.NETWORK_REQUEST_ID),
					parameter.getInt(ConstNet.NETWORK_REQUEST_DATA_TYPE));

			Message msg = mHandler.obtainMessage();
			msg.what = ConstNet.NETWORK_RETURN_RESULT;
			msg.setData(response);
			mHandler.sendMessage(msg);
		}

		public void stopNet() {
			if (isStop) {
				if (httpNet != null) {
					httpNet.stop();
				}
			}
		}
	};

}
