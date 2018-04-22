package com.orange.orangenote.util;

import java.util.Calendar;

/**
 * 获取时间日期的工具类
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/21 10:49
 * @copyright 赵蕾
 */

public class DateUtil {

    /**
     * 获取年份
     * @return
     */
    public static String getYearDateTime() {
        Calendar calendar = Calendar.getInstance();
        //获取系统的日期
        //年
        int year = calendar.get(Calendar.YEAR);
        //月
        int month = calendar.get(Calendar.MONTH) + 1;
        //日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        //获取系统时间
        //小时
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //分钟
        int minute = calendar.get(Calendar.MINUTE);
        //秒
//        int second = calendar.get(Calendar.SECOND);
        return year + "年" + month + "月" + day + "日" + "  " + hour + ":" + minute;
    }

    public static String getNowYear(){
        Calendar calendar = Calendar.getInstance();
        //获取系统的日期
        //年
        int year = calendar.get(Calendar.YEAR);
        return year + "年";
    }

    public static String getNowDate(){

        Calendar calendar = Calendar.getInstance();
        //月
        int month = calendar.get(Calendar.MONTH) + 1;
        //日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "月" + day + "日";
    }

    public static String getNowTiem(){
        Calendar calendar = Calendar.getInstance();
        //小时
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //分钟
        int minute = calendar.get(Calendar.MINUTE);
        if (minute < 10){
            return hour + ":0" + minute;
        }
        return hour + ":" + minute;
    }

}
