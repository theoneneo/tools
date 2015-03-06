package com.mybitcoin.wallet.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.KS80TSDK;
import android_serialport_api.KS80TSDK.onPrintListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.lw.db.FileUtils;
import com.mybitcoin.wallet.Configuration;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.EnvironmentMonitor;
import com.mybitcoin.wallet.ExchangeRatesProvider;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.environment.SettingInfo;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.Qr;
import com.mybitcoin.wallet.util.TransactionLog;
import com.mybitcoin.wallet.util.WalletUtils;


/**
 * Created by zhuyun on 14-4-7.
 */

/**
 * modified by Guan Caeson
 * 
 * 封装了打印机的SDK：KS80TSDK.java，只需要new一个实例mks80t，
 * 再在相应位置使用mks80t.
 * 
 */
public class CopyOfTransSuccessActivity extends  WalletActivityTimeoutBase {

	KS80TSDK mks80t;
//    private TextView mTime;
    private static final int msgKey1 = 1;
    private Thread timerThread;
    private boolean timerFlag;
    private Bitmap addressQRCodeBitmap,pkAddressQRCodeBitmap;

    private BigInteger rateBase = GenericUtils.ONE_BTC;
    private WalletApplication application;
    private Configuration config;
    private static Logger log = LoggerFactory.getLogger(CopyOfTransSuccessActivity.class);

    private String strQRAddress;
    private String transType;
    private String strPrivateKey;

    private TextView btnConfirm;


    private int coinAmount=0;
    private String  bitcoinAmount = "";
    String tele = "";
    private long beginTime;

    String mAtmAddr;
    

    @Override
    protected void onCreate(final Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
//        application = getWalletApplication();
        mAtmAddr = WalletUtils.pickOldestKey(getWalletApplication().getWallet()).toAddress(Constants.NETWORK_PARAMETERS).toString();
        
      
        application = (WalletApplication) getApplication();
        config = application.getConfiguration();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setLayout(R.layout.transsuccess);

        String currencyCode = "JPY";//config.getExchangeCurrencyCode();
        ExchangeRatesProvider.ExchangeRate exchangeRate = config.getCachedExchangeRate();
        tele  = getResources().getString(R.string.server_telephone);
        TextView currencyView = (TextView)findViewById(R.id.current_coin_name);
        currencyView.setText(currencyCode);

        CurrencyTextView rateView = (CurrencyTextView)findViewById(R.id.exchange_rate);
        rateView.setPrecision(Constants.LOCAL_PRECISION, 0);
        rateView.setAmount(WalletUtils.localValue(rateBase, exchangeRate.rate));
        rateView.setAmountText(true);   
        
        
        //new KS80TSDK 
        mks80t = new KS80TSDK();
		mks80t.setOnPrintListener(new onPrintListener(){		//监听KS80TSDK

			@Override
			public void onFinished() {
				// 这里写监听到出钞结束后的处理
				//byte[] finishflag = {(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff};
				//onDataReceived(finishflag,4);
				mHandler.sendEmptyMessage(100);
			}

			@Override
			public void onError(Exception e) {
				// 这里写监听到初始化串口出错后的处理
				//DisplayError(e.toString());
			}

			@Override
			public void onComRead(byte[] rxbuffer, int size) {
				// TODO Auto-generated method stub
				
			}

		});
        
        
        
	
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


//        mTime = (TextView)findViewById(R.id.timer);
        beginTime = System.currentTimeMillis();
//        CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",beginTime);
//        mTime.setText(sysTimerStr);
        timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();
        log.info("timerThread start.");



//        btnPrev = (TextView)findViewById(R.id.btn_prev);
//        btnPrintInvoice = (TextView)findViewById(R.id.btn_printinvoice);
        btnConfirm = (TextView)findViewById(R.id.btn_confirm);
        btnConfirm.setVisibility(4);

       /* btnPrev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(TransSuccessActivity.this,ExchangeRatesActivity.class));
                finish();
            }
        });*/
//        if("1".equals(transType))//打印纸钱包，则更换打印按钮的背景图片
//            btnPrintInvoice.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_printwallet));

       /* btnPrintInvoice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if("0".equals(transType))//交易类型为扫描二维码类型
                    printTransInvoice(strQRAddress,bitcoinAmount,String.valueOf(coinAmount));
                else                    //交易类型为纸钱包类型
                     printAddressAndPrivateKeyQRImage(strQRAddress,strPrivateKey);
            }
        });*/
        btnConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //startActivity(new Intent(CopyOfTransSuccessActivity.this, ExchangeRatesActivity.class));
            	startActivity(new Intent(CopyOfTransSuccessActivity.this, WelcomePageActivity.class));
                finish();
            }
        });
        
        if("0".equals(transType)){//交易类型为扫描二维码类型
            printTransInvoice(strQRAddress,bitcoinAmount,String.valueOf(coinAmount));
            mks80t.begin();
        }
        else {                   //交易类型为纸钱包类型
            printAddressAndPrivateKeyQRImage(strQRAddress,strPrivateKey);

            printTransInvoice(strQRAddress,bitcoinAmount,String.valueOf(coinAmount));
        	mks80t.begin();
        }

        /*Intent  intent1 = new Intent(TransSuccessActivity.this,ScanQRResultActivity.class);
        intent1.putExtra("address",strQRAddress);
        startActivity(intent1);*/
        
        
    }

   

    @Override
    protected void onStop(){
        timerFlag = false;
        mks80t.end();
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
                        startActivity(new Intent(CopyOfTransSuccessActivity.this,WelcomePageActivity.class));
                        CopyOfTransSuccessActivity.this.finish();
                    }
                    break;
                case 100:
                	btnConfirm.setVisibility(0);
                	TransInfoThread transInfoThread = new TransInfoThread();
                    transInfoThread.start();

