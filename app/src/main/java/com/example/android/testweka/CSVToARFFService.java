package com.example.android.testweka;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class CSVToARFFService extends Service {
    private final String TAG = "fengping.ma-CS";
    private final String SAVE_FILE = "/csvSaveAsArff.arff";
    private final String mCVSFileName = "/wekaCSV.csv";
    private boolean HAD_DONE_FLAGE = true;


    private File csvFile = null;
    public CSVToARFFService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                csvToarff();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int oneDay=10*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+oneDay;
        Intent timeIntent = new Intent(this,AlarmReceiver.class);
        PendingIntent pintent = PendingIntent.getBroadcast(this, 0, timeIntent, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pintent);
        return super.onStartCommand(intent, flags, startId);
    }
    public boolean csvToarff(){
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();
                csvFile = new File(sdCardDir.getCanonicalPath() + mCVSFileName);
                CSVLoader csvLoader = new CSVLoader();
                csvLoader.setSource(csvFile);
                Instances data = csvLoader.getDataSet();

                ArffSaver saver = new ArffSaver();
                saver.setInstances(data);
                File arffFile = new File(sdCardDir.getCanonicalPath() + SAVE_FILE);
                saver.setFile(arffFile);
                saver.setDestination(new File(sdCardDir.getCanonicalPath() + SAVE_FILE));
                saver.writeBatch();
//
//                FileInputStream fi = new FileInputStream(arffFile);
//                File sarff = new File(sdCardDir.getCanonicalPath() + FINAL_SAVE_FILE);
//
//                Log.i(TAG, "I open the file");
//
//
//                FileOutputStream fo = new FileOutputStream(sarff);
//
//
//                byte[] buffer = new byte[1024];
//                StringBuffer sb = new StringBuffer();
//
//                int timeIndex;
//                while ((timeIndex = sb.indexOf("{")) < 0) {
//                    readToBuffer(fi, sb);
//                }
//                fo.write(sb.substring(0,timeIndex).getBytes());
//                sb.delete(0, timeIndex);
//                fo.write("date \"HH:mm:ss\"\n".getBytes());
//                int applicationIndex;
//                while ((applicationIndex = sb.indexOf("@attribute"))<0) {
//                    readToBuffer(fi, sb);
//                }
//                sb.delete(0,applicationIndex);
//                fo.write(sb.toString().getBytes());
//                int readLength;
//                while ((readLength = fi.read(buffer)) != -1) {
//                    fo.write(buffer,0,readLength);
//                }
//                fi.close();
//                fo.flush();
//                fo.close();
//                Log.i(TAG, "I close the file");

                if (HAD_DONE_FLAGE) {
                    HAD_DONE_FLAGE = false;
                    startService(new Intent(this, CalculatePackage.class));
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        }
        return false;
    }

    private void readToBuffer(FileInputStream fi, StringBuffer sb) throws IOException {
        int readLength;
        byte[] b = new byte[1024];
        readLength = fi.read(b);
        if (readLength == -1) {
            throw new RuntimeException("读取到了-1，说明文件结束");
        }
        String s = new String (b,0,readLength,"ISO-8859-1");
        sb.append(s);
    }
}
