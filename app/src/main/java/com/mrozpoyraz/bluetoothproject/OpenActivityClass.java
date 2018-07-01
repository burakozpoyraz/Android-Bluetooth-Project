package com.mrozpoyraz.bluetoothproject;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Burak on 27.08.2017.
 */

public class OpenActivityClass {
    Context ctx;
    Class activity;

    OpenActivityClass(Context currentActivity, Class targetActivity) {
        ctx = currentActivity;
        this.activity = targetActivity;
    }

    public void openActivityWithSendingAddress(String address) {
        Intent intent = new Intent(ctx, activity);
        intent.putExtra("Device Address", address);
        ctx.startActivity(intent);
    }

    public void openActivityWithoutSendingAddress() {
        Intent intent = new Intent(ctx, activity);
        ctx.startActivity(intent);
    }
}
