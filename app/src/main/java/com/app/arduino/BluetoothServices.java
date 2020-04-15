package com.app.arduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothServices extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    private ListView devicesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devices_list);
        Button searchButton = findViewById(R.id.button_search_paired_devices);
        devicesListView = findViewById(R.id.device_list);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevices();
            }
        });
        showDevices();
    }

    private void showDevices() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (checkBluetoothAvailability()) {
            final ArrayList<BluetoothDevice> pairedDevicesList = getPairedDevices();
            final ArrayList<String> pairedDevicesNames = getDevicesNames();
            final ArrayAdapter<String> devicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDevicesNames);
            devicesListView.setAdapter(devicesAdapter);
            devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    BluetoothDevice device = pairedDevicesList.get(position);
                    Log.e("DEBUG", device.getName());
                    Intent intent = new Intent(getBaseContext(), DeviceControl.class);
                    intent.putExtra("BluetoothDevice", device);
                    startActivity(intent);
                }
            });
        }
    }

    private boolean checkBluetoothAvailability() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not available", Toast.LENGTH_LONG).show();
            return false;
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent turnOnRequest = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnRequest, 1);
            return false;
        }
        return true;
    }

    private ArrayList<BluetoothDevice> getPairedDevices() {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList pairedDevicesList = new ArrayList();
        if (pairedDevices.size() < 1) {
            return null;
        } else {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesList.add(device);
            }
        }
        return pairedDevicesList;
    }

    private ArrayList<String> getDevicesNames() {
        ArrayList<String> devicesName = new ArrayList<String>();
        for (BluetoothDevice device : pairedDevices) {
            devicesName.add(device.getName());
        }
        return devicesName;
    }
}