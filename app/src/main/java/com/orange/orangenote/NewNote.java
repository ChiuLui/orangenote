package com.orange.orangenote;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.orange.orangenote.db.Note;
import com.orange.orangenote.util.DateUtil;
import com.orange.orangenote.util.StringToAscii;

import org.litepal.crud.DataSupport;

import java.util.Calendar;
import java.util.TimeZone;

public class NewNote extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private FloatingActionButton floatingActionButton;

    private String nowTime;

    private String nowDate;

    private String nowYear;

    private String nowContent;

    private int nowId;

    private boolean nowState;

    private Toolbar toolbar;

    private EditText editText;

    private SharedPreferences.Editor editor;

    private SharedPreferences prefer;

    private int mYear, mMonth, mDay;
    private Calendar calender;

    public static boolean isRemind;

    private Menu menu;

    public static TextView textView_toolbar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        calender = Calendar.getInstance();

        //接收Intent
        final Intent intent = getIntent();
        nowId = intent.getIntExtra("nowId", -1);
        nowYear = intent.getStringExtra("nowYear");
        nowDate = intent.getStringExtra("nowDate");
        nowTime = intent.getStringExtra("nowTime");
        nowContent = intent.getStringExtra("nowContent");
        nowState = intent.getBooleanExtra("nowState", false);
        isRemind = intent.getBooleanExtra("isRemind", false);

        collapsingToolbarLayout = findViewById(R.id.collapsingtoolbar_newnote);
        collapsingToolbarLayout.setTitle(nowDate + " " + nowTime);

        editText = findViewById(R.id.edit_newnote_content);

        editText.setText(nowContent);

        textView_toolbar = findViewById(R.id.textview_toolbar_newnote);

        //如果传进来的标志为旧便签就不显示光标
        if (nowState) {
            editText.setCursorVisible(false);
        }

        //为edittext设置触摸事件
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //一触摸就显示光标
                editText.setCursorVisible(true);
                return false;
            }
        });

        editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        prefer = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = findViewById(R.id.toolbar_newnote);
        //初始化工具栏
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.back);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        floatingActionButton = findViewById(R.id.fab_nwenote_ok);
        //为浮动按钮设置点击事件
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存
                saveToDatabast();
                finish();
            }
        });

    }

    /**
     * 用于把数据保存到数据库
     */
    private void saveToDatabast() {
        //如果数据现在的数据和传过来的数据不一样(修改了)而且不为null, 就保存数据.
        if (!(StringToAscii.stringToAscii(nowContent) == StringToAscii.stringToAscii(editText.getText().toString())) && !((editText.getText()).length() <= 0) || editText.getText().equals("") || editText.getText() == null || editText.getText().equals(" ") || editText.getText().equals("\n")) {
            //创建表
            Note note = new Note();
            //如果是旧便签被修改就重新得到现在的时间, 并删除数据库中的旧表
            if (nowState) {
                note.setYear(DateUtil.getNowYear());
                note.setDate(DateUtil.getNowDate());
                note.setTime(DateUtil.getNowTiem());
                DataSupport.deleteAll(Note.class, "id = ?", nowId + "");
            } else {
                //如果是新便签, 就用创建便签时的时间
                note.setYear(nowYear);
                note.setDate(nowDate);
                note.setTime(nowTime);
            }
            //保存内容
            note.setContent(editText.getText().toString());
            note.save();
        }
        //如果旧便签修改了变成空便签, 就从数据库删除
        if ((editText.getText()).length() <= 0 || editText.getText().equals("") || editText.getText() == null || editText.getText().equals(" ") || editText.getText().equals("\n")) {
            DataSupport.deleteAll(Note.class, "id = ?", nowId + "");
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        //加载菜单文件
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.delete_toolbar).setVisible(true);
        menu.findItem(R.id.view_toolbar).setVisible(false);
        menu.findItem(R.id.allcheck_toolbar).setVisible(false);
        menu.findItem(R.id.remind_toolbar).setVisible(true);
        if (isRemind){
            menu.findItem(R.id.remind_toolbar).setIcon(R.drawable.unclock);
            showRemindTime();
        } else {
            menu.findItem(R.id.remind_toolbar).setIcon(R.drawable.clock);
            //隐藏提醒时间
            textView_toolbar.setText("");
            textView_toolbar.setVisibility(View.GONE);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 当ToolBar被点击
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //工具栏的home键被点击就保存数据
            case android.R.id.home:
                saveToDatabast();
                finish();
                break;
            //提醒功能
            case R.id.remind_toolbar:
                if (!isRemind) {
                    saveToDatabast();
                    setRemind();
                } else {
                    stopRemind(nowId);
                    Toast.makeText(this, "提醒已关闭", Toast.LENGTH_SHORT).show();
                }
                break;
            //删除键被点击
            case R.id.delete_toolbar:
                //把数据保存到SP存储中
                editor.putString("delete", String.valueOf(editText.getText()));
                editor.apply();
                //退出键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                editText.setText("");//置空
                editText.setCursorVisible(false);//取消光标
                //设置Snackbar事件, 增加用户友好
                Snackbar.make(collapsingToolbarLayout, "已清空文本", Snackbar.LENGTH_LONG).setAction("恢复", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //可恢复
                        editText.setText(prefer.getString("delete", null));
                    }
                }).show();
                break;
        }
        return true;
    }

    private void setRemind() {
        //调用日期Dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, final int month, int dayOfMonth) {

                mYear = year;
                mMonth = month;
                mDay = dayOfMonth;

                //调用时间Dialog
                TimePickerDialog dialog = new TimePickerDialog(NewNote.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //在数据库更新状态
                        isRemind = true;
                        Note note = DataSupport.find(Note.class, nowId);
                        note.setRemind(isRemind);
                        note.setYearRemind(mYear);
                        note.setMonthRemind(mMonth);
                        note.setDayRemind(mDay);
                        note.setHourRemind(hourOfDay);
                        note.setMinuteRemind(minute);
                        note.save();
                        //更换图标
                        menu.findItem(R.id.remind_toolbar).setIcon(R.drawable.unclock);
                        //设置定时任务
                        startRemind(mYear, mMonth, mDay, hourOfDay, minute);
                        //显示提示时间
                        showRemindTime();
                    }
                }, calender.get(Calendar.HOUR), calender.get(Calendar.MINUTE), true);
                dialog.show();

            }
        }, calender.get(Calendar.YEAR), calender.get(Calendar.MONTH),
                calender.get(Calendar.DAY_OF_MONTH));
        //设置显示的最小日期
        Calendar minCalendar = Calendar.getInstance();
        minCalendar.set(Calendar.YEAR, calender.get(Calendar.YEAR));
        minCalendar.set(Calendar.MONTH, calender.get(Calendar.MONTH));
        minCalendar.set(Calendar.DAY_OF_MONTH, calender.get(Calendar.DAY_OF_MONTH));
