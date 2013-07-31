package com.android.demo;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcher);
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent goIntent = new Intent();
				goIntent.setClass(LauncherActivity.this, DemoActivity.class);
				startActivity(goIntent);
			}
		}, 3*1000);
	}
}