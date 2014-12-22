/**
 * 
 */
package com.bitocean.atm.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.bitocean.atm.controller.AppManager;

/**
 * @author bing.liu
 * 
 */
public class Util {

	private Context mContext;

	public Util(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void showFeatureToast(String info) {
		TextView text = new TextView(mContext);
        text.setText(info);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        text.setBackgroundColor(Color.BLACK);
        Toast toast = new Toast(mContext);
        toast.setView(text);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();	
	}
	
	public void showLog(String log){
		ApplicationInfo info = AppManager.getContext().getApplicationInfo();
	    if(0!=((info.flags) & ApplicationInfo.FLAG_DEBUGGABLE)){
	    	Log.d("BITOCEAN ATM", log);
	    }
	}
	
	public ProgressDialog showProgressBar(String info){
		ProgressDialog dialog=new ProgressDialog(mContext);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage(info);
		dialog.setIndeterminate(false);
		dialog.setCancelable(false);
		dialog.show();
		return dialog;
	}
	

    public static int dip2px(Context context, float dipValue) {
    	final float scale = context.getResources().getDisplayMetrics().density;
    	return (int) (dipValue * scale + 0.5f);
    }
}
