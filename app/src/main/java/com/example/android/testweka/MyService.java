package com.example.android.testweka;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MyService extends Service {

    private final String TAG = "fengpingma";
    private String currentPackName = "";

    private Context mContext;
    private Timer mTimer;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getTime();
            }
        };
        mTimer.schedule(task, 1000, 500);
        return super.onStartCommand(intent, flags, startId);
    }

    private void getTime() {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> usageStatsList = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usageStatsList = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time -
                    500, time);
        }
        if (usageStatsList != null && !usageStatsList.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SortedMap<Long, UsageStats> usageStatsSortedMap = new TreeMap<>();
                for (UsageStats usageStats : usageStatsList) {
                    usageStatsSortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!usageStatsSortedMap.isEmpty()) {
                    UsageStats usageStats = usageStatsSortedMap.get(usageStatsSortedMap.lastKey());
                    if (!currentPackName.equals(usageStats.getPackageName())) {
                        currentPackName = usageStats.getPackageName();
                        PackageManager pm = getPackageManager();
                        try {
                            Log.i(TAG, pm.getApplicationLabel(pm.getApplicationInfo(currentPackName, PackageManager
                                    .GET_META_DATA)).toString());
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG, currentPackName + resultString(usageStatsSortedMap.lastKey()));
                    }
                }
            }
        }

    }

    public String resultString(long time) {
        String pattern = "yyyy年MM月dd日 HH点mm分ss秒 SSS毫秒";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date(time));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
