package com.orange.orangenote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.orange.orangenote.db.Note;
import com.orange.orangenote.util.DateUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout_main;

    private CoordinatorLayout coordinatorLayout;

    private FloatingActionButton floatingActionButton;

    private NavigationView navigationView;

    private Toolbar toolbar_main;

    private RecyclerView recyclerView;

    private NoteAdapter adapter;

    private List<Note> noteList;

    //得到ActionBar的实例
    private ActionBar actionBar;

    public static boolean isDelete = false;

    public static List<Integer> deleteposition;

    public static List<Note> deleteNote;

    public static Menu menu;

    public static boolean isListView = true;

    private SharedPreferences.Editor editor;

    private SharedPreferences prefer;

    LinearLayoutManager linearLayoutManager;

    StaggeredGridLayoutManager staggeredGridLayoutManager;

    public static int isAllCheck = 0;

    /** 是否全选状态_正常未全选 */
    public static final int isAllCheck_NORMAL = 0;
    /** 是否全选状态_全选中 */
    public static final int isAllCheck_CHECK = 1;
    /** 是否全选状态_取消全选后 */
    public static final int isAllCheck_UPCHECK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();

        prefer = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        isListView = prefer.getBoolean("isListView", true);

        toolbar_main = findViewById(R.id.toolbar_main);

        drawerLayout_main = findViewById(R.id.drawerlayout_main);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        floatingActionButton = findViewById(R.id.fab_main_add);

        navigationView = findViewById(R.id.nav_view);

        recyclerView = findViewById(R.id.recycler_main);

        linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        if (isListView) {
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }

        //设置刷新适配器
        recordAdapter();

        deleteposition = new ArrayList<>();

        deleteNote = new ArrayList<>();

        //设置toolbar和Actionbar一样效果
        setSupportActionBar(toolbar_main);
        //得到ActionBar的实例
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            //设置系统最左边的HomeASUp按钮
            actionBar.setDisplayHomeAsUpEnabled(true);
            //给按钮设置图片
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            //设置Applogo
            actionBar.setLogo(R.mipmap.orange);
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewNote.class);
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
                startActivity(intent);
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_call:
                        drawerLayout_main.closeDrawers();
                        break;
                }
                return true;
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
        if (isListView){
            menu.findItem(R.id.view_toolbar).setIcon(R.drawable.viewgallery);
        } else {
            menu.findItem(R.id.view_toolbar).setIcon(R.drawable.viewlist);
        }
        if (!isDelete){
            menu.findItem(R.id.delete_toolbar).setVisible(false);
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
            case android.R.id.home:
                drawerLayout_main.openDrawer(GravityCompat.START);
                break;
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
                break;
                //全选
            case R.id.allcheck_toolbar:
                //每次点击判断是否全选
                if (deleteNote.size() != noteList.size()){
                    //不是全选就选择正常状态
                    isAllCheck = isAllCheck_NORMAL;
                }
                //是正常状态下点击
                if (isAllCheck == isAllCheck_NORMAL){
                    //全选
                    isAllCheck = isAllCheck_CHECK;
                    deleteNote.clear();
                    for (Note note : noteList){
                        deleteNote.add(note);
                    }
                    //在全选中状态下点击
                } else if (isAllCheck == isAllCheck_CHECK){
                    //取消全选
                    isAllCheck = isAllCheck_UPCHECK;
                    deleteNote.clear();
                    isAllCheck = isAllCheck_NORMAL;//把状态重置回正常状态
                }
                adapter.notifyDataSetChanged();
                break;
                //删除
            case R.id.delete_toolbar:
                //如果待删除数组不为空
                if (deleteNote != null && deleteNote.size() > 0) {
                    //弹出dialog提示框
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("删除便签");
                    dialog.setMessage("确认要删除所选的 " + deleteNote.size() + " 条便签吗?");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //点击确定
                            if (isDelete) {
                                //把退出删除模式
                                isDelete = false;
                                isAllCheck = isAllCheck_NORMAL;
                                menu.findItem(R.id.delete_toolbar).setVisible(false);
                                menu.findItem(R.id.allcheck_toolbar).setVisible(false);
                                menu.findItem(R.id.view_toolbar).setVisible(true);
                            }
                            //遍历待删除列表
                            for (Note note : deleteNote) {
                                //如果设置了提醒功能
                                if (note.getRemind()) {
                                    //取消提醒
                                    Intent intent = new Intent(MainActivity.this, RemindReceiver.class);
                                    PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, note.getId(),
                                            intent, 0);
                                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                                    //取消警报
                                    am.cancel(pi);
                                }
                                //删除列表中的对象
                                adapter.deleteNote(note);
                            }
                            //清除待删除列表
                            deleteNote.clear();
                            //刷新适配器
                            adapter.notifyDataSetChanged();

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
                    if (isDelete) {
                        isDelete = false;
                        deleteNote.clear();
                        isAllCheck = isAllCheck_NORMAL;
                        menu.findItem(R.id.delete_toolbar).setVisible(false);
                        menu.findItem(R.id.allcheck_toolbar).setVisible(false);
                        menu.findItem(R.id.view_toolbar).setVisible(true);
                    }
                    adapter.notifyDataSetChanged();
                }
                break;
        }
        return true;
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

//        noteList = DataSupport.findAll(Note.class);
        //查询倒序
        noteList = DataSupport.order("id desc").find(Note.class);
        adapter = new NoteAdapter(MainActivity.this, this.noteList);
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
                    isDelete = false;
                    isAllCheck = isAllCheck_NORMAL;
                    menu.findItem(R.id.delete_toolbar).setVisible(false);
                    menu.findItem(R.id.allcheck_toolbar).setVisible(false);
                    menu.findItem(R.id.view_toolbar).setVisible(true);
                    deleteNote.clear();//清除待删除列表
                    adapter.notifyDataSetChanged();
                    return false;
                }
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        editor.putBoolean("isListView", isListView);
        editor.apply();
        if (isDelete) {
            isDelete = false;
            isAllCheck = isAllCheck_NORMAL;
            menu.findItem(R.id.delete_toolbar).setVisible(false);
            menu.findItem(R.id.allcheck_toolbar).setVisible(false);
            menu.findItem(R.id.view_toolbar).setVisible(true);
            deleteNote.clear();//清除待删除列表
            adapter.notifyDataSetChanged();
        }
    }
}
