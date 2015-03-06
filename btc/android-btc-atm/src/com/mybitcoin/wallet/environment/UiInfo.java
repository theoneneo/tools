/**
 *  AUTHOR: F
 *  DATE: 2014.6.10
 */

package com.mybitcoin.wallet.environment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.mybitcoin.wallet.ui.WalletActivityBase;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

public class UiInfo {
    private static final String LOG_TAG = "UiInfo";
    private static final boolean DEBUG_FLAG = false;

    // 初始化标记
    private static final String TAG = "tag";
    private static final String TAG_DEFAULT_VALUE = "0";

    private Context mActivity;
    private SharedPreferences mSharedPref;
    private String mTagValue;

    // 当前的Activity
    private static WalletActivityBase mCurrActivity;

    // Common
    public static final String COMMON_PRV_BTN = "common.prv_btn";
    public static final String COMMON_NXT_BTN = "common.nxt_btn";
    public static final String COMMON_CFM_BTN = "common.cfm_btn";
    public static final String COMMON_CNL_BTN = "common.cnl_btn";
    public static final String COMMON_FNS_BTN = "common.fns_btn";
    public static final String COMMON_BTC_ABB = "common.btc_abb";
    public static final String COMMON_CASH_ABB = "common.cash_abb";
    public static final String COMMON_RATE_ABB = "common.rate_abb";
    public static final String COMMON_TIMEOUT_TITLE = "common.timeout_title";
    public static final String COMMON_TRADETYPE_CASH = "common.tradetype_cash";

    private void setDefaultUiInfoForCommon() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(COMMON_PRV_BTN, "上一步");
        edit.putString(COMMON_NXT_BTN, "下一步");
        edit.putString(COMMON_CFM_BTN, "确认");
        edit.putString(COMMON_CNL_BTN, "取消");
        edit.putString(COMMON_FNS_BTN, "完成");
        edit.putString(COMMON_BTC_ABB, "BTC");
        edit.putString(COMMON_CASH_ABB, "RMB");
        edit.putString(COMMON_RATE_ABB, "RMB/BTC");
        edit.putString(COMMON_TIMEOUT_TITLE, "Remaining operating time: ");
        edit.putString(COMMON_TRADETYPE_CASH, "卖出比特币");

