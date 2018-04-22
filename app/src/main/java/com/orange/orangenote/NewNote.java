package com.orange.orangenote;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.Toast;

import com.orange.orangenote.db.Note;
import com.orange.orangenote.util.DateUtil;
import com.orange.orangenote.util.StringToAscii;

import org.litepal.crud.DataSupport;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        //接收Intent
        final Intent intent = getIntent();
        nowId = intent.getIntExtra("nowId", -1);
        nowYear = intent.getStringExtra("nowYear");
        nowDate = intent.getStringExtra("nowDate");
        nowTime = intent.getStringExtra("nowTime");
        nowContent = intent.getStringExtra("nowContent");
        nowState = intent.getBooleanExtra("nowState", false);

        collapsingToolbarLayout = findViewById(R.id.collapsingtoolbar_newnote);
        collapsingToolbarLayout.setTitle(nowDate + " " + nowTime);

        editText = findViewById(R.id.edit_newnote_content);

        editText.setText(nowContent);

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
        Log.e("TAG", "!!!!!!!!!!!!!!!!!!!!!!nowContent == editText.getText() = " + (StringToAscii.stringToAscii(nowContent) == StringToAscii.stringToAscii(editText.getText().toString())));
        //如果数据现在的数据和传过来的数据不一样(修改了)而且不为null, 就保存数据.
        if (!(StringToAscii.stringToAscii(nowContent) == StringToAscii.stringToAscii(editText.getText().toString())) && !((editText.getText()).length() <= 0) || editText.getText().equals("") || editText.getText() == null || editText.getText().equals(" ") || editText.getText().equals("\n")) {
            Log.e("TAG", "!!!!!!!!!!!!!!!!!!!!!!nowContent = " + nowContent);
            Log.e("TAG", "!!!!!!!!!!!!!!!!!!!!!!editText.getText() = " + editText.getText());
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
        //加载菜单文件
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
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
