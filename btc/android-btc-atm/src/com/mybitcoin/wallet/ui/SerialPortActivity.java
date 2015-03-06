/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.mybitcoin.wallet.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import javax.annotation.Nonnull;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mybitcoin.wallet.WalletApplication;

import android_serialport_api.SerialPort;

import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SerialPortActivity extends Activity {

	protected WalletApplication mApplication;
	protected SerialPort mPrintSerialPort,mScanSerialPort;
	protected OutputStream mPrintOutputStream,mScanOutputStream;
	protected  InputStream mPrintInputStream,mScanInputStream;
//	private ReadPrintPortThread mReadPrintThread;
//    private ReadScanPortThread mReadScanThread;
    protected  int serialPortType = 0;

    private static Logger log = LoggerFactory.getLogger(SerialPortActivity.class);



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (WalletApplication) getApplication();
	}

    public  OutputStream getPrintOutputStream(){
        mPrintOutputStream = mPrintSerialPort.getOutputStream();
        return mPrintOutputStream;
    }
    public  OutputStream getScanOutputStream(){
        mScanOutputStream = mScanSerialPort.getOutputStream();

        return mScanOutputStream;
    }

    public InputStream getScanInputStream(){
        mScanInputStream = mScanSerialPort.getInputStream();
        return mScanInputStream;
    }
	protected abstract void onDataReceived(final byte[] buffer, final int size);

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    /**
     * Convert hex string to byte[]
     *
     * @param hexString
     *            the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;

    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    
    /////////////////////////////////////////
    
    private static final String LOG_TAG = "WalletActivityBase";
    private static final boolean DEBUG_FLAG = true;

    private TextView mCounterView;

    private WalletActivityTimeoutController.ActivityCountingDownEventListener mDefaultCountingDownListener = new WalletActivityTimeoutController.ActivityCountingDownEventListener() {
        @Override
        public void onCountingDown(final long timeoutSec) {
            if (mCounterView != null) {
            	SerialPortActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mCounterView.setText(String.valueOf(timeoutSec));
                    }
                });
            }
        }
    };

    public WalletActivityTimeoutController.ActivityTimeoutedEventListener mDefaultTimeoutedListener = new WalletActivityTimeoutController.ActivityTimeoutedEventListener() {
        @Override
        public void onTimeouted() {
        	if(checkActivity()){
        	((NewReceiverCoinActivity)SerialPortActivity.this).TransProgress();
        	}else{
            gotoActivity(SelectOperationActivity.class);
            finish();
        	}
        }
    };
    
    public boolean checkActivity(){
    	if(this instanceof NewReceiverCoinActivity){
    		return true;
    	}else {
    		return false;
    	}
    }
    
    protected void gotoActivity(@Nonnull Class<?> activityCls) {
        startActivity(new Intent(this, activityCls));
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        mCounterView = (TextView) findViewById(R.id.timeoutbase_counter);

        WalletActivityTimeoutController.getInstance().setmCountingDownEventListener(mDefaultCountingDownListener);
        WalletActivityTimeoutController.getInstance().setTimeoutedEventListener(mDefaultTimeoutedListener);

        WalletActivityTimeoutController.getInstance().start(this);
        WalletActivityTimeoutController.getInstance().setTimeout(300);
        dLog("ActivityTimeoutController.getInstance.start is called in onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        WalletActivityTimeoutController.getInstance().end();
        dLog("ActivityTimeoutController.getInstance.end is called in onPause");
    }

    // 将超类的layout加入到R.layout.timeout_base中的timeout_base_content中
    // 使得timeout_base_counter部分由该类托管，并由ActivityTimeoutController进行超时管理
    private LinearLayout addLinearLayout(int layoutResID) {
        LinearLayout layoutBase = (LinearLayout) getLayoutInflater().inflate(R.layout.timeout_base, null);
        LinearLayout layoutBaseContent = (LinearLayout) layoutBase.findViewById(R.id.timeoutbase_content);

        layoutBaseContent.addView(getLayoutInflater().inflate(layoutResID, null));

        dLog("addLinearLayout add " + layoutResID + " to R.id.timeoutbase_content");

        return layoutBase;
    }

    // 将组合好的layout设置为当前activity的layout
    protected void setLayout(int LayoutResID) {
        LinearLayout layout = addLinearLayout(LayoutResID);

        setContentView(layout);
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG == true) {
//            Log.d(LOG_TAG, logStr);
        }
    }
    
    public void init(){
    	UiInfo uiInfo = new UiInfo(this);
    	 TextView timeoutBaseTitle = (TextView) findViewById(R.id.timeoutbase_title);
         timeoutBaseTitle.setText(uiInfo.getTextByName(UiInfo.COMMON_TIMEOUT_TITLE));
    }
}
