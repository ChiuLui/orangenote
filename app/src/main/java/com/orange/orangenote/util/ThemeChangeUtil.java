package com.orange.orangenote.util;

import android.app.Activity;

import com.orange.orangenote.R;

/**
 * 改变主题工具类
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/06/03 10:30
 * @copyright 赵蕾
 */

public class ThemeChangeUtil {

    /**
     * 改变主题
     * @param activity
     * @param isThemeLight
     */
    public static void changeTheme(Activity activity, Boolean isThemeLight){
        if (isThemeLight){
            activity.setTheme(R.style.ThemeLight);
        } else {
            activity.setTheme(R.style.ThemeDark);
        }
    }

}
