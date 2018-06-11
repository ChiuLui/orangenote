package com.orange.orangenote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;
import com.orange.orangenote.db.Note;
import com.orange.orangenote.db.NoteImagePath;
import com.orange.orangenote.util.DateUtil;
import com.orange.orangenote.util.StringToAscii;
import com.orange.orangenote.util.ThemeChangeUtil;
import com.orange.orangenote.util.UriToPath;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import id.zelory.compressor.Compressor;
import jp.wasabeef.richeditor.RichEditor;


import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static com.orange.orangenote.MainActivity.isTheme_Light;


public class NewNote extends AppCompatActivity implements View.OnLayoutChangeListener {

    /**
     * 折叠工具栏
     */
    private CollapsingToolbarLayout collapsingToolbarLayout;

    /**
     * 保存按钮
     */
    private FloatingActionButton floatingActionButton;

    /**
     * 传过来的分钟
     */
    private String nowTime;

    /**
     * 传过来的日期
     */
    private String nowDate;

    /**
     * 传过来的年份
     */
    private String nowYear;

    /**
     * 传过来的内容
     */
    private String nowContent;

    /**
     * 传过来的Note对象Id
     */
    private int nowId;

    /**
     * 判断当前便签是否为旧标签  true:旧标签 false:新便签
     */
    private boolean nowState;

    /**
     * 传过来的是否私密便签
     */
    private boolean nowIsSecret;

    /**
     * ActionBarLayout布局
     */
    private AppBarLayout appbarlayout_newnote;

    /**
     * 用来代替ActionBar的ToolBar
     */
    private Toolbar toolbar;

    /**
     * ActionBar控件
     */
    private ActionBar actionBar;

    /**
     * 富文本
     */
    private RichEditor richText;

    /**
     * SP存储的保存对象
     */
    private SharedPreferences.Editor editor;

    /**
     * 取SP存储的对象
     */
    private SharedPreferences prefer;

    /**
     * 公共的年.月.日
     */
    private int mYear, mMonth, mDay;

    /**
     * 日期对象
     */
    private Calendar calender;

    /**
     * 是否设置了提醒
     */
    public static boolean isRemind;

    /**
     * 公共的菜单对象
     */
    private Menu menu;

    /**
     * 显示提醒的时间
     */
    public static TextView textView_toolbar;

    /**
     * 当前对象是否保存过
     */
    private boolean isSave = false;

    /**
     * 定义底部工具栏按钮
     */
    private ImageButton imgbtn_break, imgbtn_rebreak, imgbtn_image, imgbtn_center,
            imgbtn_bold, imgbtn_italic, imgbtn_underline, imgbtn_fontsize,
            imgbtn_checkbox, imgbtn_insertlink, imgbtn_textcolor, imgbtn_textbackgroundcolor,
            imgbtn_unorderedlist, imgbtn_orderedlist;

    /**
     * 富文本的字体大小
     */
    private int FONTSIZE = 4;

    /**
     * 当前是否居中
     */
    private boolean isFontCenter = false;

    private boolean isChanged_TextColor;

    private boolean isChanged_TextBackgroundColor;

    /**
     * 颜色选择id
     */
    public static final int DIALGE_ID = 0;

    /**
     * 图片选择的请求码
     */
    private final int REQUEST_CODE_CHOOSE = 1;//定义请求码常量

    /**
     * 插入的图片uri地址
     */
    private String saveUri = null;

    /**
     * 用于保存选择的图片URI
     */
    private List<Uri> mSelected;

    /**
     * 富文本的滑动视图
     */
    private NestedScrollView scrollview_newnote;

    /**
     * 底下工具栏的滑动视图
     */
    private HorizontalScrollView scrollview_bottomttoolbaar;

    /**
     * Activity最外层的Layout视图
     */
    private View coordinatorlayout_newnote;

    /**
     * 屏幕高度
     */
    private int screenHeight = 0;

    /**
     * 软件盘弹起后所占高度阀值
     */
    private int keyHeight = 0;

    /**
     * 取你触摸点相对于屏幕顶边的偏移
     */
    private int onTouch_getRawY;

    /**
     * 取相对于你触摸的view的顶边的偏移(Y坐标)
     */
    private int onTouch_getY;

