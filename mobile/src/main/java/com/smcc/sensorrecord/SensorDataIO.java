package com.smcc.sensorrecord;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.smcc.sensordesc.SensorData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 进行传感器数据读写操作
 */
public class SensorDataIO {
    private String TAG = "SensorDataIO";

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
    Queue<SensorData> sensorDataQueue = new LinkedList<>();

    public SensorDataIO() {
        try {

            int numFile = dataType.length;

            String dir_path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(dir_path);
            File file = null;
            out = new FileOutputStream[numFile];

            if (!dir.exists())
                dir.mkdir();

            for (int i = 0; i < numFile; i++) {

                file = new File(dir_path + File.separator + dataType[i]);

                //删除旧文件，便于测试
                if (file.exists())
                    file.delete();

                file.createNewFile();

                out[i] = new FileOutputStream(file);
                out[i].write(contentTitle[i].getBytes());

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getArrayIndex(String dataName) {
        int index = 0;
        for (int i = 0; i < dataType.length; i++) {
            if (dataType[i].equals(dataName + "_DATA.txt")) {
                index = i;
                break;
            }
        }
        return index;
    }

    //封装好的写入的方法
    public void writeSensorData(SensorData sensorData) {
        new writeSensorDataTask().execute(sensorData);
    }

    //写入文件的异步线程
    class writeSensorDataTask extends AsyncTask<SensorData, String, Boolean> {

        SensorData data;
        String dataName;
        int index;
        float[] values;
        float secondToBegin;
        String currentTime;

        @Override
        protected Boolean doInBackground(SensorData... sensorData) {

            secondToBegin = sensorData[0].getSencondToBegin();
            index = getArrayIndex(sensorData[0].getDataName());


            //时间更改，将相同时间的全部写入
            if (nowTime == secondToBegin)
                sensorDataQueue.offer(sensorData[0]);
            else {

                nowTime = secondToBegin;
                while (sensorDataQueue.isEmpty() == false) {

                    SensorData sd = sensorDataQueue.poll();
                    //第七个文件存储时间相同的信息
                    WriteFileByStream(sd, 7);

                }
            }

            WriteFileByStream(sensorData[0], index);

            return true;
        }

        //实现了写入操作，需要传入data和对应的文件
        void WriteFileByStream(SensorData sensorData, int index) {
            try {

                Init(sensorData);

                out[index].write((secondToBegin + " ").getBytes());
                //因不同的输出流，其需要的编码不同，因此分情况设置，避免乱码
                if (index == 1 || index == 0 || index == 7)
                    out[index].write((currentTime + " ").getBytes("GB2312"));
                else
                    out[index].write((currentTime + " ").getBytes("UTF-8"));

                //讲数据格式化，精确到两位小数
                for (int i = 0; i < values.length; i++) {
                    String temp = new DecimalFormat("0.00").format(values[i]) + " ";
                    out[index].write(temp.getBytes());
                }
                if (index == 7)
                    out[index].write(dataName.getBytes("UTF-8"));
                out[index].write("\n".getBytes());

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        //用于数据的初始化
        void Init(SensorData sensorData) {
            data = sensorData;
            dataName = data.getDataName();
            //得到的当前数据类型
            values = data.getValues();
            secondToBegin = data.getSencondToBegin();
            currentTime = data.getCurrentTime();
        }


    }


}
