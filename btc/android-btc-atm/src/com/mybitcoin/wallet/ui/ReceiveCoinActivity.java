package com.mybitcoin.wallet.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.DateFormat;

import com.mybitcoin.wallet.Configuration;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.ExchangeRatesProvider;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.ExchangeRatesProvider.ExchangeRate;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.uri.BitcoinURI;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;

import com.mybitcoin.wallet.Configuration;

import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.Qr;
import com.mybitcoin.wallet.util.WalletUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuyun on 14-4-7.
 */
public class ReceiveCoinActivity extends SerialPortActivity {
    private WalletApplication application;
    private Wallet wallet;

    private Configuration config;
    private TextView viewCoinAmount;
    private static final int msgKey1 = 1;
    private Thread timerThread;
    private boolean timerFlag;
    private BigInteger rateBase = GenericUtils.ONE_BTC;
    private Address newAddress = null;
    TextView cancelBtn;
    TextView confirmBtn;

    private static Logger log = LoggerFactory.getLogger(ReceiveCoinActivity.class);

    private String strQRAddress;
    private String transType;
    private String strPrivateKey;

    SendThread mSendThread;
    ReadThread mReadThread;
    private boolean cashFlag;
    private boolean sendThreadFlag,readThreadFlag;

    private int coinAmount = 0;
    private float bitcoinAmount = 0;//amount of the received coins

    private CurrencyCalculatorLink amountCalculatorLink;
    CurrencyAmountView btcAmountView,localAmountView;
    private long beginTime;
    BigInteger avaiableBitcoinAmount = null;


    @Override
    protected void onCreate(final Bundle saveInstanceState){
        serialPortType = 1;
        super.onCreate(saveInstanceState);
        application = mApplication;
        wallet = application.getWallet();
        config = application.getConfiguration();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.receivecoin);

//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);

        application.startBlockchainService(false);

        avaiableBitcoinAmount = wallet.getBalance(BalanceType.AVAILABLE);


        String currencyCode = config.getExchangeCurrencyCode();
        ExchangeRate exchangeRate = config.getCachedExchangeRate();

        TextView currencyView = (TextView)findViewById(R.id.current_coin_name);
        currencyView.setText(currencyCode);

        CurrencyTextView rateView = (CurrencyTextView)findViewById(R.id.exchange_rate);
        rateView.setPrecision(Constants.LOCAL_PRECISION, 0);
        rateView.setAmount(WalletUtils.localValue(rateBase, exchangeRate.rate));


        btcAmountView = (CurrencyAmountView) findViewById(R.id.coins_amount_btc);
        btcAmountView.setCurrencySymbol(config.getBtcPrefix());
        btcAmountView.setInputPrecision(config.getBtcMaxPrecision());
        btcAmountView.setHintPrecision(config.getBtcPrecision());
        btcAmountView.setShift(config.getBtcShift());


        localAmountView = (CurrencyAmountView) findViewById(R.id.coins_amount_local);
        localAmountView.setInputPrecision(Constants.LOCAL_PRECISION);
        localAmountView.setHintPrecision(Constants.LOCAL_PRECISION);

        amountCalculatorLink = new CurrencyCalculatorLink(btcAmountView, localAmountView);
        amountCalculatorLink.setExchangeDirection(false);
        amountCalculatorLink.setExchangeRate(config.getCachedExchangeRate());

        viewCoinAmount = (TextView)amountCalculatorLink.activeTextView();


        TextView addressView = (TextView)findViewById(R.id.wallet_address);

        Intent intent = getIntent();
        //判断交易类型
         transType = intent.getStringExtra("transType");
        log.info("transType is :"+transType);
        if(("0".equals(transType))){
            //如果是扫描二维码，则使用扫描的二维码地址
            strQRAddress = intent.getStringExtra("qrAddress");
        }else{
        //如果是打印纸钱包，则生成新的地址
            newAddress = addNewAddress();
            strQRAddress = newAddress.toString();
        }
        log.info("qrAddress is :"+strQRAddress);

        addressView.setText(strQRAddress);

