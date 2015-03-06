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
import android.content.ComponentName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.util.TransactionLog;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhuyun on 14-4-7.
 */
public class TransFailedActivity extends SerialPortActivity {

//    private TextView mTime;
    private static final int msgKey1 = 1;
    private Thread timerThread;
    private boolean timerFlag;
    private static Logger log = LoggerFactory.getLogger(TransFailedActivity.class);
    private String strQRAddress;
    private String transType;
    private int coinAmount=0;
    private String bitcoinAmount="";
    String tele = "";
    private long beginTime;
    private WalletApplication application;


    @Override
    protected void onCreate(final Bundle saveInstanceState){
        serialPortType = 0;
        super.onCreate(saveInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.transfail);

//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);

        application = mApplication;

//        mTime = (TextView)findViewById(R.id.timer);
        beginTime = System.currentTimeMillis();
//        CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",beginTime);
//        mTime.setText(sysTimerStr);
        timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();
//        log.info("timerThread start.");
        tele  = getResources().getString(R.string.server_telephone);

        //获取交易类型和二维码地址
        Intent intent = getIntent();
        transType = intent.getStringExtra("transType");
//        log.info("transType is :"+transType);
        strQRAddress = intent.getStringExtra("qrAddress");
//        log.info("strQRAddress is :"+strQRAddress);

        coinAmount = intent.getIntExtra("coinAmount",0);
//        log.info("coinAmount is :"+coinAmount);
        bitcoinAmount = intent.getStringExtra("bitcoinAmount");
//        log.info("bitcoinAmount is :"+bitcoinAmount);

        //输出到日志文件
        TransactionLog.writeLogtoFile(transType, strQRAddress, String.valueOf(coinAmount), bitcoinAmount, "失败");

        //发送短信
        application.sendMessage("失败");


//        TextView btnPrintInvoice = (TextView)findViewById(R.id.btn_printinvoice);
        TextView  btnConfirm = (TextView)findViewById(R.id.btn_confirm);

       /* btnPrintInvoice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                    printTransInvoice(strQRAddress,bitcoinAmount,String.valueOf(coinAmount));

            }
        });*/
        btnConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(TransFailedActivity.this, ExchangeRatesActivity.class));
                finish();

            }
        });

        try {
            if(mScanOutputStream != null){
                mScanOutputStream.flush();
                mScanOutputStream.close();
                mScanOutputStream = null;

            }
            mPrintOutputStream = application.getPrintSerialPort().getOutputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

        printTransInvoice(strQRAddress,bitcoinAmount,String.valueOf(coinAmount));
    }


        /*
     * print invoice of  transaction
     *
     */
    private void printTransInvoice(String address,String bitcoinAmount,String coinAmount){
            try{
                if(mPrintOutputStream == null){
                    try {

                        mPrintOutputStream = application.getPrintSerialPort().getOutputStream();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                log.info("mPrintOutputStream is :"+mPrintOutputStream);
                //打印汉字
                mPrintOutputStream.write(hexStringToBytes("1C26"));
                mPrintOutputStream.write(hexStringToBytes("1C2100"));
                //居中
                mPrintOutputStream.write(hexStringToBytes("1B6101"));
                mPrintOutputStream.write("欢迎使用比特币交易机".getBytes(Charset.forName("GB18030")));

                //换行
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));

                //居左
                mPrintOutputStream.write(hexStringToBytes("1B6100"));

                mPrintOutputStream.write("转入帐号:".getBytes(Charset.forName("GB18030")));
                mPrintOutputStream.write(address.getBytes());
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));

                mPrintOutputStream.write("转入比特币数量:".getBytes(Charset.forName("GB18030")));
                mPrintOutputStream.write(bitcoinAmount.getBytes());
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));


                mPrintOutputStream.write("交易金额: ".getBytes(Charset.forName("GB18030")));

                //取消汉字
//                mPrintOutputStream.write(hexStringToBytes("1C2E"));
                mPrintOutputStream.write(coinAmount.getBytes());
                mPrintOutputStream.write("元".getBytes(Charset.forName("GB18030")));

                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));

                //打印汉字
                mPrintOutputStream.write(hexStringToBytes("1C26"));
                mPrintOutputStream.write(hexStringToBytes("1C2100"));

                long sysTime = System.currentTimeMillis();
                String  sysTimerStr = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(sysTime);
                mPrintOutputStream.write("交易日期:".getBytes(Charset.forName("GB18030")));
                mPrintOutputStream.write(sysTimerStr.getBytes(Charset.forName("GB18030")));
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));

                mPrintOutputStream.write("交易结果:失败".getBytes(Charset.forName("GB18030")));
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));

                mPrintOutputStream.write("客服电话:".getBytes(Charset.forName("GB18030")));
                mPrintOutputStream.write(tele.getBytes());
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));
                mPrintOutputStream.write(hexStringToBytes("0A"));


                //取消汉字
                mPrintOutputStream.write(hexStringToBytes("1C2E"));

                //切纸
                mPrintOutputStream.write(hexStringToBytes("1D5601"));
                mPrintOutputStream.flush();
//            btnPrintInvoice.setVisibility(View.GONE);

            }catch(Exception e){
                e.printStackTrace();
            }
//            log.info("print invoice of  transaction");
    }

    @Override
    protected void onStop(){
        try {
            if(mPrintOutputStream != null){
                mPrintOutputStream.flush();
                mPrintOutputStream.close();
                mPrintOutputStream = null;
            }
            application.closePrintSerialPort();

        } catch (IOException e) {
            e.printStackTrace();
        }

        timerFlag = false;
        super.onStop();


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
                        startActivity(new Intent(TransFailedActivity.this,ExchangeRatesActivity.class));
                        TransFailedActivity.this.finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // 打开数据接收线程，接收串口返回的数据
    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {

    }



}
