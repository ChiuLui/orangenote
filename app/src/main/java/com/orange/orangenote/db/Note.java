package com.orange.orangenote.db;

import org.litepal.crud.DataSupport;

/**
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
