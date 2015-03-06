/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.bitcoin.core.Wallet.BalanceType;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.SettingInfo;
import com.mybitcoin.wallet.environment.UiInfo;
import com.mybitcoin.wallet.util.GenericUtils;

public class TradeModeActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "TradeModeActivity";
    private static final boolean DEBUG_FLAG = true;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.trade_mode);

        TextView tmBitcoinBtn = (TextView) findViewById(R.id.trademode_bitcoin_btn);
        TextView tmBillBtn = (TextView) findViewById(R.id.trademode_cash_btn);
        TextView tmQrBtn = (TextView) findViewById(R.id.trademode_qr_btn);
        TextView tmCancel = (TextView) findViewById(R.id.trademode_cancel_btn);

        tmBitcoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                checkbitcoin();
            }
        });

        tmBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    gotoActivity(CashTradeExchangeRatesActivity.class);
            }
        });

        tmQrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(CashTradeShowScanQrActivity.class);
            }
        });
        
        tmCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	 gotoActivity(WelcomePageActivity.class);
            }
        });

//        // test
//        TextView tmKycBtn = (TextView) findViewById(R.id.trademode_kyc_btn);
//        //tmKycBtn.setVisibility(View.INVISIBLE);
//        tmKycBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                gotoActivity(CashTradeKycLoginActivity.class);
//            }
//        });
    }
    
    private void checkbitcoin(){
        SettingInfo setting = new SettingInfo(this);
    	BigInteger balance = getWalletApplication().getWallet().getBalance(BalanceType.AVAILABLE);
    	String formattedAvailableBalanceStr = GenericUtils.formatValue(balance, 8, 0);
        if(Double.valueOf(formattedAvailableBalanceStr) <= setting.getBalanceWarningThreshold()){
			TextView text = new TextView(
					this);
			text.setText("Sorry BTM cannot serve you now");//
			text.setBackgroundColor(Color.BLACK);
			text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
			Toast toast = new Toast(this);
			toast.setView(text);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
        }else{
        	gotoActivity(ExchangeRatesActivity.class);
        }
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView title = (TextView) findViewById(R.id.trademode_title);
        TextView btc = (TextView) findViewById(R.id.trademode_bitcoin_btn);
        TextView cash = (TextView) findViewById(R.id.trademode_cash_btn);
        TextView qr = (TextView) findViewById(R.id.trademode_qr_btn);
        TextView tmCancel = (TextView) findViewById(R.id.trademode_cancel_btn);
        
        title.setText(getUiInfo().getTextByName(UiInfo.TRADEMODE_TITLE));
        btc.setText(getUiInfo().getTextByName(UiInfo.TRADEMODE_BTC_BTN));
        cash.setText(getUiInfo().getTextByName(UiInfo.TRADEMODE_CASH_BTN));
        qr.setText(getUiInfo().getTextByName(UiInfo.TRADEMODE_QR_BTN));
        tmCancel.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
        
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
