package com.example.android.testweka;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MyService extends Service {

    private final String TAG = "fengpingma";
    private String mCurrentPackName = "";

    private String mCVSFileName = "testCSV.csv";
    private String mFilePath = null;
    private File csvFile=null;
    private BufferedWriter csvWriter = null;
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
        createCSV();
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

    private void createCSV() {
        Object[] head = {"week", "time", "application"};
        List<Object> headList = Arrays.asList(head);
        csvWriter = null;
        StringBuilder sb=new StringBuilder();

        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();

                Log.i(TAG, sdCardDir.getCanonicalPath());
                csvFile = new File(sdCardDir.getCanonicalPath() +"/" +mCVSFileName);
                Log.i(TAG, sdCardDir.getCanonicalPath());
                if (!csvFile.exists()) {
                    csvFile.createNewFile();
                    csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "GB2312"), 1024);
                    for (Object oneOfHead : head) {
                        sb.append(oneOfHead + ",");
                    }
                    sb.setLength(sb.length()-1);
                    Log.i(TAG,sb.toString());

                    csvWriter.write(sb.toString());
                    csvWriter.newLine();
                    csvWriter.flush();
                }else {
                    csvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile,true), "GB2312"), 1024);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTime() {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        StringBuilder sb = new StringBuilder();
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
                    if (!mCurrentPackName.equals(usageStats.getPackageName())) {
                        mCurrentPackName = usageStats.getPackageName();
                        PackageManager pm = getPackageManager();
                        try {
                            Log.i(TAG, pm.getApplicationLabel(pm.getApplicationInfo(mCurrentPackName, PackageManager
                                    .GET_META_DATA)).toString());
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG, mCurrentPackName + resultString(usageStatsSortedMap.lastKey()));
                        String timepattern = "HH:mm:ss";
                        String weekpattern = "u";
                        SimpleDateFormat timeformatter = new SimpleDateFormat(timepattern);
                        SimpleDateFormat weekformatter = new SimpleDateFormat(weekpattern);
                        sb.append(timeformatter.format(usageStatsSortedMap.lastKey()) + ",");
                        sb.append(weekformatter.format(usageStatsSortedMap.lastKey()) + ",");
                        sb.append(mCurrentPackName);
                        try {
                            csvWriter.write(sb.toString());
                            csvWriter.newLine();
                            csvWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
        try {
            mTimer.cancel();
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }
}
