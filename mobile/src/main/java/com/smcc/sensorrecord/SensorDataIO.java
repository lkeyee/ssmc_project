package com.smcc.sensorrecord;

import android.hardware.Sensor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.smcc.sensordesc.SensorData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 进行传感器数据读写操作
 */
public class SensorDataIO {
    private String TAG = "SensorDataIO";

    private OutputStream out[];
    private Map<String, Integer> sensorMap = new HashMap<>();
    private BlockingQueue<SensorData> sensorDataQueue = new LinkedBlockingQueue<>();
    private Queue<SensorData> sensorDataSameQueue = new LinkedList<>();//相同的时间的队列
    private Boolean isRunning = true;
    private String[] fileName;
    private float nowTime=0;

    public SensorDataIO(Map<String, Integer> map) {
        this.sensorMap = map;
        fileName = map.keySet().toArray(new String[map.size()]);
        init();
        new Thread(runnable).start();
    }

    private void init() {
        try {
            int numFile = fileName.length, i;
            String dir_path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(dir_path);
            File file = null;
            out = new FileOutputStream[numFile + 1];
            if (!dir.exists())
                dir.mkdir();
            for (i = 0; i < fileName.length; i++) {
                file = new File(dir_path + File.separator + fileName[i] + ".txt");
                //删除旧文件，便于测试
                if (file.exists())
                    file.delete();
                file.createNewFile();
                out[i] = new FileOutputStream(file);
            }
            File sum = new File(dir_path + File.separator + "SameTime.txt");
            out[i] = new FileOutputStream(sum);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(SensorData data) {
        try {
            sensorDataQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        isRunning = false;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    SensorData sensorData = sensorDataQueue.take();
                    writeSensorData(sensorData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    //封装好的写入的方法
    private void writeSensorData(SensorData sensorData) {
        float secondToBegin = sensorData.getSencondToBegin();
        String currentTime=sensorData.getCurrentTime();
        int index = getArrayIndex(getKey(sensorData.getDataType()));
        //时间更改，将相同时间的全部写入
        if (nowTime==secondToBegin )
            sensorDataSameQueue.offer(sensorData);
        else {
            nowTime=secondToBegin;
            boolean first = true;
            int sameTimeIndex=fileName.length;
            try {
                while (sensorDataSameQueue.isEmpty() == false) {
                    if (first) {
                        out[sameTimeIndex].write("START\n".getBytes());
                        out[sameTimeIndex].write((currentTime+ "\n").getBytes());
                        first = false;
                    }
                    SensorData sd = sensorDataSameQueue.poll();
                    float[] values = sd.getValues();
                    String type = getKey(sd.getDataType());
                    type = type.substring(type.indexOf("sensor") + 7,type.indexOf("sensor")+11) +" ";
                    out[sameTimeIndex].write(type.getBytes());
                    out[sameTimeIndex].write(("x:" + new DecimalFormat("0.00").format(values[0])  +
                            " y:"+ new DecimalFormat("0.00").format(values[1]) + " " +
                            " z:" + new DecimalFormat("0.00").format(values[2]) + "\n").getBytes());
                    Log.d(TAG, "writeSensorData: "+"write");
                }
                if (first==false)
                    out[sameTimeIndex].write("END\n\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        WriteFileByStream(sensorData, index);
    }


    //实现了写入操作，需要传入data和对应的文件
    void WriteFileByStream(SensorData sensorData, int index) {
        float[] values = sensorData.getValues();
        float secondToBegin = sensorData.getSencondToBegin();
        String currentTime = sensorData.getCurrentTime();
        try {
            out[index].write((secondToBegin + " ").getBytes());
            out[index].write((currentTime + " ").getBytes());
            //讲数据格式化，精确到两位小数
            for (int i = 0; i < values.length; i++) {
                String temp = new DecimalFormat("0.00").format(values[i]) + " ";
                out[index].write(temp.getBytes());
            }
            out[index].write("\n".getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getArrayIndex(String dataName) {
        int index = 0;
        for (int i = 0; i < fileName.length; i++) {
            if (fileName[i].equals(dataName)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private String getKey(int value) {
        String key = null;
        for (Map.Entry<String, Integer> entry : sensorMap.entrySet()) {
            if (value == entry.getValue()) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }

}
