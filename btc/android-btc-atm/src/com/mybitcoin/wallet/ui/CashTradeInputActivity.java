/**
 *  AUTHOR: F
 *  DATE: 2014.5.31
 */

package com.mybitcoin.wallet.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.mybitcoin.wallet.CashTradeInfoLog;
import com.mybitcoin.wallet.R;
import com.mybitcoin.wallet.environment.UiInfo;
import com.umeng.analytics.MobclickAgent;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;

public class CashTradeInputActivity extends WalletActivityTimeoutBase {
    private static final String LOG_TAG = "CashTradeInputActivity";
    private static final boolean DEBUG_FLAG = true;

    private int CASH_MIN_AMOUNT; // 现金最小单位

    private double mExchangeRate;
    private float mHandlingChargeProportion;

    private TextView cnlBtn;
    private TextView nxtBtn;
    private TextView incBtn;
    private TextView decBtn;
    private TextView vldText;
    private TextView amountCashText;
    private TextView amountBtcText;

    private String mExceed;
    private String mOverflow;
    private String mValid;
    private String mCashInput;
    private String mBtcCaculated;

    private CashTradeInfoLog mCashTradeInfoLog;

    @Override
    protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setLayout(R.layout.cash_trade_input);

        vldText = (TextView) findViewById(R.id.cashtrade_input_vld_txt);
        amountBtcText = (TextView) findViewById(R.id.cashtrade_input_btc_txt);
        amountCashText = (TextView) findViewById(R.id.cashtrade_input_cash_txt);

