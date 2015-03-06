/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.mybitcoin.wallet.Configuration;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.EnvironmentMonitor;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.ExchangeRatesProvider.ExchangeRate;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.TransConfidenceMonitor;
import com.mybitcoin.wallet.environment.SettingInfo;
import com.mybitcoin.wallet.environment.TradeCmd;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.update.Config;
import com.mybitcoin.wallet.update.NetworkTool;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.WalletUtils;

public class WelcomePageActivity extends WalletActivityBase {
    private static final String LOG_TAG = "WelcomePageActivity";
    private static final boolean DEBUG_FLAG = true;

    private static final int BLOCKCHAIN_SERVICE_RESTART_PERIOD_SEC = 10; // blockchain service重启时间10s

    private TradeCmd mTradeCmd;
    private TradeCmdController mTradeCmdController;

    private BlockChainServiceStarter mBlockChainServiceStarter;
    private TransConfidenceMonitor mTransConfidenceMonitor;
    private EnvironmentMonitor mEnvironmentMonitor;
    
    //自动更新用
    public ProgressDialog pBar;
    private Handler handler = new Handler();
    private String path = "/mnt/sdcard/";
    private Configuration config;
    private int newVerCode = 0;
    private String newVerName = "";
    
    private CurrencyTextView buy_rate;
    private CurrencyTextView sell_rate;

    private static final int ID_RATE_LOADER = 1;
    private SettingInfo setInfo;


    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setContentView(R.layout.welcome_page);
        new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateVer();
			}
        	
        }).start();
        
        buy_rate = (CurrencyTextView) findViewById(R.id.buy_rate);
        buy_rate.setPrecision(Constants.LOCAL_PRECISION, 0);
        
        sell_rate = (CurrencyTextView) findViewById(R.id.sell_rate);
        sell_rate.setPrecision(Constants.LOCAL_PRECISION, 0);

        RelativeLayout welPage = (RelativeLayout) findViewById(R.id.welcome_page);
        setInfo = new SettingInfo(WelcomePageActivity.this);
        welPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoop = false;
                if (!getSettingInfo().getKycEnable()) // 如果未启动KYC，则直接进入汇率显示界面
                    gotoActivity(TradeModeActivity.class);
                else
                    gotoActivity(CashTradeKycLoginActivity.class); // 启动KYC登录界面
