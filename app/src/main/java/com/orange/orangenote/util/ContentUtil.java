package com.orange.orangenote.util;

/**
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/21 18:01
 * @copyright 赵蕾
 */

public class ContentUtil {

    private static final int TITLE_LENGHT = 20;
    private static final int CONTENT_LENGHT = 100;

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
            if (temp.length() < TITLE_LENGHT){
                return temp;
            } else {
                //否则截取前20个字
                return temp.substring(0, TITLE_LENGHT);
            }
        }
    }

    /**
     * 在瀑布流界面用于裁剪标题
     * @param content
     * @param length
     * @return
     */
    public static String getTitle(String content,int length){
        String temp = content;
        //---有换行符
        if (temp.contains("\n")){
            return temp.substring(0,temp.indexOf("\n"));
        } else {
            //---没换行符
            //长度小于20个字,直接返回
            if (temp.length() < length){
                return temp;
            } else {
                //否则截取前20个字
                return temp.substring(0, length);
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
                     temp = temp;
                 } else {
                     temp = "";
                 }
             } else {
                 if (content.substring(0,content.indexOf("\n")).length() > TITLE_LENGHT) {
                     temp = "..." + temp;
                 } else {
                     temp = temp;
                 }
             }
        } else {
            //没换行符
            if (temp.length() < TITLE_LENGHT){
                temp = "";
            } else {
                temp = temp.substring(TITLE_LENGHT);
            }
        }

        //长度小于100个字,直接返回
        if (temp.length() < CONTENT_LENGHT){
            temp = temp;
        } else {
            //否则截取前100个字
            temp = temp.substring(0,CONTENT_LENGHT) + "...";
        }
        return temp;
    }

    /**
     * 在瀑布流界面用于裁剪内容
     * @param content
     * @param title_lenght
     * @return
     */
    public static String getContent(String content, int title_lenght){
        String temp = content;
        //---有换行符
        if (temp.contains("\n")){
            //截取换行符后面的文字
            temp = temp.substring(temp.indexOf("\n")+1);//+1是因为不截取换行符
            if (temp.contains("\n")){
                temp = temp.substring(0,temp.indexOf("\n"));
                if (temp.length() > 0) {
                    temp = temp;
                } else {
                    temp = "";
                }
            } else {
                if (content.substring(0,content.indexOf("\n")).length() > title_lenght) {
                    temp = "..." + temp;
                } else {
                    temp = temp;
                }
            }
        } else {
            //没换行符
            if (temp.length() < title_lenght){
                temp = "";
            } else {
                temp = temp.substring(title_lenght);
            }
        }

        //长度小于100个字,直接返回
        if (temp.length() < CONTENT_LENGHT){
            temp = temp;
        } else {
            //否则截取前100个字
            temp = temp.substring(0,CONTENT_LENGHT) + "...";
        }
        return temp;
    }


    /**
     * 截取用于放在提示界面的内容
     * @param content
     * @return
     */
    public static String getDialogContent(String content){
        String temp = content;
        if (content.length() <= 60){
            return temp;
        }else {
            return temp.substring(0,60) + "...";
        }
    }

    /**
     * 用于更换HTML中的换行和空格
     * 用于剔除HTML格式
     * @param content
     * @return
     */
    public static String getNoHtmlContent(String content){
        String temp = content;
        temp = temp.replaceAll("<br>", "\n"); //更换<br>和标签
        temp = temp.replaceAll("&nbsp;", " "); //更换&nbsp;标签
        temp = temp.replaceAll("</?[^>]+>", ""); //剔出<html>的标签
//        temp = temp.replaceAll("<a>\\s*|\t|\r|\n</a>", "");//去除字符串中的空格,回车,换行符,制表符
        return temp;
    }

}
