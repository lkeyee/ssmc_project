package com.smcc.sensorrecord;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MobileActivity extends AppCompatActivity {
    private static final String TAG = "MobileActivity";
    private List<CheckBox> checkBoxes=new ArrayList<CheckBox>();
    public HashMap<String,Integer> sensorTypesMap;
    public Set<Integer> sensorTypes;
    public Button record;
    public Button connect;
    public Transfer transfer;

    //通过ServiceConnection来监听与service的连接
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorRecordService.SensorRecordBinder sensorRecordBinder = (SensorRecordService.SensorRecordBinder) service;
            //启动传感器
            sensorRecordBinder.start(sensorTypes);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_mobile);
        final String[] checkBoxText= new String[]{
                "acceleraation sensor", "gyroscope", "magnetometer","pressure sensor"
                ,"rotating vector sensor", "temperature sensor"
        };
        sensorTypesMap = new HashMap<String, Integer>(){};

        sensorTypesMap.put("acceleraation sensor", Sensor.TYPE_ACCELEROMETER);
        sensorTypesMap.put("gyroscope",Sensor.TYPE_GYROSCOPE);
        sensorTypesMap.put("magnetometer",Sensor.TYPE_MAGNETIC_FIELD);  //有争议
        sensorTypesMap.put("pressure sensor",Sensor.TYPE_PRESSURE);
        sensorTypesMap.put("rotating vector sensor",Sensor.TYPE_ROTATION_VECTOR);
        sensorTypesMap.put("temperature sensor",Sensor.TYPE_AMBIENT_TEMPERATURE);


        /*
         * 查看android api的Activity 中的 getLayoutInflater()方法 ：public LayoutInflater getLayoutInflater ()
         * 返回的是 LayoutInflater 这个就是Android中的动态加载布局
         * 查看 LayoutInflater，它表示会加载一个布局xml文件到相应的视图对象中，它主要使用inflate()方法来实现的
         * public View inflate (int resource, ViewGroup root)
         * 它会从指定的xml资源中加载一个新的视图布局，并且填充它的父视图，如果没有父视图的话，root参数设置为null。
         */
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_mobile, null);

        //给指定的checkbox赋值
        for(int i=0; i < checkBoxText.length; i++) {
            //先获得checkBox.xml的对象
            CheckBox checkBoxLayout = (CheckBox) getLayoutInflater().inflate(R.layout.checkbox, null);
            //循环加入CheckBox
            checkBoxes.add(checkBoxLayout);
            checkBoxes.get(i).setText(checkBoxText[i]);

            //实现了在main主布局中，通过LinearLayout在for循环中添加checkbox
            linearLayout.addView(checkBoxLayout, i + 1);
            setContentView(linearLayout);

            final Button record = (Button) findViewById(R.id.record);
        }

        record = (Button) findViewById(R.id.record);
        connect = (Button) findViewById(R.id.connect);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //建立手机与wear的连接
                if(record.getText().equals("确定")) {
                    //支线程
                    new connectToRecord().execute(null,null,null);
                    Toast.makeText(MobileActivity.this, "Success.", Toast.LENGTH_SHORT).show();
                    record.setText("取消");
                }
                else if(record.getText().equals("取消")){
                    Log.d(TAG, "onClick: 断开服务");
                    record.setText("确定");
                    Toast.makeText(MobileActivity.this, "Success.", Toast.LENGTH_SHORT).show();
                    unbindService(mConnection);
                }
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connect.getText().equals("连接")){
                    Toast.makeText(MobileActivity.this, "Succeed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: 连接wear");
                    Transfer mtransfer = new Transfer(MobileActivity.this);
                    transfer = mtransfer;
                    //连接wear
                    transfer.connect();
                    if(transfer.isconnected()){
                        Log.d(TAG, "onClick: transfer is connected");
                        connect.setText("断开");
                    }
                    else{
                        Log.d(TAG, "onClick: fail to connect");
                    }
                }
                else if (connect.getText().equals("断开")){
                    Toast.makeText(MobileActivity.this, "Succeed", Toast.LENGTH_SHORT).show();
                    if(transfer.isconnected()){
                        //断开连接
                        transfer.disconnect();
                        connect.setText("连接");
                        Log.d(TAG, "onClick: transfer is disconnected");
                    }
                    else {
                        Log.d(TAG, "onClick: fail to disconnect");
                    }
                }
            }
        });
    }

    class connectToRecord extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... strings) {
            HashSet<Integer> ss = new HashSet<Integer>();
            sensorTypes=ss;
            String str = "";
            //将所选的值加入str
            for (CheckBox box : checkBoxes) {
                if(box.isChecked()) {
                    str += box.getText() + "\n";
                    sensorTypes.add(sensorTypesMap.get(box.getText()));
                }
            }
            if ("".equals(str)) {
                str += "未选择";
            }
            Log.d(TAG, "doInBackground: 111");
            publishProgress(str);

            //onProgressUpdate(str);
            Log.d(TAG, "doInBackground: 222");

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String SensorSelected = values[0];

            if(!SensorSelected.equals("未选择")) {
                //启动SensorRecordService
                Intent intent = new Intent(MobileActivity.this, SensorRecordService.class);
                Log.d(TAG, "onProgressUpdate: 启动连接");
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
            else {
                Log.d(TAG, "onProgressUpdate: 未选择");
            }
            Log.d(TAG, "onProgressUpdate: "+SensorSelected);
            new AlertDialog.Builder(MobileActivity.this).setMessage(SensorSelected).setPositiveButton("关闭", null).create().show();
        }
    }
}
