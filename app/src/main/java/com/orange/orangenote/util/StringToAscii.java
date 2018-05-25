package com.orange.orangenote.util;

/**
 * 字符串转Ascii码再叠加, 用于比较字符串.
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/22 17:00
 * @copyright 赵蕾
 */

public class StringToAscii {

    /**
     * 字符串转ASCII码
     * @param value
     * @return
     */
    public static int stringToAscii(String value){
        char[] chars = value.toCharArray();
        int result = 0;
        for (char char1 : chars){
            result += (int) char1;
        }
        return result;

    }

}