        cancelBtn  = (TextView)findViewById(R.id.btn_cancel);
        confirmBtn = (TextView)findViewById(R.id.btn_confirm);

        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                /*viewCoinAmount.setText("1");
                coinAmount = 1;
                log.info("bitcoinAmount is :"+GenericUtils.formatValue(amountCalculatorLink.getAmount(), config.getBtcMaxPrecision(), config.getBtcShift()));
//                log.info("bitcoinAmount1 is :"+ new DecimalFormat("##0.00").format(amountCalculatorLink.getAmount().floatValue()/100000));
//                confirmBtn.setEnabled(true);
                calculationLeftBitCoinAmount();*/

                cashFlag = false;
                startActivity(new Intent(ReceiveCoinActivity.this,SelectOperationActivity.class));
                finish();
            }
        });


        confirmBtn.setEnabled(false);
        confirmBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if((amountCalculatorLink.getAmount()).floatValue() <= 0)
                    return;
                cashFlag = false;
                /*try{//关闭纸币识别器的识别
                // stop
                    if(mScanOutputStream != null)
                     mScanOutputStream.write(hexStringToBytes("7F8001093582")); // disable

                }catch (IOException e){
                    e.printStackTrace();
                }*/

                Intent intent = new Intent(ReceiveCoinActivity.this, TransProgressActivity.class);
                intent.putExtra("transType",transType);
                intent.putExtra("qrAddress",strQRAddress);
                intent.putExtra("privateKey",strPrivateKey);
                intent.putExtra("coinAmount",coinAmount);
//                intent.putExtra("bitcoinAmount","1000");
                intent.putExtra("bitcoinAmount",GenericUtils.formatValue(amountCalculatorLink.getAmount(), config.getBtcMaxPrecision(), config.getBtcShift()));

                startActivity(intent);
                finish();
            }
        });

//        mTime = (TextView)findViewById(R.id.timer);
        beginTime = System.currentTimeMillis();
//        CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",beginTime);

//        mTime.setText(sysTimerStr);
        /*timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();*/
//        log.info("timerThread start.");



        cashFlag=true;
        sendThreadFlag = true;
        readThreadFlag = true;


    }

    /**
     * 比较剩余的比特币数量是否小于等于1毫比特币
     */
    private void calculationLeftBitCoinAmount(){
        //Toast.makeText(this,"剩余数量"+String.valueOf(avaiableBitcoinAmount.subtract(amountCalculatorLink.getAmount()).floatValue()),Toast.LENGTH_SHORT).show();
        BigInteger vAvbBitAmount  = avaiableBitcoinAmount;
        BigInteger vCalLinkAmount = amountCalculatorLink.getAmount();
//        Toast.makeText(this,"amountCalculatorLink.getAmount() is :"+amountCalculatorLink.getAmount(),Toast.LENGTH_SHORT).show();
//        Toast.makeText(this,"avaiableBitcoinAmount is :"+avaiableBitcoinAmount,Toast.LENGTH_SHORT).show();
//        Toast.makeText(this,"leftBitcoinAmount is :"+vAvbBitAmount.subtract(vCalLinkAmount),Toast.LENGTH_SHORT).show();
        BigInteger leftBitcoinAmount = vAvbBitAmount.subtract(vCalLinkAmount);
        if(leftBitcoinAmount.compareTo(WalletApplication.MinAvaiableBitcoinAmount) <= 0 ){

            Toast.makeText(this,"此ATM机内的有效比特币数量不足，请不要再放入钱币！",Toast.LENGTH_SHORT).show();
            cashFlag = false;
            /*try{//关闭纸币识别器的识别
                // stop
                if(mScanOutputStream != null)
                    mScanOutputStream.write(hexStringToBytes("7F8001093582")); // disable

            }catch (IOException e){
                e.printStackTrace();
            }*/
        }
    }
    private void updateView(){

    }
    @Override
    protected void onStop(){
        coinAmount = 0;
        cashFlag=false;
        sendThreadFlag =false;
        readThreadFlag =false;
        timerFlag = false;
        super.onStop();

    }
