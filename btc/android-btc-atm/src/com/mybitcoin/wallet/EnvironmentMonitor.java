package com.mybitcoin.wallet;

import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mybitcoin.wallet.environment.*;
import com.mybitcoin.wallet.util.WalletUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EnvironmentMonitor extends Thread {
    private static final String LOG_TAG = "EnvironmentMonitor";
    
    private static final boolean DEBUG_FLAG = false;

    private static final int MONITOR_CHECK_PERIOD_SEC = 10;        // 每10s检查各子线程状态

    public static final String URL_BASE = "http://dashboard.bitocean.com:8081";    // 基本Url

    private CpuInfoThread mCThread;
    private MemoryInfoThread mMThread;
    private NetworkInfoThread mNThread;
    private SettingInfoThread mSThread;
    private UiInfoThread mUThread;
    private TradeCmdThread mTThread;

    private SettingInfo mSettingInfo;
    private TradeCmd mTradeCmd;

    private String mAtmAddr;
    private RequestQueue mQueue;

    public EnvironmentMonitor(WalletApplication walletApp) {
        mAtmAddr = WalletUtils.pickOldestKey(walletApp.getWallet()).toAddress(Constants.NETWORK_PARAMETERS).toString();
        mQueue = Volley.newRequestQueue(walletApp.getApplicationContext());
        
        

        mCThread = new CpuInfoThread();
        mMThread = new MemoryInfoThread();
        mNThread = new NetworkInfoThread();
        mSThread = new SettingInfoThread();
        mUThread = new UiInfoThread();
        mTThread = new TradeCmdThread();


        mSettingInfo = new SettingInfo(walletApp.getApplicationContext());
        mTradeCmd = new TradeCmd(walletApp.getApplicationContext());
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (mCThread == null) {
                    mCThread.start();
                    dLog("Start CpuInfoThread");
                } else if (mCThread.isInterrupted() || !mCThread.isAlive()) {
                    mCThread.start();
                    dLog("Start CpuInfoThread");
                }

                if (mMThread == null) {
                    mMThread.start();
                    dLog("Start MemoryInfoThread");
                } else if (mMThread.isInterrupted() || !mMThread.isAlive()) {
                    mMThread.start();
                    dLog("Start MemoryInfoThread");
                }

                if (mNThread == null) {
                    mNThread.start();
                    dLog("Start NetworkInfoThread");
                } else if (mNThread.isInterrupted() || !mNThread.isAlive()) {
                    mNThread.start();
                    dLog("Start NetworkInfoThread");
                }

                if (mSThread == null) {
                    mSThread.start();
                    dLog("Start SettingInfoThread");
                } else if (mSThread.isInterrupted() || !mSThread.isAlive()) {
                    mSThread.start();
                    dLog("Start SettingInfoThread");
                }

                if (mUThread == null) {
                    mUThread.start();
                    dLog("Start UiInfoThread");
                } else if (mUThread.isInterrupted() || !mUThread.isAlive()) {
                    mUThread.start();
                    dLog("Start UiInfoThread");
                }

                if (mTThread == null) {
                    mTThread.start();
                    dLog("Start TradeCmdThread");
                } else if (mTThread.isInterrupted() || !mTThread.isAlive()) {
                    mTThread.start();
                    dLog("Start TradeCmdThread");
                }

                sleep(MONITOR_CHECK_PERIOD_SEC * 1000);
            }
        } catch (InterruptedException e) {
            dLog("EnvironmentMonitor: thread is interrupted");
        }
    }

    private class CpuInfoThread extends Thread {
        private String URL = URL_BASE + "/cpu_info/";
        private String ATM_NAME = "atm_name";
        private String TIME = "time";

        Response.Listener<JSONObject> rspListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dLog("CpuInfoThread: Response is " + response.toString());
            }
        };

        Response.ErrorListener errListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dLog("CpuInfoThread: " + error.toString());
            }
        };

        @Override
        public void run() {
            while (true) {
                float cpuUsage = CpuInfo.syncGetSystemCpuUsage(mSettingInfo.getCpuInfoPostPeriodSec() * 1000);
                // 若出错则睡眠一定时间间隔后重试
                if(cpuUsage < 0) {
                    try {
                        sleep(mSettingInfo.getSettingInfoGetPeriodSec() * 1000);
                    } catch (InterruptedException e) {
                        dLog("CpuInfoThread: thread is interrupted: " + e.toString());
                        continue; // TODO
                    }
                }

                try {
                    JSONObject postBody = new JSONObject();
                    postBody.put(ATM_NAME, mAtmAddr);
                    postBody.put(TIME, getCurrentDateAndTimeStr());
                    postBody.put(CpuInfo.CPU_USAGE, cpuUsage);
                    postBody.put(CpuInfo.TIME_PERIOD, mSettingInfo.getCpuInfoPostPeriodSec());

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, postBody, rspListener, errListener);
                    dLog(postBody.toString());
                    dLog(URL);

                    dLog("CpuInfoThread: POST is preparing to send to: " + URL + ", JSON: " + postBody.toString());
                    mQueue.add(request);
                } catch (JSONException e) {
                    dLog("CpuInfoThread: error when handling json");
                }
            }
        }
    }

    private class MemoryInfoThread extends Thread {
        private String URL = URL_BASE + "/memory_info/";
        private String ATM_NAME = "atm_name";
        private String TIME = "time";

        Response.Listener<JSONObject> rspListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dLog("MemoryInfoThread: Response is " + response.toString());
            }
        };

        Response.ErrorListener errListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dLog("MemoryInfoThread: " + error.toString());
            }
        };

        @Override
        public void run() {
            try {
                while (true) {

                    float memoryUsage = MemoryInfo.getSystemMemUsage();

                    try {
                        JSONObject postBody = new JSONObject();
                        postBody.put(ATM_NAME, mAtmAddr);
                        postBody.put(TIME, getCurrentDateAndTimeStr());
                        postBody.put(MemoryInfo.MEMORY_USAGE, memoryUsage);
                        postBody.put(MemoryInfo.TIME_PERIOD, mSettingInfo.getMemoryInfoPostPeriodSec());

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, postBody, rspListener, errListener);

                        dLog("MemoryInfoThread: POST is preparing to send to: " + URL + ", JSON: " + postBody.toString());
                        mQueue.add(request);
                    } catch (JSONException e) {
                        dLog("MemoryInfoThread: error when handling json");
                    }
                    sleep(mSettingInfo.getMemoryInfoPostPeriodSec() * 1000);
                }
            } catch (InterruptedException e) {
                dLog("MemoryInfoThread: thread is interrupted: " + e.toString());
            }
        }
    }

    private class NetworkInfoThread extends Thread {
        private String URL = URL_BASE + "/network_info/";
        private String ATM_NAME = "atm_name";
        private String TIME = "time";

        Response.Listener<JSONObject> rspListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dLog("NetworkInfoThread: Response is " + response.toString());
            }
        };

        Response.ErrorListener errListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dLog("NetworkInfoThread: " + error.toString());
            }
        };

        @Override
        public void run() {
            while (true) {
                float[] netThroughput = NetworkInfo.syncGetNetworkThroughput(mSettingInfo.getNetworkInterface(), mSettingInfo.getNetworkInfoPostPeriodSec() * 1000);

                // 若出错则睡眠一定时间间隔后重试
                boolean sleepFlag = false;
                for(float tp : netThroughput) {
                    if(tp < 0) {
                        try {
                            sleep(mSettingInfo.getSettingInfoGetPeriodSec() * 1000);
                        } catch (InterruptedException e) {
                            dLog("NetworkInfoThread: thread is interrupted: " + e.toString());
                        }
                        sleepFlag = true;
                        break;
                    }
                }
                if(sleepFlag)
                    continue; // 回到循环开始

                try {

                    JSONObject postBody = new JSONObject();
                    postBody.put(ATM_NAME, mAtmAddr);
                    postBody.put(TIME, getCurrentDateAndTimeStr());
                    postBody.put(NetworkInfo.INTERFACE, NetworkInfo.INTERFACE_WIRELESS0);
                    postBody.put(NetworkInfo.THROUGHPUT_TX, netThroughput[0]);
                    postBody.put(NetworkInfo.THROUGHPUT_RX, netThroughput[1]);
                    postBody.put(NetworkInfo.TIME_PERIOD, mSettingInfo.getNetworkInfoPostPeriodSec());

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, postBody, rspListener, errListener);

                    dLog("NetworkInfoThread: POST is preparing to send to: " + URL + ", JSON: " + postBody.toString());
                    mQueue.add(request);
                } catch (JSONException e) {
                    dLog("NetworkInfoThread: error when handling json");
                }
            }
        }
    }

    private class SettingInfoThread extends Thread {
        private String URL = URL_BASE + "/setting_info/" + mAtmAddr + "/";

        Response.Listener<JSONObject> rspListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dLog("SettingInfoThread: Response is " + response.toString());
                mSettingInfo.setSettingInfoByJSON(response);
            }
        };

        Response.ErrorListener errListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dLog("SettingInfoThread: " + error.toString());
            }
        };

        @Override
        public void run() {
            try {
                while (true) {
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, rspListener, errListener);

                    dLog("SettingInfoThread: GET is preparing to send to: " + URL);
                    mQueue.add(request);

                    sleep(mSettingInfo.getSettingInfoGetPeriodSec() * 1000);
                }
            } catch (InterruptedException e) {
                dLog("SettingInfoThread: thread is interrupted: " + e.toString());
            }
        }
    }

    private class UiInfoThread extends Thread {
        private String URL = URL_BASE + "/ui_info/" + mAtmAddr + "/";

        Response.Listener<JSONObject> rspListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dLog("UiInfoThread: Response is " + response.toString());
                UiInfo.setUiInfoByJSONStatic(response);
            }
        };

        Response.ErrorListener errListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dLog("UiInfoThread: " + error.toString());
            }
        };

        @Override
        public void run() {
            try {
                while (true) {
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, rspListener, errListener);

                    dLog("UiInfoThread: GET is preparing to send to: " + URL);

                    mQueue.add(request);

                    sleep(mSettingInfo.getUiInfoGetPeriodSec() * 1000);
                }
            } catch (InterruptedException e) {
                dLog("UiInfoThread: thread is interrupted: " + e.toString());
            }
        }
    }

    private class TradeCmdThread extends Thread {
        private String URL = URL_BASE + "/trade_cmd/" + mAtmAddr + "/";

        Response.Listener<JSONObject> rspListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dLog("TradeCmdThread: Response is " + response.toString());
                mTradeCmd.setTradeCmdByJSON(response);
            }
        };

        Response.ErrorListener errListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dLog("TradeCmdThread: " + error.toString());
            }
        };

        @Override
        public void run() {
            try {
                while (true) {
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null, rspListener, errListener);

                    dLog("TradeCmdThread: GET is preparing to send to: " + URL);
                    mQueue.add(request);
                   

                    sleep(mTradeCmd.getTradeCmdGetPeriodSec() * 1000);
                }
            } catch (InterruptedException e) {
                dLog("TradeCmdThread: thread is interrupted: " + e.toString());
            }
        }
    }

    private String getCurrentDateAndTimeStr() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    private static void dLog(@Nonnull String logStr) {
        if (DEBUG_FLAG) {
//            Log.d(LOG_TAG, logStr);
        }
    }
}
