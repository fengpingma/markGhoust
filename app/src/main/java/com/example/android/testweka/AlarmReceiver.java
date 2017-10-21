package com.example.android.testweka;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by android on 17-10-18.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent receiveIntent = new Intent(context, CSVToARFFService.class);
        context.startService(receiveIntent);
    }
}
