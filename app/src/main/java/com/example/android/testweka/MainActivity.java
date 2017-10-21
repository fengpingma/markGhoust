package com.example.android.testweka;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "fengping.ma.test";
    private final String HOUSE_FILE_NAME = "/house.arff";
    private final String CONTACT_FILE_NAME = "/contact-lenses.arff";


    TextView display;
    TextView displayresult;
    TextView creatArffResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        //define button
        Button read = (Button) findViewById(R.id.readarff);
        Button caculate = (Button) findViewById(R.id.caculate);
        Button createArffButton = (Button) findViewById(R.id.createArffButton);
        Button openPermission = (Button) findViewById(R.id.openPermission);
        Button openServer = (Button) findViewById(R.id.openServer);
        Button closeServer = (Button) findViewById(R.id.closeServer);

        //define Textview to dispaly some message of progress result
        display = (TextView) findViewById(R.id.displayarff);
        creatArffResult = (TextView) findViewById(R.id.creatArffResult);
        displayresult = (TextView) findViewById(R.id.displayResult);

        //grant Write Permission and
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                granteWritePermission();
                display.setText(calculatePriceOfhouse());
            }
        });


        caculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                granteWritePermission();
                displayresult.setText(classifyGlasses());
            }
        });

        createArffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                granteWritePermission();
                checkUsagePermission();

                creatArffResult.setText(readUsageStats().toString());


