package com.smcc.sensorrecord;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MobileActivity extends AppCompatActivity {

    private static final String TAG = "MobileActivity";

    private static final int PERMISSION_REQUESTCODE_RECORD = 1;

    //在这里设置想提供给用户的监听接口
    //保存 CheckBox 和 Sensor 的映射
    private List<SensorCheckBox> mCheckboxToSensorMapping = new ArrayList<>(Arrays.asList(
            new SensorCheckBox(Sensor.STRING_TYPE_ACCELEROMETER, Sensor.TYPE_ACCELEROMETER),
            new SensorCheckBox(Sensor.STRING_TYPE_GRAVITY, Sensor.TYPE_GRAVITY),
            new SensorCheckBox(Sensor.STRING_TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE),
            new SensorCheckBox(Sensor.STRING_TYPE_LINEAR_ACCELERATION, Sensor.TYPE_LINEAR_ACCELERATION),
            new SensorCheckBox(Sensor.STRING_TYPE_ROTATION_VECTOR, Sensor.TYPE_ROTATION_VECTOR),
            new SensorCheckBox(Sensor.STRING_TYPE_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD),
            new SensorCheckBox(Sensor.STRING_TYPE_ORIENTATION, Sensor.TYPE_ORIENTATION)));

    private Button record;
    private Button connect;
    private Transfer mTransfer;
    private Set<Integer> sensorSelected = new HashSet<>();
    private Map<String,Integer> sensorSelectedMap=new HashMap<>();


    /**
     * 建立 Checkbox 与 Sensor 的映射关系
     */
    private class SensorCheckBox {
        CheckBox checkBox;
        int sensor;
        private String checkBoxText;

        /**
         * 构造方法
         *
         * @param checkBoxText 需要 CheckBox 展示的文字
         * @param sensor       {@link Sensor#getType()}
         */
        SensorCheckBox(String checkBoxText, int sensor) {
            this.checkBoxText = checkBoxText;
            this.sensor = sensor;
        }

        void loadCheckBox() {
            checkBox = new CheckBox(MobileActivity.this);
            checkBox.setText(checkBoxText);
        }
    }

    //通过ServiceConnection来监听与service的连接
    private ServiceConnection mConnection = new ServiceConnection() {

        private SensorRecordService.SensorRecordBinder mService;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = (SensorRecordService.SensorRecordBinder) service;
            mService.start(sensorSelectedMap);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.stop();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //动态加载布局
        intiViews();
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermission(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.BODY_SENSORS})) {
                    recordSensor();
                }
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectWear();
            }
        });
    }

    private void intiViews() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        for (SensorCheckBox sensorCheckBox : mCheckboxToSensorMapping) {
            sensorCheckBox.loadCheckBox();//加载文字到checkbox上
            layout.addView(sensorCheckBox.checkBox);
        }
        record = new Button(this);
        connect = new Button(this);
        layout.addView(record);
        record.setText("确定");
        record.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        record.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(connect);
        connect.setText("连接");
        connect.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        connect.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        setContentView(layout);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void recordSensor() {
        //建立手机与wear的连接
        if (record.getText().equals("确定")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("已经选中:\n");
            for (SensorCheckBox sensorCheckBox : mCheckboxToSensorMapping) {
                if (sensorCheckBox.checkBox.isChecked()) {
                    stringBuilder.append(sensorCheckBox.checkBox.getText().toString());
                    stringBuilder.append("\n");
                    sensorSelected.add(sensorCheckBox.sensor);
                    sensorSelectedMap.put(sensorCheckBox.checkBox.getText().toString(),sensorCheckBox.sensor);
                }
            }
            showToast("Success.");
            new AlertDialog.Builder(MobileActivity.this)
                    .setMessage(stringBuilder.toString())
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MobileActivity.this, SensorRecordService.class);
                            bindService(intent, mConnection, BIND_AUTO_CREATE);
                        }
                    })
                    .create()
                    .show();
            record.setText("取消");
        } else if (record.getText().equals("取消")) {
            record.setText("确定");
            showToast("Success.");
            unbindService(mConnection);
        }
    }

    private void connectWear() {
        if (connect.getText().equals("连接")) {
            mTransfer = new Transfer(MobileActivity.this);
            mTransfer.connect();
            if (mTransfer.isConnected()) {
                connect.setText("断开");
            } else {
                showToast("failed");
                Log.e(TAG, "onClick: fail to connect");
            }
        } else if (connect.getText().equals("断开")) {
            Toast.makeText(MobileActivity.this, "Succeed", Toast.LENGTH_SHORT).show();
            if (mTransfer.isConnected()) {
                mTransfer.disconnect();
                connect.setText("连接");
                Log.d(TAG, "onClick: mTransfer is disconnected");
            } else {
                Log.e(TAG, "onClick: fail to disconnect");
            }
        }
    }

    private boolean checkAndRequestPermission(String[] permissions) {
        List<String> permissionList = new ArrayList<>(Arrays.asList(permissions));
        List<String> needRequest = new ArrayList<>();
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                needRequest.add(permission);
            }
        }
        if (!needRequest.isEmpty()) {//说明肯定有拒绝的权限
            ActivityCompat.requestPermissions(this,
                    needRequest.toArray(new String[needRequest.size()]),
                    PERMISSION_REQUESTCODE_RECORD);
        }
        return needRequest.isEmpty();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUESTCODE_RECORD:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            showToast("拒绝权限无法正常工作");
                            return;
                        }
                    }
                    recordSensor();
                }
                break;
            default:
                break;
        }
    }
}
