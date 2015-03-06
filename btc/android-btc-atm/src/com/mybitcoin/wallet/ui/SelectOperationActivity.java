package com.mybitcoin.wallet.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.ui.ExchangeRatesActivity.UpdateText;
/**
 * Created by zhuyun on 14-4-7.
 */
public class SelectOperationActivity extends AbstractWalletActivity {

    private TextView mTime;
    private static final int msgKey1 = 1;
    private Thread timerThread;
    private boolean timerFlag;

    public static final String INTENT_EXTRA_RESULT = "result";
    private long beginTime;

    
    @Override
    protected void onCreate(final Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        

        setLayout(R.layout.selectoperation);

        TextView scanQrBtn  = (TextView)findViewById(R.id.btn_scanqr);
        TextView printWalletBtn = (TextView)findViewById(R.id.btn_printwallet);
        TextView cancelBtn = (TextView)findViewById(R.id.btn_cancel);

        scanQrBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
            	Intent intent = new Intent(SelectOperationActivity.this,ScanQRResultActivity.class);
            
            	
            	startActivity(intent);
                
                SelectOperationActivity.this.finish();


            }
        });

        printWalletBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                checkDialog();

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
//                finish();
                
                SelectOperationActivity.this.finish();

            }
        });

        beginTime = System.currentTimeMillis();

        timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();
        log.info("timerThread start.");
    }
    
    private void checkDialog(){
        Intent intent = new Intent(SelectOperationActivity.this,NewReceiverCoinActivity.class);
        intent.putExtra("transType","1");
      
        startActivity(intent);
        SelectOperationActivity.this.finish();
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
                        startActivity(new Intent(SelectOperationActivity.this,ExchangeRatesActivity.class));
                        SelectOperationActivity.this.finish();

                    }
                    break;
                case 555:
                	init();
                	break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onResume()
    {
        super.onResume();
        
        init();
        new Thread(new UpdateText()).start();
    }
    public void init(){
    	super.init();
        UiInfo uiInfo = new UiInfo(this);
        
        TextView tvTitle = (TextView)findViewById(R.id.txtTitle);
        TextView title = (TextView)findViewById(R.id.tip);
        
        TextView btn_scanqr=(TextView)findViewById(R.id.btn_scanqr);
        
        TextView btn_printwallet=(TextView)findViewById(R.id.btn_printwallet);
        
        TextView btn_cancel=(TextView)findViewById(R.id.btn_cancel);
        
        tvTitle.setText(uiInfo.getTextByName(UiInfo.TRADEMODE_BTC_BTN));
        title.setText(uiInfo.getTextByName(UiInfo.cointrade_buycoinmode_title));
        btn_scanqr.setText(uiInfo.getTextByName(UiInfo.cointrade_buycoinmode_qrscan_btn));
        btn_printwallet.setText(uiInfo.getTextByName(UiInfo.cointrade_buycoinmode_paperwallet_btn));
        btn_cancel.setText(uiInfo.getTextByName(UiInfo.COMMON_PRV_BTN));
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	flag = false;
    }
    boolean flag = true;
    class UpdateText implements Runnable{
		@Override
		public void run() {
			while(flag){
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(555);
			}
		}
    }
}
