package com.example.updatedemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class MainActivity extends Activity {
        private static final String TAG = "Update";
        public ProgressDialog pBar;
        private Handler handler = new Handler();
        private String path = "/mnt/sdcard/";

        private int newVerCode = 0;
        private String newVerName = "";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                justUpdate();
//                

        }
        @Override
        protected void onResume() {
        	super.onResume();
        	justUpdate();
        }

        
        
        private void justUpdate(){
        	StringBuffer sb = new StringBuffer();
            sb.append(", 是否更新?");
        	Dialog dialog = new AlertDialog.Builder(MainActivity.this)
            .setTitle("软件更新")
            .setMessage(sb.toString())
            // 设置内容
            .setPositiveButton("更新",// 设置确定按钮
                            new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                    int which) {
                                            pBar = new ProgressDialog(MainActivity.this);
                                            pBar.setTitle("正在下载");
                                            pBar.setMessage("请稍候...");
                                            pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                            downFile(Config.UPDATE_SERVER
                                                            + Config.UPDATE_APKNAME);
                                    }

                            })
            .setNegativeButton("暂不更新",
                            new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                            // 点击"取消"按钮之后退出程序
                                            finish();
                                    }
                            }).create();// 创建
			// 显示对话框
			dialog.show();
        	
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

}