//                PackageManager packageManager = getPackageManager();
//
//                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
//
//                if (intent != null) {
//                    startActivity(intent);
//                }
            }
        });

        openPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkUsagePermission()) {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivityForResult(intent, 100);
                } else {
                    Toast.makeText(MainActivity.this, "权限已开启",Toast.LENGTH_SHORT);
                }
            }
        });
        openServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                granteWritePermission();
                startService(new Intent(MainActivity.this, StoreDate.class));
                Toast.makeText(MainActivity.this, "服务已开启", Toast.LENGTH_SHORT).show();
            }
        });
        closeServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, StoreDate.class));
                Toast.makeText(MainActivity.this, "服务已关闭", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (requestCode == 100) {
                AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                int mode = 0;
                mode = appOps.checkOpNoThrow("android:get_usage_stats", Process.myUid(), getPackageName());
                boolean granted = mode == AppOpsManager.MODE_ALLOWED;
                if (!granted) {
                    Toast.makeText(this, "请开启该权限", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(this, "权限已开启！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean checkUsagePermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode;
            mode = appOps.checkOpNoThrow("android:get_usage_stats", Process.myUid(), getPackageName());
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            if (granted){
                return true;
            };
        }
        return false;
    }


    public Boolean readUsageStats() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        Calendar beginCal = Calendar.getInstance();
        beginCal.add(Calendar.HOUR_OF_DAY, -1);
        Calendar endCal = Calendar.getInstance();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCal
                    .getTimeInMillis(), endCal.getTimeInMillis());

            List<String> usrPackageName = new ArrayList<>();
            PackageManager pm = getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(0);
            for (PackageInfo pi : packs) {
                if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && (pi.applicationInfo.flags &
                        ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                    usrPackageName.add(pi.packageName);
                }
            }


            StringBuilder sb = new StringBuilder();
            for (UsageStats usagestats : stats) {
                if (usrPackageName.contains(usagestats.getPackageName())) {
                    //sb.append(usagestats.getPackageName()+"|");

                    sb.append("pName: " + usagestats.getPackageName() + "\n First: " + millisecondToString(usagestats.getFirstTimeStamp())
                            + "\n Last: " + millisecondToString(usagestats.getLastTimeStamp()) + "\n Lasttimeused: " + millisecondToString(usagestats
                            .getLastTimeUsed())+ "\n Total: " + usagestats.getTotalTimeInForeground()+"毫秒\n");
                }
            }

            //return stats.get(11).getPackageName();
            Log.i(TAG, sb.toString());
//            String[] pNameArray = sb.toString().split("\\|");
//            Intent intent = pm.getLaunchIntentForPackage(pNameArray[0]);
//            if (intent != null) {
//                startActivity(intent);
//            }
        }

        return false;
    }
    public String millisecondToString(long time){
        String pattern = "yyyy年MM月dd日 HH点mm分ss秒 SSS毫秒";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date(time));
    }
    public void granteWritePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "I'm granting the permission");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public String classifyGlasses() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();
                File arffFile = new File(sdCardDir.getCanonicalPath() + CONTACT_FILE_NAME);
                Log.i(TAG, sdCardDir.getCanonicalPath());
                if (!arffFile.exists()) {
                    Log.i(TAG, "File doesn't exist");
                    return null;
                }
                FileInputStream fis = new FileInputStream(arffFile);

                Instances data = new Instances(new BufferedReader(new InputStreamReader(fis)));

                data.setClassIndex(data.numAttributes() - 1);

                Classifier cfs = new NaiveBayes();

                try {
                    cfs.buildClassifier(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Instance testInst;

                Evaluation testingEvaluation = new Evaluation(data);

                int length = data.numInstances();
                for (int i = 0; i < length; i++) {
                    testInst = data.instance(i);
                    testingEvaluation.evaluateModelOnceAndRecordPrediction(cfs, testInst);
                }

//                return "分类正确率"+(1-testingEvaluation.errorRate());
//                Log.i(TAG, cfs.toString());
//                return cfs.toString()+"\n分类正确率"+(1-testingEvaluation.errorRate());

                Instance test = new DenseInstance(5);
                test.setDataset(data);

                // Attribute age = data.attribute("age");
                List<String> lage = new ArrayList<>();
                lage.add("young");
                lage.add("pre-presbyopic");
                lage.add("presbyopic");
                lage.add("old");
                Attribute age = new Attribute("age", lage, 0);
                Attribute spectacle_prescrip = data.attribute("spectacle-prescrip");
                Attribute astigmatism = data.attribute("astigmatism");
                Attribute tear_prod_rate = data.attribute("tear-prod-rate");
                Attribute contact_lenses = data.attribute("contact-lenses");
                test.setValue(age, "old");
                test.setValue(spectacle_prescrip, "myope");
                test.setValue(astigmatism, "no");
                test.setValue(tear_prod_rate, "normal");

//                ArffSaver saver = new ArffSaver();
//                saver.setInstances(data);
//                saver.setFile(new File(sdCardDir.getCanonicalPath() + SAVE_FILE));
//                saver.setDestination(new File(sdCardDir.getCanonicalPath() + SAVE_FILE));
//                saver.writeBatch();
//                Instance test = data.instance(0);
                try {
                    Log.i(TAG, cfs.classifyInstance(test) + "");
                    Log.i(TAG, test.toString());
                    Log.i(TAG, data.attribute(4).value((int) cfs.classifyInstance(test)));
                    return cfs.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String calculatePriceOfhouse() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();

                Log.i(TAG, sdCardDir.getCanonicalPath());
                File arffFile = new File(sdCardDir.getCanonicalPath() + CONTACT_FILE_NAME);
                Log.i(TAG, sdCardDir.getCanonicalPath());
                if (!arffFile.exists()) {
                    Log.i(TAG, "File doesn't exist");
                    return null;
                }
                FileInputStream fis = new FileInputStream(sdCardDir.getCanonicalPath() + HOUSE_FILE_NAME);

                Log.i(TAG, sdCardDir.getCanonicalPath());

                Instances data = new Instances(new BufferedReader(new InputStreamReader(fis)));

                data.setClassIndex(data.numAttributes() - 1);

                LinearRegression linearRegression = new LinearRegression();

                try {
                    linearRegression.buildClassifier(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                double[] coef = linearRegression.coefficients();
                for (int i = 0; i < coef.length; i++) {
                    Log.i(TAG, coef[i] + "");
                }

                double myHouseValue = (coef[0] * 3198) +
                        (coef[1] * 9696) +
                        (coef[2] * 5) +
                        (coef[3] * 3) +
                        (coef[4] * 1) +
                        coef[6];

                return String.valueOf(myHouseValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You granted the permission", Toast.LENGTH_SHORT);
                    Log.i(TAG, "You granted the permission");
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT);
                    Log.i(TAG, "You denied the permission");
                }
                break;
            default:
        }
    }

}
