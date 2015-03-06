package com.mybitcoin.wallet.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.text.format.DateFormat;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.uri.BitcoinURI;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.io.IOException;
import java.math.BigInteger;

import com.mybitcoin.wallet.Configuration;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.ExchangeRatesProvider;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.Qr;
import com.mybitcoin.wallet.util.WalletUtils;
import com.mybitcoin.wallet.util.TransactionLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by zhuyun on 14-4-7.
 */
public class TransSuccessActivity extends  SerialPortActivity {

//    private TextView mTime;
    private static final int msgKey1 = 1;
    private Thread timerThread;
    private boolean timerFlag;
    private Bitmap addressQRCodeBitmap,pkAddressQRCodeBitmap;

    private BigInteger rateBase = GenericUtils.ONE_BTC;
    private WalletApplication application;
    private Configuration config;
    private static Logger log = LoggerFactory.getLogger(TransSuccessActivity.class);

    private TextView printBtn;
    private String strQRAddress;
    private String transType;
    private String strPrivateKey;

    private TextView btnPrev,btnPrintInvoice,btnConfirm;


    private int coinAmount=0;
    private String  bitcoinAmount = "";
    String tele = "";
    private long beginTime;


    @Override
    protected void onCreate(final Bundle saveInstanceState){
        serialPortType = 0;
        super.onCreate(saveInstanceState);
//        application = getWalletApplication();
        application = mApplication;
        config = application.getConfiguration();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.transsuccess);

//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);


        String currencyCode = config.getExchangeCurrencyCode();
        ExchangeRatesProvider.ExchangeRate exchangeRate = config.getCachedExchangeRate();
        tele  = getResources().getString(R.string.server_telephone);
        TextView currencyView = (TextView)findViewById(R.id.current_coin_name);
        currencyView.setText(currencyCode);

        CurrencyTextView rateView = (CurrencyTextView)findViewById(R.id.exchange_rate);
        rateView.setPrecision(Constants.LOCAL_PRECISION, 0);
        rateView.setAmount(WalletUtils.localValue(rateBase, exchangeRate.rate));
//        rateView.setAmount(WalletUtils.localValue(rateBase, new BigInteger("1100")));

        //获取交易类型和二维码地址
        Intent intent = getIntent();
        transType = intent.getStringExtra("transType");
//        log.info("transType is :"+transType);
        strQRAddress = intent.getStringExtra("qrAddress");
//        log.info("strQRAddress is :"+strQRAddress);
        strPrivateKey = intent.getStringExtra("privateKey");
//        log.info("strPrivateKey is :"+strPrivateKey);
        coinAmount = intent.getIntExtra("coinAmount",0);
//        log.info("coinAmount is :"+coinAmount);
        bitcoinAmount = intent.getStringExtra("bitcoinAmount");
//        log.info("bitcoinAmount is :"+bitcoinAmount);
        //输出到日志文件
        TransactionLog.writeLogtoFile(transType,strQRAddress,String.valueOf(coinAmount),bitcoinAmount,"成功");
        //发送短信
        application.sendMessage("成功");

        TextView coinAmountView = (TextView)findViewById(R.id.coin_amount);
        coinAmountView.setText("￥"+String.valueOf(coinAmount));

        TextView bitcoinAmountView = (TextView)findViewById(R.id.bitcoin_amount);
        bitcoinAmountView.setText(bitcoinAmount);


        beginTime = System.currentTimeMillis();

        timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();
        log.info("timerThread start.");


