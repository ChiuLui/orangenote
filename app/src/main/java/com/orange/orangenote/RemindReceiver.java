package com.orange.orangenote;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.orange.orangenote.db.Note;
import com.orange.orangenote.util.ContentUtil;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class RemindReceiver extends BroadcastReceiver {

    private String nowTime;

    private String nowDate;

    private String nowYear;

    private String nowContent;

    private int nowId;

    private boolean nowState;

    private MediaPlayer mMediaPlayer = new MediaPlayer();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Log.e("TAG", "onReceive() 收到广播!!");

        nowId = intent.getIntExtra("nowId", -1);
        nowYear = intent.getStringExtra("nowYear");
        nowDate = intent.getStringExtra("nowDate");
        nowTime = intent.getStringExtra("nowTime");
        nowContent = intent.getStringExtra("nowContent");
        nowState = intent.getBooleanExtra("nowState", true);

        //-------------------------------------------如果没有悬浮窗权限
        if (!Settings.canDrawOverlays(context)) {
            //发通知
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
                    .setContentTitle(ContentUtil.getTitle(ContentUtil.getNoHtmlContent(nowContent)))//标题
                    .setContentText(ContentUtil.getContent(ContentUtil.getNoHtmlContent(nowContent)))//内容
                    .setWhen(System.currentTimeMillis())//显示时间
                    .setSmallIcon(R.mipmap.ic_launcher)//小图片
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))//大图片
                    .setContentIntent(pi)//跳转Intent
                    .setAutoCancel(true)//点击自动取消
                    .setSound(Uri.fromFile(new File("/system/media/audio/ringtones/Luna.ogg")))//铃声
                    .setVibrate(new long[]{0, 1000, 1000, 1000})//震动(别忘了权限)
                    .setLights(Color.GREEN, 1000, 1000)//呼吸灯
                    .setDefaults(Notification.DEFAULT_ALL)//默认铃声
                    .setPriority(Notification.PRIORITY_MAX)
                    .build();
            manager.notify(nowId, notification);
        } else {
        //-------------------------------------如果有悬浮窗权限
            //点亮屏幕
            wakeUpAndUnlock(context);
            //弹出dialog
            final Intent noteIntent1 = new Intent(context, NewNote.class);
            noteIntent1.putExtra("nowId", nowId);
            noteIntent1.putExtra("nowYear", nowYear);
            noteIntent1.putExtra("nowDate", nowDate);
            noteIntent1.putExtra("nowTime", nowTime);
            noteIntent1.putExtra("nowContent", nowContent);
            noteIntent1.putExtra("nowState", nowState);
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(context);
            builder.setTitle("橙子便签提醒");
            builder.setIcon(R.mipmap.orangenote_circle);
            builder.setMessage(ContentUtil.getDialogContent(ContentUtil.getNoHtmlContent(nowContent)));
            builder.setCancelable(false);
            builder.setPositiveButton("查看", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                    }
                    context.startActivity(noteIntent1);
                }
            });
            builder.setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                    }
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);//位于所有内容之上
            alertDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
            alertDialog.show();

            //播放铃声
            startAlarm(context);

        }

        //修改数据库数据
        NewNote.isRemind = false;
        Note note = DataSupport.find(Note.class, nowId);
        note.setRemind(false); // raise the price
        note.setToDefault("yearRemind");
        note.setToDefault("monthRemind");
        note.setToDefault("dayRemind");
        note.setToDefault("hourRemind");
        note.setToDefault("minuteRemind");
        note.save();
    }

    /**
     * 唤醒屏幕
     *
     * @param context
     */
    public static void wakeUpAndUnlock(Context context) {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire(10000); // 点亮屏幕
            wl.release(); // 释放
        }
        // 屏幕解锁
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        // 屏幕锁定
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard(); // 解锁
    }


    /**
     * 播放闹铃
     *
     * @param context
     */
    private void startAlarm(Context context) {
        mMediaPlayer = MediaPlayer.create(context, getSystemDefultRingtoneUri());
        mMediaPlayer.setLooping(true);
        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }

    //获取系统默认铃声的Uri
    private Uri getSystemDefultRingtoneUri() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    }

}
