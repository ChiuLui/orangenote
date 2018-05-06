package com.orange.orangenote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.orange.orangenote.db.Note;
import com.orange.orangenote.util.ContentUtil;

import org.litepal.crud.DataSupport;

import java.io.File;

import static android.content.Context.NOTIFICATION_SERVICE;

public class RemindReceiver extends BroadcastReceiver {

    private String nowTime;

    private String nowDate;

    private String nowYear;

    private String nowContent;

    private int nowId;

    private boolean nowState;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("TAG", "onReceive() 收到广播!!" );

        nowId = intent.getIntExtra("nowId", -1);
        nowYear = intent.getStringExtra("nowYear");
        nowDate = intent.getStringExtra("nowDate");
        nowTime = intent.getStringExtra("nowTime");
        nowContent = intent.getStringExtra("nowContent");
        nowState = intent.getBooleanExtra("nowState", true);


        Intent noteIntent = new Intent(context, NewNote.class);
        noteIntent.putExtra("nowId", nowId);
        noteIntent.putExtra("nowYear", nowYear);
        noteIntent.putExtra("nowDate", nowDate);
        noteIntent.putExtra("nowTime", nowTime);
        noteIntent.putExtra("nowContent", nowContent);
        noteIntent.putExtra("nowState", nowState);
        PendingIntent pi = PendingIntent.getActivity(context, 0, noteIntent, 0);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
                .setContentTitle(ContentUtil.getTitle(nowContent))//标题
                .setContentText(ContentUtil.getContent(nowContent))//内容
                .setWhen(System.currentTimeMillis())//显示时间
                .setSmallIcon(R.mipmap.ic_launcher)//小图片
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))//大图片
                .setContentIntent(pi)//跳转Intent
                .setAutoCancel(true)//点击自动取消
                .setSound(Uri.fromFile(new File("/system/media/audio/ringtones/Luna.ogg")))//铃声
                .setVibrate(new long[] {0, 1000, 1000, 1000})//震动(别忘了权限)
                .setLights(Color.GREEN, 1000, 1000)//呼吸灯
                .setDefaults(Notification.DEFAULT_ALL)//默认铃声
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        manager.notify(nowId, notification);

        NewNote.isRemind = false;
        Note note = DataSupport.find(Note.class, nowId);
        note.setRemind(false); // raise the price
        note.setToDefault("yearRemind");
        note.setToDefault("monthRemind");
        note.setToDefault("dayRemind");
        note.setToDefault("hourRemind");
        note.setToDefault("minuteRemind");
        note.save();
        //隐藏提示文本
        NewNote.textView_toolbar.setText("");
        NewNote.textView_toolbar.setVisibility(View.GONE);
    }
}
