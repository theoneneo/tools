package com.mybitcoin.wallet.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.net.Uri;
import android.content.DialogInterface;
import android.media.RingtoneManager;

import java.math.BigInteger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;

import org.bitcoin.protocols.payments.Protos.Payment;

import com.google.bitcoin.core.WrongNetworkException;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionConfidence;
import com.google.bitcoin.core.TransactionConfidence.ConfidenceType;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.google.bitcoin.core.Wallet.SendRequest;

import com.mybitcoin.wallet.Configuration;
import com.mybitcoin.wallet.Constants;
import com.mybitcoin.wallet.PaymentIntent;
import com.mybitcoin.wallet.PaymentIntent.Standard;
import com.mybitcoin.wallet.WalletApplication;
import com.mybitcoin.wallet.offline.DirectPaymentTask;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.PaymentProtocol;
import com.mybitcoin.wallet.util.WalletUtils;
import com.mybitcoin.wallet.integration.android.BitcoinIntegration;
import com.mybitcoin.wallet.R;




/**
 * Created by zhuyun on 14-4-7.
 */
public class TransProgressActivity extends AbstractBindServiceActivity {


    private WalletApplication application;
    private Configuration config;
    private Wallet wallet;

//    private TextView mTime;
    private static final int msgKey1 = 1;
    private static final int msgSuccess = 2;
    private static final int msgFailed = 3;
    private Thread timerThread;
    private boolean timerFlag;
    private String strQRAddress;
    private String transType;
    private String strPrivateKey;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    private State state = State.INPUT;

    private enum State
    {
        INPUT, PREPARATION, SENDING, SENT, FAILED
    }
    private Transaction sentTransaction = null;

    private Boolean directPaymentAck = null;

    private int coinAmount=0;
    private BigInteger bitcoinAmount;

    private String strBitcoinAmount="";

    @CheckForNull
    private BluetoothAdapter bluetoothAdapter;

    private TransactionsListAdapter sentTransactionListAdapter;

    private PaymentIntent paymentIntent;

    public static final String INTENT_EXTRA_PAYMENT_INTENT = "payment_intent";


    private long beginTime;
//    ImageButton  confirmBtn;

    
    @Override
    protected void onCreate(final Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.transprogressing);

//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.customtitle);

        application = getWalletApplication();
        config = application.getConfiguration();
        wallet = application.getWallet();


        backgroundThread = new HandlerThread("backgroundThread", Process.THREAD_PRIORITY_BACKGROUND);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

//        mTime = (TextView)findViewById(R.id.timer);
        beginTime = System.currentTimeMillis();
//        CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",beginTime);

//        mTime.setText(sysTimerStr);
        /*timerFlag = true;
        timerThread = new TimeThread();
        timerThread.start();
        log.info("timerThread start.");*/



        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        Intent intent = getIntent();
        //获取交易类型和二维码地址
        transType = intent.getStringExtra("transType");
        log.info("transType is :"+transType);
        strQRAddress = intent.getStringExtra("qrAddress");
        log.info("strQRAddress is :"+strQRAddress);
        strPrivateKey = intent.getStringExtra("privateKey");
        log.info("strPrivateKey is :"+strPrivateKey);
        coinAmount = intent.getIntExtra("coinAmount",0);
        log.info("coinAmount is :" + coinAmount);
        strBitcoinAmount  = intent.getStringExtra("bitcoinAmount");
        log.info("strBitcoinAmount is :"+strBitcoinAmount);

        String[] strs = strBitcoinAmount.split("\\.");
        log.info("strs'length is "+strs.length);
        if(strs.length == 2){

            String str2 = strs[1];
            int length = str2.length();
            if(length <5){
                int j = 5-length;
                for(int i=0;i<j;i++)
                    strBitcoinAmount += "0";
            }
        }
        log.info("newStrBitcoinAmount is :"+strBitcoinAmount);
        bitcoinAmount = new BigInteger(strBitcoinAmount.replace(".",""));
        log.info("transbitcoinAmount is :"+bitcoinAmount);

        try {
            paymentIntent = PaymentIntent.fromAddress(strQRAddress, null);
        } catch (WrongNetworkException wrongNetworkException) {
            wrongNetworkException.printStackTrace();
        } catch (AddressFormatException addressFormatException) {
            addressFormatException.printStackTrace();
        }


