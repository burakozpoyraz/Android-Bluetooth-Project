package com.mrozpoyraz.bluetoothproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BondedDevicesActivity extends AppCompatActivity {
    private static final String TAG = "BondedDevicesActivity";
    BluetoothAdapter bluetoothAdapter;
    ListView bondedDevicesListView;
    MyDeviceAdapterClass deviceAdapter;
    ArrayList<MyDeviceClass> bondedDevicesList;
    String deviceName;
    String deviceAddress;
    OpenActivityClass openActivityClass;
    int requestCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonded_devices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bondedDevicesListView = (ListView) findViewById(R.id.bondedDevicesListview);
        bondedDevicesList = new ArrayList<MyDeviceClass>();

        Set<BluetoothDevice> bondedDevicesSet = bluetoothAdapter.getBondedDevices();
        if (bondedDevicesSet.isEmpty()) {
            Toast.makeText(this, "Daha önce bağlanılmış cihaz yoktur.", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice device : bondedDevicesSet) {
                MyDeviceClass bndDvc = new MyDeviceClass();
                bndDvc.name = device.getName();
                bndDvc.address = device.getAddress();
                bondedDevicesList.add(bndDvc);
                Log.d(TAG, "Paired device found");
                Log.d("Device Name:", bndDvc.name);
                Log.d("Device Address:", bndDvc.address);
            }

            deviceAdapter = new MyDeviceAdapterClass(this, bondedDevicesList);
            deviceAdapter.notifyDataSetChanged();
            bondedDevicesListView.setAdapter(deviceAdapter);

            bondedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    bluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "onItemClick: Clicked on a device.");
                    deviceName = bondedDevicesList.get(position).name;
                    deviceAddress = bondedDevicesList.get(position).address;
                    Log.d(TAG, "Device Name: " + deviceName);
                    Log.d(TAG, "Device Address: " + deviceAddress);
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableAdapter, requestCode);
                    } else {
                        openActivityClass = new OpenActivityClass(BondedDevicesActivity.this, ConnectedActivity.class);
                        openActivityClass.openActivityWithSendingAddress(deviceAddress);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Log.d(TAG, "deviceName = " + deviceName);
            Log.d(TAG, "deviceAddress = " + deviceAddress);
            Log.d(TAG, "Trying to connect to.. " + deviceName);
            openActivityClass = new OpenActivityClass(BondedDevicesActivity.this, ConnectedActivity.class);
            openActivityClass.openActivityWithSendingAddress(deviceAddress);
        } else if (resultCode == RESULT_CANCELED){
            Toast.makeText(BondedDevicesActivity.this, "Bağlantı reddedildi.", Toast.LENGTH_SHORT).show();
        }
    }
}
