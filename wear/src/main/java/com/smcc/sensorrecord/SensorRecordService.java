package com.smcc.sensorrecord;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.smcc.sensordesc.SensorData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * 读取传感器数据并异步存储到本地
 */
public class SensorRecordService extends Service {

    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;

    private float startTime;

    @Override
    public void onCreate() {
        //1.获取SensorManager实例
        mSensorManager = (SensorManager) this.getSystemService(Service.SENSOR_SERVICE);
        mSensorEventListener = new MySensorEventListener();
        super.onCreate();
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
            float secondToBegin = (System.currentTimeMillis() - startTime) / 1000.00f; //计算从任务开始到现在的用时
            String currentTime = getCurrTime();//当前时间戳
            SensorData data;//待写入的传感器数据
            Log.d(TAG, "onSensorChanged1:  " + event.sensor.getType() + " time = " + secondToBegin);
            switch (event.sensor.getType()) {
                //处理不同类型的传感器的传回数据
                case Sensor.TYPE_ACCELEROMETER:
                    /*float ax = event.values[0];
                    float ay = event.values[1];
                    float az = event.values[2];*/
                    data = new SensorData(SensorData.ACCELEROMETER, event.values);
                    break;
                case Sensor.TYPE_GRAVITY:
                    data = new SensorData(SensorData.GRAVITY, event.values);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    data = new SensorData(SensorData.GYROSCOPE, event.values);
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    data = new SensorData(SensorData.LINEAR_ACCELERATION, event.values);
                    break;
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    data = new SensorData(SensorData.ROTATION_VECTOR, event.values);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    data = new SensorData(SensorData.MAGNETIC_FIELD, event.values);
                    break;
                case Sensor.TYPE_ORIENTATION:
                    data = new SensorData(SensorData.ORIENTATION, event.values);
                    break;
                default:
                    break;
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
        public List<Integer> start(Set<Integer> sensorTypes) {
            //2.注册对应传感器的监听器
            List<Integer> cannotRegister = new ArrayList<>();
            for (int sensorType : sensorTypes) {
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

    private static String getCurrTime() {
        return new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒").getDateInstance().format(
                new Date(System.currentTimeMillis()));
    }
}
