package com.mrozpoyraz.bluetoothproject;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BaslangicActivity extends AppCompatActivity {
    private static final String TAG = "BaslangicActivity";
    BluetoothAdapter bluetoothAdapter;
    Button enablingButton;
    Button bondedDevicesButton;
    Button availableDevicesButton;
    OpenActivityClass openActivityClass;
    int requestCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baslangic);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enablingButton = (Button) findViewById(R.id.enablingButton);
        bondedDevicesButton = (Button) findViewById(R.id.bondedDevicesButton);
        availableDevicesButton = (Button) findViewById(R.id.availableDevicesButton);

        if (bluetoothAdapter == null) {
            Toast.makeText(BaslangicActivity.this, "Telefonunuzda Bluetooth bulunmamaktadır.", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                enablingButton.setText("BLUETOOTH KAPAT");
                enablingButton.setBackgroundColor(getResources().getColor(R.color.customGreen));
                bondedDevicesButton.setEnabled(true);
                availableDevicesButton.setEnabled(true);
                Log.d(TAG, "Bluetooth açık");
                Toast.makeText(BaslangicActivity.this, "BLUETOOTH AÇIK", Toast.LENGTH_SHORT).show();
            } else {
                enablingButton.setText("BLUETOOTH AÇ");
                enablingButton.setBackgroundColor(getResources().getColor(R.color.customRed));
                bondedDevicesButton.setEnabled(false);
                availableDevicesButton.setEnabled(false);
                Log.d(TAG, "Bluetooth kapalı");
                Toast.makeText(BaslangicActivity.this, "BLUETOOTH KAPALI", Toast.LENGTH_SHORT).show();
            }
        }

        enablingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter == null) {
                    Toast.makeText(BaslangicActivity.this, "Telefonunuzda Bluetooth bulunmamaktadır.", Toast.LENGTH_SHORT).show();
                } else {
                    if (bluetoothAdapter.isEnabled()) {
                        bluetoothAdapter.disable();
                        enablingButton.setText("BLUETOOTH AÇ");
                        enablingButton.setBackgroundColor(getResources().getColor(R.color.customRed));
                        bondedDevicesButton.setEnabled(false);
                        availableDevicesButton.setEnabled(false);
                        Log.d(TAG, "Bluetooth kapalı");
                        Toast.makeText(BaslangicActivity.this, "BLUETOOTH KAPALI", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableAdapter, requestCode);
                    }
                }
            }
        });

        bondedDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityClass = new OpenActivityClass(BaslangicActivity.this, BondedDevicesActivity.class);
                openActivityClass.openActivityWithoutSendingAddress();
            }
        });

        availableDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityClass = new OpenActivityClass(BaslangicActivity.this, AvailableDevicesActivity.class);
                openActivityClass.openActivityWithoutSendingAddress();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            enablingButton.setText("BLUETOOTH KAPAT");
            enablingButton.setBackgroundColor(getResources().getColor(R.color.customGreen));
            bondedDevicesButton.setEnabled(true);
            availableDevicesButton.setEnabled(true);
            Log.d(TAG, "Bluetooth açık");
            Toast.makeText(BaslangicActivity.this, "BLUETOOTH AÇIK", Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_CANCELED) {
            enablingButton.setText("BLUETOOTH AÇ");
            enablingButton.setBackgroundColor(getResources().getColor(R.color.customRed));
            bondedDevicesButton.setEnabled(false);
            availableDevicesButton.setEnabled(false);
            Log.d(TAG, "Bluetooth kapalı");
            Toast.makeText(BaslangicActivity.this, "BLUETOOTH BAĞLANTISI REDDEDİLDİ", Toast.LENGTH_SHORT).show();
        }
    }
}
