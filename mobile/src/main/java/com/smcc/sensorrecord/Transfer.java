package com.smcc.sensorrecord;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.smcc.sensordesc.SensorData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 与手表的数据通信处理
 */

public class Transfer implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final Activity mActivty;
    private GoogleApiClient mGoogleApiClient;

    //文件名，按类型分
    private String dataType[] = {SensorData.ACCELEROMETER + "_DATA.txt", SensorData.GRAVITY + "_DATA.txt", SensorData.GYROSCOPE + "_DATA.txt",
            SensorData.LINEAR_ACCELERATION + "_DATA.txt", SensorData.ROTATION_VECTOR + "_DATA.txt",
            SensorData.MAGNETIC_FIELD + "_DATA.txt", SensorData.ORIENTATION + "_DATA.txt", "SameTime_DATA.txt"};
    //文件内容的标头
    private String contentTitle[] = {"second     time     ax     ay     az\n", "second     time     gx     gy     gz\n",
            "second     time     gyx     gyy     gyz\n", "second     time     lx     ly     lz\n",
            "second    time    rx   ry   rz\n", "second     time   mx   my   mz\n",
            "second     time     ox     oy     oz\n", "second    time    x   y   z   type\n"};

    private OutputStream out[];
    float nowTime = 0;
    //相同的时间的队列
    LinkedBlockingQueue<SensorData> sensorDataQueue = new LinkedBlockingQueue<>();
    private String TAG = "Transfer";

    SensorData data;
    String dataName;
    int index;
    float[] values;
    float secondToBegin;
    String currentTime;

    /**
     * 构造
     */
    public Transfer(Context context) {
        if (context instanceof Activity) {
            mActivty = (Activity) context;
        } else {
            throw new IllegalStateException("set activity to init Transfer");
        }

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

    }

    /**
     * 建立连接
     */
    public void connect() {
        mGoogleApiClient.connect();
    }

    /**
     * 检查连接是否建立
     */
    public boolean isConnected() {
        return mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting();
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    /**
     * 数据监听，处理从手表发送的传感器数据
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: ");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                notify();
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/sensor_data") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    SensorData sensorData;
                    for (String key : dataMap.keySet()) {
                        sensorData = new SensorData(key, dataMap.getFloatArray(key));
                        Log.d(TAG, "onDataChanged: ");
                        //写入本地
                        writeSensorData(sensorData);
                    }
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    private void writeSensorData(SensorData sensorData) {
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
