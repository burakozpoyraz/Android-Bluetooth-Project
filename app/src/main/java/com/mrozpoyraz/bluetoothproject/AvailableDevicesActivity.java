package com.mrozpoyraz.bluetoothproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class AvailableDevicesActivity extends AppCompatActivity {
    public static final String TAG = "AvailableDevicesActvty";
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice clickedDevice;
    ListView availableDevicesListView;
    MyDeviceAdapterClass deviceAdapter;
    ArrayList<MyDeviceClass> availableDevicesList;
    String deviceName;
    String deviceAddress;
    OpenActivityClass openActivityClass;
    int deviceDuplication = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_devices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        availableDevicesListView = (ListView) findViewById(R.id.availableDevicesListview);
        availableDevicesList = new ArrayList<MyDeviceClass>();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        versionControl();

        bluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        deviceAdapter = new MyDeviceAdapterClass(this, availableDevicesList);
        deviceAdapter.notifyDataSetChanged();
        availableDevicesListView.setAdapter(deviceAdapter);

        availableDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "onItemClick: Clicked on a device.");
                deviceName = availableDevicesList.get(position).name;
                deviceAddress = availableDevicesList.get(position).address;
                clickedDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
                Log.d(TAG, "deviceName = " + deviceName);
                Log.d(TAG, "deviceAddress = " + deviceAddress);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    Log.d(TAG, "Trying to connect to " + deviceName);
                }
                openActivityClass = new OpenActivityClass(AvailableDevicesActivity.this, ConnectedActivity.class);
                openActivityClass.openActivityWithSendingAddress(deviceAddress);
            }
        });
    }

    protected final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(AvailableDevicesActivity.this, "Arama başlatıldı.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Arama başlatıldı.");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "OnReceive:ACTION_FOUND");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                MyDeviceClass dvc = new MyDeviceClass();

                if (device.getName() == null) {
                    dvc.name = "UNDEFINED NAME";
                } else {
                    dvc.name = device.getName();
                }
                dvc.address = device.getAddress();
                Log.d("Device Name:", dvc.name);
                Log.d("Device Address:", dvc.address);

                //To prevent any device to shown twice
                for (int i = 0; i < availableDevicesList.size(); i++) {
                    if (availableDevicesList.get(i).address.equals(dvc.address)) {
                        deviceDuplication = 1;
                        break;
                    }
                }
                if (deviceDuplication == 0) {
                    availableDevicesList.add(dvc);
                    deviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(AvailableDevicesActivity.this, "Arama tamamlandı.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Arama tamamlandı");
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is called.");
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void versionControl () {
        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck += this.checkSelfPermission("Manifeest.permission.ACCESS_COARSE_LOCATION");
        }
        if (permissionCheck != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }
    }
}
