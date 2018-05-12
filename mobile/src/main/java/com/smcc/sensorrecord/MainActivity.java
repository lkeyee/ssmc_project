package com.smcc.sensorrecord;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.IBinder;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smcc.sensordesc.SensorData;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    SensorRecordService.SensorRecordBinder binder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (SensorRecordService.SensorRecordBinder) iBinder;
            Set<Integer> list = new LinkedHashSet<Integer>() {
            };
            list.add(Sensor.TYPE_ACCELEROMETER);
            list.add(Sensor.TYPE_GRAVITY);
            list.add(Sensor.TYPE_GYROSCOPE);
            list.add(Sensor.TYPE_LINEAR_ACCELERATION);
            list.add(Sensor.TYPE_ROTATION_VECTOR);
            list.add(Sensor.TYPE_MAGNETIC_FIELD);
            binder.start(list);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    TextView tv_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_show = findViewById(R.id.textView);
        SensorDataIO sensorDataIO=new SensorDataIO();
        Intent startIntent=new Intent(this,SensorRecordService.class);
        bindService(startIntent,connection,BIND_AUTO_CREATE);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

}