    /**
     * 取到的是控件在屏幕中你能看到的高度(可能上面或下面有隐藏的部分)
     */
    private int richText_Height;

    /**
     * 取到的是控件真实的高度包括上面或下面隐藏的部分
     */
    private int richText_MeasuredHeight;

    private String TextColor;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefer = PreferenceManager.getDefaultSharedPreferences(this);
        isTheme_Light = prefer.getBoolean("isTheme_Light", true);
        ThemeChangeUtil.changeTheme(this, isTheme_Light);
        setContentView(R.layout.activity_new_note);

        coordinatorlayout_newnote = findViewById(R.id.coordinatorlayout_newnote);
        //获取屏幕高度
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/3
        keyHeight = screenHeight / 3;

        scrollview_newnote = findViewById(R.id.scrollview_newnote);

        initBottonToolBar();

        initBottonToolBarListener();

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
        nowIsSecret = intent.getBooleanExtra("nowIsSecret", false);

        collapsingToolbarLayout = findViewById(R.id.collapsingtoolbar_newnote);
        collapsingToolbarLayout.setTitle(nowDate + " " + nowTime);

        textView_toolbar = findViewById(R.id.textview_toolbar_newnote);
        appbarlayout_newnote = findViewById(R.id.appbarlayout_newnote);

        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);
        richText = findViewById(R.id.richedit_newnote_content);
        richText.setAnimation(animation);
        richText.setEditorHeight(450);
        richText.setEditorFontSize(16);
        if (isTheme_Light) {
            richText.setEditorFontColor(Color.parseColor("#333333"));
        } else {
            richText.setEditorFontColor(Color.parseColor("#dadada"));
            richText.setBackgroundColor(Color.parseColor("#303030"));
        }
        richText.setPadding(10, 10, 10, 10);
        richText.setPlaceholder("随便记点什么吧...");
        richText.setHtml(nowContent);

        richText.setOnTouchListener(new mOnTouchListener());

        /**
         * 监听内容改变
         */
        richText.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                //内容有改变就显示撤回按钮
                imgbtn_break.setVisibility(View.VISIBLE);
                //没有改变就隐藏撤回和重构按钮
                if (StringToAscii.stringToAscii(nowContent) == StringToAscii.stringToAscii(text)) {
                    imgbtn_break.setVisibility(View.GONE);
                }
                //不为空内容
                if (!(richText.getHtml().isEmpty())
                        && (text.length() > 0)
                        && !(text.equals(""))
                        && (text != null)
                        && !(text.equals(" "))
                        && !(text.equals("\n"))
                        && !(text.equals("<br><br>"))
                        && !(text.equals("&nbsp;"))) {
                    //保存
                    saveToDatabast();

                    //从数据库删除不存在的图片地址
                    List<NoteImagePath> noteImagePaths = LitePal.where("noteId = ?", nowId + "").find(NoteImagePath.class);
                    if (!(noteImagePaths.isEmpty())) {
                        //取出每个对象对比
                        for (NoteImagePath imagePath : noteImagePaths) {
                            //没有就从列表中删除
                            if (text.indexOf(imagePath.getImagePath()) == -1) {
                                Toast.makeText(NewNote.this, "删除图片", Toast.LENGTH_SHORT).show();
                                imagePath.delete();
                            }
                        }
                    }
                }
                //添加插入的图片地址到数据库
                if (saveUri != null) {
                    saveNoteImagePath(nowId, saveUri);
                }
                //如果richText控件高度变大, 就滚动高度
