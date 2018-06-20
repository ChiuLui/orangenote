package com.orange.orangenote.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Http工具类
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/06/17 14:10
 * @copyright 赵蕾
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request)
                .enqueue(callback);

    }
}
