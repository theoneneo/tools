package com.mybitcoin.wallet.ui;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android_serialport_api.UBASDK;
import android_serialport_api.UBASDK.onUBAListener;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.mybitcoin.wallet.Configuration;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.ExchangeRatesProvider.ExchangeRate;
import com.mybitcoin.wallet.environment.SettingInfo;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.WalletUtils;

public class NewReceiverCoinActivity extends SerialPortActivity {
	private BigInteger rateBase = GenericUtils.ONE_BTC;
	TextView received_coins_amount;
	TextView bitcoin_amount;
	CurrencyTextView rateView;
	private String strQRAddress;
	private Address newAddress = null;
	private WalletApplication application;

	private CurrencyCalculatorLink amountCalculatorLink;
	private Configuration config;
	// EditText mReception;
	private String transType;
	private static Logger log = LoggerFactory
			.getLogger(NewReceiverCoinActivity.class);
	TextView cancelBtn;
	TextView confirmBtn;
	UBASDK muba;
	private boolean cashFlag;
	CurrencyAmountView btcAmountView, localAmountView;
	// public static boolean flag = false;
	private String strPrivateKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		application = mApplication;
		config = application.getConfiguration();

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);btn_confirm
		setLayout(R.layout.new_receive_coin);

		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);

		application.startBlockchainService(false);

		String currencyCode = config.getExchangeCurrencyCode();
		ExchangeRate exchangeRate = config.getCachedExchangeRate();

		TextView currencyView = (TextView) findViewById(R.id.current_coin_name);
		currencyView.setText(currencyCode);

		rateView = (CurrencyTextView) findViewById(R.id.exchange_rate);
		rateView.setPrecision(Constants.LOCAL_PRECISION, 0);
		rateView.setAmount(WalletUtils.localValue(rateBase, exchangeRate.rate));
		rateView.setAmountText(true);

		TextView addressView = (TextView) findViewById(R.id.wallet_address);

		Intent intent = getIntent();
		// 判断交易类型

		// 如果是打印纸钱包，则生成新的地址
		transType = intent.getStringExtra("transType");
		// log.info("transType is :"+transType);
		if (("0".equals(transType))) {
			// 如果是扫描二维码，则使用扫描的二维码地址
			strQRAddress = intent.getStringExtra("qrAddress");
		} else {
			newAddress = addNewAddress();
			strQRAddress = newAddress.toString();
		}

		addressView.setText(strQRAddress);

		received_coins_amount = (TextView) findViewById(R.id.received_coins_amount);
		bitcoin_amount = (TextView) findViewById(R.id.bitcoin_amount);

		// mReception = (EditText) findViewById(R.id.EditTextReception); // 接收框

		// EditText Emission = (EditText) findViewById(R.id.EditTextEmission);
		// // 发送框
		muba = new UBASDK();
		muba.setOnUBAListener(new onUBAListener() { // 监听UBA

			@Override
			public void onCash(final int num) {
				// 这里写监听到入钞结束后的处理
//				onFinishedToast(num);
				NewReceiverCoinActivity.this.runOnUiThread(new Runnable() {
					public void run() { 
						try{
						checkBitCoin(num);
						}catch(Exception e){
					    	TextView ttextView = new TextView(NewReceiverCoinActivity.this);
					    	ttextView.setText(e.getMessage());
					    	ttextView.setTextColor(Color.RED);
					    	ttextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
					    	AlertDialog.Builder cbuilder = new AlertDialog.Builder(NewReceiverCoinActivity.this);  
					    	cbuilder.setView(ttextView)
					    	       .setCancelable(false)  
					    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {  
					    	           public void onClick(DialogInterface dialog, int id) {  
					    	        	   dialog.cancel(); 
					    	           }  
					    	       }).create().show();  
						}
					}
				});
			}

			@Override
			public void onComRead(byte[] rxbuffer, int size) {
				// 这里写监听到串口收到数据后的处理
				onDataReceived(rxbuffer, size);
			}

			@Override
			public void onError(Exception e) {
				// 这里写监听到初始化串口出错后的处理
				DisplayError(e);
			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
				NewReceiverCoinActivity.this.finish();
				// cancelBtn.setVisibility(0);
				// confirmBtn.setVisibility(0);
			}
		});

		muba.mystart();
		// flag = true;
		muba.stopflag = true;

		btcAmountView = (CurrencyAmountView) findViewById(R.id.coins_amount_btc);
		btcAmountView.setCurrencySymbol(config.getBtcPrefix());
		btcAmountView.setInputPrecision(config.getBtcMaxPrecision());
		btcAmountView.setHintPrecision(config.getBtcPrecision());
		btcAmountView.setShift(config.getBtcShift());

		localAmountView = (CurrencyAmountView) findViewById(R.id.coins_amount_local);
		localAmountView.setInputPrecision(Constants.LOCAL_PRECISION);
		localAmountView.setHintPrecision(Constants.LOCAL_PRECISION);

		final BigInteger Proportionrate = GenericUtils.toNanoCoins(
				rateView.getAmountText(), 0);
		ExchangeRate amountCalexchangeRate = new ExchangeRate("JPY",
				Proportionrate, config.getCachedExchangeRate().source);
		amountCalculatorLink = new CurrencyCalculatorLink(btcAmountView,
				localAmountView);
		amountCalculatorLink.setExchangeDirection(false);
		amountCalculatorLink.setExchangeRate(amountCalexchangeRate);

		cancelBtn = (TextView) findViewById(R.id.btn_prev);
		// cancelBtn.setVisibility(4);
		confirmBtn = (TextView) findViewById(R.id.btn_confirm);
		// confirmBtn.setVisibility(4);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				/*
				 * viewCoinAmount.setText("1"); coinAmount = 1;
				 * log.info("bitcoinAmount is :"
				 * +GenericUtils.formatValue(amountCalculatorLink.getAmount(),
				 * config.getBtcMaxPrecision(), config.getBtcShift())); //
				 * log.info("bitcoinAmount1 is :"+ new
				 * DecimalFormat("##0.00").format
				 * (amountCalculatorLink.getAmount().floatValue()/100000)); //
				 * confirmBtn.setEnabled(true); calculationLeftBitCoinAmount();
				 */

				// flag = false;
				muba.stopflag = false;

				cashFlag = false;

				NewReceiverCoinActivity.this.finish();

			}
		});

		// confirmBtn.setEnabled(false);
		confirmBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
	        	TextView textView = new TextView(NewReceiverCoinActivity.this);
	        	textView.setText("We will print the receipt of the transaction, please carefully keep it.");
	        	textView.setTextColor(Color.RED);
	        	textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
	        	AlertDialog.Builder builder = new AlertDialog.Builder(NewReceiverCoinActivity.this);  
	        	builder.setView(textView)
	        	       .setCancelable(false)  
	        	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {  
	        	           public void onClick(DialogInterface dialog, int id) {  
	        	        	   dialog.cancel(); 
	        	        	   TransProgress();
	        	           }  
	        	       }).create().show();  
			}
		});
		
		popWarning();
	}
	
	private void popWarning(){
		TextView textView = new TextView(this);
    	textView.setText("Your transaction cannot be cancelled after you insert bank notes.");
    	textView.setTextColor(Color.RED);
    	textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	builder
    			.setView(textView)
    	       .setCancelable(false)  
    	       
    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {  
    	           public void onClick(DialogInterface dialog, int id) {  
    	        	   dialog.cancel();
    	           }  
    	       })  
    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {  
    	           public void onClick(DialogInterface dialog, int id) {  
    	                dialog.cancel();  
    	                finish();
    	           }  
    	       }).create().show();  
	}
	
	public void checkBitCoin(int num){
		coinAmount = num;
		received_coins_amount.setText(String.valueOf(num));
		String rate = rateView.getText().toString();
		float rateF = Float.valueOf(rate);

		SettingInfo setting = new SettingInfo(
				NewReceiverCoinActivity.this);

		float bitNum = (coinAmount / rateF);
		bitcoin_amount.setText(String.valueOf(bitNum));
		cancelBtn.setVisibility(View.INVISIBLE);
		
		
        BigInteger balance = WalletApplication.getStaticWalletApplication().getWallet().getBalance(BalanceType.AVAILABLE);
        String formattedBalanceStr = GenericUtils.formatValue(balance, 8, 0);
        
        if(Double.parseDouble(formattedBalanceStr) - amountCalculatorLink.getAmount().floatValue()*0.00000001 <= setting.getBalanceWarningThreshold()){
        	muba.myfinish();
        	TextView textView = new TextView(this);
        	textView.setText("BTM has reached limitation, we will finish your current transaction");
        	textView.setTextColor(Color.RED);
        	textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        	builder.setView(textView)
        	       .setCancelable(false)  
        	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {  
        	           public void onClick(DialogInterface dialog, int id) {  
        	        	   dialog.cancel(); 
        	        	   TransProgress();
        	           }  
        	       }).create().show();  
        }
	}
    
    public void TransProgress(){
		if (amountCalculatorLink == null
				|| amountCalculatorLink.getAmount() == null
				|| (amountCalculatorLink.getAmount()).floatValue() <= 0)
			return;
		Log.i("NewReceiverCoinActivity",
				"amountCalculatorLink.getAmount()).floatValue()="
						+ amountCalculatorLink.getAmount().floatValue());
		cashFlag = false;

		// flag = false;
		muba.stopflag = false;
		/*
		 * try{//关闭纸币识别器的识别 // stop if(mScanOutputStream != null)
		 * mScanOutputStream.write(hexStringToBytes("7F8001093582")); //
		 * disable
		 * 
		 * }catch (IOException e){ e.printStackTrace(); }
		 */

		Intent intent = new Intent(NewReceiverCoinActivity.this,
				TransProgressActivity.class);
		intent.putExtra("transType", transType);
		intent.putExtra("qrAddress", strQRAddress);
		intent.putExtra("privateKey", strPrivateKey);
		intent.putExtra("coinAmount", coinAmount);

		// intent.putExtra("bitcoinAmount","1000");
		intent.putExtra(
				"bitcoinAmount",
				GenericUtils.formatValue(
						amountCalculatorLink.getAmount(),
						config.getBtcMaxPrecision(),
						config.getBtcShift()));

		startActivity(intent);
		finish();
    }

	public Address addNewAddress() {
		ECKey key = new ECKey();
		Address address = key.toAddress(Constants.NETWORK_PARAMETERS);
		String strAddress = address.toString();

		Log.i("NewReceiverCoinActivity", "new address is :" + strAddress);
		strPrivateKey = key.getPrivateKeyEncoded(Constants.NETWORK_PARAMETERS)
				.toString();
		Log.i("NewReceiverCoinActivity", "key  is :" + strPrivateKey);

		return address;
	}

	public void DisplayError(Exception e) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(e.toString());
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				NewReceiverCoinActivity.this.finish();
			}
		});
		b.show();
	}

	// 入钞完毕toast一下已入钞总额
	protected void onFinishedToast(final int num) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Toast.makeText(getApplicationContext(),
				// "入钞总数:  " + Integer.toString(num),
				// Toast.LENGTH_SHORT).show();
				Message msg = new Message();
				msg.what = GET_MONEY;
				msg.obj = num;
				handler.sendMessage(msg);
			}
		});
	}

	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			public void run() {
				Log.i("Coin", "size = " + size);
			}
		});
	}

	public void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				NewReceiverCoinActivity.this.finish();
			}
		});
		b.show();
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

	private int coinAmount;

	final int GET_MONEY = 1;
	Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == GET_MONEY) {
				coinAmount = (Integer) msg.obj;
				received_coins_amount.setText(String.valueOf(coinAmount));
				String rate = rateView.getText().toString();
				float rateF = Float.valueOf(rate);

				SettingInfo setting = new SettingInfo(
						NewReceiverCoinActivity.this);

				float bitNum = (coinAmount / rateF);// *(1-setting.getHandlingChargeProportion());
				bitcoin_amount.setText(String.valueOf(bitNum));
			} else if (msg.what == 555) {
				init();
			}
		}
	};

	protected void onResume() {
		super.onResume();
		init();

		new Thread(new UpdateText()).start();
	};

	public void init() {
		super.init();
		UiInfo uiInfo = new UiInfo(this);

		TextView tvTitle = (TextView) findViewById(R.id.txtTitle);
		TextView tvTip = (TextView) findViewById(R.id.tip);
		TextView tvCurrentCashType = (TextView) findViewById(R.id.tvCurrentCashType);
		TextView tvCurrentExchangeRate = (TextView) findViewById(R.id.tvCurrentExchangeRate);
		TextView tvCashValue = (TextView) findViewById(R.id.tvCashValue);
		TextView tvCoinValue = (TextView) findViewById(R.id.tvCoinValue);
		TextView tvWalletAddr = (TextView) findViewById(R.id.tvWalletAddr);

		tvTitle.setText(uiInfo.getTextByName(UiInfo.TRADEMODE_BTC_BTN));
		tvTip.setText(uiInfo.getTextByName(UiInfo.cointrade_inputcash_title));
		tvCurrentCashType.setText(uiInfo
				.getTextByName(UiInfo.cointrade_inputcash_currentcashtype));
		tvCurrentExchangeRate.setText(uiInfo
				.getTextByName(UiInfo.cointrade_inputcash_currentexchagerate));
		tvCashValue.setText(uiInfo
				.getTextByName(UiInfo.cointrade_inputcash_cashvalue));
		tvCoinValue.setText(uiInfo
				.getTextByName(UiInfo.cointrade_inputcash_coinvalue));
		tvWalletAddr.setText(uiInfo
				.getTextByName(UiInfo.cointrade_inputcash_walletaddr));

		TextView tvPre = (TextView) findViewById(R.id.btn_prev);
		TextView tvOk = (TextView) findViewById(R.id.btn_confirm);
		tvPre.setText(uiInfo.getTextByName(UiInfo.COMMON_PRV_BTN));
		tvOk.setText(uiInfo.getTextByName(UiInfo.COMMON_CFM_BTN));
	}

	protected void onDestroy() {
		muba.myfinish();
		// flag = false;
		muba.stopflag = false;
		super.onDestroy();
		flag1 = false;
	}

	boolean flag1 = true;

	class UpdateText implements Runnable {
		@Override
		public void run() {
			while (flag1) {
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(555);
			}
		}
	}

}
