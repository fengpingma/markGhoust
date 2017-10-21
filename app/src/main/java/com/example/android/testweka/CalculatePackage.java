package com.example.android.testweka;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class CalculatePackage extends Service {
    Timer timer = null;
    Classifier cfs = null;


    private String TAG = "fengping.ma-CA";
    private final String SAVE_FILE = "/csvSaveAsArff.arff";
    private Instances data;
    Attribute week;
    Attribute time;


    public CalculatePackage() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();

        TimerTask buildTask = new TimerTask() {
            @Override
            public void run() {
                buildCFS();
            }
        };
        TimerTask predictTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (cfs) {
                    if (cfs != null) {
                        predictPackage();
                    }
                }
            }
        };
        timer.schedule(buildTask, 1000, 1000 * 60 * 60);
        timer.schedule(predictTask, 1000, 1000 * 10);
        return super.onStartCommand(intent, flags, startId);
    }

    private String predictPackage() {
        String resultPackage = null;
        Instance target = new DenseInstance(3);
        target.setDataset(data);

        String weekPattern = "u";

        long current = System.currentTimeMillis();

        Date nowTime = new Date(current);

        long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
        target.setValue(week, Integer.parseInt(convertTime(weekPattern, nowTime)));
        target.setValue(time, current - zero);
        try {
            Log.i(TAG, cfs.classifyInstance(target) + "");
            Log.i(TAG, target.toString());
            Log.i(TAG, cfs.toString());
            int result = (int) cfs.classifyInstance(target);
            Log.i(TAG, result + "");
            if (result > 0) {
                resultPackage = data.attribute(2).value(result);
                Log.i(TAG, resultPackage);
            }
            return resultPackage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String buildCFS() {

        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();
                File arffFile = new File(sdCardDir.getCanonicalPath() + SAVE_FILE);
                Log.i(TAG, sdCardDir.getCanonicalPath());
                if (!arffFile.exists()) {
                    Log.i(TAG, "File doesn't exist");
                    return null;
                }
//                FileInputStream fis = new FileInputStream(arffFile);
//
//                Instances data = new Instances(new BufferedReader(new InputStreamReader(fis)));

                ArffLoader arffLoader = new ArffLoader();

                arffLoader.setFile(arffFile);

                data = arffLoader.getDataSet();

                data.setClassIndex(data.numAttributes() - 1);

                cfs = new NaiveBayes();

                try {
                    cfs.buildClassifier(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                Instance testInst;

//                Evaluation testingEvaluation = new Evaluation(data);
//
//                int length = data.numInstances();
//                for (int i = 0; i < length; i++) {
//                    testInst = data.instance(i);
//                    testingEvaluation.evaluateModelOnceAndRecordPrediction(cfs, testInst);
//                }

//                return "分类正确率"+(1-testingEvaluation.errorRate());
//                Log.i(TAG, cfs.toString());
//                return cfs.toString()+"\n分类正确率"+(1-testingEvaluation.errorRate());

//                Instance test = new DenseInstance(5);
//                test.setDataset(data);

                // Attribute age = data.attribute("age");
//                List<String> lage = new ArrayList<>();
//                lage.add("young");
//                lage.add("pre-presbyopic");
//                lage.add("presbyopic");
//                lage.add("old");
//                Attribute age = new Attribute("age", lage, 0);
//                Attribute spectacle_prescrip = data.attribute("spectacle-prescrip");
//                Attribute astigmatism = data.attribute("astigmatism");
//                Attribute tear_prod_rate = data.attribute("tear-prod-rate");
//                Attribute contact_lenses = data.attribute("contact-lenses");
//                test.setValue(age, "old");
//                test.setValue(spectacle_prescrip, "myope");
//                test.setValue(astigmatism, "no");
//                test.setValue(tear_prod_rate, "normal");

//                ArffSaver saver = new ArffSaver();
//                saver.setInstances(data);
//                saver.setFile(new File(sdCardDir.getCanonicalPath() + SAVE_FILE));
//                saver.setDestination(new File(sdCardDir.getCanonicalPath() + SAVE_FILE));
//                saver.writeBatch();
//                Instance test = data.instance(0);

                week = data.attribute("week");
                time = data.attribute("time");






            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String convertTime(String pattern, Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(time);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
