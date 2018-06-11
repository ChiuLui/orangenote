package com.orange.orangenote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.orange.orangenote.db.Note;
import com.orange.orangenote.db.NoteImagePath;
import com.orange.orangenote.util.DateUtil;
import com.orange.orangenote.util.ThemeChangeUtil;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.orange.orangenote.MainActivity.isTheme_Light;

/**
 * 私密便签页面
 * @author 神经大条蕾弟
 * @date   2018/05/28 17:11
 */
public class SecretActivity extends AppCompatActivity {

    /** 添加便签按钮 */
    private FloatingActionButton floatingActionButton;

    /** 代替ActionBar的Toolbar */
    private Toolbar toolbar_main;

    /** RecyclerView视图 */
    private RecyclerView recyclerView;

    /** 自定义适配器 */
    private SecretAdapter adapter;

    /** 储存Note对象List */
    private List<Note> noteList;

    /** ActionBar对象 */
    private ActionBar actionBar;

    /** 当前是否为删除状态 */
    public static boolean isDelete = false;

    /** 待删除的Note对象列表 */
    public static List<Note> deleteNote;

    /** 菜单实例 */
    public static Menu menu;

    /** 当前是否为列表视图 true:当前是列表视图  false:当前是瀑布流视图 */
    public static boolean isListView = true;

    /** SP存储对象 */
    private SharedPreferences.Editor editor;

    /** 取SP的对象 */
    private SharedPreferences prefer;

    /** 列表对象管理器 */
    LinearLayoutManager linearLayoutManager;

    /** 瀑布流对象管理器 */
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    /** 当前是否为全选状态 */
    public static int isAllCheck = 0;

    /** 是否全选状态_正常未全选 */
    public static final int isAllCheck_NORMAL = 0;
    /** 是否全选状态_全选中 */
    public static final int isAllCheck_CHECK = 1;
    /** 是否全选状态_取消全选后 */
    public static final int isAllCheck_UPCHECK = 2;

    public static final long ADDTIMESTAMP = 1000000000*100;

    public static boolean isTop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editor = PreferenceManager.getDefaultSharedPreferences(SecretActivity.this).edit();
        prefer = PreferenceManager.getDefaultSharedPreferences(SecretActivity.this);
        isTheme_Light = prefer.getBoolean("isTheme_Light", true);
        ThemeChangeUtil.changeTheme(this, isTheme_Light);
        setContentView(R.layout.activity_secret);
        isListView = prefer.getBoolean("isListView", true);

        toolbar_main = findViewById(R.id.toolbar_main);

        floatingActionButton = findViewById(R.id.fab_main_add);

        recyclerView = findViewById(R.id.recycler_main);

        linearLayoutManager = new LinearLayoutManager(SecretActivity.this, LinearLayoutManager.VERTICAL, false);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        if (isListView) {
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }

        //设置刷新适配器
        recordAdapter();

        deleteNote = new ArrayList<>();

