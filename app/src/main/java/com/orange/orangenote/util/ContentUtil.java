package com.orange.orangenote.util;

/**
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/21 18:01
 * @copyright 赵蕾
 */

public class ContentUtil {

    public static String getTitle(String content){
        String temp = content;
        //截取的长度 < 20 后面还有
        //
        if ((temp.substring(0,temp.indexOf("\n"))).length() < 20 && temp.length() > (temp.substring(0,temp.indexOf("\n"))).length()){
            return temp.substring(0,temp.indexOf("\n"));
        } else {
            return temp;
        }
    }

    public static String getContent(String content){
        String temp = content;
        //截取的长度 < 20 后面还有
        if ((temp.substring(0,temp.indexOf("\n"))).length() < 20 && temp.length() > (temp.substring(0,temp.indexOf("\n"))).length()){
            return temp.substring(temp.indexOf("\n"));
        } else if (temp.length() < 20){//截取长度的 < 20 后面没有了
            return "";
        }
        return temp;
    }

}
