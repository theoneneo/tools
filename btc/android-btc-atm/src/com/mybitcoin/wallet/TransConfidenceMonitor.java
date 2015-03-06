package com.mybitcoin.wallet;

import android.content.ContentResolver;
import android.util.Log;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionConfidence;
import com.google.bitcoin.core.TransactionConfidence.ConfidenceType;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.WalletUtils;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TransConfidenceMonitor extends Thread {
    private static final String LOG_TAG = "TransConfidenceMonitor";
    private static final boolean DEBUG_FLAG = true;

    private static int CONFIDENCE_CHECK_SEC = 30; // 每30s检测一次交易确认数
    private static int MIN_CONFIDENCE = 1; // 确认交易所需的最小confidence

    private Wallet mWallet;
    private ContentResolver mCr;

    public TransConfidenceMonitor(@Nonnull Wallet wallet, ContentResolver cr) {
        mWallet = wallet;
        mCr = cr;
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<Transaction> transList = mWallet.getTransactionsByTime();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                String dateStr = formatter.format(date);

                dLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!! CHECKING ATM WALLET !!!!!!!!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!! DATE: " + dateStr + " !!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                dLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                BigInteger balance = mWallet.getBalance(BalanceType.AVAILABLE);
                String formattedAvailableBalanceStr = GenericUtils.formatValue(balance, 8, 0); // 单位：比特币，精度：小数点后8位


                BigInteger balance1 = mWallet.getBalance(BalanceType.ESTIMATED);
                String formattedEstimatedBalanceStr = GenericUtils.formatValue(balance1, 8, 0); // 单位：比特币，精度：小数点后8位

                dLog("!!!ATM TOTAL BTC BALANCE!!!: ");
                dLog("\tAVAILABLE: " + formattedAvailableBalanceStr);
                dLog("\tESTIMATED: " + formattedEstimatedBalanceStr);

                dLog("");
                dLog("!!!TRANSACTIONS IN　WALLET!!!: ");
                dLog("");

                int count = 0;
                for (Transaction trans : transList) {
                    TransactionConfidence confidence = trans.getConfidence();
                    String transHashStr = trans.getHashAsString();
                    ConfidenceType confidenceType = confidence.getConfidenceType();
                    int depth = confidence.getDepthInBlocks();

                    Address payerAddr = WalletUtils.getFirstFromAddress(trans);
                    if(payerAddr == null)
                    	continue;
                    String payerAddrStr = payerAddr.toString();

                    final BigInteger btcAmount = trans.getValue(mWallet);
                    String formattedBtcAmountStr = GenericUtils.formatValue(btcAmount, 8, 0);	// 单位：比特币，精度：小数点后8位

                    dLog("");
                    dLog("");
                    dLog("\t----------------------------------------------------------");
                    dLog("\tTRANSACTION: " + count);
                    dLog("\t\tTransHash: " + transHashStr + ", Confidence Type: " + confidenceType.getValue() + ", Confidence Depth: " + depth + ", BTC: " + formattedBtcAmountStr + ", Payer Address: " + payerAddrStr);


                    String tradeID = CashTradeInfoLog.queryMatchedTradeIdByTransHash(mCr, transHashStr);
                    if (tradeID == null) { // 未找到匹配TradeID
                        dLog("");
                        dLog("\t\tThe transaction is an UNRECORDED transaction, it doesn't matched with any trades by TransHash");

//                        // 若未收到广播而钱包中显示未识别交易，则进行补救
//                        String newTradeID = CashTradeInfoLog.queryMatchedTradeIdByBTC(mCr, formattedBtcAmountStr);
//                        if(newTradeID == null) {
//                            dLog("\t\t\tit also doesn't match any INITED state Trades by Btc Amount");
//                        } else {
//                            dLog("\t\t\tit matches the INITED state trade with tradeID: " + tradeID + " by Btc Amount: " + formattedBtcAmountStr);
//                            CashTradeInfoLog tradeInfo = new CashTradeInfoLog(mCr, newTradeID);
//
//                            // 设置当前交易为PAYING状态，并填充支付者地址信息
//                            tradeInfo.setPayingState(transHashStr, payerAddrStr, null);
//                            dLog("\t\t\tSet PAYING state for it");
//                        }
                    } else { // 找到匹配的TradeID
                        dLog("\t\tThe transaction is an RECORDED transaction of tradeID: " + tradeID);
                        CashTradeInfoLog info = new CashTradeInfoLog(mCr, tradeID);
                        if (depth >= MIN_CONFIDENCE) {
                            if (info.getCurrentState().equals(CashTradeInfoLog.STATE_PAYING)) {
                                info.setPayedState();
                                dLog("\t\t\tSet PAYED state for it");
                            }
                        }

                        info.updateConfidenceDepth(depth);
                        dLog("\t\t\tUpdate new confidence depth: " + depth);
                    }

                    count++;

                    dLog("\t----------------------------------------------------------");
                    dLog("");
                    dLog("");
                }

                Thread.sleep(CONFIDENCE_CHECK_SEC * 1000);
            } catch (InterruptedException e) {
                // 调用end()方法后产生该异常，退出循环，结束线程，停止计时
                dLog("TransConfidenceMonitor is interupted by WelcomePageActivity");
                return;
            } catch(Exception ee){
            	
            }
        }
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
