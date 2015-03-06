/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;

import javax.annotation.Nonnull;

public abstract class WalletActivityTimeoutBase extends WalletActivityBase {
    private static final String LOG_TAG = "WalletActivityTimeoutBase";
    private static final boolean DEBUG_FLAG = true;

    private TextView mCounterView;
    protected String picDir = "/mnt/sdcard/DCIM/";
   

    private WalletActivityTimeoutController.ActivityCountingDownEventListener mDefaultCountingDownListener = new WalletActivityTimeoutController.ActivityCountingDownEventListener() {
        @Override
        public void onCountingDown(final long timeoutSec) {
            if (mCounterView != null) {
                WalletActivityTimeoutBase.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mCounterView.setText(String.valueOf(timeoutSec));
                    }
                });
            }
        }
    };

    private WalletActivityTimeoutController.ActivityTimeoutedEventListener mDefaultTimeoutedListener = new WalletActivityTimeoutController.ActivityTimeoutedEventListener() {
        @Override
        public void onTimeouted() {
            gotoActivity(WelcomePageActivity.class);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        mCounterView = (TextView) findViewById(R.id.timeoutbase_counter);

        WalletActivityTimeoutController.getInstance().setmCountingDownEventListener(mDefaultCountingDownListener);
        WalletActivityTimeoutController.getInstance().setTimeoutedEventListener(mDefaultTimeoutedListener);

        WalletActivityTimeoutController.getInstance().start(this);
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
        LinearLayout subLayout = (LinearLayout) getLayoutInflater().inflate(layoutResID, null);

        layoutBaseContent.addView(subLayout);

        dLog("addLinearLayout add " + layoutResID + " to R.id.timeoutbase_content");

        return layoutBase;
    }

    // 将组合好的layout设置为当前activity的layout
    protected void setLayout(int LayoutResID) {
        LinearLayout layout = addLinearLayout(LayoutResID);

        setContentView(layout);
    }

    public void updateUiInfo() {
        TextView timeoutBaseTitle = (TextView) findViewById(R.id.timeoutbase_title);
        timeoutBaseTitle.setText(getUiInfo().getTextByName(UiInfo.COMMON_TIMEOUT_TITLE));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
    boolean hasImage;
    public byte[] getBitemapByte(Bitmap bitmap){
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
    	try {
    		out.flush();
    		out.close();
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    	return out.toByteArray();
    }
    
    
}
