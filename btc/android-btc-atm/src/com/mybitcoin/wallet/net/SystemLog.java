package com.mybitcoin.wallet.net;

import com.mybitcoin.wallet.util.Global;

import android.util.Log;

public class SystemLog {

	private SystemLog() {
	}

	public static void debug(String tag, String msg) {
		if (Global.debug) {
			if (msg == null) {
				msg = "null";
			}
			Log.i("Quill", tag + " ==> " + msg);
		}
	}

	public static void debug(String tag, int msg) {
		debug(tag, String.valueOf(msg));
	}

	public static void debug(String tag, long msg) {
		debug(tag, String.valueOf(msg));
	}

	public static void debug(String tag, boolean msg) {
		debug(tag, String.valueOf(msg));
	}

	public static void error(String tag, String msg) {
		if (Global.debug) {
			if (msg == null) {
				msg = "null";
			}
			Log.e("Quill", tag + " ==> " + msg);
		}
	}

	public static void error(String tag, int msg) {
		error(tag, String.valueOf(msg));
	}

	public static void error(String tag, long msg) {
		error(tag, String.valueOf(msg));
	}

	public static void error(String tag, boolean msg) {
		error(tag, String.valueOf(msg));
	}
}
