package com.app.arduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.UUID;

public class DeviceControl extends AppCompatActivity {

    private String DEBUG_TAG = "DEBUG TAG";

    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice device;
    private TextView collisionState;
    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_control);
        Bundle bundle = getIntent().getExtras();
        device = bundle.getParcelable("BluetoothDevice");
        TextView deviceName = findViewById(R.id.device_name);
        deviceName.setText(device.getName());
        Button onButton = findViewById(R.id.on_button);
        Button offButton = findViewById(R.id.off_button);
        Button backButton = findViewById(R.id.back_button);
        Button stopButton = findViewById(R.id.stop_button);
        collisionState = findViewById(R.id.collision_alert);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnect();
            }
        });
        onButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage("FORWARD");
            }
        });
        offButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage("BACKWARD");
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage("STOP");
            }
        });
        connectToDevice();
    }

    void connectToDevice() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            device = bluetoothAdapter.getRemoteDevice(device.getAddress());
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            bluetoothSocket.connect();
            sendMessage("CONNECTED");
            new updateUI().start();
            Log.e(DEBUG_TAG, "Device connected");
        } catch (IOException e) {
            e.printStackTrace();
            onBackPressed();
            displayErrorMessage(e.getMessage());
        }
    }

    void disconnect(){
        try {
            sendMessage("DISCONNECTED");
            bluetoothSocket.close();
            Log.e(DEBUG_TAG, "Device disconnected");
        } catch (IOException e) {
            e.printStackTrace();
            displayErrorMessage(e.getMessage());
        }
        onBackPressed();
    }

    void displayErrorMessage(final String error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
    }

    void sendMessage(String message) {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.getOutputStream().write(message.getBytes());
                Log.e(DEBUG_TAG, message + " signal sent");
            } catch (IOException e) {
                displayErrorMessage(e.getMessage());
            }
        }
    }

    class updateUI extends Thread {
        public void run() {
            try {
                BufferedInputStream inStream = new BufferedInputStream(bluetoothSocket.getInputStream());
                byte[] bytes = new byte[1024];
                int count;
                String message;
                while ((count = inStream.read(bytes)) >= 0) {
                    message = new String(bytes, 0, count);
                    if (message.equals("1")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                collisionState.setText(R.string.collision);
                                collisionState.setTextColor(Color.RED);
                            }
                        });
                    } else if (message.equals("0")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                collisionState.setText(R.string.safe);
                                collisionState.setTextColor(Color.GREEN);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                Log.e(DEBUG_TAG, e.getMessage());
            }
        }
    }
}