//        datePickerDialog.setTitle("设置日期");
        datePickerDialog.getDatePicker().setMinDate(minCalendar.getTimeInMillis());
        //显示Dialog
        datePickerDialog.show();
    }

    /**
     * 设置提醒
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     */
    private void startRemind(int year, int month, int day, int hour, int minute) {
        Log.e("TAG", "startRemind()" + "已设置提醒" + mYear + "年" + month + "月" + day + "日" + " " + hour + ":" + minute );
        //得到日历实例，主要是为了下面的获取时间
        Calendar mCalendar = Calendar.getInstance();
//        mCalendar.setTimeInMillis(System.currentTimeMillis());

//        //获取当前毫秒值
        long systemTime = System.currentTimeMillis();

//        //是设置日历的时间，主要是让日历的年月日和当前同步
//        mCalendar.setTimeInMillis(System.currentTimeMillis());
        //设置日期
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        //设置在几点提醒  设置的为传进来的 hour点
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        //设置在几分提醒  设置的为传进来的 minute分
        mCalendar.set(Calendar.MINUTE, minute);
        //下面这两个看字面意思也知道
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        //上面设置的就是日期和时间

        //        //获取上面设置的 时间 的毫秒值
//        long selectTime = mCalendar.getTimeInMillis();
//
//        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
//        if(systemTime > selectTime) {
//            mCalendar.add(Calendar.DAY_OF_MONTH, 1);
//        }

        //AlarmReceiver.class为广播接受者
        Intent intent = new Intent(NewNote.this, RemindReceiver.class);
        intent.putExtra("nowId", nowId);
        intent.putExtra("nowYear", nowYear);
        intent.putExtra("nowDate", nowDate);
        intent.putExtra("nowTime", nowTime);
        intent.putExtra("nowContent", nowContent);
        intent.putExtra("nowState", nowState);
        PendingIntent pi = PendingIntent.getBroadcast(NewNote.this, nowId, intent, 0);
        //得到AlarmManager实例
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        //**********注意！！下面的两个根据实际需求任选其一即可*********

        /**
         * 单次提醒
         * mCalendar.getTimeInMillis() 上面设置时间的毫秒值
         */
        am.setExact(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pi);
                //获取上面设置的 时间 的毫秒值
        long selectTime = mCalendar.getTimeInMillis();

        // 如果当前时间小于设置的时间，(合法)那么就弹出提示框
        if(systemTime < selectTime) {
            String h = hour + "";
            String m = minute + "";
            if (hour < 10) {
                h = "0" + hour;
            }
            if (minute < 10) {
                m = "0" + minute;
            }
            Toast.makeText(this, "将会在: " + mYear + "年" + ++month  + "月" + day + "日" + " " + h + ":" + m + " 提醒您", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 取消提醒
     */
    public void stopRemind(int requestCode) {

        Intent intent = new Intent(NewNote.this, RemindReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(NewNote.this, requestCode,
                intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        //取消警报
        am.cancel(pi);
        //更换图标
        menu.findItem(R.id.remind_toolbar).setIcon(R.drawable.clock);
        //隐藏提醒时间
        textView_toolbar.setText("");
        textView_toolbar.setVisibility(View.GONE);
        //更换状态
        isRemind = false;
        //数据库更新状态
        Note note = DataSupport.find(Note.class, nowId);
        note.setRemind(isRemind);
        note.setToDefault("yearRemind");
        note.setToDefault("monthRemind");
        note.setToDefault("dayRemind");
        note.setToDefault("hourRemind");
        note.setToDefault("minuteRemind");
        note.save();
    }

    /**
     * 显示提示时间的方法
     */
    private void showRemindTime() {
        //显示提示时间
        Calendar calendar = Calendar.getInstance();
        Note note = DataSupport.find(Note.class, nowId);
        String hour = note.getHourRemind() + "";
        String minute = note.getMinuteRemind() + "";
        if (note.getHourRemind() < 10) {
            hour = "0" + note.getHourRemind();
        }
        if (note.getMinuteRemind() < 10) {
            minute = "0" + note.getMinuteRemind();
        }
        //同年
        if (note.getYearRemind() == calendar.get(Calendar.YEAR)){
            //同月
            if (note.getMonthRemind() == calendar.get(Calendar.MONTH)){
                //同日
                if (note.getDayRemind() == calendar.get(Calendar.DAY_OF_MONTH)){
                    //同小时
                    if (note.getHourRemind() == calendar.get(Calendar.HOUR_OF_DAY)){
                        //同分钟
                        if (note.getMinuteRemind() <= calendar.get(Calendar.MINUTE)) {
                            Toast.makeText(this, "请选择大于当前的时间", Toast.LENGTH_SHORT).show();
                            stopRemind(nowId);
//                            textView_toolbar.setText("明天" + note.getHourRemind() + ":" + note.getMinuteRemind());
                        } else {
                            textView_toolbar.setText(note.getMinuteRemind() - calendar.get(Calendar.MINUTE) + "分钟后");
                        }
                    } else {
                        if (note.getHourRemind() <= calendar.get(Calendar.HOUR_OF_DAY)){
                            Toast.makeText(this, "请选择大于当前的时间", Toast.LENGTH_SHORT).show();
                            stopRemind(nowId);
//                            textView_toolbar.setText("明天" + note.getHourRemind() + ":" + note.getMinuteRemind());
                        } else {
                            textView_toolbar.setText("今天" + hour + ":" + minute);
                        }
                    }
                } else {
                    if (note.getDayRemind() - calendar.get(Calendar.DAY_OF_MONTH) == 1){
                        textView_toolbar.setText("明天" + hour + ":" + minute);
                    } else if (note.getDayRemind() - calendar.get(Calendar.DAY_OF_MONTH) == 2){
                        textView_toolbar.setText("后天" + hour + ":" + minute);
                    } else {
                        textView_toolbar.setText(note.getMonthRemind() + 1 + "月" + note.getDayRemind() + "日" + hour + ":" + minute);
                    }
                }
            } else {
                textView_toolbar.setText(note.getMonthRemind() + 1 + "月" + note.getDayRemind() + "日" + hour + ":" + minute);
            }
        } else {
            textView_toolbar.setText(note.getYearRemind() + "日" + note.getMonthRemind() + 1 + "月" + note.getDayRemind() + "日" + hour + ":" + minute);
        }
        textView_toolbar.setVisibility(View.VISIBLE);
    }

    /**
     * 监听回退键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                //保存数据
                saveToDatabast();
                break;
        }
        return true;
    }

}
