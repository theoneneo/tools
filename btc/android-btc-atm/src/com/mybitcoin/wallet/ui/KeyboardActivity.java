package com.mybitcoin.wallet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mybitcoin.wallet.R;

public class KeyboardActivity extends Activity{
	int index = 0;
	String num = null;
	TextView text;
	Button btn_1, btn_2, btn_3,btn_4, btn_5, btn_6,btn_7, btn_8, btn_9,btn_0, btn_clean, btn_ok;
	
	protected void onCreate(final Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        if(this.getIntent() != null){
        	index = getIntent().getIntExtra("index", 1);
        	num = getIntent().getStringExtra("num");
        }
        setContentView(R.layout.activity_keyboard);
        
        text = (TextView)findViewById(R.id.text);
        text.setText(num);
        
        btn_1 = (Button)findViewById(R.id.btn_1);
        btn_1.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"1";
				text.setText(str);
			}
        });
        btn_2 = (Button)findViewById(R.id.btn_2);
        btn_2.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"2";
				text.setText(str);
			}
        });
        btn_3 = (Button)findViewById(R.id.btn_3);
        btn_3.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"3";
				text.setText(str);
			}
        });
        btn_4 = (Button)findViewById(R.id.btn_4);
        btn_4.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"4";
				text.setText(str);
			}
        });
        btn_5 = (Button)findViewById(R.id.btn_5);
        btn_5.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"5";
				text.setText(str);
			}
        });
        btn_6 = (Button)findViewById(R.id.btn_6);
        btn_6.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"6";
				text.setText(str);
			}
        });
        btn_7 = (Button)findViewById(R.id.btn_7);
        btn_7.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"7";
				text.setText(str);
			}
        });
        btn_8 = (Button)findViewById(R.id.btn_8);
        btn_8.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"8";
				text.setText(str);
			}
        });
        btn_9 = (Button)findViewById(R.id.btn_9);
        btn_9.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"9";
				text.setText(str);
			}
        });
        btn_0 = (Button)findViewById(R.id.btn_0);
        btn_0.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText()+"0";
				text.setText(str);
			}
        });
        btn_clean = (Button)findViewById(R.id.btn_clean);
        btn_clean.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = text.getText().toString();
				
				String end;
				if(text.getText().length() > 0)
					end = str.substring(0, text.getText().length()-1);
				else
					end = "";
				text.setText(end);
			}
        });
        btn_ok = (Button)findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent data = new Intent();
				data.putExtra("index", index);
				data.putExtra("num", text.getText());
				KeyboardActivity.this.setResult(RESULT_OK, data);
				finish();
			}
        });
	}
}
