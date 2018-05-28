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

    /** 便签的ID */
    private int id;

    /** 便签的年份 */
    private String year;

    /** 便签的日期 */
    private String date;

    /** 便签的时间 */
    private String time;

    /** 便签的内容 */
    private String content;

    /** 是否设置了提醒 */
    private boolean isRemind;

    /** 设置的提醒 : 年份 */
    private int yearRemind;

    /** 设置的提醒 : 月份 */
    private int monthRemind;

    /** 设置的提醒 : 日期 */
    private int dayRemind;

    /** 设置的提醒 : 小时 */
    private int hourRemind;

    /** 设置的提醒 : 分钟 */
    private int minuteRemind;

    /** 毫秒值 */
    private long timeStamp;

    /** 是否为置顶便签 */
    private boolean isTop;

    /** 是否为私密便签 */
    private boolean isSecret;

    public boolean isSecret() {
        return isSecret;
    }

    public void setSecret(boolean secret) {
        isSecret = secret;
    }

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

    public boolean isRemind() {
        return isRemind;
    }

    public void setRemind(boolean remind) {
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
