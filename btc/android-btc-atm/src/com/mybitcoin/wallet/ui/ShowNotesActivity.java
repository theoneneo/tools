package com.mybitcoin.wallet.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.text.format.DateFormat;

import com.mybitcoin.wallet.R;
/**
 * Created by zhuyun on 14-4-7.
 */
public class ShowNotesActivity extends AbstractWalletActivity {

    private TextView mTime;
    private static final int msgKey1 = 1;
    private Thread timerThread;
    private boolean timerFlag;
    private long beginTime;
    @Override
    protected void onCreate(final Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.notes);

//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);

        TextView preBtn  = (TextView)findViewById(R.id.notes_back_btn);
        TextView nextBtn = (TextView)findViewById(R.id.notes_next_btn);

        preBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
               /* Intent intent = new Intent(ShowNotesActivity.this,ExchangeRatesActivity.class);
                startActivity(intent);
                ShowNotesActivity.this.finish();*/
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(ShowNotesActivity.this,SelectOperationActivity.class));
            }
        });

//        mTime = (TextView)findViewById(R.id.timer);
        beginTime = System.currentTimeMillis();
//        CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",beginTime);
//        mTime.setText(sysTimerStr);
        timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();
        log.info("timerThread start.");
    }

    @Override
    protected void onStop(){
        super.onStop();
        timerFlag = false;

    }

    public class TimeThread extends Thread{
        @Override
        public void run(){
            while(timerFlag){
                try{
                    Thread.sleep(1000);
                    Message message = new Message();
                    message.what = msgKey1;
                    mHandler.sendMessage(message);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case msgKey1:
                    long sysTime = System.currentTimeMillis();
//                    CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",sysTime);
//                    mTime.setText(sysTimerStr);
//                    log.info("time2："+sysTimerStr);
                    if((sysTime - beginTime) > 60000){
                        startActivity(new Intent(ShowNotesActivity.this,ExchangeRatesActivity.class));
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
