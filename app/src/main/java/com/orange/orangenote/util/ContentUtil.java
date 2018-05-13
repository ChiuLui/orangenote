package com.orange.orangenote.util;

/**
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/21 18:01
 * @copyright 赵蕾
 */

public class ContentUtil {

    private static final int LENGHT = 20;

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
            if (temp.length() < LENGHT){
                return temp;
            } else {
                //否则截取前20个字
                return temp.substring(0,LENGHT);
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
                 if (content.substring(0,content.indexOf("\n")).length() > LENGHT) {
                     return "..." + temp;
                 } else {
                     return temp;
                 }
             }
        } else {
            //没换行符
            if (temp.length() < LENGHT){
                return "";
            } else {
                return temp.substring(LENGHT);
            }
        }
    }

    public static String getDialogContent(String content){
        String temp = content;
        if (content.length() <= 60){
            return temp;
        }else {
            return temp.substring(0,60) + "...";
        }
    }

    public static String getNoHtmlContent(String content){
        String temp = content;
        temp = temp.replaceAll("<br>", "\n"); //更换<br>和标签
        temp = temp.replaceAll("&nbsp;", " "); //更换&nbsp;标签
        temp = temp.replaceAll("</?[^>]+>", ""); //剔出<html>的标签
//        temp = temp.replaceAll("<a>\\s*|\t|\r|\n</a>", "");//去除字符串中的空格,回车,换行符,制表符
        return temp;
    }

}
