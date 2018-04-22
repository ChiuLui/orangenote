package com.orange.orangenote.util;

/**
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/21 18:01
 * @copyright 赵蕾
 */

public class ContentUtil {

    /**
     * 把一段字符串截取成要显示的标题
     * @param content
     * @return
     */
    public static String getTitle(String content){
        String temp = content;
        //---有换行符
        if (temp.contains("\n")){
            return temp.substring(0,temp.indexOf("\n"));
        } else {
            //---没换行符
            //长度小于20个字,直接返回
            if (temp.length() < 20){
                return temp;
            } else {
                //否则截取前20个字
                return temp.substring(0,20);
            }
        }
    }

    /**
     * 把一段字符串截取成要显示的内容概略
     * @param content
     * @return
     */
    public static String getContent(String content){
        String temp = content;
        //---有换行符
        if (temp.contains("\n")){
            //截取换行符后面的文字
             temp = temp.substring(temp.indexOf("\n")+1);//+1是因为不截取换行符
             if (temp.contains("\n")){
                 temp = temp.substring(0,temp.indexOf("\n"));
                 if (temp.length() > 0) {
                     return temp;
                 } else {
                     return "";
                 }
             } else {
                 if (content.substring(0,content.indexOf("\n")).length() > 20) {
                     return "..." + temp;
                 } else {
                     return temp;
                 }
             }
        } else {
            //没换行符
            if (temp.length() < 20){
                return "";
            } else {
                return temp.substring(20);
            }
        }
    }

}
