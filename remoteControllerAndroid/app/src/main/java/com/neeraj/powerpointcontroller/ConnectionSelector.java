package com.neeraj.powerpointcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ConnectionSelector extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_selector);
    }
    public void onClickSearchDevice(View view){
        Intent intent = new Intent(ConnectionSelector.this,BluetoothDeviceFinder.class);
        startActivity(intent);
    }
}
