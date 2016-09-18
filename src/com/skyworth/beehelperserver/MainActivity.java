package com.skyworth.beehelperserver;

import com.skyworth.beehelperserver.message.ServerSession;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ServerSession.getInstance(this).initConnection();
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ServerSession.getInstance(this).closeConnection();
	}
}
