package com.example.myruns;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import androidx.core.app.NotificationCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class SensorClassifierService extends Service implements SensorEventListener {

    public class SensorBinder extends Binder {

        public void setHandler(GpsInputActivity.MyHandler h) { handler = h; }

        public void start() { startSensorService();}

        public void stop() { stopSensorService();}

        public String getPredictedActivity() {
            switch((int) max_class) {
                case 0:
                    return "Standing";
                case 1:
                    return "Walking";
                case 2:
                    return "Running";
                default:
                    return "Other";
            }
        }
    }

    private class SendClassificationTask extends TimerTask {

        @Override
        public void run() {
            Bundle bundle = new Bundle();
            double c = 3;
            if (!classes.isEmpty()) c = classes.get(classes.size()-1);

            switch((int) c) {
                case 0:
                    bundle.putString(CodeKeys.CLASS_ACT_KEY, "Standing");
                    break;
                case 1:
                    bundle.putString(CodeKeys.CLASS_ACT_KEY, "Walking");
                    break;
                case 2:
                    bundle.putString(CodeKeys.CLASS_ACT_KEY, "Running");
                    break;
                default:
                    bundle.putString(CodeKeys.CLASS_ACT_KEY, "Other");
                    break;
            }

            Message msg = handler.obtainMessage();
            msg.setData(bundle);
            msg.what = CodeKeys.SENSOR_SERVICE_MSG;
            handler.sendMessage(msg);

            timer.schedule(new SendClassificationTask(), 3000);
        }
    }

    private class OnSensorChangedThread extends Thread {

        @Override
        public void run() {
            Instance inst = new DenseInstance( CodeKeys.ACCELEROMETER_BLOCK_CAPACITY + 2);
            inst.setDataset(mDataSet);
            int blockSize = 0;
            FFT fft = new FFT(CodeKeys.ACCELEROMETER_BLOCK_CAPACITY);
            double[] accBlock = new double[CodeKeys.ACCELEROMETER_BLOCK_CAPACITY];
            double[] im = new double[CodeKeys.ACCELEROMETER_BLOCK_CAPACITY];

            while (true) {
                try {
                    // need to check if the thread is interrupted
                    if (!isInterrupted()) {


                        // Dumping buffer
                        accBlock[blockSize++] = mAccBuffer.take();

                        if (blockSize == CodeKeys.ACCELEROMETER_BLOCK_CAPACITY) {
                            blockSize = 0;

                            // time = System.currentTimeMillis();
                            double max = .0;
                            for (double val : accBlock) {
                                if (max < val) {
                                    max = val;
                                }
                            }

                            fft.fft(accBlock, im);

                            for (int i = 0; i < accBlock.length; i++) {
                                double mag = Math.sqrt(accBlock[i] * accBlock[i] + im[i]
                                        * im[i]);
                                inst.setValue(i, mag);
                                im[i] = .0; // Clear the field
                            }

                            // Append max after frequency component
                            inst.setValue(CodeKeys.ACCELEROMETER_BLOCK_CAPACITY, max);
                            mDataSet.add(inst);

                            double classified = wekaWrapper.classifyInstance(inst);
                            addClassification(classified);
                        }
                    }
                    else {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
        }
    }

    private SensorManager mSensorManager;
    private Instances mDataSet;
    private static ArrayBlockingQueue<Double> mAccBuffer;
    private ArrayList<Double> classes;
    private WekaWrapper wekaWrapper;
    OnSensorChangedThread T;
    Timer timer;
    GpsInputActivity.MyHandler handler;
    int[] holder = new int[] {0, 0, 0, 0};
    double max_class = 3;
    int max_freq = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        mAccBuffer = new ArrayBlockingQueue<>(
                CodeKeys.ACCELEROMETER_BUFFER_CAPACITY);
        wekaWrapper = new WekaWrapper();
        classes = new ArrayList<>();
        timer = new Timer();
    }

    public void addClassification(double c) {
        holder[(int) c] = holder[(int) c] + 1;
        classes.add(c);
        if (holder[(int) c] > max_freq) {
            max_class = c;
            max_freq = holder[(int) c];
            timer.schedule(new SendClassificationTask(), 0);
        }
    }

    public void startSensorService() {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);


        // Create the container for attributes
        ArrayList<Attribute> allAttr = new ArrayList<>();

        // Adding FFT coefficient attributes
        DecimalFormat df = new DecimalFormat("0000");

        for (int i = 0; i < CodeKeys.ACCELEROMETER_BLOCK_CAPACITY; i++) {
            allAttr.add(new Attribute(CodeKeys.FEAT_FFT_COEF_LABEL + df.format(i)));
        }
        // Adding the max feature
        allAttr.add(new Attribute(CodeKeys.FEAT_MAX_LABEL));

        // Construct the data set with the attributes specified as allAttr and
        // capacity 10000
        mDataSet = new Instances(CodeKeys.FEAT_SET_NAME, allAttr, CodeKeys.FEATURE_SET_CAPACITY);

        // Set the last column/attribute (standing/walking/running) as the class
        // index for classification
        mDataSet.setClassIndex(mDataSet.numAttributes() - 1);

        Intent i = new Intent(this, GpsInputActivity.class);
        // Read:
        // http://developer.android.com/guide/topics/manifest/activity-element.html#lmode
        // IMPORTANT!. no re-create activity
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "sensor_notification_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setContentTitle(getApplicationContext().getString(R.string.ui_sensor_service_notification_title));
        builder.setContentText(getResources().getString(R.string.ui_sensor_service_notification_content));
        builder.setContentIntent(pi).build();
        builder.setSmallIcon(R.drawable.greend);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);


        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(channelId, "Sensor Service", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("This channel is used by sensor service");
                notificationManager.createNotificationChannel(channel);
            }
        }

        startForeground(CodeKeys.SENSOR_SERVICE_CODE, notification);

        timer.schedule(new SendClassificationTask(), 3000);
        T = new OnSensorChangedThread();
        T.start();
    }

    public void stopSensorService() {
        T.interrupt();
        mSensorManager.unregisterListener(this);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            double m = Math.sqrt(event.values[0] * event.values[0]
                    + event.values[1] * event.values[1] + event.values[2]
                    * event.values[2]);

            // Inserts the specified element into this queue if it is possible
            // to do so immediately without violating capacity restrictions,
            // returning true upon success and throwing an IllegalStateException
            // if no space is currently available. When using a
            // capacity-restricted queue, it is generally preferable to use
            // offer.

            try {
                mAccBuffer.add(m);
            } catch (IllegalStateException e) {

                // Exception happens when reach the capacity.
                // Doubling the buffer. ListBlockingQueue has no such issue,
                // But generally has worse performance
                ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<>(
                        mAccBuffer.size() * 2);

                mAccBuffer.drainTo(newBuf);
                mAccBuffer = newBuf;
                mAccBuffer.add(m);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SensorBinder();
    }
}
