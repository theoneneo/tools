package com.mybitcoin.wallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.Qr;

/**
 * Created by zhuyun on 14-4-7.
 */
public class ScanQRResultActivity extends AbstractWalletActivity {

    public static final String INTENT_EXTRA_RESULT = "result";

//    private TextView mTime;
    private static final int msgKey1 = 1;
    private Thread timerThread;
    private boolean timerFlag;

    private String qrAddress;
    private Bitmap qrCodeBitmap;
    private static final int REQUEST_CODE_SCAN = 0;
    private TextView scanAddress, scanAmount;
    private ImageView bitcoinAddressQrView;
    private TextView preBtn,confirmBtn;
    private long beginTime;
    
    
    @Override
    protected void onCreate(final Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        startActivityForResult(new Intent(ScanQRResultActivity.this,ScanActivity.class), REQUEST_CODE_SCAN);

       
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setLayout(R.layout.scanqrresult);

//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);

        scanAddress = (TextView)findViewById(R.id.scan_address);
        scanAmount = (TextView)findViewById(R.id.scan_amount);
        scanAmount.setVisibility(View.GONE);
        bitcoinAddressQrView = (ImageView)findViewById(R.id.bitcoin_address_qr);


        preBtn  = (TextView)findViewById(R.id.btn_prev);
        confirmBtn = (TextView)findViewById(R.id.btn_confirm);

        preBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
//                startActivityForResult(new Intent(ScanQRResultActivity.this,SelectOperationActivity.class), REQUEST_CODE_SCAN);
                   finish();

            }
        });
        confirmBtn.setEnabled(false);
        confirmBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){                
                checkDialog();
            }
        });
//        mTime = (TextView)findViewById(R.id.timer);
        beginTime = System.currentTimeMillis();
//        CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",beginTime);
//        mTime.setText(sysTimerStr);
        timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();
//        log.info("timerThread start.");

       /* Intent intent  = getIntent();
        String input = intent.getStringExtra("address");
        log.info("input is :"+input);
        manuSetInput(input);*/
    }
    
    private void checkDialog(){
        if(qrAddress != null && !("".equals(qrAddress))){
            Intent intent = new Intent(ScanQRResultActivity.this,NewReceiverCoinActivity.class);
            intent.putExtra("transType","0");
          
            intent.putExtra("qrAddress",getQRAddress());
            startActivity(intent);
            finish();
        }
    }

    private void manuSetInput(String input){
        setQRAddress(input);
        qrCodeBitmap =makeQRImage(input);
        scanAddress.setText(input);
        bitcoinAddressQrView.setImageBitmap(qrCodeBitmap);
    }
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {

        log.info("back result");
        if (requestCode == REQUEST_CODE_SCAN && resultCode == Activity.RESULT_OK)
        {
             String input = intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT).replace("bitcoin:","");
             input = input.replace("//","");
             input = input.replace("\t","");
             input = input.replace("\n","");
//            final String input = intent.getStringExtra(SelectOperationActivity.INTENT_EXTRA_RESULT);
            log.info("scanAddress is :"+input);
            int index = input.indexOf("?amount=");
            if(index>0){
            	
            	String acount = input.substring(index+8);
            	
            	scanAmount.setVisibility(View.GONE);
            	scanAmount.setText(acount);
            	
            	input=input.substring(0, input.lastIndexOf("?"));
            }
            
            setQRAddress(input);
            qrCodeBitmap =makeQRImage(input);
            scanAddress.setText(input);
            bitcoinAddressQrView.setImageBitmap(qrCodeBitmap);
            confirmBtn.setEnabled(true);
        }
    }

    private Bitmap makeQRImage(String addressStr){
//        final String addressStr = BitcoinURI.convertToBitcoinURI(selectedAddress, null, null, null);
        log.info("addressStr is :"+addressStr);
        final int size = (int) (256 * getResources().getDisplayMetrics().density);
        return Qr.bitmap(addressStr, size);
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
                    if((sysTime - beginTime) > 120000){
                        startActivity(new Intent(ScanQRResultActivity.this,ExchangeRatesActivity.class));
                        ScanQRResultActivity.this.finish();
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

    public void setQRAddress(String address){
        this.qrAddress = address;
    }
    public String getQRAddress(){
        return this.qrAddress;
    }
    
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
        TextView tip = (TextView)findViewById(R.id.tip);
        
        TextView tvAddress=(TextView)findViewById(R.id.tvAddress);
        
        tvTitle.setText(uiInfo.getTextByName(UiInfo.TRADEMODE_BTC_BTN));
        tip.setText(uiInfo.getTextByName(UiInfo.cointrade_displayqrcode_qrcode));
        tvAddress.setText(uiInfo.getTextByName(UiInfo.cointrade_displayqrcode_addr));
        
        TextView btn_prev = (TextView)findViewById(R.id.btn_prev);
        TextView btn_confirm = (TextView)findViewById(R.id.btn_confirm);
        
        btn_prev.setText(uiInfo.getTextByName(UiInfo.COMMON_PRV_BTN));
        btn_confirm.setText(uiInfo.getTextByName(UiInfo.COMMON_CFM_BTN));

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
