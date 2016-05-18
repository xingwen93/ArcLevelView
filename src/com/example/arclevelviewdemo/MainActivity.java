package com.example.arclevelviewdemo;

import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {
	
	ArcLevelView view;
	
	TextView tv;
	
	ProgressDialog dialog;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (ArcLevelView) findViewById(R.id.arc_level_view);
        tv = (TextView) findViewById(R.id.money);
        findViewById(R.id.btn_down).setOnClickListener(this);
//        findViewById(R.id.btn_up).setOnClickListener(this);
        view.setBottomTextPaddingTop(2);
        view.postDelayed(new Runnable() {
			@Override
			public void run() {
				 dialog = new ProgressDialog(MainActivity.this);
				 dialog.setMessage("正在加载...");
				 dialog.show();
			}
		}, 200);
     
        view.postDelayed(new Runnable() {
			@Override
			public void run() {
				dialog.dismiss();
				view.setMaxProgress(300000);
				view.setCurrentProgress(9999.1f);
				tv.setText("总金额: " + view.getCurrentProgress());
			}
		}, 2000);
    }

    Random r = new Random();
	@Override
	public void onClick(View v) {
//		float[] valus = {9999.1f, 10001.1f, 49999.1f, 50001.1f, 299999.9f, 300000.1f};
//		
//		view.setCurrentProgress(valus[r.nextInt(valus.length)]);
//		tv.setText("总金额: " + view.getCurrentProgress());
		
	 	PackageManager packageManager = this.getPackageManager(); 
	  	Intent intent = packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone"); 
	  	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) ; 
	  	this.startActivity(intent);
	}
}
