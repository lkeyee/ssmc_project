package com.smcc.sensorrecord;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WearActivity extends AppCompatActivity {
    private List<CheckBox> checkBoxes=new ArrayList<CheckBox>();
    private static final String TAG = "WearActivity";
    private String[] data = {"Apple","Banana","Orange"};
    private Button btn_connnect;
    public Set<Integer> sensorTypes;
    public HashMap<String,Integer> sensorTypesMap;
    private ListView listView;
    private ListAdapter adapter;
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
            //关闭传感器
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }else {
            Log.d(TAG, "onCreate: fail to hide");
        }

        sensorTypesMap = new HashMap<String, Integer>(){};

        sensorTypesMap.put("acceleraation sensor", Sensor.TYPE_ACCELEROMETER);
        sensorTypesMap.put("gyroscope",Sensor.TYPE_GYROSCOPE);
        sensorTypesMap.put("magnetometer",Sensor.TYPE_MAGNETIC_FIELD);  //有争议
        sensorTypesMap.put("pressure sensor",Sensor.TYPE_PRESSURE);
        sensorTypesMap.put("rotating vector sensor",Sensor.TYPE_ROTATION_VECTOR);
        sensorTypesMap.put("temperature sensor",Sensor.TYPE_AMBIENT_TEMPERATURE);

        initView();
        initData();

//        final String[] checkBoxText=new String[]{
//                "gyroscope", "magnetometer"
//        };
//
////        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_wear, null);
//        for(int i=0; i<checkBoxText.length; i++){
//            CheckBox checkBoxLayout = (CheckBox) getLayoutInflater().inflate(R.layout.checkbox, null);
//            checkBoxes.add(checkBoxLayout);
//            checkBoxes.get(i).setText(checkBoxText[i]);
//        }
//        ArrayAdapter<CheckBox> adapter = new ArrayAdapter<CheckBox>(
//                WearActivity.this, android.R.layout.simple_list_item_1, checkBoxes
//        );
//        ListView listView= (ListView) findViewById(R.id.wearable_list);
//        listView.setAdapter(adapter);

    }
    private void initView(){
        btn_connnect = (Button) findViewById(R.id.btn_connect);
        btn_connnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WearActivity.this, "Success.", Toast.LENGTH_SHORT).show();
                if(btn_connnect.getText().equals("确定")) {
                    //Servive
                    //支线程
                    new connectToRecord().execute(null,null,null);
                    Toast.makeText(WearActivity.this, "Success.", Toast.LENGTH_SHORT).show();
                    btn_connnect.setText("取消");

                    //Transfer
                    Transfer mtransfer = new Transfer(WearActivity.this);
                    transfer = mtransfer;
                    //连接wear
                    transfer.connect();
                    if(transfer.isconnected()){
                        Log.d(TAG, "onClick: transfer is connected");
                    }
                    else{
                        Log.d(TAG, "onClick: fail to connect");
                    }
                }
                else if(btn_connnect.getText().equals("取消")){
                    //Servive
                    Log.d(TAG, "onClick: 断开服务");
                    btn_connnect.setText("确定");
                    Toast.makeText(WearActivity.this, "Success.", Toast.LENGTH_SHORT).show();
                    unbindService(mConnection);

                    //Transfer
                    if(transfer.isconnected()){
                        //断开连接
                        transfer.disconnect();
                        Log.d(TAG, "onClick: transfer is disconnected");
                    }
                    else {
                        Log.d(TAG, "onClick: fail to disconnect");
                    }
                }
            }
        });
        listView = (ListView) findViewById(R.id.wearable_list);
    }

    private void initData() {
        // 默认显示的数据
        List<Bean> list = new ArrayList<Bean>();
        list.add(new Bean("acceleraation sensor"));
        list.add(new Bean("gyroscope"));
        list.add(new Bean("magnetometer"));
        list.add(new Bean("pressure sensor"));
        list.add(new Bean("rotating vector sensor"));
        list.add(new Bean("temperature sensor"));


        adapter = new ListAdapter(this);
        adapter.setData(list);
        listView.setAdapter(adapter);
    }

    class connectToRecord extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            HashSet<Integer> ss = new HashSet<Integer>();
            sensorTypes=ss;
            String str = "";

            //将所选的值加入str
            Map<Integer, Boolean> map = adapter.getMap();
            for(int i=0; i<map.size(); i++){
                if(map.get(i)){
                    Bean bean = (Bean) adapter.getItem(i);
                    str += bean.getTitle()+"\n";
                    sensorTypes.add(sensorTypesMap.get(bean.getTitle()));
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
                Intent intent = new Intent(WearActivity.this, SensorRecordService.class);
                Log.d(TAG, "onProgressUpdate: 启动连接");
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
            else {
                Log.d(TAG, "onProgressUpdate: 未选择");
            }
            Log.d(TAG, "onProgressUpdate: "+SensorSelected);
            new AlertDialog.Builder(WearActivity.this).setMessage(SensorSelected).setPositiveButton("关闭", null).create().show();
        }
    }
}