//            	gotoActivity(TradeModeActivity.class);
            }
        });

        mTradeCmd = new TradeCmd(getWalletApplication().getApplicationContext());
        	
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRefresher();
        new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateVer();
			}
        	
        }).start();
        

        // 显示ATM机器的地址和私钥
        Wallet wallet = getWalletApplication().getWallet();

        final Address localAddress = WalletUtils.pickOldestKey(wallet).toAddress(Constants.NETWORK_PARAMETERS);
        dLog("!!!ATM ADDRESS!!!: " + localAddress.toString());

        for (final ECKey key : wallet.getKeys())
            if (key.getPrivKeyBytes() == null)
                throw new Error("found read-only key, but wallet is likely an encrypted wallet from the future");
            else if (!wallet.isKeyRotating(key))
                dLog("!!!ATM PRIVATE KEY!!!: " + key.getPrivateKeyEncoded(Constants.NETWORK_PARAMETERS).toString());

        BigInteger balance = getWalletApplication().getWallet().getBalance(BalanceType.AVAILABLE);
        String formattedBalanceStr = GenericUtils.formatValue(balance, 8, 0); // 单位：比特币，精度：小数点后8位
        dLog("!!!ATM BTC BALANCE!!!: " + formattedBalanceStr);

        // 开启BlockChainServiceStarter线程，开启BlockChainService
        if (mBlockChainServiceStarter == null) { // 线程对象为空
            mBlockChainServiceStarter = new BlockChainServiceStarter();
            mBlockChainServiceStarter.start();
        } else if (mBlockChainServiceStarter.isInterrupted() || !mBlockChainServiceStarter.isAlive()) { // 线程已被中断或停止
            mBlockChainServiceStarter = new BlockChainServiceStarter();
            mBlockChainServiceStarter.start();
        }

        // 开启TransConfidenceMonitor线程，更新wallet中交易的confidence，并计入CashTradeInfoLog中
        if (mTransConfidenceMonitor == null) { // 线程对象为空
            mTransConfidenceMonitor = new TransConfidenceMonitor(getWalletApplication().getWallet(), getContentResolver());
            mTransConfidenceMonitor.start();
            dLog("Start TransConfidenceMonitor");
        } else if (mTransConfidenceMonitor.isInterrupted() || !mTransConfidenceMonitor.isAlive()) { // 线程已被中断或停止
            mTransConfidenceMonitor = new TransConfidenceMonitor(getWalletApplication().getWallet(), getContentResolver());
            mTransConfidenceMonitor.start();
            dLog("Start TransConfidenceMonitor");
        }

        // 开启EnvironmentMonitor线程，远程上传/下载参数信息
        if (mEnvironmentMonitor == null) { // 线程对象为空
            mEnvironmentMonitor = new EnvironmentMonitor(getWalletApplication());
            mEnvironmentMonitor.start();
            dLog("Start EnvironmentMonitor");
        } else if (mTransConfidenceMonitor.isInterrupted() || !mEnvironmentMonitor.isAlive()) { // 线程已被中断或停止
            mEnvironmentMonitor = new EnvironmentMonitor(getWalletApplication());
            mEnvironmentMonitor.start();
            dLog("Start EnvironmentMonitor");
        }

        // 开启TradeCmdController线程，监视服务器对ATM机的开启/关闭交易指令
        if (mTradeCmdController == null) { // 线程对象为空
            mTradeCmdController = new TradeCmdController();
            mTradeCmdController.start();
        } else if (mTradeCmdController.isInterrupted() || !mTradeCmdController.isAlive()) { // 线程已被中断或停止
            mTradeCmdController = new TradeCmdController();
            mTradeCmdController.start();
        }
    }

    private class BlockChainServiceStarter extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    sleep(BLOCKCHAIN_SERVICE_RESTART_PERIOD_SEC * 1000); // 睡眠BLOCKCHAIN_SERVICE_RESTART_PERIOD_SEC秒
                } catch (InterruptedException e) {
                    dLog("BlockChainServiceStarter: thread is interrupted");
                }

                getWalletApplication().startBlockchainService(false);
                dLog("Start BlockChainService");
            }
        }
    }

    private class TradeCmdController extends Thread {
        @Override
        public void run() {
            try {
                sleep(1500); // 睡眠1.5s，确保WelcomePageActivity.onResume()执行完毕
            } catch (InterruptedException e) {
                dLog("TradeCmdController: thread is interrupted");
            }
            // 此处不采用无限循环，确保所有TradeCmd都只在切换回WelcomePage后处理，防止正在进行中的交易被突然打断
            if (mTradeCmd.getSwShutdown()) {
                dLog("SW_SHUTDOWN is true, go to TradePauseActivity from WelcomePageActivity");
                gotoActivity(TradePauseActivity.class);
            }
        }
    }

    @Override
    public void updateUiInfo() {
        TextView title = (TextView) findViewById(R.id.welcomepage_title);
        TextView hint = (TextView) findViewById(R.id.welcomepage_hint);

        title.setText(getUiInfo().getTextByName(UiInfo.WELCOMEPAGE_TITLE));
        hint.setText(getUiInfo().getTextByName(UiInfo.WELCOMEPAGE_HINT));
        
        TextView buy_dw = (TextView) findViewById(R.id.buy_dw);
        TextView sell_dw = (TextView) findViewById(R.id.sell_dw);
        
        buy_dw.setText(getUiInfo().getTextByName(UiInfo.COMMON_CASH_ABB));
        sell_dw.setText(getUiInfo().getTextByName(UiInfo.COMMON_CASH_ABB));
        
        TextView buy_btc = (TextView) findViewById(R.id.buy_btc);
        TextView sell_btc = (TextView) findViewById(R.id.sell_btc);
        
        buy_btc.setText(getUiInfo().getTextByName(UiInfo.COMMON_BTC_ABB));
        sell_btc.setText(getUiInfo().getTextByName(UiInfo.COMMON_BTC_ABB));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
    
    private void justUpdate(){
    	StringBuffer sb = new StringBuffer();
        sb.append(", 是否更新?");
    	Dialog dialog = new AlertDialog.Builder(WelcomePageActivity.this)
        .setTitle("软件更新")
        .setMessage(sb.toString())
        // 设置内容
        .setPositiveButton("更新",// 设置确定按钮
                        new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                int which) {
//                                        pBar = new ProgressDialog(WelcomePageActivity.this);
//                                        pBar.setTitle("正在下载");
//                                        pBar.setMessage("请稍候...");
//                                        pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                                        downFile(Config.UPDATE_SERVER
//                                                        + Config.UPDATE_APKNAME);
                                	startActivityByPackageName("com.example.updatedemo");
                                }

                        })
        .setNegativeButton("暂不更新",
                        new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                        // 点击"取消"按钮之后退出程序
                                        onResume();
                                }
                        }).create();// 创建
		// 显示对话框
		dialog.show();
    	
    }

    private boolean getServerVerCode() {
            try {
            		dLog("getServerVerCode");
                    String verjson = NetworkTool.getContent(Config.UPDATE_VERJSON);
                    dLog("verjson" + verjson);
                   
                    //返回版本符号要求后，不用此句
                    verjson = verjson.substring(0, verjson.indexOf("."));
                    
                    		newVerCode = Integer.valueOf(verjson);
                    dLog("newVerCode " + String.valueOf(newVerCode));
                    
//                    JSONArray array = new JSONArray(verjson);
//                    if (array.length() > 0) {
//                            JSONObject obj = array.getJSONObject(0);
//                            try {
//                                    newVerCode = Integer.parseInt(obj.getString("verCode"));
//                                    newVerName = obj.getString("verName");
//                            } catch (Exception e) {
//                                    newVerCode = -1;
//                                    newVerName = "";
//                                    return false;
//                            }
//                    }
            } catch (Exception e) {
            	newVerCode = -1;
            }
            return true;
    }

    

    void downFile(final String urlStr) {
    	Log.i("hehe", urlStr);
            pBar.show();
            new Thread() {
                    public void run() {
                    	try {
                        	URL url = new URL(urlStr);
                        	URLConnection con = url.openConnection();
                    
                        
                            InputStream is = con.getInputStream();
                            FileOutputStream fileOutputStream = null;
                            if (is != null) {
                            	String fileName = path + Config.UPDATE_SAVENAME;

                                        File file = new File(fileName);
                                        Log.i("hehe", file.getAbsolutePath());
                                        fileOutputStream = new FileOutputStream(file);

                                        byte[] buf = new byte[1024];
                                        int ch = -1;
                                        int count = 0;
                                        while ((ch = is.read(buf)) != -1) {
                                                fileOutputStream.write(buf, 0, ch);
                                                count += ch;
                                        }

                                }
                                fileOutputStream.flush();
                                if (fileOutputStream != null) {
                                        fileOutputStream.close();
                                }
                                down();
                        } catch (ClientProtocolException e) {
                                e.printStackTrace();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                    }

            }.start();

    }

    void down() {
            handler.post(new Runnable() {
                    public void run() {
                            pBar.cancel();
                            update();
                    }
            });

    }

    void update() {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(path, Config.UPDATE_SAVENAME)),
                            "application/vnd.android.package-archive");
            startActivity(intent);
    }
    
    private  void startActivityByPackageName(String packageName){
        List<ResolveInfo> list = findActivitiesForPackage(this,packageName);
        ResolveInfo info = list.get(0);
        ComponentName componentName = new ComponentName(
                info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name);
        
        Intent intent = getActivity(componentName, Intent.FLAG_ACTIVITY_NEW_TASK | 
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(intent);
    }
    
    private static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        return apps != null ? apps : new ArrayList<ResolveInfo>();
    }
    
    final static Intent getActivity(ComponentName className, int launchFlags) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        return intent;
    }
    
    private void updateVer(){
    	
        
        if (getServerVerCode()){
        	int verCode = Config.getVerCode(this);
        	dLog("new " + String.valueOf(newVerCode) + " now "+String.valueOf(verCode));
        	if (newVerCode > verCode){
        		justUpdate();
        	}
        }
        
    	
    }
    
    private ExchangeRateRefresherThread refresherThread;
    private LoaderManager loaderManager;
    public static final String CNY = "CNY";
    
    private boolean isLoop = true;
    private class ExchangeRateRefresherThread extends Thread {
        @Override
        public void run() {
                try {
                    while(isLoop){
	                    final String rate = requestExchangeRates();
	                    if(rate!= null){

	                    	WelcomePageActivity.this.runOnUiThread(new Runnable() {
	                    		public void run() {
	                    			if(setInfo == null)
	                    				return;
	    	            	        float handlingChargeProportion = setInfo.getHandlingChargeProportion();
	    	            	        Double d1 = Double.valueOf(rate)*(1 + handlingChargeProportion);
	    	            	        DecimalFormat df1 = new DecimalFormat("0.00"); 
	    	            	        String str1 = String.valueOf(df1.format(d1));
	    	            	        buy_rate.setText(str1);
	    	            	        
	    	            	        Double d2 = Double.valueOf(rate)*(1 - handlingChargeProportion);
	    	            	        DecimalFormat df2 = new DecimalFormat("0.00"); 
	    	            	        String str2 = String.valueOf(df2.format(d2));
	    	            	        sell_rate.setText(str2);
	                    		}
	                    	});
	                    }
	                    Thread.sleep(15 * 1000);
                    }
                } catch (InterruptedException e) {

                }
        }

    }
    
    public void startRefresher() {
        if (refresherThread != null && !refresherThread.isInterrupted())
            refresherThread.interrupt();

        isLoop = true;
        refresherThread = new ExchangeRateRefresherThread();
        refresherThread.start();
    }

    public void endRefresher() {
    	if(refresherThread != null)
    		refresherThread.interrupt();
    	refresherThread = null;
    }


    @Override
    protected void onPause() {
        super.onPause();

        endRefresher();
        dLog("endRefresher is called in onPause");
    }
    
    
    private BigInteger rateBase = GenericUtils.ONE_BTC;
      
    private static final BigInteger handCNY = new BigInteger("10000000000000000");
    
	private String requestExchangeRates()
	{
		//从自己服务器上拿数据
		String uriAPI = "http://dashboard.bitocean.com:8081/exchange_rate/" + WalletActivityBase.publiKeyForOuter;
		DefaultHttpClient client = new DefaultHttpClient();  
        HttpGet httpGet = new HttpGet(uriAPI);  
        HttpResponse httpResponse;
        final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
        try {  
            httpResponse = client.execute(httpGet);  
  
  
            if (httpResponse.getStatusLine().getStatusCode() == 200) {  
                // 第3步：使用getEntity方法获得返回结果  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                try {
					JSONObject jsonResult = new JSONObject(strResult);
					return jsonResult.getString("price");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
            }  
        } catch (ClientProtocolException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }
		return null;
	}
}
