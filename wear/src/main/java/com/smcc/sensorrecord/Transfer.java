package com.smcc.sensorrecord;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.smcc.sensordesc.SensorData;

/**
 * 与手机的数据通信处理
 */

public class Transfer implements DataApi.DataListener {
    private GoogleApiClient mGoogleApiClient;

    /**
     * 构造
     */
    public Transfer(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Wearable.DataApi.addListener(mGoogleApiClient, Transfer.this);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        //TODO:onConnectionSuspended
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        //TODO:onConnectionFailed
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    /**
     * 发送sensorData到mobile端
     *
     * @param data 待发送的数据
     */
    public void send(SensorData data) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/sensor_data");
        putDataMapReq.getDataMap().putFloatArray(data.getDataName(), data.getValues());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    /**
     * 建立连接
     */
    public void connect() {
        mGoogleApiClient.connect();
    }

    /**
     * 检查是否连接
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

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }
}