//                    sendTradeSuccessSms();
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

            /*final String addressStr = BitcoinURI.convertToBitcoinURI(address, null, null, null);*/
            
            final int size = (int) (256);
           
            addressQRCodeBitmap = Qr.bitmap(strAddress, size);
            pkAddressQRCodeBitmap = Qr.bitmap(strPrivateKey,size);
            log.info("addressStr is :"+strAddress+" "+addressQRCodeBitmap.getWidth());
            mks80t.printAddressAndPrivateKeyQRImage(strAddress, strPrivateKey, tele, addressQRCodeBitmap, pkAddressQRCodeBitmap);

    }

    /*
     * print invoice of  transaction
     *
     */
    private void printTransInvoice(String address,String bitcoinAmount,String coinAmount){
    	//mks80t.printTransInvoice(address, bitcoinAmount, coinAmount, tele);
    	mks80t.printTransInvoice(address, bitcoinAmount, coinAmount,tele,true) ;
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
    protected void onDataReceived(final byte[] buffer, final int size) {
        /*runOnUiThread(new Runnable() {
            public void run() {
                if (mReception != null) {
                    mReception.append(new String(buffer, 0, size));
                }
            }
        });*/
    }


	@Override
	public void updateUiInfo() {
		// TODO Auto-generated method stub
		
	}
	private String mHandlingChargeProportion;
	
    private class TransInfoThread extends Thread {
        private String URL = EnvironmentMonitor.URL_BASE + "/transaction/";
        private String ATM_NAME = "atm_name";
        private String TIME = "time";
        private String TRANS_DIRECTION = "direction";
        private String BTC_AMOUNT = "btc_amount";
        private String CASH_AMOUNT = "cash_amount";
        private String EXCHANGE_RATE = "exchange_rate";
        private String HANDLING_CHARGE_PROPORTION = "handling_charge_proportion";
        private String PAYER_ADDR = "payer_addr";

        public static final String DIRECTION_BUY_COINS = "buy_coins";
        private static final String TRADE_STATUS = "trade_status";
        private static final int TRADE_STATUS_SUCCESS = 0;
        private RequestQueue mQueue;

        public TransInfoThread() {
            mQueue = Volley.newRequestQueue(getApplicationContext());
        }

        Response.Listener<JSONObject> rspListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //dLog("TransInfoThread: Response is " + response.toString());
            }
        };

        Response.ErrorListener errListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //dLog("TransInfoThread: " + error.toString());
            }
        };

        @Override
        public void run() {
            try {
                JSONObject postBody = new JSONObject();
                postBody.put(ATM_NAME, mAtmAddr);
                postBody.put(PAYER_ADDR, strQRAddress);
                postBody.put(TIME, getCurrentDateAndTimeStr());
                postBody.put(TRANS_DIRECTION, DIRECTION_BUY_COINS);
                String amount = bitcoinAmount.substring(4);
                Double amountD = Double.valueOf(amount);
                //amountD = amountD/1000;
                
                BigInteger balance = getWalletApplication().getWallet().getBalance(BalanceType.ESTIMATED);
                String formattedBalanceStr = GenericUtils.formatValue(balance, 8, 0);
                
                postBody.put(BTC_AMOUNT, String.valueOf(amountD));
                postBody.put(CASH_AMOUNT, String.valueOf(coinAmount));
                postBody.put(EXCHANGE_RATE, ExchangeRatesFragment.rate);
                
                SettingInfo setting = new SettingInfo(CopyOfTransSuccessActivity.this);
                postBody.put(HANDLING_CHARGE_PROPORTION, String.valueOf(setting.getHandlingChargeProportion()));

                if(Double.parseDouble(formattedBalanceStr) - amountD <= setting.getBalanceWarningThreshold()){
                    postBody.put(TRADE_STATUS, 3);
                    FileUtils.append("STATUS.txt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\n");
//                    FileUtils.append("STATUS.txt", "Left Value:"+(Double.parseDouble(formattedBalanceStr) - amountD)+"\n");
//                    FileUtils.append("STATUS.txt", "WarnValue:"+setting.getBalanceWarningThreshold()+"\n");
//                    FileUtils.append("STATUS.txt", "ATM_NAME:"+mAtmAddr+"\n");
//                    FileUtils.append("STATUS.txt", "PAYER_ADDR:"+strQRAddress+"\n");
//                    FileUtils.append("STATUS.txt", "TRANS_DIRECTION:"+DIRECTION_BUY_COINS+"\n");
//                    FileUtils.append("STATUS.txt", "BTC_AMOUNT:"+amountD+"\n");
//                    FileUtils.append("STATUS.txt", "CASH_AMOUNT:"+coinAmount+"\n");
//                    FileUtils.append("STATUS.txt", "EXCHANGE_RATE:"+ExchangeRatesFragment.rate+"\n");
//                    FileUtils.append("STATUS.txt", "HANDLING_CHARGE_PROPORTION:"+setting.getHandlingChargeProportion()+"\n");
//                    FileUtils.append("STATUS.txt", "TRADE_STATUS:"+3+"\n");
//                    FileUtils.append("STATUS.txt", "\n");
//                    Toast.makeText(CopyOfTransSuccessActivity.this, "TRADE_STATUS:3", Toast.LENGTH_LONG).show();
                }
                else{
                    postBody.put(TRADE_STATUS, TRADE_STATUS_SUCCESS);
//                  
//                  FileUtils.append("STATUS.txt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\n");
//                  FileUtils.append("STATUS.txt", "ATM_NAME:"+mAtmAddr+"\n");
//                  FileUtils.append("STATUS.txt", "PAYER_ADDR:"+strQRAddress+"\n");
//                  FileUtils.append("STATUS.txt", "TRANS_DIRECTION:"+DIRECTION_BUY_COINS+"\n");
//                  FileUtils.append("STATUS.txt", "BTC_AMOUNT:"+amountD+"\n");
//                  FileUtils.append("STATUS.txt", "CASH_AMOUNT:"+coinAmount+"\n");
//                  FileUtils.append("STATUS.txt", "EXCHANGE_RATE:"+ExchangeRatesFragment.rate+"\n");
//                  FileUtils.append("STATUS.txt", "HANDLING_CHARGE_PROPORTION:"+setting.getHandlingChargeProportion()+"\n");
//                  FileUtils.append("STATUS.txt", "TRADE_STATUS:"+TRADE_STATUS_SUCCESS+"\n");
//                  FileUtils.append("STATUS.txt", "\n");
                  
//                    Toast.makeText(CopyOfTransSuccessActivity.this, "TRADE_STATUS:0", Toast.LENGTH_LONG).show();
                }
                
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, postBody, rspListener, errListener);

                log.info("TransInfoThread: POST is preparing to send to: " + URL + ", JSON: " + postBody.toString());
                mQueue.add(request);
            } catch (JSONException e) {
                //dLog("TransInfoThread: error when handling json");
            }
        }

        private String getCurrentDateAndTimeStr() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            return formatter.format(date);
        }
    }

    public void sendTradeSuccessSms() {
        String baseUrl = getSettingInfo().getSmsPlatformUrl();
        String smsTag = "&smsText=";

        /**
         * 短信模板：
         *
         * 贵平台<atm_name>于<time>发生一笔卖出比特币交易，交易额为卖出比特币<btc_amount>，兑换人民币<cash_amount>，即时汇率为<exchange_rate>，手续费率为<handling_charge_proportion>。
         * 其中：
         *   atm_public_key:                ATM机的公钥
         *   time:                          交易发生的时间，格式为：2014-06-01 12:00:00
         *   direction:                     “卖出比特币“或“买入比特币”，二选一
         *   amount:                        交易总额，以比特币为单位，精确到小数点后6位
         *   exchange_rate:                 交易汇率，法币：比特币，精确到小数点后2位
         *   handling_charge_proportion:    手续费率，1.05表示手续费为5%
         */

        // 将模板中的<xxx>标签替换为实际数值
        String content = getUiInfo().getTextByName(UiInfo.cointrade_tradesuccess_sms_template);
        content = content.replaceAll("<atm_name>", mAtmAddr);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"); // 注意冒号在URL中为非法字符，故使用“-”代替冒号
        Date date = new Date();
        String dateStr = formatter.format(date);
        content = content.replaceAll("<time>", dateStr);
        String amount = bitcoinAmount.substring(4);
        Double amountD = Double.valueOf(amount);
        amountD = amountD/1000;
        content = content.replaceAll("<btc_amount>", String.valueOf(amountD));
        content = content.replaceAll("<cash_amount>", String.valueOf(coinAmount));
        content = content.replaceAll("<exchange_rate>", ExchangeRatesFragment.rate);
        SettingInfo setting = new SettingInfo(CopyOfTransSuccessActivity.this);
        content = content.replaceAll("<handling_charge_proportion>", String.valueOf(setting.getHandlingChargeProportion()));

        String smsMobileArrStr = getSettingInfo().getSmsMobileArrStr();
        String[] smsMobileArr = smsMobileArrStr.split(",");
        if (smsMobileArr == null) {
            //dLog("sending sms error: smsMobileArr is null");
            return;
        }

        for (String mobile : smsMobileArr) {
            StringBuilder sb = new StringBuilder();
            sb.append(baseUrl).append(mobile).append(smsTag).append(content);

            String sms = sb.toString();

            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(sms);

            try {
                client.execute(httpPost);
               // dLog("Sending SMS via HTTP: " + sms + ", destination: " + mobile);
            } catch (IOException e) {
                //dLog("Error when sending SMS via HTTP: " + sms + ", destination: " + mobile + ", error: " + e.toString());
            }
        }
    }
    
    protected void onResume() {
		super.onResume();
		init();
		new Thread(new UpdateText()).start();
	};
	private void init(){
		UiInfo uiInfo = new UiInfo(this);
		
		TextView tvTip = (TextView)findViewById(R.id.tvTitle);
		TextView tvCurrentCashType = (TextView)findViewById(R.id.tvCurrentCashType);
		TextView tvCurrentExchangeRate = (TextView)findViewById(R.id.tvCurrentExchangeRate);
		TextView tvCashValue = (TextView)findViewById(R.id.tvCashValue);
		TextView tvCoinValue = (TextView)findViewById(R.id.tvCoinValue);
		TextView tvSecurity = (TextView)findViewById(R.id.tvSecurity);
		
		tvTip.setText(uiInfo.getTextByName(UiInfo.cointrade_tradesuccess_title));
		tvCurrentCashType.setText(uiInfo.getTextByName(UiInfo.cointrade_tradesuccess_currentcashtype));
		tvCurrentExchangeRate.setText(uiInfo.getTextByName(UiInfo.cointrade_tradesuccess_currentexchagerate));
		tvCashValue.setText(uiInfo.getTextByName(UiInfo.cointrade_tradesuccess_cashvalue));
		tvCoinValue.setText(uiInfo.getTextByName(UiInfo.cointrade_tradesuccess_coinvalue));
		tvSecurity.setText(uiInfo.getTextByName(UiInfo.cointrade_tradesuccess_security));
		
	
		TextView tvOk = (TextView)findViewById(R.id.btn_confirm);
		
		tvOk.setText(uiInfo.getTextByName(UiInfo.COMMON_CFM_BTN));
	}
	 @Override
	    protected void onDestroy() {
	
	    	super.onDestroy();
	    	flag1 = false;
	    }
	    boolean flag1 = true;
	    class UpdateText implements Runnable{
			@Override
			public void run() {
				while(flag1){
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
