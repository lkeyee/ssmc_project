package com.smcc.sensorrecord;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.smcc.sensordesc.SensorData;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static android.content.ContentValues.TAG;

/**
 * 读取传感器数据并异步存储到本地
 */
public class SensorRecordService extends Service {

    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    private SensorDataIO mSensorDataIO;

    private long startTime;

    public SensorRecordService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //1.获取SensorManager实例
        mSensorManager = (SensorManager) this.getSystemService(Service.SENSOR_SERVICE);
        mSensorEventListener = new MySensorEventListener();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SensorRecordBinder();
    }

    /**
     * 监听器，监听传感器返回的数据
     * {@link SensorEventListener}
     */
    public class MySensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float secondToBegin = (float) ( (System.currentTimeMillis() -startTime)/1000.00f) ; //计算从任务开始到现在的用时
            String currentTime = getCurrTime();//当前时间戳
            String sensorName=null;
            switch (event.sensor.getType()) {
                //处理不同类型的传感器的传回数据
                case Sensor.TYPE_ACCELEROMETER:
                    sensorName=Sensor.STRING_TYPE_ACCELEROMETER;
                    break;
                case Sensor.TYPE_GRAVITY:
                    sensorName=Sensor.STRING_TYPE_GRAVITY;
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    sensorName=Sensor.STRING_TYPE_GYROSCOPE;
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    sensorName=Sensor.STRING_TYPE_LINEAR_ACCELERATION;
                    break;
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    sensorName=Sensor.STRING_TYPE_ROTATION_VECTOR;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    sensorName=Sensor.STRING_TYPE_MAGNETIC_FIELD;
                    break;
                case Sensor.TYPE_ORIENTATION:
                    sensorName=Sensor.STRING_TYPE_ORIENTATION;
                    break;
            }
            if(sensorName!=null) {
                SensorData sensorData = new SensorData(secondToBegin, currentTime, event.sensor.getType(), event.values);
                mSensorDataIO.put(sensorData);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    /**
     * Binder实现类
     */
    public class SensorRecordBinder extends Binder {
        /**
         * 注册待监听的传感器接收器，将需要监听的传感器的常量{@link Sensor}放入集合传入进来，
         * 举例来说，如果想要监听加速度，可以把常量{@link Sensor#TYPE_ACCELEROMETER}作为
         * sensorTypes中的一个变量传入。如果无法监听某个传感器，将会把该传感器的常量保存在
         * 一个集合中并返回这个常量集合。
         *
         * @param sensorTypes {@link Sensor}中代表传感器的常量如{@link Sensor#TYPE_ACCELEROMETER}
         * @return 无法监听的传感器
         */
        public List<Integer> start(Map<String,Integer> sensorTypes) {
            mSensorDataIO=new SensorDataIO(sensorTypes);
            //2.注册对应传感器的监听器
            List<Integer> cannotRegister = new ArrayList<>();
            Collection<Integer> sensorTypesNum=sensorTypes.values();
            for (int sensorType : sensorTypesNum) {
                if (mSensorManager.getDefaultSensor(sensorType) != null) {
                    mSensorManager.registerListener(mSensorEventListener,
                            mSensorManager.getDefaultSensor(sensorType),
                            SensorManager.SENSOR_DELAY_FASTEST);
                } else {
                    cannotRegister.add(sensorType);
                }
            }
            startTime = System.currentTimeMillis();
            return cannotRegister.isEmpty() ? null : cannotRegister;
        }

        public void stop() {
            startTime = 0;
            mSensorManager.unregisterListener(mSensorEventListener);
        }

    }

    public static String getCurrTime() {
        Date date=new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String formatDate = dateFormatter.format(date);
        return formatDate;
    }


}