        edit.commit();
    }

    // WelcomePageActivity
    public static final String WELCOMEPAGE_TITLE = "welcomepage.title";
    public static final String WELCOMEPAGE_HINT = "welcomepage.hint";

    private void setDefaultUiInfoForWelcomePage() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(WELCOMEPAGE_TITLE, "欢迎使用\n比特币自助售卖机");
        edit.putString(WELCOMEPAGE_HINT, "点击屏幕，开始交易");

        edit.commit();
    }

    // TradeModeActivity
    public static final String TRADEMODE_TITLE = "trademode.title";
    public static final String TRADEMODE_BTC_BTN = "trademode.bitcoin_btn";
    public static final String TRADEMODE_CASH_BTN = "trademode.cash_btn";
    public static final String TRADEMODE_QR_BTN = "trademode.qr_btn";

    private void setDefaultUiInfoForTradeMode() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(TRADEMODE_TITLE, "请选择交易模式");
        edit.putString(TRADEMODE_BTC_BTN, "购买比特币");
        edit.putString(TRADEMODE_CASH_BTN, "卖出比特币");
        edit.putString(TRADEMODE_QR_BTN, "扫描提款二维码");

        edit.commit();
    }

    
    
    // CashTradeExchageRateActivity
    public static final String CASHTRADE_EXCHANGERATE_TITLE = "cashtrade.exchangerate.title";
    public static final String CASHTRADE_EXCHANGERATE_HINT = "cashtrade.exchangerate.hint";

    private void setDefaultUiInfoForCashTradeExchangeRate() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_EXCHANGERATE_TITLE, "当前汇率");
        edit.putString(CASHTRADE_EXCHANGERATE_HINT, "汇率更新完毕后，点击下一步继续交易");

        edit.commit();
    }

    // CashTradeInputActivity
    public static final String CASHTRADE_INPUT_TITLE = "cashtrade.input.title";
    public static final String CASHTRADE_INPUT_HINT = "cashtrade.input.hint";
    public static final String CASHTRADE_INPUT_EXCEED = "cashtrade.input.exceed";
    public static final String CASHTRADE_INPUT_OVERFLOW = "cashtrade.input.overflow";
    public static final String CASHTRADE_INPUT_VALID = "cashtrade.input.valid";

    private void setDefaultUiInfoForCashTradeInput() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_INPUT_TITLE, "请输入卖出比特币数额");
        edit.putString(CASHTRADE_INPUT_HINT, "本机仅支持100元面额现金兑换");
        edit.putString(CASHTRADE_INPUT_EXCEED, "超出交易限额，请修改输入金额");
        edit.putString(CASHTRADE_INPUT_OVERFLOW, "本机现金余额不足，请修改输入金额");
        edit.putString(CASHTRADE_INPUT_VALID, "数额验证通过，请点击下一步继续交易");

        edit.commit();
    }

    // CashTradeRequestCoinActivity
    public static final String CASHTRADE_REQUESTCOIN_TITLE = "cashtrade.requestcoin.title";
    public static final String CASHTRADE_REQUESTCOIN_HINT = "cashtrade.requestcoin.hint";
    public static final String CASHTRADE_REQUESTCOIN_BTC_TITLE = "cashtrade.requestcoin.btc_title";
    public static final String CASHTRADE_REQUESTCOIN_CASH_TITLE = "cashtrade.requestcoin.cash_title";

    private void setDefaultUiInfoForCashTradeRequestCoin() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_REQUESTCOIN_TITLE, "支付比特币");
        edit.putString(CASHTRADE_REQUESTCOIN_HINT, "请扫描二维码地址转账");
        edit.putString(CASHTRADE_REQUESTCOIN_BTC_TITLE, "支付比特币：");
        edit.putString(CASHTRADE_REQUESTCOIN_CASH_TITLE, "兑换现金：");

        edit.commit();
    }

    // CashTradeCheckPaymentActivity
    public static final String CASHTRADE_CHECKPAYMENT_TITLE = "cashtrade.checkpayment.title";
    public static final String CASHTRADE_CHECKPAYMENT_HINT = "cashtrade.checkpayment.hint";

    private void setDefaultUiInfoForCashTradeCheckPayment() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_CHECKPAYMENT_TITLE, "系统正在确认您的支付");
        edit.putString(CASHTRADE_CHECKPAYMENT_HINT, "请稍等");

        edit.commit();
    }

    // CashTradeCheckPaymentTimeoutActivity
    public static final String CASHTRADE_CHECKPAYMENTTIMEOUT_TITLE = "cashtrade.checkpaymenttimeout.title";
    public static final String CASHTRADE_CHECKPAYMENTTIMEOUT_HINT = "cashtrade.checkpaymenttimeout.hint";

    private void setDefaultUiInfoForCashTradeCheckPaymentTimeout() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_CHECKPAYMENTTIMEOUT_TITLE, "未收到您的转账交易广播，转账失败");
        edit.putString(CASHTRADE_CHECKPAYMENTTIMEOUT_HINT, "请联系客服:010-88888888");

        edit.commit();
    }

    // CashTradeShowCashQrActivity
    public static final String CASHTRADE_SHOWCASHQR_TITLE = "cashtrade.showcashqr.title";
    public static final String CASHTRADE_SHOWCASHQR_HINT1 = "cashtrade.showcashqr.hint1";
    public static final String CASHTRADE_SHOWCASHQR_HINT2 = "cashtrade.showcashqr.hint2";
    public static final String CASHTRADE_SHOWCASHQR_BTC_TITLE = "cashtrade.showcashqr.btc_title";
    public static final String CASHTRADE_SHOWCASHQR_CASH_TITLE = "cashtrade.showcashqr.cash_title";

    private void setDefaultUiInfoForCashTradShowCashQr() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_SHOWCASHQR_TITLE, "提款信息");
        edit.putString(CASHTRADE_SHOWCASHQR_HINT1, "请妥善保管提款二维码");
        edit.putString(CASHTRADE_SHOWCASHQR_HINT2, "请于约20分钟后凭二维码提取现金，您的等待对于我们很重要");
        edit.putString(CASHTRADE_SHOWCASHQR_BTC_TITLE, "支付比特币：");
        edit.putString(CASHTRADE_SHOWCASHQR_CASH_TITLE, "兑换现金：");

        edit.commit();
    }

    // CashTradeShowScanQrActivity
    public static final String CASHTRADE_SHOWSCANQR_TITLE = "cashtrade.showscanqr.title";
    public static final String CASHTRADE_SHOWSCANQR_HINT_DEFAULT = "cashtrade.showscanqr.hint_default";
    public static final String CASHTRADE_SHOWSCANQR_HINT_UNCONFIRMED = "cashtrade.showscanqr.hint_unconfirmed";
    public static final String CASHTRADE_SHOWSCANQR_HINT_UNKNOWN = "cashtrade.showscanqr.hint_unknown";
    public static final String CASHTRADE_SHOWSCANQR_HINT_FORBIDDEN = "cashtrade.showscanqr.hint_forbidden";
    public static final String CASHTRADE_SHOWSCANQR_RESCAN_BTN = "cashtrade.showscanqr.rescan_btn";

    private void setDefaultUiInfoForCashTradShowScanQr() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_SHOWSCANQR_TITLE, "验证提款二维码");
        edit.putString(CASHTRADE_SHOWSCANQR_HINT_DEFAULT, "请等待");
        edit.putString(CASHTRADE_SHOWSCANQR_HINT_UNCONFIRMED, "您的交易尚未确认完毕，请耐心等待");
        edit.putString(CASHTRADE_SHOWSCANQR_HINT_UNKNOWN, "二维码错误，交易不存在");
        edit.putString(CASHTRADE_SHOWSCANQR_HINT_FORBIDDEN, "交易状态非法，禁止交易");
        edit.putString(CASHTRADE_SHOWSCANQR_RESCAN_BTN, "重新扫描");

        edit.commit();
    }

    // CashTradeCheckingOutctivity
    public static final String CASHTRADE_CHECKINGOUT_TITLE = "cashtrade.checkingout.title";
    public static final String CASHTRADE_CHECKINGOUT_HINT = "cashtrade.checkingout.hint";
    public static final String CASHTRADE_CHECKINGOUT_BTC_TITLE = "cashtrade.checkingout.btc_title";
    public static final String CASHTRADE_CHECKINGOUT_CASH_TITLE = "cashtrade.checkingout.cash_title";
    public static final String CASHTRADE_CHECKINGOUT_RATE_TITLE = "cashtrade.checkingout.rate_title";
    public static final String CASHTRADE_CHECKINGOUT_CHECKOUT = "cashtrade.checkingout.checkout";

    private void setDefaultUiInfoForCashTradeCheckingOut() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_CHECKINGOUT_TITLE, "交易明细");
        edit.putString(CASHTRADE_CHECKINGOUT_HINT, "请点击“提款”按钮提取现金");
        edit.putString(CASHTRADE_CHECKINGOUT_BTC_TITLE, "卖出比特币：");
        edit.putString(CASHTRADE_CHECKINGOUT_CASH_TITLE, "兑换现金：");
        edit.putString(CASHTRADE_CHECKINGOUT_RATE_TITLE, "交易汇率：");
        edit.putString(CASHTRADE_CHECKINGOUT_CHECKOUT, "提款");

        edit.commit();
    }

    // CashTradeCashOutActivity
    public static final String CASHTRADE_CASHOUT_TITLE = "cashtrade.cashout.title";

    private void setDefaultUiInfoForCashTradeCashOut() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_CASHOUT_TITLE, "正在出钞");

        edit.commit();
    }

    // CashTradeSuccessActivity
    public static final String CASHTRADE_SUCCESS_TITLE = "cashtrade.success.title";
    public static final String CASHTRADE_SUCCESS_BTC_TITLE = "cashtrade.success.btc_title";
    public static final String CASHTRADE_SUCCESS_CASH_TITLE = "cashtrade.success.cash_title";
    public static final String CASHTRADE_SUCCESS_RATE_TITLE = "cashtrade.success.rate_title";
    public static final String CASHTRADE_SUCCESS_SMS_TEMPLATE = "cashtrade.success.sms_template";

    private void setDefaultUiInfoForCashTradeSuccess() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_SUCCESS_TITLE, "交易成功");
        edit.putString(CASHTRADE_SUCCESS_BTC_TITLE, "成功卖出：");
        edit.putString(CASHTRADE_SUCCESS_CASH_TITLE, "成功兑换：");
        edit.putString(CASHTRADE_SUCCESS_RATE_TITLE, "交易汇率：");
        edit.putString(CASHTRADE_SUCCESS_SMS_TEMPLATE, "贵平台<atm_name>于<time>发生一笔卖出比特币交易，交易额为卖出比特币<btc_amount>，兑换人民币<cash_amount>，即时汇率为<exchange_rate>，手续费率为<handling_charge_proportion>。");

        edit.commit();
    }

    // CashTradeFailureActivity
    public static final String CASHTRADE_FAILURE_TITLE = "cashtrade.failure.title";
    public static final String CASHTRADE_FAILURE_HINT = "cashtrade.failure.hint";

    private void setDefaultUiInfoForCashTradeFailure() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_FAILURE_TITLE, "交易失败");
        edit.putString(CASHTRADE_FAILURE_HINT, "请取走交易失败凭条，并联系客服:010-88888888");

        edit.commit();
    }

    // CashTradeKycLoginActivity
    public static final String CASHTRADE_KYCLOGIN_NEWUSER_TITLE = "cashtrade.kyclogin.newuser_title";
    public static final String CASHTRADE_KYCLOGIN_NEWUSER_HINT = "cashtrade.kyclogin.newuser_hint";
    public static final String CASHTRADE_KYCLOGIN_TITLE = "cashtrade.kyclogin.title";
    public static final String CASHTRADE_KYCLOGIN_PHONE_TITLE = "cashtrade.kyclogin.phone_title";
    public static final String CASHTRADE_KYCLOGIN_SENDMSG_BTN = "cashtrade.kyclogin.sendmsg_btn";
    public static final String CASHTRADE_KYCLOGIN_VERI_TITLE = "cashtrade.kyclogin.veri_title";
    public static final String CASHTRADE_KYCLOGIN_REG_BTN = "cashtrade.kyclogin.reg_btn";
    public static final String CASHTRADE_KYCLOGIN_LOGIN_BTN = "cashtrade.kyclogin.login_btn";

    private void setDefaultUiInfoForCashTradeKycLogin() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_KYCLOGIN_NEWUSER_TITLE, "首次使用\n请点击“注册”按钮");
        edit.putString(CASHTRADE_KYCLOGIN_NEWUSER_HINT, "提示：根据法律要求，新用户注册需扫描ID卡或护照等带有照片的身份证件");
        edit.putString(CASHTRADE_KYCLOGIN_TITLE, "KYC登录");
        edit.putString(CASHTRADE_KYCLOGIN_PHONE_TITLE, "手机号：");
        edit.putString(CASHTRADE_KYCLOGIN_SENDMSG_BTN, "获取验证码");
        edit.putString(CASHTRADE_KYCLOGIN_VERI_TITLE, "验证码：");
        edit.putString(CASHTRADE_KYCLOGIN_REG_BTN, "注册");
        edit.putString(CASHTRADE_KYCLOGIN_LOGIN_BTN, "登录");

        edit.commit();
    }

    // Common UI settings for CashTradeKycRegister1Activity, CashTradeKycRegister2Activity and CashTradeKycRegister3Activity
    public static final String CASHTRADE_KYCREGISTER_STEP = "cashtrade.kycregister.step";
    public static final String CASHTRADE_KYCREGISTER_STEP1 = "cashtrade.kycregister.step1";
    public static final String CASHTRADE_KYCREGISTER_STEP2 = "cashtrade.kycregister.step2";
    public static final String CASHTRADE_KYCREGISTER_STEP3 = "cashtrade.kycregister.step3";

    private void setDefaultUiInfoForCashTradeKycRegister() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_KYCREGISTER_STEP, "用户注册步骤：");
        edit.putString(CASHTRADE_KYCREGISTER_STEP1, "Step1：头像采集");
        edit.putString(CASHTRADE_KYCREGISTER_STEP2, "Step2：证件扫描");
        edit.putString(CASHTRADE_KYCREGISTER_STEP3, "Step3：手机号码注册");

        edit.commit();
    }

    // CashTradeKycRegister1Activity
    public static final String CASHTRADE_KYCREGISTER1_TITLE = "cashtrade.kycregister1.title";
    public static final String CASHTRADE_KYCREGISTER1_RESCAN_BTN = "cashtrade.kycregister1.rescan_btn";

    private void setDefaultUiInfoForCashTradeKycRegister1() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_KYCREGISTER1_TITLE, "Step1：请面向摄像头采集您的头像");
        edit.putString(CASHTRADE_KYCREGISTER1_RESCAN_BTN, "重拍");

        edit.commit();
    }

    // CashTradeKycRegister2Activity
    public static final String CASHTRADE_KYCREGISTER2_TITLE = "cashtrade.kycregister2.title";
    public static final String CASHTRADE_KYCREGISTER2_RESCAN_BTN = "cashtrade.kycregister2.rescan_btn";

    private void setDefaultUiInfoForCashTradeKycRegister2() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_KYCREGISTER2_TITLE, "Step2：请扫描您的证件");
        edit.putString(CASHTRADE_KYCREGISTER2_RESCAN_BTN, "重拍");

        edit.commit();
    }

    // CashTradeKycRegister3Activity
    public static final String CASHTRADE_KYCREGISTER3_TITLE = "cashtrade.kycregister3.title";
    public static final String CASHTRADE_KYCREGISTER3_PHONE_TITLE = "cashtrade.kycregister3.phone_title";
    public static final String CASHTRADE_KYCREGISTER3_SENDMSG_BTN = "cashtrade.kycregister3.sendmsg_btn";
    public static final String CASHTRADE_KYCREGISTER3_VERI_TITLE = "cashtrade.kycregister3.veri_title";
    public static final String CASHTRADE_KYCREGISTER3_LOGIN_BTN = "cashtrade.kycregister3.login_btn";

    private void setDefaultUiInfoForCashTradeKycRegister3() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_KYCREGISTER3_TITLE, "Step3: Please register phone number");
        edit.putString(CASHTRADE_KYCREGISTER3_PHONE_TITLE, "手机号：");
        edit.putString(CASHTRADE_KYCREGISTER3_SENDMSG_BTN, "获取验证码");
        edit.putString(CASHTRADE_KYCREGISTER3_VERI_TITLE, "验证码：");
        edit.putString(CASHTRADE_KYCREGISTER3_LOGIN_BTN, "登录");

        edit.commit();
    }

    // CashTradeKycSuccessActivity
    public static final String CASHTRADE_KYCSUCCESS_TITLE = "cashtrade.kycsuccess.title";
    public static final String CASHTRADE_KYCSUCCESS_INFO_TITLE = "cashtrade.kycsuccess.hint";
    public static final String CASHTRADE_KYCSUCCESS_NAME_TITLE = "cashtrade.kycsuccess.phone_title";

    private void setDefaultUiInfoForCashTradeKycSuccess() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_KYCSUCCESS_TITLE, "您已通过KYC验证");
        edit.putString(CASHTRADE_KYCSUCCESS_INFO_TITLE, "您的个人信息如下：");
        edit.putString(CASHTRADE_KYCSUCCESS_NAME_TITLE, "姓名：");

        edit.commit();
    }

    // CashTradeKycFailureActivity
    public static final String CASHTRADE_KYCFAILURE_TITLE = "cashtrade.kycfailure.title";
    public static final String CASHTRADE_KYCFAILURE_RETRY_PREFIX = "cashtrade.kycfailure.retry_prefix";
    public static final String CASHTRADE_KYCFAILURE_RETRY_SUFFIX = "cashtrade.kycfailure.retry_suffix";
    public static final String CASHTRADE_KYCFAILURE_RETRY_BTN = "cashtrade.kycfailure.retry_btn";

    private void setDefaultUiInfoForCashTradeKycFailure() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(CASHTRADE_KYCFAILURE_TITLE, "KYC验证失败");
        edit.putString(CASHTRADE_KYCFAILURE_RETRY_PREFIX, "本日您还有");
        edit.putString(CASHTRADE_KYCFAILURE_RETRY_SUFFIX, "次尝试机会");
        edit.putString(CASHTRADE_KYCFAILURE_RETRY_BTN, "重新登录");

        edit.commit();
    }

    // TradePauseActivity
    public static final String TRADE_PAUSE_TITLE = "cashtrade.pause.title";

    private void setDefaultUiInfoForTradePause() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(TRADE_PAUSE_TITLE, "对不起，本机暂停交易");

        edit.commit();
    }
    
    
    public static final String cointrade_buycoinmode_title = "cointrade.buycoinmode.title";
    public static final String cointrade_buycoinmode_qrscan_btn = "cointrade.buycoinmode.qrscan_btn";
    public static final String cointrade_buycoinmode_paperwallet_btn = "cointrade.buycoinmode.paperwallet_btn";
    public static final String cointrade_displayqrcode_addr = "cointrade.displayqrcode.addr";
    public static final String cointrade_displayqrcode_qrcode = "cointrade.displayqrcode.qrcode";
    public static final String cointrade_inputcash_title = "cointrade.inputcash.title";
    public static final String cointrade_inputcash_currentcashtype = "cointrade.inputcash.currentcashtype";
    public static final String cointrade_inputcash_currentexchagerate = "cointrade.inputcash.currentexchagerate";
    public static final String cointrade_inputcash_cashvalue = "cointrade.inputcash.cashvalue";
    public static final String cointrade_inputcash_coinvalue = "cointrade.inputcash.coinvalue";
    public static final String cointrade_inputcash_walletaddr = "cointrade.inputcash.walletaddr";
    public static final String cointrade_tradesuccess_title = "cointrade.tradesuccess.title";
    public static final String cointrade_tradesuccess_currentcashtype = "cointrade.tradesuccess.currentcashtype";
    public static final String cointrade_tradesuccess_currentexchagerate = "cointrade.tradesuccess.currentexchagerate";
    public static final String cointrade_tradesuccess_cashvalue = "cointrade.tradesuccess.cashvalue";
    public static final String cointrade_tradesuccess_coinvalue = "cointrade.tradesuccess.coinvalue";
    public static final String cointrade_tradesuccess_security = "cointrade.tradesuccess.security";
    public static final String cointrade_failure_title = "cointrade.failure.title";
    public static final String cointrade_failure_hint = "cointrade.failure.hint";
    public static final String cointrade_tradesuccess_sms_template = "cointrade.tradesuccess.sms_template";

    private void setDefaultUiInfoForBuy() {
        SharedPreferences.Editor edit = mSharedPref.edit();

        edit.putString(cointrade_buycoinmode_title,"请选择提币方式");
        edit.putString(cointrade_buycoinmode_qrscan_btn,"扫描二维码");
        edit.putString(cointrade_buycoinmode_paperwallet_btn,"打印纸钱包");
        edit.putString(cointrade_displayqrcode_addr,"您的钱包地址是");
        edit.putString(cointrade_displayqrcode_qrcode, "钱包地址二维码");
        edit.putString(cointrade_inputcash_title, "请放入纸币");
        edit.putString(cointrade_inputcash_currentcashtype, "当前币种");
        edit.putString(cointrade_inputcash_currentexchagerate, "汇率");
        edit.putString(cointrade_inputcash_cashvalue, "接受的纸币总值:");
        edit.putString(cointrade_inputcash_coinvalue, "已兑换的比特币数量:");
        edit.putString(cointrade_inputcash_walletaddr, "你的比特币地址:");
        edit.putString(cointrade_tradesuccess_title, "交易成功！");
        edit.putString(cointrade_tradesuccess_currentcashtype, "当前币种");
        edit.putString(cointrade_tradesuccess_currentexchagerate, "汇率");
        edit.putString(cointrade_tradesuccess_cashvalue, "接受的纸币总值:");
        edit.putString(cointrade_tradesuccess_coinvalue, "已兑换的比特币数量:");
        edit.putString(cointrade_tradesuccess_security, "请注意安全！");
        edit.putString(cointrade_failure_title, "交易失败");
        edit.putString(cointrade_failure_hint, "请取走交易失败凭条，并联系客服:010-88888888");
        edit.putString(cointrade_tradesuccess_sms_template,"贵平台<atm_name>于<time>发生一笔买入比特币交易，交易额为买入比特币<btc_amount>，花费人民币<cash_amount>，即时汇率为<exchange_rate>，手续费率为<handling_charge_proportion>。");
        edit.commit();
    }
    
    public static final String appstart_skip_btn = "appstart.skip_btn";
    public static final String appstart_next_btn = "appstart.next_btn";
    public static final String appstart_welcometitle = "appstart.welcometitle";
    public static final String appstart_pagetitle_wificonfig = "appstart.pagetitle.wificonfig";
    public static final String appstart_wifissid = "appstart.wifissid";
    public static final String appstart_wifipassword = "appstart.wifipassword";
    public static final String appstart_pagetitle_timeconfig = "appstart.pagetitle.timeconfig";
    public static final String appstart_date = "appstart.date";
    public static final String appstart_time = "appstart.time";
    public static final String appstart_pagetitle_usernameconfig = "appstart.pagetitle.usernameconfig";
    public static final String appstart_username = "appstart.username";
    public static final String appstart_password = "appstart.password";
    public static final String appstart_pagetitle_keydisplay = "appstart.pagetitle.keydisplay";
    public static final String appstart_publickey = "appstart.publickey";
    public static final String appstart_privatekey = "appstart.privatekey";

    private void setDefaultUiInfoForAppstart() {
        SharedPreferences.Editor edit = mSharedPref.edit();
        edit.putString(appstart_skip_btn,"跳过");
        edit.putString(appstart_next_btn,"下一步");
        edit.putString(appstart_welcometitle,"欢迎使用比特币自动售卖机，请进行启动设置");
        edit.putString(appstart_pagetitle_wificonfig,"WIFI密码设置");
        edit.putString(appstart_wifissid,"SSID：");
        edit.putString(appstart_wifipassword,"WIFI密码：");
        edit.putString(appstart_pagetitle_timeconfig,"日期和时间设置");
        edit.putString(appstart_date,"日期：");
        edit.putString(appstart_time,"时间：");
        edit.putString(appstart_pagetitle_usernameconfig,"用户名和密码输入");
        edit.putString(appstart_username,"用户名：");
        edit.putString(appstart_password,"密码：");
        edit.putString(appstart_pagetitle_keydisplay,"显示公钥和私钥");
        edit.putString(appstart_publickey,"公钥：");
        edit.putString(appstart_privatekey,"私钥：");
        edit.commit();
    }

    private final String[] mKeyList = {TAG,

            COMMON_CFM_BTN,
            COMMON_CNL_BTN,
            COMMON_NXT_BTN,
            COMMON_PRV_BTN,
            COMMON_FNS_BTN,
            COMMON_BTC_ABB,
            COMMON_CASH_ABB,
            COMMON_RATE_ABB,
            COMMON_TIMEOUT_TITLE,
            COMMON_TRADETYPE_CASH,

            WELCOMEPAGE_TITLE,
            WELCOMEPAGE_HINT,

            TRADEMODE_TITLE,
            TRADEMODE_BTC_BTN,
            TRADEMODE_CASH_BTN,
            TRADEMODE_QR_BTN,

            CASHTRADE_EXCHANGERATE_TITLE,
            CASHTRADE_EXCHANGERATE_HINT,

            CASHTRADE_INPUT_TITLE,
            CASHTRADE_INPUT_HINT,
            CASHTRADE_INPUT_EXCEED,
            CASHTRADE_INPUT_OVERFLOW,
            CASHTRADE_INPUT_VALID,

            CASHTRADE_REQUESTCOIN_TITLE,
            CASHTRADE_REQUESTCOIN_HINT,
            CASHTRADE_REQUESTCOIN_BTC_TITLE,
            CASHTRADE_REQUESTCOIN_CASH_TITLE,

            CASHTRADE_CHECKPAYMENT_TITLE,
            CASHTRADE_CHECKPAYMENT_HINT,

            CASHTRADE_CHECKPAYMENTTIMEOUT_TITLE,
            CASHTRADE_CHECKPAYMENTTIMEOUT_HINT,

            CASHTRADE_SHOWCASHQR_TITLE,
            CASHTRADE_SHOWCASHQR_HINT1,
            CASHTRADE_SHOWCASHQR_HINT2,
            CASHTRADE_SHOWCASHQR_BTC_TITLE,
            CASHTRADE_SHOWCASHQR_CASH_TITLE,

            CASHTRADE_SHOWSCANQR_TITLE,
            CASHTRADE_SHOWSCANQR_HINT_DEFAULT,
            CASHTRADE_SHOWSCANQR_HINT_FORBIDDEN,
            CASHTRADE_SHOWSCANQR_HINT_UNCONFIRMED,
            CASHTRADE_SHOWSCANQR_HINT_UNKNOWN,
            CASHTRADE_SHOWSCANQR_RESCAN_BTN,

            CASHTRADE_CHECKINGOUT_TITLE,
            CASHTRADE_CHECKINGOUT_HINT,
            CASHTRADE_CHECKINGOUT_BTC_TITLE,
            CASHTRADE_CHECKINGOUT_CASH_TITLE,
            CASHTRADE_CHECKINGOUT_RATE_TITLE,
            CASHTRADE_CHECKINGOUT_CHECKOUT,

            CASHTRADE_CASHOUT_TITLE,

            CASHTRADE_SUCCESS_TITLE,
            CASHTRADE_SUCCESS_BTC_TITLE,
            CASHTRADE_SUCCESS_CASH_TITLE,
            CASHTRADE_SUCCESS_RATE_TITLE,

            CASHTRADE_FAILURE_TITLE,
            CASHTRADE_FAILURE_HINT,

            CASHTRADE_KYCLOGIN_NEWUSER_TITLE,
            CASHTRADE_KYCLOGIN_TITLE,
            CASHTRADE_KYCLOGIN_NEWUSER_HINT,
            CASHTRADE_KYCLOGIN_PHONE_TITLE,
            CASHTRADE_KYCLOGIN_SENDMSG_BTN,
            CASHTRADE_KYCLOGIN_VERI_TITLE,
            CASHTRADE_KYCLOGIN_REG_BTN,
            CASHTRADE_KYCLOGIN_LOGIN_BTN,

            CASHTRADE_KYCREGISTER_STEP,
            CASHTRADE_KYCREGISTER_STEP1,
            CASHTRADE_KYCREGISTER_STEP2,
            CASHTRADE_KYCREGISTER_STEP3,

            CASHTRADE_KYCREGISTER1_TITLE,
            CASHTRADE_KYCREGISTER1_RESCAN_BTN,

            CASHTRADE_KYCREGISTER2_TITLE,
            CASHTRADE_KYCREGISTER2_RESCAN_BTN,

            CASHTRADE_KYCREGISTER3_TITLE,
            CASHTRADE_KYCREGISTER3_PHONE_TITLE,
            CASHTRADE_KYCREGISTER3_SENDMSG_BTN,
            CASHTRADE_KYCREGISTER3_VERI_TITLE,
            CASHTRADE_KYCREGISTER3_LOGIN_BTN,

            CASHTRADE_KYCSUCCESS_TITLE,
            CASHTRADE_KYCSUCCESS_INFO_TITLE,
            CASHTRADE_KYCSUCCESS_NAME_TITLE,

            CASHTRADE_KYCFAILURE_TITLE,
            CASHTRADE_KYCFAILURE_RETRY_PREFIX,
            CASHTRADE_KYCFAILURE_RETRY_SUFFIX,
            CASHTRADE_KYCFAILURE_RETRY_BTN,

            TRADE_PAUSE_TITLE,
            
            
            cointrade_buycoinmode_title,
            cointrade_buycoinmode_qrscan_btn,
            cointrade_buycoinmode_paperwallet_btn,
            cointrade_displayqrcode_addr,
            cointrade_displayqrcode_qrcode,
            cointrade_inputcash_title,
            cointrade_inputcash_currentcashtype,
            cointrade_inputcash_currentexchagerate,
            cointrade_inputcash_cashvalue,
            cointrade_inputcash_coinvalue,
            cointrade_inputcash_walletaddr,
            cointrade_tradesuccess_title,
            cointrade_tradesuccess_currentcashtype,
            cointrade_tradesuccess_currentexchagerate,
            cointrade_tradesuccess_cashvalue,
            cointrade_tradesuccess_coinvalue,
            cointrade_tradesuccess_security,
            cointrade_failure_title,
            cointrade_failure_hint,
            cointrade_tradesuccess_sms_template,
            
            appstart_skip_btn,
            appstart_next_btn,
            appstart_welcometitle,
            appstart_pagetitle_wificonfig,
            appstart_wifissid,
            appstart_wifipassword,
            appstart_pagetitle_timeconfig,
            appstart_date,
            appstart_time,
            appstart_pagetitle_usernameconfig,
            appstart_username,
            appstart_password,
            appstart_pagetitle_keydisplay,
            appstart_publickey,
            appstart_privatekey

            };

    public UiInfo(@Nonnull Context activity) {
        mActivity = activity;
        mSharedPref = mActivity.getApplicationContext().getSharedPreferences("ui_info", Context.MODE_APPEND);
        setDefaultUiInfoForAppstart();
        if (mSharedPref.getString(TAG, null) == null) {
            setDefaultUiInfo();
        }

        mTagValue = null;

        dLog("UiInfo initialized");
    }

    private void setDefaultUiInfo() {
        // 添加所有页面
        setDefaultUiInfoForCommon();
        setDefaultUiInfoForWelcomePage();
        setDefaultUiInfoForTradeMode();
        setDefaultUiInfoForCashTradeFailure();
        setDefaultUiInfoForCashTradeExchangeRate();
        setDefaultUiInfoForCashTradeInput();
        setDefaultUiInfoForCashTradeRequestCoin();
        setDefaultUiInfoForCashTradeCheckPayment();
        setDefaultUiInfoForCashTradeCheckPaymentTimeout();
        setDefaultUiInfoForCashTradShowCashQr();
        setDefaultUiInfoForCashTradShowScanQr();
        setDefaultUiInfoForCashTradeCheckingOut();
        setDefaultUiInfoForCashTradeCashOut();
        setDefaultUiInfoForCashTradeSuccess();
        setDefaultUiInfoForCashTradeKycLogin();
        setDefaultUiInfoForCashTradeKycRegister();
        setDefaultUiInfoForCashTradeKycRegister1();
        setDefaultUiInfoForCashTradeKycRegister2();
        setDefaultUiInfoForCashTradeKycRegister3();
        setDefaultUiInfoForCashTradeKycSuccess();
        setDefaultUiInfoForCashTradeKycFailure();
        setDefaultUiInfoForTradePause();
        setDefaultUiInfoForBuy();
        setDefaultUiInfoForAppstart();
        
        SharedPreferences.Editor edit = mSharedPref.edit();
        edit.putString(TAG, TAG_DEFAULT_VALUE); // 默认标记
        edit.commit();
    }

    public String getTextByName(@Nonnull String viewName) {
        return mSharedPref.getString(viewName, null);
    }

    public boolean needUpdateUiInfo() {
        if (mTagValue == null) // 当UiInfo被创建时，mTagVaule为null，需要更新Ui
            return true;

        String newTag = mSharedPref.getString(TAG, null);    // newTag不为空
        if (mTagValue.equals(newTag))
            return false;

        return true;
    }

    public void updateUiInfoTag() {
        mTagValue = mSharedPref.getString(TAG, null);    // mTagValue不为空
    }

    public static void setCurrActivity(@Nonnull WalletActivityBase activity) {
        mCurrActivity = activity;
    }

    public void setUiInfoByJSON(@Nonnull JSONObject json) {
        SharedPreferences.Editor edit = mSharedPref.edit();

        for (String key : mKeyList) {
            try {
                edit.putString(key, json.getString(key));
                dLog("put key: " + key + ", value: " + json.getString(key) + "into mSharedPref");
            } catch (JSONException e) {
                dLog("error when put the value of key: " + key + " into mSharedPref, error: " + e.toString());
                continue;
            }
        }

        edit.commit();

        // 刷新当前Activity的UI，其它Activity将在被唤醒时在onResume中刷新UI
        if (!needUpdateUiInfo())
            return;

        ((Activity)mActivity).runOnUiThread(new Runnable() {
            public void run() {
                dLog("update ui info of current activity");
                if(mActivity instanceof WalletActivityBase){
                	((WalletActivityBase)mActivity).updateUiInfo();
                }
                
            }
        });
    }

    public static void setUiInfoByJSONStatic(@Nonnull JSONObject json) {
        if (mCurrActivity == null) {
            dLog("mCurrActivity is null, cancel setting ui info by json");
            return;
        }

        mCurrActivity.getUiInfo().setUiInfoByJSON(json);
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
            Log.d(LOG_TAG, logStr);
        }
    }
}