//                if (richText.getHeight() > richText_Height){
//                    scrollview_newnote.scrollTo(0,screenHeight + (richText.getHeight() - richText_Height));
//                }
//                if (richText.getMeasuredHeight() > richText_MeasuredHeight){
//                    scrollview_newnote.scrollTo(0,  (onTouch_getY - (screenHeight / 4)) + (richText.getMeasuredHeight() - richText_MeasuredHeight));
//                    richText_MeasuredHeight = richText.getMeasuredHeight();
//                }

            }
        });

        toolbar = findViewById(R.id.toolbar_newnote);
        //初始化工具栏
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
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
     * 保存插入的图片uri地址
     *
     * @param nowId
     * @param path
     */
    private void saveNoteImagePath(int nowId, String path) {
        NoteImagePath noteImagePath = new NoteImagePath();
        noteImagePath.setNoteId(nowId);
        noteImagePath.setImagePath(path);
        noteImagePath.save();
        saveUri = null;
    }

    /**
     * 初始化底部工具栏监听
     */
    private void initBottonToolBarListener() {
        imgbtn_image.setOnClickListener(new mClick());
        imgbtn_center.setOnClickListener(new mClick());
        imgbtn_bold.setOnClickListener(new mClick());
        imgbtn_italic.setOnClickListener(new mClick());
        imgbtn_fontsize.setOnClickListener(new mClick());
        imgbtn_checkbox.setOnClickListener(new mClick());
        imgbtn_break.setOnClickListener(new mClick());
        imgbtn_rebreak.setOnClickListener(new mClick());
        imgbtn_underline.setOnClickListener(new mClick());
        imgbtn_insertlink.setOnClickListener(new mClick());
        imgbtn_textcolor.setOnClickListener(new mClick());
        imgbtn_textbackgroundcolor.setOnClickListener(new mClick());
        imgbtn_unorderedlist.setOnClickListener(new mClick());
        imgbtn_orderedlist.setOnClickListener(new mClick());
    }

    /**
     * 初始化底部工具栏控件
     */
    private void initBottonToolBar() {
        scrollview_bottomttoolbaar = findViewById(R.id.scrollview_bottomttoolbaar);
        imgbtn_image = findViewById(R.id.imgbtn_image);
        imgbtn_center = findViewById(R.id.imgbtn_center);
        imgbtn_bold = findViewById(R.id.imgbtn_bold);
        imgbtn_italic = findViewById(R.id.imgbtn_italic);
        imgbtn_fontsize = findViewById(R.id.imgbtn_fontsize);
        imgbtn_checkbox = findViewById(R.id.imgbtn_checkbox);
        imgbtn_break = findViewById(R.id.imgbtn_break);
        imgbtn_rebreak = findViewById(R.id.imgbtn_rebreak);
        imgbtn_underline = findViewById(R.id.imgbtn_underline);
        imgbtn_insertlink = findViewById(R.id.imgbtn_insertlink);
        imgbtn_textcolor = findViewById(R.id.imgbtn_textcolor);
        imgbtn_textbackgroundcolor = findViewById(R.id.imgbtn_textbackgroundcolor);
        imgbtn_unorderedlist = findViewById(R.id.imgbtn_unorderedlist);
        imgbtn_orderedlist = findViewById(R.id.imgbtn_orderedlist);
    }

    /**
     * 用于把数据保存到数据库
     */
    private void saveToDatabast() {
        //如果数据现在的数据和传过来的数据不一样(修改了)而且不为null, 就保存数据.
        int i = StringToAscii.stringToAscii(nowContent);
        int j = StringToAscii.stringToAscii(richText.getHtml());
        boolean result = (i != j);
        Log.e("TAG", "!!!!!!!!!!!!!!!!!!!!!!!!nowContent=" + nowContent);
        Log.e("TAG", "!!!!!!!!!!!!!!!!!!!!!!!!richText.getHtml()=" + richText.getHtml());
        Log.e("TAG", "!!!!!!!!!!!!!!!!!!!!!!!!(StringToAscii.stringToAscii(nowContent))=" + i);
        Log.e("TAG", "!!!!!!!!!!!!!!!!!!!!!!!!(StringToAscii.stringToAscii(richText.getHtml()))=" + j);
        Log.e("TAG", "!!!!!!!!!!!!!!!!!!!!!!!!((StringToAscii.stringToAscii(nowContent)) != (StringToAscii.stringToAscii(richText.getHtml())))=" + result);
        if (((StringToAscii.stringToAscii(nowContent)) != (StringToAscii.stringToAscii(richText.getHtml())))
                && ((richText.getHtml()).length() > 0)
                && !(richText.getHtml().isEmpty())
                && !(richText.getHtml().equals(""))
                && (richText.getHtml() != null)
                && !(richText.getHtml().equals(" "))
                && !(richText.getHtml().equals("\n"))
                && !(richText.getHtml().equals("<br><br>"))
                && !(richText.getHtml().equals("&nbsp;"))) {
            Note note = null;
            if (isSave || nowState) {//如果是旧便签或保存过就得到旧对象
                note = LitePal.find(Note.class, nowId);
            } else {
                //创建表
                note = new Note();
            }
            //如果是旧便签被修改就重新得到现在的时间, 并删除数据库中的旧表
            if (nowState) {
                note.setYear(DateUtil.getNowYear());
                note.setDate(DateUtil.getNowDate());
                note.setTime(DateUtil.getNowTiem());
            } else {
                //如果是新便签, 就用创建便签时的时间
                note.setYear(nowYear);
                note.setDate(nowDate);
                note.setTime(nowTime);
            }
            //保存内容
            note.setContent(richText.getHtml());
            //是否为置顶便签
            if (note.isTop()) {
                note.setTimeStamp(DateUtil.getTimeStamp() + MainActivity.ADDTIMESTAMP);
            } else {
                note.setTimeStamp(DateUtil.getTimeStamp());
            }
            //保存是否为私密便签
            note.setSecret(nowIsSecret);
            //----------保存到数据库
            note.save();
            nowId = note.getId();
            nowContent = note.getContent();
            //把保存标志改变
            isSave = true;
            Log.e("TAG", "-----------------保存内容richText.getHtml() :" + richText.getHtml());
            Log.e("TAG", "-----------------保存内容note.getContent() :" + note.getContent());
        }
        //如果旧便签修改了变成空便签, 就从数据库删除
        if ((richText.getHtml()).length() <= 0 || richText.getHtml().equals("") || richText.getHtml() == null || richText.getHtml().equals(" ") || richText.getHtml().equals("\n") || (richText.getHtml().equals("<br><br>")) || (richText.getHtml().equals("&nbsp;"))) {
            if (isRemind) {
                stopRemind(nowId);
            }
            LitePal.deleteAll(Note.class, "id = ?", nowId + "");
        }
        Log.e("TAG", "当前时间毫秒值 = " + DateUtil.getTimeStamp());
    }

    /**
     * 菜单栏创建的回调
     *
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        //加载菜单文件
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    /**
     * 菜单栏准备完成的回调
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.delete_toolbar).setVisible(true);
        menu.findItem(R.id.secret_toolbar).setVisible(false);
        menu.findItem(R.id.view_toolbar).setVisible(false);
        menu.findItem(R.id.allcheck_toolbar).setVisible(false);
        menu.findItem(R.id.remind_toolbar).setVisible(true);
        menu.findItem(R.id.top_toolbar).setVisible(false);
        if (isRemind) {
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
                    if (!Settings.canDrawOverlays(this)) {
                        //没有悬浮窗权限,跳转申请
                        new AlertDialog.Builder(this)
                                .setTitle("显示悬浮窗")
                                .setIcon(R.mipmap.orangenote_circle)
                                .setMessage("允许显示悬浮窗可在锁屏下提醒.\n否则只能通过通知提醒您.\n 设置步骤:\n 找到“ 橙子便签 ”->显示悬浮窗->允许")
                                .setPositiveButton("去设置悬浮窗提醒", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("通知提醒", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveToDatabast();
                                        setRemind();
                                    }
                                })
                                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();

                    } else {
                        saveToDatabast();
                        setRemind();
                    }
                } else {
                    stopRemind(nowId);
                    Toast.makeText(this, "提醒已关闭", Toast.LENGTH_SHORT).show();
                }
                break;
            //删除键被点击
            case R.id.delete_toolbar:
                //把数据保存到SP存储中
                editor.putString("delete", String.valueOf(richText.getHtml()));
                editor.apply();
                //退出键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(richText.getWindowToken(), 0);
                richText.setHtml("");//置空
//                editText.setCursorVisible(false);//取消光标
                //设置Snackbar事件, 增加用户友好
                Snackbar.make(collapsingToolbarLayout, "已清空文本", Snackbar.LENGTH_LONG).setAction("恢复", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //可恢复
                        richText.setHtml(prefer.getString("delete", null));
                    }
                }).show();
                break;
        }
        return true;
    }

    /**
     * 设置提醒时间
     */
    private void setRemind() {
        if ((richText.getHtml()).length() <= 0 || richText.getHtml().equals("") || richText.getHtml() == null || richText.getHtml().equals(" ") || richText.getHtml().equals("\n") || (richText.getHtml().equals("<br><br>")) || (richText.getHtml().equals("&nbsp;"))) {
            return;
        }
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
                        Note note = LitePal.find(Note.class, nowId);
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
        Log.e("TAG", "startRemind()" + "已设置提醒" + mYear + "年" + month + "月" + day + "日" + " " + hour + ":" + minute);
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
        if (systemTime < selectTime) {
            String h = hour + "";
            String m = minute + "";
            if (hour < 10) {
                h = "0" + hour;
            }
            if (minute < 10) {
                m = "0" + minute;
            }
            Toast.makeText(this, "将会在: " + mYear + "年" + ++month + "月" + day + "日" + " " + h + ":" + m + " 提醒您", Toast.LENGTH_LONG).show();
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
        Note note = LitePal.find(Note.class, nowId);
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
        Note note = LitePal.find(Note.class, nowId);
        String hour = note.getHourRemind() + "";
        String minute = note.getMinuteRemind() + "";
        if (note.getHourRemind() < 10) {
            hour = "0" + note.getHourRemind();
        }
        if (note.getMinuteRemind() < 10) {
            minute = "0" + note.getMinuteRemind();
        }
        //同年
        if (note.getYearRemind() == calendar.get(Calendar.YEAR)) {
            //同月
            if (note.getMonthRemind() == calendar.get(Calendar.MONTH)) {
                //同日
                if (note.getDayRemind() == calendar.get(Calendar.DAY_OF_MONTH)) {
                    //同小时
                    if (note.getHourRemind() == calendar.get(Calendar.HOUR_OF_DAY)) {
                        //同分钟
                        if (note.getMinuteRemind() <= calendar.get(Calendar.MINUTE)) {
                            Toast.makeText(this, "请选择大于当前的时间", Toast.LENGTH_SHORT).show();
                            stopRemind(nowId);
//                            textView_toolbar.setText("明天" + note.getHourRemind() + ":" + note.getMinuteRemind());
                        } else {
                            textView_toolbar.setText(note.getMinuteRemind() - calendar.get(Calendar.MINUTE) + "分钟后");
                        }
                    } else {
                        if (note.getHourRemind() <= calendar.get(Calendar.HOUR_OF_DAY)) {
                            Toast.makeText(this, "请选择大于当前的时间", Toast.LENGTH_SHORT).show();
                            stopRemind(nowId);
//                            textView_toolbar.setText("明天" + note.getHourRemind() + ":" + note.getMinuteRemind());
                        } else {
                            textView_toolbar.setText("今天" + hour + ":" + minute);
                        }
                    }
                } else {
                    if (note.getDayRemind() - calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                        textView_toolbar.setText("明天" + hour + ":" + minute);
                    } else if (note.getDayRemind() - calendar.get(Calendar.DAY_OF_MONTH) == 2) {
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

    /**
     * 权限返回的
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveToDatabast();
                    setRemind();
                } else {
                    Toast.makeText(this, "必须允许该权限才能使用提醒功能", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意读写权限才能使用插入图片功能", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    showMatisse();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        coordinatorlayout_newnote.addOnLayoutChangeListener(this);
    }



    /**
     * 监听布局改变(监听软键盘)
     *
     * @param v
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param oldLeft
     * @param oldTop
     * @param oldRight
     * @param oldBottom
     */
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (TextColor != null){
            richText.setTextColor(Color.parseColor(TextColor));
        }
        //现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            appbarlayout_newnote.setExpanded(false);
            scrollview_bottomttoolbaar.setVisibility(View.VISIBLE);
            richText_Height = richText.getHeight();
            richText_MeasuredHeight = richText.getMeasuredHeight();
            if (screenHeight / 2 < onTouch_getRawY) {
//                scrollview_newnote.scrollTo(0, screenHeight / 2 + (onTouch_getRawY - screenHeight));
                scrollview_newnote.scrollTo(0, onTouch_getY - (screenHeight / 4));
            }

//            scrollview_newnote.scrollTo(bottom, right);
            //软键盘关闭
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
            appbarlayout_newnote.setExpanded(true);
            scrollview_bottomttoolbaar.setVisibility(View.GONE);
        }

    }

    /**
     * 触摸事件
     */
    private class mOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.richedit_newnote_content:
                    onTouch_getRawY = (int) event.getRawY();
                    onTouch_getY = (int) event.getY();
                    Log.e("TAG", "onTouchEvent: Y=" + onTouch_getRawY);
                    Log.e("TAG", "onTouchEvent: Y=" + onTouch_getY);
                    break;
            }
            return false;
        }
    }

    /**
     * 点击事件监听
     */
    private class mClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //撤销
                case R.id.imgbtn_break:
                    richText.undo();
                    imgbtn_rebreak.setVisibility(View.VISIBLE);
                    Log.e("TAG", "onClick: imgbtn_break");
                    break;
                //重做
                case R.id.imgbtn_rebreak:
                    richText.redo();
                    imgbtn_break.setVisibility(View.VISIBLE);
                    Log.e("TAG", "onClick: imgbtn_break");
                    break;
                //插入图片
                case R.id.imgbtn_image:
                    //获取焦点
                    Log.e("TAG", "onClick: imgbtn_image");
                    if (Build.VERSION.SDK_INT >= 24) {
                        List<String> permissionList = new ArrayList<>();
                        if (ContextCompat.checkSelfPermission(NewNote.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                        if (ContextCompat.checkSelfPermission(NewNote.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                        if (ContextCompat.checkSelfPermission(NewNote.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            permissionList.add(Manifest.permission.CAMERA);
                        }
                        if (!permissionList.isEmpty()) {
                            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
                            ActivityCompat.requestPermissions(NewNote.this, permissions, 2);
                        } else {
                            showMatisse();
                        }
                    } else {
                        showMatisse();
                    }
                    break;
                //居中
                case R.id.imgbtn_center:
                    if (isFontCenter) {
                        richText.setAlignLeft();
                        isFontCenter = false;
                    } else {
                        richText.setAlignCenter();
                        isFontCenter = true;
                    }
                    break;
                //粗体
                case R.id.imgbtn_bold:
                    richText.setBold();
                    Log.e("TAG", "!!!!!!!!!!!!!imgbtn_bold");
                    break;
                //斜体
                case R.id.imgbtn_italic:
                    richText.setItalic();
                    break;
                //下划线
                case R.id.imgbtn_underline:
                    richText.setUnderline();
                    break;
                //字体大小
                case R.id.imgbtn_fontsize:
                    if (FONTSIZE < 7) {
                        richText.setFontSize(FONTSIZE++);
                    }
                    if (FONTSIZE == 7) {
                        FONTSIZE = 3;
                    }
                    Log.e("TAG", "!!!!!!!!!!!!!FONTSIZE = " + FONTSIZE);
                    break;
                case R.id.imgbtn_insertlink:
                    richText.insertLink("", "");
                    break;
                case R.id.imgbtn_textcolor:
                    if (isTheme_Light) {
                        if (isChanged_TextColor) {
                            richText.setTextColor(Color.parseColor("#333333"));
                            TextColor = null;
                        } else {
                            opeAdvancenDialog();
                        }
                    } else {
                        if (isChanged_TextColor) {
                            richText.setTextColor(Color.parseColor("#dadada"));
                            TextColor = null;
                        } else {
                            opeAdvancenDialog();
                        }
                    }
                    Log.e("TAG", "!!!!!!!!!!!!!!到末尾啦" );
                    isChanged_TextColor = !isChanged_TextColor;
                    break;
                case R.id.imgbtn_textbackgroundcolor:
                    if (isTheme_Light) {
                        richText.setTextBackgroundColor(isChanged_TextBackgroundColor ? Color.parseColor("#FFFFFF") : Color.YELLOW);
                    } else {
                        richText.setTextBackgroundColor(isChanged_TextBackgroundColor ? Color.parseColor("#303030") : Color.YELLOW);
                    }
                    isChanged_TextBackgroundColor = !isChanged_TextBackgroundColor;
                    break;
                case R.id.imgbtn_unorderedlist:
                    richText.setBullets();
                    break;
                case R.id.imgbtn_orderedlist:
                    richText.setNumbers();
                    break;
                //待办事项
                case R.id.imgbtn_checkbox:
                    richText.insertTodo();
                    break;
            }
        }
    }

    /**
     * 弹出颜色选择dialog
     */
    private void opeAdvancenDialog() {
        Log.e("TAG", "!!!!!!!!!!!!!进入颜色选择器啦");
        //隐藏窗口
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(richText.getWindowToken(), 0);
        int color = Color.RED;
        //传入的默认color
        ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().setColor(color)
                .setDialogTitle(R.string.cpv_default_title)
        //设置dialog标题
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
        //设置为自定义模式
                .setShowAlphaSlider(true)
        //设置有透明度模式，默认没有透明度
                .setDialogId(DIALGE_ID)
        //设置Id,回调时传回用于判断
                .setAllowPresets(true)
        //不显示预知模式
                .create();
        //Buider创建
        colorPickerDialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
            @Override
            public void onColorSelected(int dialogId, int color) {
                Log.e("TAG", "!!!!!!!!!选择了颜色啦");
                if (dialogId == DIALGE_ID) {
                    richText.setTextColor(Color.parseColor("#" + Integer.toHexString(color)));
                    TextColor = "#" + Integer.toHexString(color);
                }
            }

            @Override
            public void onDialogDismissed(int dialogId) {

            }
        });
        //设置回调，用于获取选择的颜色
        colorPickerDialog.show(this.getFragmentManager(), "color-picker-dialog");
    }

    /**
     * 图片选择
     */
    private void showMatisse() {
        Matisse
                .from(NewNote.this)
                .choose(MimeType.allOf())//照片视频全部显示
                .capture(true) //是否提供拍照功能
                .captureStrategy(new CaptureStrategy(true, "com.orange.orangenote.fileprovider"))//存储到哪里
                .countable(true)//有序选择图片
                .maxSelectable(1)//最大选择数量为1
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))//图片显示表格的大小
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)//图像选择和预览活动所需的方向。
                .thumbnailScale(0.85f)//缩放比例
                .theme(isTheme_Light == true ? R.style.Matisse_Zhihu : R.style.Matisse_Dracula)//主题  暗色主题 R.style.Matisse_Dracula
                .imageEngine(new GlideEngine())//加载方式
                .forResult(REQUEST_CODE_CHOOSE);//请求码
    }

    /**
     * 选择图片后的回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            for (Uri uri : mSelected) {
                Log.e("TAG", "------------------------得到选择对象" + "  uri: " + uri.toString());
                //压缩图片
                imageCompression(uri);
            }
        }
    }

    /**
     * 压缩图片
     *
     * @param uri
     */
    private void imageCompression(Uri uri) {

        Log.e("TAG", "--------------------进入方法成功");
        if (uri.toString().indexOf("content://media/") != -1) {
            //uri路径为相相册时
            File file = new File(UriToPath.getRealFilePath(this, uri));
            File compressedImageFile = null;
            try {
                compressedImageFile = new Compressor(this)
                        .setMaxWidth(240)
                        .setMaxHeight(200)
                        .setQuality(80)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile(file);
                Toast.makeText(this, "压缩成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            richText.insertImage(compressedImageFile.toString(), "image_1");
            saveUri = compressedImageFile.toString();
            Log.e("TAG", "------------------------最终设置的选择对象" + " uri: " + compressedImageFile.toString());
        } else {
            //uri路径为相机时
            Log.e("TAG", "-----------------------uri路径为相机时,转换后的 uri : " + UriToPath.getCameraUriToPath(uri));
            File file = new File(UriToPath.getCameraUriToPath(uri));
            File compressedImageFile = null;
            try {
                compressedImageFile = new Compressor(this)
                        .setMaxWidth(240)
                        .setMaxHeight(200)
                        .setQuality(100)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile(file);
                Toast.makeText(this, "压缩成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("TAG", "------------------------最终设置的选择对象" + "  uri: " + compressedImageFile.toString());
            richText.insertImage(compressedImageFile.toString(), "image_1");
            file.delete();
            saveUri = compressedImageFile.toString();
        }
    }

}