        //把比特币发送到指定的二维码地址
      handlePayment(strQRAddress,bitcoinAmount);

    }
    private void startSuccessActivity(){

        final Intent result = new Intent(TransProgressActivity.this,CopyOfTransSuccessActivity.class);

        result.putExtra("transType",transType);
        result.putExtra("qrAddress",strQRAddress);
        result.putExtra("privateKey",strPrivateKey);
        result.putExtra("coinAmount",coinAmount);
        result.putExtra("bitcoinAmount",config.getBtcPrefix()+strBitcoinAmount);
    
        TransProgressActivity.this.startActivity(result);
        TransProgressActivity.this.finish();
    }
    private void startFailedActivity(){

        Intent intent = new Intent(TransProgressActivity.this,CopyOfTransFailedActivity.class);
        intent.putExtra("transType",transType);
        intent.putExtra("qrAddress",strQRAddress);
        intent.putExtra("coinAmount",coinAmount);
        intent.putExtra("bitcoinAmount",config.getBtcPrefix()+strBitcoinAmount);
    
        TransProgressActivity.this.startActivity(intent);
        TransProgressActivity.this.finish();
    }
    private final TransactionConfidence.Listener sentTransactionConfidenceListener = new TransactionConfidence.Listener()
    {
        @Override
        public void onConfidenceChanged(final Transaction tx, final TransactionConfidence.Listener.ChangeReason reason)
        {
            TransProgressActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
//                    sentTransactionListAdapter.notifyDataSetChanged();

                    final TransactionConfidence confidence = sentTransaction.getConfidence();
                    final ConfidenceType confidenceType = confidence.getConfidenceType();
                    final int numBroadcastPeers = confidence.numBroadcastPeers();

                    if (state == State.SENDING)
                    {
                        if (confidenceType == ConfidenceType.DEAD)
                            state = State.FAILED;
                        else if (numBroadcastPeers > 1 || confidenceType == ConfidenceType.BUILDING)
                            state = State.SENT;


                    }

                    if (reason == ChangeReason.SEEN_PEERS && confidenceType == ConfidenceType.PENDING)
                    {
                        // play sound effect
                        final int soundResId = getResources().getIdentifier("send_coins_broadcast_" + numBroadcastPeers, "raw",
                                TransProgressActivity.this.getPackageName());
                        if (soundResId > 0)
                            RingtoneManager.getRingtone(TransProgressActivity.this, Uri.parse("android.resource://" + TransProgressActivity.this.getPackageName() + "/" + soundResId))
                                    .play();
                    }
                }
            });
        }
    };
    private void handlePayment(String strAddress,BigInteger amount)
    {
        Address address = null;
        try {
            address = new Address(Constants.NETWORK_PARAMETERS
                    , strAddress);


            state = State.PREPARATION;

            // final payment intent
            final PaymentIntent finalPaymentIntent = paymentIntent.mergeWithEditedValues(amount,address);
            final BigInteger finalAmount = finalPaymentIntent.getAmount();
            log.info("finalAmount is :"+finalAmount.toString());

            // prepare send request
            final SendRequest sendRequest = finalPaymentIntent.toSendRequest();
            final Address returnAddress = WalletUtils.pickOldestKey(wallet).toAddress(Constants.NETWORK_PARAMETERS);
            sendRequest.changeAddress = returnAddress;//log.info("returnAddress is :"+returnAddress.toString());
            sendRequest.emptyWallet = paymentIntent.mayEditAmount() && finalAmount.equals(wallet.getBalance(BalanceType.AVAILABLE));
            //sendRequest.ensureMinRequiredFee = true;//TODO
            log.info("ensureMinRequiredFee is :"+sendRequest.ensureMinRequiredFee);
            log.info("emptyWallet is :"+sendRequest.emptyWallet);

            new SendCoinsOfflineTask(wallet, backgroundHandler)
            {
                @Override
                protected void onSuccess(final Transaction transaction)
                {
                    sentTransaction = transaction;

                    state = State.SENDING;


                    sentTransaction.getConfidence().addEventListener(sentTransactionConfidenceListener);

                    final Payment payment = PaymentProtocol.createPaymentMessage(sentTransaction, returnAddress, finalAmount, null,
                            paymentIntent.payeeData);

                    directPay(payment);
                    application.broadcastTransaction(sentTransaction);

                    log.info("Transaction successed.");
                    TransProgressActivity.this.longToast(R.string.send_coins_success_msg);

                    mHandler.sendEmptyMessageDelayed(500, 10*1000);
                  

                }

                private void directPay(final Payment payment)
                {
    //                if (directPaymentEnableView.isChecked()){
                        final DirectPaymentTask.ResultCallback callback = new DirectPaymentTask.ResultCallback()
                        {
                            @Override
                            public void onResult(final boolean ack)
                            {
                                directPaymentAck = ack;

                                if (state == State.SENDING)
                                    state = State.SENT;
                                log.info("Direct payment successed.");
                            }

                            @Override
                            public void onFail(final int messageResId, final Object... messageArgs)
                            {
                                final DialogBuilder dialog = DialogBuilder.warn(TransProgressActivity.this, R.string.send_coins_fragment_direct_payment_failed_title);
                                dialog.setMessage(paymentIntent.paymentUrl + "\n" + getString(messageResId, messageArgs) + "\n\n"
                                        + getString(R.string.send_coins_fragment_direct_payment_failed_msg));
                                dialog.setPositiveButton(R.string.button_retry, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(final DialogInterface dialog, final int which)
                                    {
                                        directPay(payment);
                                    }
                                });
                                dialog.setNegativeButton(R.string.button_dismiss, null);
                                dialog.show();
                            }
                        };

                        if (paymentIntent.isHttpPaymentUrl())
                        {
                            new DirectPaymentTask.HttpPaymentTask(backgroundHandler, callback, paymentIntent.paymentUrl, application.httpUserAgent())
                                    .send(payment);
                        }
                        else if (paymentIntent.isBluetoothPaymentUrl() && bluetoothAdapter != null && bluetoothAdapter.isEnabled())
                        {
                            new DirectPaymentTask.BluetoothPaymentTask(backgroundHandler, callback, bluetoothAdapter, paymentIntent.getBluetoothMac())
                                    .send(payment);
                        }
    //                }
                }

                @Override
                protected void onInsufficientMoney(@Nullable final BigInteger missing)
                {
                    state = State.INPUT;

                    final BigInteger estimated = wallet.getBalance(BalanceType.ESTIMATED);
                    final BigInteger available = wallet.getBalance(BalanceType.AVAILABLE);
                    final BigInteger pending = estimated.subtract(available);

                    final int btcShift = config.getBtcShift();
                    final int btcPrecision = config.getBtcMaxPrecision();
                    final String btcPrefix = config.getBtcPrefix();

                    final DialogBuilder dialog = DialogBuilder.warn(TransProgressActivity.this, R.string.send_coins_fragment_insufficient_money_title);
                    final StringBuilder msg = new StringBuilder();
                    if (missing != null)
                        msg.append(
                                String.format(getString(R.string.send_coins_fragment_insufficient_money_msg1),
                                        btcPrefix + ' ' + GenericUtils.formatValue(missing, btcPrecision, btcShift))).append("\n\n");
                    if (pending.signum() > 0)
                        msg.append(getString(R.string.send_coins_fragment_pending, GenericUtils.formatValue(pending, btcPrecision, btcShift))).append(
                                "\n\n");
                    msg.append(getString(R.string.send_coins_fragment_insufficient_money_msg2));
                    dialog.setMessage(msg);
                   /* dialog.setPositiveButton(R.string.send_coins_options_empty, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which)
                        {
    //                        handleEmpty();
                        }
                    });*/
                    dialog.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(final DialogInterface dialog,final int which){


                            startFailedActivity();

                        }
                    });
                    dialog.show();
                }

                @Override
                protected void onFailure()
                {
                    state = State.FAILED;
                    TransProgressActivity.this.longToast(R.string.send_coins_error_msg);


                    startFailedActivity();

                }
            }.sendCoinsOffline(sendRequest); // send asynchronously
        } catch (AddressFormatException e) {
            e.printStackTrace();
            startFailedActivity();
        }
    }

    private final DialogInterface.OnClickListener activityDismissListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
            TransProgressActivity.this.finish();
        }
    };
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
//                    long sysTime = System.currentTimeMillis();
//                    CharSequence sysTimerStr =  android.text.format.DateFormat.format("yyyy年MM月dd日 hh:mm:ss",sysTime);
//                    mTime.setText(sysTimerStr);
//                    log.info("time2："+sysTimerStr);
//                    if((sysTime - beginTime) > 60000)
//                        startActivity(new Intent(TransProgressActivity.this,ExchangeRatesActivity.class));
                    break;
                case 500:
                	startSuccessActivity();
                break;
                default:
                    break;
            }
        }
    };
}
