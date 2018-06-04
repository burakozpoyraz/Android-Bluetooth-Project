package com.mrozpoyraz.bluetoothproject;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.Charset;

public class ConnectedActivity extends AppCompatActivity {
    private static final String TAG = "ConnectedActivity";
    private static final String FEEDBACK_OK = "ok$";
    private static final String FEEDBACK_ERR = "err$";
    BluetoothConnectionService bluetoothConnectionService;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    ProgressDialog progressDialog;
    String address;
    Handler handler;
    String readMessage;
    String sendMessage;
    Button sendButton;
    EditText messageText;
    OpenActivityClass openActivityClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        Log.d(TAG, "is started.");

        progressDialog = new ProgressDialog(ConnectedActivity.this);
        progressDialog.setMessage("Lütfen bekleyiniz...");
        progressDialog.setTitle("Cihaza bağlanılıyor");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        handler = new Handler();

        Intent intent = getIntent();
        address = intent.getStringExtra("Device Address");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        sendButton = (Button) findViewById(R.id.sendButton);
        messageText = (EditText) findViewById(R.id.messageText);

        bluetoothConnectionService = BluetoothConnectionService.getInstanceBCS();
        bluetoothConnectionService.setDevice(bluetoothDevice);
        bluetoothConnectionService.startClient();
        bluetoothConnectionService.setOnBluetoothConnectingListener(new BluetoothConnectionService.OnBluetoothConnectingListener() {
            @Override
            public void onSuccess() {
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Log.d(TAG, "Connection is successfull.");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ConnectedActivity.this, "Bağlantı başarıyla tamamlandı", Toast.LENGTH_SHORT).show();
                    }
                });
                bluetoothConnectionService.connected();
            }

            @Override
            public void onFailure() {
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Log.d(TAG, "Connection is failed.");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ConnectedActivity.this, "Cihaza bağlanılamadı, lütfen bağlantılarınızı kontrol ederek tekrar deneyiniz."
                                , Toast.LENGTH_SHORT).show();
                    }
                });
                openActivityClass = new OpenActivityClass(ConnectedActivity.this, BaslangicActivity.class);
                openActivityClass.openActivityWithoutSendingAddress();
            }
        });

        bluetoothConnectionService.setOnBluetoothConnectionListener(new BluetoothConnectionService.OnBluetoothConnectionListener() {
            @Override
            public void onConnectionLost() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ConnectedActivity.this, "Bağlantı kesildi", Toast.LENGTH_SHORT).show();
                    }
                });
                openActivityClass = new OpenActivityClass(ConnectedActivity.this, BaslangicActivity.class);
                openActivityClass.openActivityWithoutSendingAddress();
            }

            @Override
            public void onRead() {
                readMessage = bluetoothConnectionService.getIncomingMessage();
                if (readMessage.equals(FEEDBACK_OK)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ConnectedActivity.this, "İşleminiz başarıyla gerçekleştirildi.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (readMessage.equals(FEEDBACK_ERR)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ConnectedActivity.this, "İşlem başarısız, lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage = messageText.getText().toString();
                byte[] bytes = sendMessage.getBytes(Charset.defaultCharset());
                bluetoothConnectionService.write(bytes);
            }
        });
    }
}