        cnlBtn = (TextView) findViewById(R.id.cashtrade_input_cnl_btn);
        cnlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoActivity(TradeModeActivity.class);
            }
        });

        nxtBtn = (TextView) findViewById(R.id.cashtrade_input_nxt_btn);
        nxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dLog("Cash: " + mCashInput + ", BTC: " + mBtcCaculated + " are preparing to send to CashTradeRequestCoinActivity");
                mCashTradeInfoLog.setInitingState(mBtcCaculated, mCashInput, null, null);
                dLog("Cash: " + mCashInput + ", BTC: " + mBtcCaculated + " are sending to CashTradeRequestCoinActivity");
                mCashTradeInfoLog.startActivityFromActivity(CashTradeInputActivity.this, CashTradeRequestCoinActivity.class);
            }
        });

        incBtn = (TextView) findViewById(R.id.cashtrade_input_inc_btn);
        incBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newAmount = Integer.parseInt(amountCashText.getText().toString()) + CASH_MIN_AMOUNT;
                // 先检查单次交易限额，再检查本机余额
                if (newAmount > getSettingInfo().getCashOutUpperLimit()) {
                    vldText.setText(mExceed);
                    nxtBtn.setVisibility(View.INVISIBLE);
                    incBtn.setVisibility(View.INVISIBLE);
                } else if (newAmount > getStoredCash()) {
                    vldText.setText(mOverflow);
                    nxtBtn.setVisibility(View.INVISIBLE);
                    incBtn.setVisibility(View.INVISIBLE);
                } else {
                    vldText.setText(mValid);
                    nxtBtn.setVisibility(View.VISIBLE);
                    incBtn.setVisibility(View.VISIBLE);
                    decBtn.setVisibility(View.VISIBLE);
                }

                // 对于单次交易限额和本机余额冲突情况，依然在页面显示Cash和BTC数额
                updateCashStrAndBtcStr(newAmount);
                amountCashText.setText(mCashInput);
                amountBtcText.setText(mBtcCaculated);
            }
        });

        decBtn = (TextView) findViewById(R.id.cashtrade_input_dec_btn);
        decBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newAmount = Integer.parseInt(amountCashText.getText().toString()) - CASH_MIN_AMOUNT;
                if (newAmount < CASH_MIN_AMOUNT) {
                    nxtBtn.setVisibility(View.INVISIBLE);
                    decBtn.setVisibility(View.INVISIBLE);
                } else if (newAmount == CASH_MIN_AMOUNT) {
                    updateCashStrAndBtcStr(newAmount);
                    amountCashText.setText(mCashInput);
                    amountBtcText.setText(mBtcCaculated);
                    decBtn.setVisibility(View.INVISIBLE);
                } else {
                    updateCashStrAndBtcStr(newAmount);
                    amountCashText.setText(mCashInput);
                    amountBtcText.setText(mBtcCaculated);
                    vldText.setText(mValid);
                    nxtBtn.setVisibility(View.VISIBLE);
                    incBtn.setVisibility(View.VISIBLE);
                    decBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        WalletActivityTimeoutController.getInstance().setTimeout(90);
        MobclickAgent.onResume(this);
        // 在此进入界面时，清空状态输入
        mExchangeRate = -1;
        mCashInput = null;
        mBtcCaculated = null;

        // 清空mCashTradeInfoLog
        mCashTradeInfoLog = null;

        amountCashText.setText("0"); // 初始化为0
        amountBtcText.setText("0"); // 初始化为0
        decBtn.setVisibility(View.INVISIBLE); // 初始化不显示“-”按钮
        nxtBtn.setVisibility(View.INVISIBLE); // 初始化不显示“下一步”按钮

        CASH_MIN_AMOUNT = getSettingInfo().getCashMinAmount(); // 在onResume中更新最小交易数额，以免正在进行的交易数据突然改变

        String tradeID = CashTradeInfoLog.getTradeIdFromIntent(this);
        if (tradeID == null) {
            dLog("the intent does not contain INTENT_EXTRA_TRADE_ID, go back to WelcomePage");
            gotoActivity(WelcomePageActivity.class);
        } else {
            mCashTradeInfoLog = new CashTradeInfoLog(getContentResolver(), tradeID);
            mExchangeRate = Double.valueOf(mCashTradeInfoLog.getExchangeRateStr());
            mHandlingChargeProportion = Float.valueOf(mCashTradeInfoLog.getHandlingChargeProportionStr());
            dLog("exchange rate: " + mExchangeRate + " received from CashTradeExchangeRatesActivity");
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    // TODO 获取机器现金余额
    private int getStoredCash() {
        return 1000000;
    }

    private void updateCashStrAndBtcStr(int input) {
        // 计算Cash输入所能兑换的BTC数额， x/input = 1/mExchangeRate
        double rst = (double) (input / mExchangeRate);

//        // 在计算结果中附加手续费
//        rst = rst * (1 + mHandlingChargeProportion);

        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(CashTradeInfoLog.BTC_FRACTION_PRECISION); // 调整最大小数位数
        String rstStr = formater.format(rst);

        //update mCashInput and mBtcCaculated
        mCashInput = String.valueOf(input);
        mBtcCaculated = rstStr;

        dLog("updateCashStrAndBtcStr: exchange rate: " + mExchangeRate + "input: " + input + ", rst: " + rst + ", rstStr:　" + rstStr);
    }

    @Override
    public void updateUiInfo() {
        super.updateUiInfo();

        TextView tradeType = (TextView) findViewById(R.id.cashtrade_input_tradetype);
        tradeType.setText(getUiInfo().getTextByName(UiInfo.COMMON_TRADETYPE_CASH));

        TextView title = (TextView) findViewById(R.id.cashtrade_input_title);
        TextView hint = (TextView) findViewById(R.id.cashtrade_input_hint);
        TextView cashAbb = (TextView) findViewById(R.id.cashtrade_input_cash_abb);
        TextView btcAbb = (TextView) findViewById(R.id.cashtrade_input_btc_abb);

        title.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_INPUT_TITLE));
        hint.setText(getUiInfo().getTextByName(UiInfo.CASHTRADE_INPUT_HINT));
        cashAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_CASH_ABB));
        btcAbb.setText(getUiInfo().getTextByName(UiInfo.COMMON_BTC_ABB));
        mExceed = getUiInfo().getTextByName(UiInfo.CASHTRADE_INPUT_EXCEED);
        mOverflow = getUiInfo().getTextByName(UiInfo.CASHTRADE_INPUT_OVERFLOW);
        mValid = getUiInfo().getTextByName(UiInfo.CASHTRADE_INPUT_VALID);
        cnlBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_CNL_BTN));
        nxtBtn.setText(getUiInfo().getTextByName(UiInfo.COMMON_NXT_BTN));
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
