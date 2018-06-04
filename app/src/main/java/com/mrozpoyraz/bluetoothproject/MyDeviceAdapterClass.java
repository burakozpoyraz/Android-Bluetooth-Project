package com.mrozpoyraz.bluetoothproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Burak on 27.08.2017.
 */

public class MyDeviceAdapterClass extends BaseAdapter {
    Context ctx;
    ArrayList<MyDeviceClass> DeviceList;

    public MyDeviceAdapterClass(Context context, ArrayList<MyDeviceClass> dvcList) {
        ctx = context;
        DeviceList = dvcList;
    }
    @Override
    public int getCount() {
        return DeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return DeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MyDeviceClass device = DeviceList.get(position);
        final View gorunum = LayoutInflater.from(ctx).inflate(R.layout.devices_listview_layout, null);
        TextView deviceName = (TextView) gorunum.findViewById(R.id.deviceName);
        deviceName.setText(device.name);
        TextView deviceAddress = (TextView) gorunum.findViewById(R.id.deviceAddress);
        deviceAddress.setText(device.address);
        return gorunum;
    }
}
