package com.mybitcoin.wallet.util;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mybitcoin.wallet.net.ConstNet;
import com.mybitcoin.wallet.net.SystemLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class Tools {

	@SuppressLint("SimpleDateFormat")
	public static String getTime(){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return sdf.format(new Date());
	}
	
	public static boolean isWifiConnected(Context context){
		
        final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        final android.net.NetworkInfo wifi =connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      
        if(wifi.isAvailable()&&wifi.isConnected())
            return true;
        else
            return false;
	}

	public static synchronized String getRandomCode(){
		Random random = new Random();
		int one = random.nextInt(10);
		int two = random.nextInt(10);
		int three = random.nextInt(10);
		int four = random.nextInt(10);
		StringBuilder sb = new StringBuilder();
		sb.append(one).append(two).append(three).append(four);
		return sb.toString();
	}
	public static boolean ExistSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	public static int checkNetStatus() {
		ConnectivityManager cm = (ConnectivityManager) Global.context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		int netStatus = ConstNet.LOCAL_NETWORK_NOT_CONNECT;
		try {
			if (cm != null) {
				int type = cm.getActiveNetworkInfo().getType();
				if (ConnectivityManager.TYPE_WIFI == type) {
					netStatus = ConstNet.LOCAL_NETWORK_WIFI;
				} else if (ConnectivityManager.TYPE_MOBILE == type) {
					netStatus = ConstNet.LOCAL_NETWORK_MOBILE;
				}
			}
			return netStatus;
		} catch (Exception e) {
//			SystemLog.debug("checkNetStatus", e.getMessage());
			return netStatus;
		} finally {

		}
	}

	public static Bitmap getBitmap(Uri uriFile, int scale) {

		Bitmap bmp = null;
		try {

			ParcelFileDescriptor pfd = Global.context.getContentResolver()
					.openFileDescriptor(uriFile, "r");
			FileDescriptor fd = pfd.getFileDescriptor();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			options.inSampleSize = computerSampleSize(options, scale);
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			bmp = BitmapFactory.decodeFileDescriptor(fd, null, options);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return bmp;
	}

	private static int computerSampleSize(BitmapFactory.Options options,
			int sampleSize) {

		int h = options.outHeight;
		int candidate = h / sampleSize;

		if (candidate == 0)
			return 1;
		else
			return candidate;
	}

	public static boolean checkIDCard(String string) {
		String idCard = "[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{4}";
		Pattern pattern = Pattern.compile(idCard);
		Matcher matcher = pattern.matcher(string);

		return matcher.matches();
	}

	public static boolean checkPostcode(String string) {
		String postcode = "[1-9]\\d{5}";
		Pattern pattern = Pattern.compile(postcode);
		Matcher matcher = pattern.matcher(string);

		return matcher.matches();
	}

	// ^[0-9]+\.{0,1}[0-9]{0,2}$
	public static boolean checkNumber(String string) {
		Pattern pattern = Pattern.compile("^[0-9]+\\.{0,1}[0-9]{0,2}$");
		Matcher matcher = pattern.matcher(string);

		return matcher.matches();
	}

	public static boolean checkEmail(String string) {
		String email = "[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}";
		Pattern pattern = Pattern.compile(email);
		Matcher m = pattern.matcher(string);
		return m.matches();

	}

	public static boolean checkMobile(String mobileNumber) {
		String mobile = "^\\d{3}-?\\d{8}|\\d{4}-?\\d{8}$";
		// String mobile =
		// "0?(13\\d|15[012356789]|18[0236789]|14[57])-?\\d{3}-?\\d{1}-?\\d{4}";
		Pattern p = Pattern.compile(mobile);
		Matcher m = p.matcher(mobileNumber);
		return m.matches();
	}

	public static boolean checkPhone(String phoneNumber) {

		String phone = "((\\d{11})|(\\d{3}-\\d{8})|(\\d{4}-\\d{7})|(\\d{3}-\\d{4}-\\d{4}))";
		Pattern p = Pattern.compile(phone);
		Matcher m = p.matcher(phoneNumber);
		return m.matches();

	}

	public static boolean checkURL(String stringURL) {
		String url = "((https|http|ftp|rtsp|mms)?://)"
				+ "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp??ser@
				+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形�???RL- 199.194.52.184
				+ "|" // ???IP??OMAIN�?????
				+ "([0-9a-z_!~*'()-]+\\.)*" // ???- www.
				+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // �?��???
				+ "[a-z]{2,6})" // first level domain- .com or .museum
				+ "(:[0-9]{1,4})?" // �??- :80
				+ "((/?)|" // a slash isn't required if there is no file name
				+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)";
		Pattern p = Pattern.compile(url);
		Matcher m = p.matcher(stringURL);
		return m.matches();

	}

	public static String intTobinary(int i) {

		String s = "";

		while (i > 0) {
			if (i % 2 != 0)
				s = "1" + s;
			else
				s = "0" + s;
			i /= 2;
		}
		return s;
	}
}