//    @Override
    protected  void onResume(){
        cashFlag=true;
        sendThreadFlag =true;
        readThreadFlag =true;
        timerFlag = true;
        try {
            if(mPrintOutputStream != null){
                mPrintOutputStream.flush();
                mPrintOutputStream.close();
                mPrintOutputStream = null;
            }
            mScanOutputStream = application.getScanSerialPort().getOutputStream();
            mScanInputStream = application.getScanSerialPort().getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSendThread = new SendThread();
        mReadThread = new ReadThread();

        mSendThread.start();
        mReadThread.start();
        super.onResume();
    }

    protected  void onPause(){
        coinAmount = 0;
        cashFlag=false;
        timerFlag = false;
        sendThreadFlag =false;
        readThreadFlag =false;


        super.onPause();
    }
    protected void onDestroy() {
        coinAmount = 0;
        cashFlag=false;
        sendThreadFlag =false;
        readThreadFlag =false;

        timerFlag = false;
        mSendThread = null;
        mReadThread = null;

        /*try {
            if(mScanOutputStream != null){
                mScanOutputStream.flush();
                mScanOutputStream.close();
                mScanOutputStream = null;
            }
            if(mScanInputStream != null){
                mScanInputStream.close();
                mScanInputStream = null;

            }
            application.closeScanSerialPort();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        super.onDestroy();
    }

    //add new address
    public Address  addNewAddress(){
        ECKey key  = new ECKey();
        Address address = key.toAddress(Constants.NETWORK_PARAMETERS);
       String strAddress =  address.toString();
        log.info("new address is :"+strAddress);
        strPrivateKey  = key.getPrivateKeyEncoded(Constants.NETWORK_PARAMETERS).toString();
        log.info("private key  is :"+strPrivateKey);

        return address;
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
                    /*if((sysTime - beginTime) > 120000 && (amountCalculatorLink != null) && (amountCalculatorLink.getAmount().floatValue() <= 0))
                        startActivity(new Intent(ReceiveCoinActivity.this,ExchangeRatesActivity.class));
*/
                    break;
                default:
                    break;
            }
        }
    };



    private class ReadThread extends Thread {

        @SuppressWarnings("null")
        @Override
        public void run() {
           // super.run();
            log.info("ReadThread start:");

            byte[] temp = new byte[128];	//buffer断了后用来储存当前buffer，并和下一个buffer合并
            int tsize = 0;			//buffer断了，当前读到的buffer的size
            int flag = 0; 			// 1表示buffer断了
            while (readThreadFlag) {  //readThreadFlag
                log.info("readThread is running");

                int size;
                try {
                    byte[] buffer = new byte[128];
                    if (mScanInputStream == null)
                        return;
                    size = mScanInputStream.read(buffer);
                    if (size > 0) {
                        if (flag == 0) { // flag==0
                            if (buffer[0] == (byte) 0x7f) {
                                if ((size - 5) == buffer[2]) {
                                    onDataReceived(buffer, size);
                                    flag = 0;
                                } else { // 7f开头，数据丢失
                                    temp = buffer;
                                    tsize = size;
                                    flag = 1;
                                }
                            } else { // 非7f开头
                                temp = buffer;
                                tsize = size;
                                flag = 1;
                            }
                        } else { // flag==1

                            buffer = getMergeBytes(temp, tsize, buffer, size); // 合并buffer和上一次buffer
                            if (buffer[2] == (byte) (tsize + size - 5)) {		//buffer完整了
                                onDataReceived(buffer, tsize + size);
                                temp = new byte[128];
                                tsize = 0;
                                flag = 0;
                            } else {											//buffer还断着，接着储存，和下一次的buffer再合并
                                flag = 1;
                                temp = buffer;
                                tsize = tsize + size;
                            }

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * 合并两个byte数组 将pByteA的前numA和pByteB的前numB合并
     *
     * @param pByteA
     * @param numA
     * @param pByteB
     * @param numB
     * @return
     */
    public static byte[] getMergeBytes(byte[] pByteA, int numA, byte[] pByteB,
                                       int numB) {

        byte[] b = new byte[numA + numB];
        for (int i = 0; i < numA; i++) {
            b[i] = pByteA[i];
        }
        for (int i = 0; i < numB; i++) {
            b[numA + i] = pByteB[i];
        }
        return b;
    }


    /**
     * 发送线程
     * @author DPI
     *
     */
    private class SendThread extends Thread {

        @Override
        public void run() {
            while (cashFlag && sendThreadFlag && (mScanOutputStream != null)) {                            log.info("mScanOutputStream is :"+mScanOutputStream);

                try {
                    //发送命令间隔为550ms
                    //以下3步用于同步设备
                    mScanOutputStream.write(hexStringToBytes("7f8001116582")); // sync
//                    Log.i("s", "sync");
                    // mScanOutputStream.flush();
                    super.sleep(550);
                    mScanOutputStream.write(hexStringToBytes("7f0001116608"));// sync
                    // mScanOutputStream.flush();
                    super.sleep(550);
                    mScanOutputStream.write(hexStringToBytes("7f8001116582"));// sync
                    super.sleep(550);

                    //以下3步用于生产key
                    mScanOutputStream
                            .write(hexStringToBytes("7F00094A9774116800000000894d"));//generator
                    super.sleep(550);
                    mScanOutputStream
                            .write(hexStringToBytes("7F80094B9F78F905000000009a34"));//modulus
                    super.sleep(550);
                    mScanOutputStream
                            .write(hexStringToBytes("7F00094C73A5CE0000000000b47c"));//request key
                    super.sleep(550);


                    mScanOutputStream.write(hexStringToBytes("7f800206072194")); // protocol 设置协议版本为7
                    super.sleep(550);
                    mScanOutputStream.write(hexStringToBytes("7F0001051E08")); // setup request读取设备版本、国家、通道

                    super.sleep(550);
                    mScanOutputStream.write(hexStringToBytes("7F800302FFFF25A4")); // inhibits 设置通道关闭与开启
                    super.sleep(550);
                    mScanOutputStream.write(hexStringToBytes("7F00010A3C08")); // enable 使能
                    super.sleep(550);

                    log.info("SendThread start:");
//                    log.info("cashFlag is:"+cashFlag);

                    // while循环不断poll，等待用户塞入纸币，一旦ToggleButton off就stop
                    while (cashFlag && (mScanOutputStream != null)) {
                        if (mScanOutputStream != null){
                             mScanOutputStream.write(hexStringToBytes("7F8001071202")); // poll
                            super.sleep(550);
                            if(mScanOutputStream != null){
                             mScanOutputStream.write(hexStringToBytes("7f0001071188")); // poll
                             super.sleep(550);
                            }
                        }
                        log.info("sendThread is running");
                    }
                    mScanOutputStream.write(hexStringToBytes("7F8001093582")); // disable


//
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

        }

    }

    /**
     * 处理readthread中得到的buffer，只有当一条接收命令是完整的，才会调用
     * @param buffer
     * @param size
     */
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {


					 // IMPORTANT:buffer[4]为ee时，表示确认了收币的通道，通道写在buffer[5]
					 // buffer[5]可以是01、02、03、04、05、06，分别表示1元、5元、10元、20元、50元、100元
//                    log.info("buffer is :"+printHexString(buffer,buffer.length));
//                cd /opt/androidWorkspace/mybitcoin-wallet/buffer[4] is :"+buffer[4]);
//                Toast.makeText(getApplicationContext(),"buffer is :"+buffer[4], Toast.LENGTH_SHORT).show();

                /*log.info("buffer[3] is :"+buffer[3]);

                log.info("buffer[1] is :"+buffer[1]);
                log.info("buffer[0] is :"+buffer[0]);*/
//                log.info("coinAmoun t  is :"+String.valueOf(coinAmount));

//                log.info("buffer[4] is :"+buffer[4]);
                if(buffer[4]==(byte)0xee){
                    log.info("buffer is :"+printHexString(buffer,size));
                    int receiveCoin = buffer[5];

                        if(receiveCoin == 1)
                            coinAmount += 1;
                        else if(receiveCoin == 2)
                            coinAmount += 5;
                        else if(receiveCoin == 3)
                            coinAmount += 10;
                        else if(receiveCoin == 4)
                            coinAmount += 20;
                        else if(receiveCoin == 5)
                            coinAmount += 50;
                        else if(receiveCoin == 6)
                            coinAmount += 100;
                        if(coinAmount > 0 ){
                            confirmBtn.setEnabled(true);
                            viewCoinAmount.setText(String.valueOf(coinAmount));
//                            calculationLeftBitCoinAmount();
                        }
//                    Toast.makeText(ReceiveCoinActivity.this,"收到通道"+receiveCoin, Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    /**
     * byte to hexstring
     */
    public String printHexString(byte[] b, int bsize) {
        String a = "";
        for (int i = 0; i < bsize; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            a = a + hex;
        }
        return a;
    }

    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }



}
