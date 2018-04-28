package com.orange.orangenote.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * dp转px工具类
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/28 17:48
 * @copyright 赵蕾
 */

public class dp2px {
    /**
     * dp转为px
     * @param context  上下文
     * @param dipValue dp值
     * @return
     */
    public static int dip2px(Context context, float dipValue)
    {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
    }
}