        btnConfirm = (TextView)findViewById(R.id.btn_confirm);

 
        btnConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(TransSuccessActivity.this, ExchangeRatesActivity.class));
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

        if("0".equals(transType))//交易类型为扫描二维码类型
            printTransInvoice(strQRAddress,bitcoinAmount,String.valueOf(coinAmount));
        else {                   //交易类型为纸钱包类型
            printAddressAndPrivateKeyQRImage(strQRAddress,strPrivateKey);

            printTransInvoice(strQRAddress,bitcoinAmount,String.valueOf(coinAmount));
        }

    }


    @Override
    protected void onStop(){
        timerFlag = false;
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
//                     log.info("time2："+sysTimerStr);
                    if((sysTime - beginTime) > 60000){
                        startActivity(new Intent(TransSuccessActivity.this,ExchangeRatesActivity.class));
                        TransSuccessActivity.this.finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    /*
     * print QR images of address and private key 
     *
     */
    private void printAddressAndPrivateKeyQRImage(String  strAddress,String strPrivateKey){

        try {
            /*final String addressStr = BitcoinURI.convertToBitcoinURI(address, null, null, null);*/
            log.info("addressStr is :"+strAddress);
            final int size = (int) (256 * getResources().getDisplayMetrics().density);
            addressQRCodeBitmap = Qr.bitmap(strAddress, size);
            pkAddressQRCodeBitmap = Qr.bitmap(strPrivateKey,size);

            if(mPrintOutputStream == null){
                try {

                        mPrintOutputStream = application.getPrintSerialPort().getOutputStream();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //打印汉字
            mPrintOutputStream.write(hexStringToBytes("1C26"));
            mPrintOutputStream.write(hexStringToBytes("1C2100"));
            //居中
            mPrintOutputStream.write(hexStringToBytes("1B6101"));
            mPrintOutputStream.write("欢迎使用比特币交易机".getBytes(Charset.forName("GB18030")));

            //换行
            mPrintOutputStream.write(hexStringToBytes("0A"));

            //居左
            mPrintOutputStream.write(hexStringToBytes("1B6100"));

            mPrintOutputStream.write("公钥:".getBytes(Charset.forName("GB18030")));
            mPrintOutputStream.write(strAddress.getBytes());
            mPrintOutputStream.write(hexStringToBytes("0A"));

            //取消汉字
            mPrintOutputStream.write(hexStringToBytes("1C2E"));

            mPrintOutputStream.write(getBytesFormBitmap(addressQRCodeBitmap));
            // 打印位图命令
            mPrintOutputStream.write(hexStringToBytes("1D2F03"));


            //打印汉字
            mPrintOutputStream.write(hexStringToBytes("1C26"));
            mPrintOutputStream.write(hexStringToBytes("1C2100"));

            //换行
            mPrintOutputStream.write(hexStringToBytes("0A"));
            long sysTime = System.currentTimeMillis();
            String  sysTimerStr = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(sysTime);
            mPrintOutputStream.write("交易日期:".getBytes(Charset.forName("GB18030")));
            mPrintOutputStream.write(sysTimerStr.getBytes(Charset.forName("GB18030")));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write("客服电话:".getBytes(Charset.forName("GB18030")));
            mPrintOutputStream.write(tele.getBytes());
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write("私钥:".getBytes(Charset.forName("GB18030")));
            mPrintOutputStream.write(strPrivateKey.getBytes());
            mPrintOutputStream.write(hexStringToBytes("0A"));
            //取消汉字
            mPrintOutputStream.write(hexStringToBytes("1C2E"));



            mPrintOutputStream.write(getBytesFormBitmap(pkAddressQRCodeBitmap));
            // 打印位图命令
            mPrintOutputStream.write(hexStringToBytes("1D2F03"));




            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
            mPrintOutputStream.write(hexStringToBytes("0A"));
//            mPrintOutputStream.write(hexStringToBytes("0A"));
//            mPrintOutputStream.write(hexStringToBytes("0A"));


            //切纸
            mPrintOutputStream.write(hexStringToBytes("1D5601"));
//            mPrintOutputStream.flush();



            // 清除位图命令
//            mPrintOutputStream.write(hexStringToBytes("1B40"));


//            btnPrintInvoice.setVisibility(View.GONE);
        } catch (Exception e) {
//        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /*
     * print invoice of  transaction
     *
     */
    private void printTransInvoice(String address,String bitcoinAmount,String coinAmount){
        try{

            /*if(mPrintOutputStream == null){
                try {

                    mPrintOutputStream = application.getPrintSerialPort().getOutputStream();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
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
//            mPrintOutputStream.write(hexStringToBytes("1C2E"));
//            mPrintOutputStream.write((byte)0x24);

//            mPrintOutputStream.write(hexStringToBytes("0x24"));
//            mPrintOutputStream.write(hexStringToBytes("FFE5"));


//            mPrintOutputStream.write("¥".getBytes(Charset.forName("GB11383")));
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

            mPrintOutputStream.write("交易结果:成功".getBytes(Charset.forName("GB18030")));
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
//            mPrintOutputStream.flush();


//            btnPrintInvoice.setVisibility(View.GONE);

        }catch(Exception e){
            e.printStackTrace();
        }
        log.info("print invoice of  transaction");
    }
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        try {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
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

    public static String str2Hex(String str)
    {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < str.length(); i++)
        {
            byte[] ba = str.substring(i, i + 1).getBytes();
            String tmpHex = Integer.toHexString(ba[0] & 0xFF);
            sb.append("0x" + tmpHex.toUpperCase() + " ");
            if (ba.length == 2)
            {
                tmpHex = Integer.toHexString(ba[1] & 0xff);
                sb.append("0x" + tmpHex.toUpperCase() + " ");
            }
        }
        return  sb.toString();
    }

    /*
     * Get bytes form Bitmap
     *
     */
    private  byte[] getBytesFormBitmap(Bitmap bitmap){
        int[] pixels_old = new int[bitmap.getWidth() * bitmap.getHeight()];
        int[] pixels_new = new int[bitmap.getWidth() * bitmap.getHeight()];
        byte[] bytes_new = new byte[bitmap.getWidth() * bitmap.getHeight()/8+4];
        bytes_new[0]=(byte)0x1D;
        bytes_new[1]=(byte)0x2A;
        bytes_new[2]=(byte)0x20;
        bytes_new[3]=(byte)0x20;

        bitmap.getPixels(pixels_old,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        for(int i=0;i<pixels_old.length;i++){
            if(pixels_old[i] == Color.BLACK)
                pixels_new[i] = 1;
            else
                pixels_new[i] = 0;
        }

        int sum =0 ;
        for(int i=0;i<256;i++){
            for(int j=0;j<256;j++){
//                 log.info("2^(7-j%8) is :"+(2 << (6-j%8)));
                int element = (pixels_new[j*256+i]) * (2 << (6-j%8));
//                 log.info("element is :"+ element);
                sum += element;
//                 log.info("sum is "+sum);
                if(j%8 == 7){
                    bytes_new[i*32+(j-7)/8+4] = (byte)sum;
//                     log.info("bytes_new["+i*32+(j-7)/8+4+"] is :"+bytes_new[i*32+(j-7)/8+4]);
                    sum=0;
                }
            }
        }
        return bytes_new;
    }

    private byte[] getReadBitMapBytes(Bitmap bitmap) {

        byte[] bytes = null;  //打印数据
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int heightbyte = (height - 1) / 8 + 1;
        int bufsize = width * heightbyte;
        int m1, n1;
        byte[] maparray = new byte[bufsize];

        byte[] rgb = new byte[3];

        int []pixels = new int[width * height]; //通过位图的大小创建像素点数组

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        /**解析图片 获取位图数据**/
        for (int j = 0;j < height; j++) {
            for (int i = 0; i < width; i++) {
                int pixel = pixels[width * j + i]; /**获取ＲＧＢ值**/
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                //System.out.println("i=" + i + ",j=" + j + ":(" + r + ","+ g+ "," + b + ")");
                rgb[0] = (byte)r;
                rgb[1] = (byte)g;
                rgb[2] = (byte)b;
                if (r != 255 || g != 255 || b != 255){//如果不是空白的话用黑色填充    这里如果童鞋要过滤颜色在这里处理
                    m1 = (j / 8) * width + i;
                    n1 = j - (j / 8) * 8;
                    maparray[m1] |= (byte)(1 << 7 - ((byte)n1));
                }
            }
        }
        byte[] b = new byte[322];
        int line = 0;
        int j = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /**对位图数据进行处理**/
        for (int i = 0; i < maparray.length; i++) {
            b[j] = maparray[i];
            j++;
            if (j == 322) {  /**  322图片的宽 **/
                if (line < ((322 - 1) / 8)) {
                    byte[] lineByte = new byte[329];
                    byte nL = (byte) 322;
                    byte nH = (byte) (322 >> 8);
                    int index = 5;
                    /**添加打印图片前导字符  每行的 这里是8位**/
                    lineByte[0] = 0x1B;
                    lineByte[1] = 0x2A;
                    lineByte[2] = 1;
                    lineByte[3] = nL;
                    lineByte[4] = nH;
                    /**copy 数组数据**/
                    System.arraycopy(b, 0, lineByte, index, b.length);

                    lineByte[lineByte.length - 2] = 0x0D;
                    lineByte[lineByte.length - 1] = 0x0A;
                    baos.write(lineByte, 0, lineByte.length);
                    try {
                        baos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    line++;
                }
                j = 0;
            }
        }
        bytes = baos.toByteArray();
        return bytes;
    }

    // 打开数据接收线程，接收串口返回的数据
    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        /*runOnUiThread(new Runnable() {
            public void run() {
                if (mReception != null) {
                    mReception.append(new String(buffer, 0, size));
                }
            }
        });*/
    }





}
