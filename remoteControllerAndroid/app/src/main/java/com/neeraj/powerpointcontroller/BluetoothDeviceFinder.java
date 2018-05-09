package com.neeraj.powerpointcontroller;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.HashSet;

public class BluetoothDeviceFinder extends Activity {

    private ArrayList<BluetoothDevice> devices;
    private ArrayList<String> deviceNames;
    private HashSet<String> addressHashTable;
    private ArrayAdapter<String> adapter;
    public static final String PPT_UUID = "6f5b3d7b-fb44-46f0-8120-043ee4e12261";
    BluetoothAdapter mBluetoothAdapter;
    public static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!addressHashTable.contains(device.getAddress())) {
                    devices.add(device);
                    addressHashTable.add(device.getAddress());
                    deviceNames.add(device.getName());
                    adapter.notifyDataSetChanged();
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                mBluetoothAdapter.cancelDiscovery();
                hideScanning();
            }
        }
    };

    private final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice selectedDevice = devices.get(position);
            Intent intent = new Intent(BluetoothDeviceFinder.this,PPTController.class);
            intent.putExtra(BluetoothDevice.EXTRA_DEVICE,selectedDevice);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device_finder);
        devices = new ArrayList<>();
        addressHashTable = new HashSet<>();
        deviceNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,deviceNames);
        ListView deviceList = (ListView) findViewById(R.id.device_list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(itemClickListener);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Bluetooth not supported",Toast.LENGTH_LONG).show();
        }
        else {
            mBluetoothAdapter.enable();
        }
        IntentFilter deviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter serviceFilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        IntentFilter discoveryFinishFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, deviceFoundFilter);
        registerReceiver(broadcastReceiver, serviceFilter);
        registerReceiver(broadcastReceiver, discoveryFinishFilter);
        hideScanning();
        adapter.notifyDataSetChanged();
        ActivityCompat.requestPermissions(BluetoothDeviceFinder.this,
                new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_COARSE_LOCATION_PERMISSIONS);
    }
    void enableBluetooth(){
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }
    void discover(){
        if(mBluetoothAdapter==null) return;
        enableBluetooth();
        mBluetoothAdapter.cancelDiscovery();
        devices.clear();
        deviceNames.clear();
        addressHashTable.clear();
        adapter.notifyDataSetChanged();
        if(mBluetoothAdapter.startDiscovery()){
            showScanning();
        }
    }
    void stopDiscovery(){
        mBluetoothAdapter.cancelDiscovery();
        hideScanning();
    }
    void showScanning(){
        findViewById(R.id.scanning).setVisibility(View.VISIBLE);
    }
    void hideScanning(){
        findViewById(R.id.scanning).setVisibility(View.INVISIBLE);
    }
    public void scanButtonClick(View view){
        doDiscovery();
    }
    protected void onStop(){
        super.onStop();
        stopDiscovery();

    }
    protected void onDestroy(){
        super.onDestroy();
        stopDiscovery();
    }

    public void doDiscovery() {
        int hasPermission = ActivityCompat.checkSelfPermission(BluetoothDeviceFinder.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            discover();
            return;
        }

        ActivityCompat.requestPermissions(BluetoothDeviceFinder.this,
                new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_COARSE_LOCATION_PERMISSIONS);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_PERMISSIONS: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    discover();
                } else {
                    Toast.makeText(this,
                            "Permission required",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
