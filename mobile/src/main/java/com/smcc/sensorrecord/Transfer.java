package com.smcc.sensorrecord;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.os.Bundle;
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

/**
 * 与手表的数据通信处理
 */

public class Transfer implements DataApi.DataListener ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final Activity mActivty;
    private GoogleApiClient mGoogleApiClient;

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
    public boolean isconnected() {
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
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/sensor_data") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    SensorData sensorData;
                    for (String key : dataMap.keySet()) {
                        sensorData = new SensorData(key,dataMap.getFloatArray(key));
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
        //TODO:处理从手表传送过来的传感器数据(异步)
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
