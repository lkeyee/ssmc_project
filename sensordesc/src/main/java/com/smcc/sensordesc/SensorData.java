package com.smcc.sensordesc;

/**
 * 传感器数据的对象封装
 */
public class SensorData {

    public static final String ACCELEROMETER = "ACCELEROMETER";
    public static final String GRAVITY = "GRAVITY";
    public static final String GYROSCOPE = "GYROSCOPE";
    public static final String LINEAR_ACCELERATION = "LINEAR_ACCELERATION";
    public static final String ROTATION_VECTOR = "ROTATION_VECTOR";
    public static final String MAGNETIC_FIELD = "MAGNETIC_FIELD";
    public static final String ORIENTATION = "ORIENTATION";

    private String dataName;//对应传感器的名字
    private float values[];//待写入的数据
    private float sencondToBegin;//计算从任务开始到现在的用时
    private String currentTime;//当前时间戳
    private int dataType;


    public SensorData(String dataName, float[] values) {
        this.dataName = dataName;
        this.values = values;
    }

    public SensorData(float sencondToBegin,String currentTime,int dataType,float[] values){
        this.sencondToBegin=sencondToBegin;
        this.currentTime=currentTime;
        this.dataType=dataType;
        this.values=values;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public float[] getValues() {
        return values;
    }

    public void setValues(float[] values) {
        this.values = values;
    }

    public float getSencondToBegin() {
        return sencondToBegin;
    }

    public void setSencondToBegin(long sencondToBegin){
        this.sencondToBegin=sencondToBegin;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }
}
