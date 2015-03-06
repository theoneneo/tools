package com.mybitcoin.wallet.util;

import android.content.Context;

public class Global {
	
	public static boolean debug = true;
	public static boolean test = false;
	
	public static Context context = null;
	
	public static String url = null;
	
	public static String SDCard = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
	public static String DirRoot = SDCard+"/MusicDemo";
	
	public static boolean DEFAULT_PROXY = false;
}