        //设置toolbar和Actionbar一样效果
        setSupportActionBar(toolbar_main);
        //得到ActionBar的实例
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            //给按钮设置图片
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            //设置Applogo
            actionBar.setLogo(R.mipmap.orange_ylo);
            actionBar.setTitle("私密便签");
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecretActivity.this, NewNote.class);
                String nowYear = DateUtil.getNowYear();
                String nowDate = DateUtil.getNowDate();
                String nowTime = DateUtil.getNowTiem();
                String nowContent = "";
                boolean nowState = false;
                intent.putExtra("nowYear", nowYear);
                intent.putExtra("nowDate", nowDate);
                intent.putExtra("nowTime", nowTime);
                intent.putExtra("nowContent", nowContent);
                intent.putExtra("nowState", nowState);
                intent.putExtra("nowIsSecret", true);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载菜单文件
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        menu.findItem(R.id.secret_toolbar).setIcon(R.drawable.password_open);
        if (isListView){
            menu.findItem(R.id.view_toolbar).setIcon(R.drawable.viewgallery);
        } else {
            menu.findItem(R.id.view_toolbar).setIcon(R.drawable.viewlist);
        }
        if (!isDelete){
            menu.findItem(R.id.secret_toolbar).setVisible(false);
            menu.findItem(R.id.delete_toolbar).setVisible(false);
            menu.findItem(R.id.top_toolbar).setVisible(false);
            menu.findItem(R.id.allcheck_toolbar).setVisible(false);
            menu.findItem(R.id.view_toolbar).setVisible(true);
        }
        menu.findItem(R.id.remind_toolbar).setVisible(false);
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
            //切换视图
            case R.id.view_toolbar:
                if (isListView) {
                    recyclerView.setLayoutManager(staggeredGridLayoutManager);
                    menu.findItem(R.id.view_toolbar).setIcon(R.drawable.viewlist);
                    isListView = false;
                } else {
                    recyclerView.setLayoutManager(linearLayoutManager);
                    menu.findItem(R.id.view_toolbar).setIcon(R.drawable.viewgallery);
                    isListView = true;
                }
                recordAdapter();
                break;
            //全选
            case R.id.allcheck_toolbar:
                int temp = 0;
                for (Note note : noteList) {
                    if (note.isTop() == isTop) {
                        temp++;
                    }
                }
                //每次点击判断是否全选
                if (deleteNote.size() != temp) {
                    //不是全选就选择正常状态
                    isAllCheck = isAllCheck_NORMAL;
                }
                //是正常状态下点击
                if (isAllCheck == isAllCheck_NORMAL) {
                    //全选
                    isAllCheck = isAllCheck_CHECK;
                    deleteNote.clear();
                    for (Note note : noteList) {
                        if (note.isTop() == isTop) {
                            deleteNote.add(note);
                        }
                    }
                    //在全选中状态下点击
                } else if (isAllCheck == isAllCheck_CHECK) {
                    //取消全选
                    isAllCheck = isAllCheck_UPCHECK;
                    deleteNote.clear();
                    isAllCheck = isAllCheck_NORMAL;//把状态重置回正常状态
                }
                adapter.notifyDataSetChanged();
                break;
            //私密
            case R.id.secret_toolbar:
                Toast.makeText(this, "设为私密", Toast.LENGTH_SHORT).show();
                //如果待删除数组不为空
                if (deleteNote != null && deleteNote.size() > 0) {
                    //遍历待删除列表 取消私密便签
                    for (Note note : deleteNote) {
                        note.setSecret(false);
                        note.save();
                    }
                    //退出删除模式
                    exitDeleteMode();
                } else {
                    //不满足条件的话, 只退出删除模式, 刷新视图
                    exitDeleteMode();
                }
                break;
            //置顶
            case R.id.top_toolbar:
                Toast.makeText(this, "点击置顶按钮", Toast.LENGTH_SHORT).show();
                //如果待删除数组不为空
                if (deleteNote != null && deleteNote.size() > 0) {
                    //遍历待删除列表 增加毫秒值
                    for (Note note : deleteNote) {
                        if (note.isTop() && isTop) {
                            note.setTop(false);
                            Log.e("TAG", "原本毫秒值" + note.getTimeStamp());
                            note.setTimeStamp(note.getTimeStamp() - ADDTIMESTAMP);
                            note.save();
                        } else if (!(note.isTop()) && !(isTop)) {
                            note.setTop(true);
                            Log.e("TAG", "原本毫秒值" + note.getTimeStamp());
                            note.setTimeStamp(note.getTimeStamp() + ADDTIMESTAMP);
                            note.save();
                        }
                    }
                    //退出删除模式
                    exitDeleteMode();
                } else {
                    //不满足条件的话, 只退出删除模式, 刷新视图
                    exitDeleteMode();
                }
                break;
            //删除
            case R.id.delete_toolbar:
                //如果待删除数组不为空
                if (deleteNote != null && deleteNote.size() > 0) {
                    //弹出dialog提示框
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SecretActivity.this);
                    dialog.setTitle("删除便签");
                    dialog.setMessage("确认要删除所选的 " + deleteNote.size() + " 条便签吗?");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            //点击确定
                            deleteNoteList(deleteNote);
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //点击取消不操作
                        }
                    });
                    dialog.show();
                } else {
                    //不满足条件的话, 只退出删除模式, 刷新视图
                    exitDeleteMode();
                }
                break;
        }
        return true;
    }

    /**
     * 删除便签
     *
     * @param deleteNote 要删除的便签列表
     */
    private void deleteNoteList(List<Note> deleteNote) {
        //遍历待删除列表
        for (Note note : deleteNote) {
            //如果插入了图片
            List<NoteImagePath> noteImagePaths = LitePal.where("noteId = ?", note.getId() + "").find(NoteImagePath.class);
            if (!(noteImagePaths.isEmpty())) {
                Toast.makeText(SecretActivity.this, "如果返回图片list不为空: ", Toast.LENGTH_SHORT).show();
                //循环删除当前NoteId下的图片文件
                for (NoteImagePath path : noteImagePaths) {
                    Toast.makeText(SecretActivity.this, "删除图片: " + path.getImagePath(), Toast.LENGTH_SHORT).show();
                    File file = new File(path.getImagePath());
                    file.delete();
                }
            }
            //删除当前NoteId图片地址的数据库数据
            LitePal.deleteAll(NoteImagePath.class, "noteId = ?", note.getId() + "");

            //如果设置了提醒功能
            if (note.isRemind()) {
                //取消提醒
                Intent intent = new Intent(SecretActivity.this, RemindReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(SecretActivity.this, note.getId(),
                        intent, 0);
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                //取消警报
                am.cancel(pi);
            }
            //删除列表中的对象
            adapter.deleteNote(note);
        }
        exitDeleteMode();
    }


    @Override
    protected void onResume() {
        super.onResume();
        recordAdapter();
    }

    /**
     * 刷新适配器
     */
    private void recordAdapter() {
//        noteList = DataSupport.where("isSecret = ?", true + "").order("timeStamp desc").find(Note.class);
//        noteList = DataSupport.findAll(Note.class);
        //查询倒序
        noteList = LitePal.where("isSecret = ?", "1").order("timeStamp desc").find(Note.class);
        for (Note note : noteList){
            Log.e("TAG", "-------------------------------------------- " );
            Log.e("TAG", "id = " + note.getId() );
            Log.e("TAG", "title = " + note.getTime() );
            Log.e("TAG", "content = " + note.getContent() );
            Log.e("TAG", "isSecret = " + note.isSecret() );
            Log.e("TAG", "isTop = " + note.isTop() );
            Log.e("TAG", "isRemind = " + note.isRemind() );
        }
        adapter = new SecretAdapter(SecretActivity.this, this.noteList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (isListView) {
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }
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
                //当处于删除模式, 消费回退键, 退出删除模式
                if (isDelete) {
                    exitDeleteMode();
                    return false;
                }
                break;
        }
        return true;
    }

    /**
     * 退出删除模式 并且 清空待删除数组
     */
    private void exitDeleteMode() {
        if (isDelete) {
            isDelete = false;
            deleteNote.clear();
            isAllCheck = isAllCheck_NORMAL;
            menu.findItem(R.id.secret_toolbar).setVisible(false);
            menu.findItem(R.id.delete_toolbar).setVisible(false);
            menu.findItem(R.id.top_toolbar).setVisible(false);
            menu.findItem(R.id.allcheck_toolbar).setVisible(false);
            menu.findItem(R.id.view_toolbar).setVisible(true);
            recordAdapter();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        editor.putBoolean("isListView", isListView);
        editor.apply();
        if (isDelete) {
            isDelete = false;
            isAllCheck = isAllCheck_NORMAL;
            menu.findItem(R.id.secret_toolbar).setVisible(false);
            menu.findItem(R.id.delete_toolbar).setVisible(false);
            menu.findItem(R.id.top_toolbar).setVisible(false);
            menu.findItem(R.id.allcheck_toolbar).setVisible(false);
            menu.findItem(R.id.view_toolbar).setVisible(true);
            deleteNote.clear();//清除待删除列表
            adapter.notifyDataSetChanged();
        }
    }
}
