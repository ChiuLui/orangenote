package com.orange.orangenote.db;

import org.litepal.crud.DataSupport;


/**
 * 便签类数据库
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/21 16:16
 * @copyright 赵蕾
 */

public class Note extends DataSupport {

    private int id;

    private String year;

    private String date;

    private String time;

    private String content;

    private Boolean isRemind;

    private int yearRemind;

    private int monthRemind;

    private int dayRemind;

    private int hourRemind;

    private int minuteRemind;

    private long timeStamp;

    private boolean isTop;

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getYearRemind() {
        return yearRemind;
    }

    public void setYearRemind(int yearRemind) {
        this.yearRemind = yearRemind;
    }

    public int getMonthRemind() {
        return monthRemind;
    }

    public void setMonthRemind(int monthRemind) {
        this.monthRemind = monthRemind;
    }

    public int getDayRemind() {
        return dayRemind;
    }

    public void setDayRemind(int dayRemind) {
        this.dayRemind = dayRemind;
    }

    public int getHourRemind() {
        return hourRemind;
    }

    public void setHourRemind(int hourRemind) {
        this.hourRemind = hourRemind;
    }

    public int getMinuteRemind() {
        return minuteRemind;
    }

    public void setMinuteRemind(int minuteRemind) {
        this.minuteRemind = minuteRemind;
    }

    public Boolean getRemind() {
        return isRemind;
    }

    public void setRemind(Boolean remind) {
        isRemind = remind;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
