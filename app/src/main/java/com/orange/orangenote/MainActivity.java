package com.orange.orangenote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.media.RatingCompat;
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
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.orange.orangenote.db.Note;
import com.orange.orangenote.db.NoteImagePath;
import com.orange.orangenote.util.DateUtil;
import com.orange.orangenote.util.StringToAscii;
import com.orange.orangenote.util.ThemeChangeUtil;
import com.yalantis.phoenix.PullToRefreshView;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    /**
     * 滑动菜单
     */
    private DrawerLayout drawerLayout_main;

    /**
     * 添加便签按钮
     */
    private FloatingActionButton floatingActionButton;

    /**
     * 滑动菜单里面的页面
     */
    private NavigationView navigationView;

    /**
     * 代替ActionBar的Toolbar
     */
    private Toolbar toolbar_main;

    /**
     * RecyclerView视图
     */
    private RecyclerView recyclerView;

    /**
     * 自定义适配器
     */
    private NoteAdapter adapter;

    /**
     * 储存Note对象List
     */
    private List<Note> noteList;

    /**
     * ActionBar对象
     */
    private ActionBar actionBar;

    /**
     * 当前是否为删除状态
     */
    public static boolean isDelete = false;

    /**
     * 待删除的Note对象列表
     */
    public static List<Note> deleteNote;

    /**
     * 菜单实例
     */
    public static Menu menu;

    /**
     * 当前是否为列表视图 true:当前是列表视图  false:当前是瀑布流视图
     */
    public static boolean isListView = true;

    /**
     * 当前是否为Light主题 true:当前是Light主题  false:当前是Dark主题
     */
    public static boolean isTheme_Light = true;

    /**
     * SP存储对象
     */
    private SharedPreferences.Editor editor;

    /**
     * 取SP的对象
     */
    private SharedPreferences prefer;

    /**
     * 列表对象管理器
     */
    LinearLayoutManager linearLayoutManager;

    /**
     * 瀑布流对象管理器
     */
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    /**
     * 当前是否为全选状态
     */
    public static int isAllCheck = 0;

    /**
     * 是否全选状态_正常未全选
     */
    public static final int isAllCheck_NORMAL = 0;
    /**
     * 是否全选状态_全选中
     */
    public static final int isAllCheck_CHECK = 1;
    /**
     * 是否全选状态_取消全选后
     */
    public static final int isAllCheck_UPCHECK = 2;

    /**
     * 置顶增加的毫秒值
     */
    public static final long ADDTIMESTAMP = 1000000000 * 100;

    /**
     * 判断是否置顶 true:置顶  false:未置顶
     */
    public static boolean isTop = false;

    /**
     * 下拉刷新
     */
    private PullToRefreshView mPullToRefreshView;

    /**
     * 下拉刷新时间
     */
    public static final int REFRESH_DELAY = 1;

    /**
     * 私密便签密码
     */
    private String Password = null;

    /**
     * 默认私密密码
     */
    private static final String DEFAULT_PASSWORD = "ChiuLui";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefer = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        isTheme_Light = prefer.getBoolean("isTheme_Light", true);
        ThemeChangeUtil.changeTheme(this, isTheme_Light);
        setContentView(R.layout.activity_main);

        isListView = prefer.getBoolean("isListView", true);
        Password = prefer.getString("Password", null);

        toolbar_main = findViewById(R.id.toolbar_main);

        drawerLayout_main = findViewById(R.id.drawerlayout_main);

        floatingActionButton = findViewById(R.id.fab_main_add);

        navigationView = findViewById(R.id.nav_view);

        recyclerView = findViewById(R.id.recycler_main);

        mPullToRefreshView = findViewById(R.id.pull_to_refresh);


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
            actionBar.setLogo(isTheme_Light == true ? R.mipmap.orange_ylo : R.mipmap.orange_ylo);
            actionBar.setTitle("Note");
        }

        /**
         * 下拉刷新回调
         */
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Password == null) {
                            //创建密码
                            createPassword();
                        } else {
                            //有密码
                            enterPassword();
                        }

                        mPullToRefreshView.setRefreshing(false);
                    }
                }, REFRESH_DELAY);
            }
        });

        linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        if (isListView) {
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }

        //设置刷新适配器
        recordAdapter();

        deleteNote = new ArrayList<>();

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
                intent.putExtra("nowIsSecret", false);
                startActivity(intent);
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_call:
                        drawerLayout_main.closeDrawers();

                        isTheme_Light = prefer.getBoolean("isTheme_Light", true);
                        if (isTheme_Light) {
                            setTheme(R.style.ThemeDark);
                            isTheme_Light = false;
                        } else {
                            setTheme(R.style.ThemeLight);
                            isTheme_Light = true;
                        }
                        editor.putBoolean("isTheme_Light", isTheme_Light);
                        editor.apply();
                        MainActivity.this.recreate();

                        break;
                }
                return true;
            }
        });

    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Intent mIntent = new Intent(this, MainActivity.class);
//        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(mIntent);
//        finish();
//    }

    /**
     * 创建密码dialog
     */
    private void createPassword() {
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_main_create, null);
        AlertDialog dialog;
        if (isTheme_Light) {
            dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Light)
                    .setTitle("创建密码")
                    .setIcon(R.mipmap.orangenote_circle)
                    .setMessage("创建用于访问 私密便签 的密码.")
                    .setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText password = view.findViewById(R.id.dialog_main_password);
                            EditText passwordagain = view.findViewById(R.id.dialog_main_passwordagain);
                            if (password.getText() != null && passwordagain.getText() != null) {
                                if (!(passwordagain.getText().toString().equals(password.getText().toString()))) {
                                    Toast.makeText(MainActivity.this, "您输入的密码不一致", Toast.LENGTH_SHORT).show();
                                    createPassword();
                                } else if (password.getText().toString().equals(passwordagain.getText().toString()) || (password.getText().toString()) == (passwordagain.getText().toString())) {
                                    Log.e("TAG", "创建密码: " + "password.getText()=" + password.getText() + "   passwordagain.getText()=" + passwordagain.getText());
                                    editor.putString("Password", password.getText().toString());
                                    editor.apply();
                                    //更新后重新赋值密码
                                    Password = prefer.getString("Password", null);
                                    Intent intent = new Intent(MainActivity.this, SecretActivity.class);
                                    startActivity(intent);
                                }
                                Log.e("TAG", "创建密码: " + "password.getText()=" + password.getText() + "   passwordagain.getText()=" + passwordagain.getText());
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            changeDialogButtonColor(dialog);
        } else {
            dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Dark)
                    .setTitle("创建密码")
                    .setIcon(R.mipmap.orangenote_circle)
                    .setMessage("创建用于访问 私密便签 的密码.")
                    .setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText password = view.findViewById(R.id.dialog_main_password);
                            EditText passwordagain = view.findViewById(R.id.dialog_main_passwordagain);
                            if (password.getText() != null && passwordagain.getText() != null) {
                                if (!(passwordagain.getText().toString().equals(password.getText().toString()))) {
                                    Toast.makeText(MainActivity.this, "您输入的密码不一致", Toast.LENGTH_SHORT).show();
                                    createPassword();
                                } else if (password.getText().toString().equals(passwordagain.getText().toString()) || (password.getText().toString()) == (passwordagain.getText().toString())) {
                                    Log.e("TAG", "创建密码: " + "password.getText()=" + password.getText() + "   passwordagain.getText()=" + passwordagain.getText());
                                    editor.putString("Password", password.getText().toString());
                                    editor.apply();
                                    //更新后重新赋值密码
                                    Password = prefer.getString("Password", null);
                                    Intent intent = new Intent(MainActivity.this, SecretActivity.class);
                                    startActivity(intent);
                                }
                                Log.e("TAG", "创建密码: " + "password.getText()=" + password.getText() + "   passwordagain.getText()=" + passwordagain.getText());
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            changeDialogColor(dialog);
        }
    }

    /**
     * 输入密码dialog
     */
    private void enterPassword() {
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_main_input, null);
        AlertDialog dialog;
        if (isTheme_Light) {
            dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Light)
                    .setTitle("输入密码")
                    .setIcon(R.mipmap.orangenote_circle)
                    .setMessage("请输入您设置的私密便签密码.")
                    .setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText password = view.findViewById(R.id.dialog_main_inputpassword);
                            if (!(password.getText().toString().equals(Password)) && !(password.getText().toString().equals(DEFAULT_PASSWORD))) {
                                Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                                enterPassword();
                            } else if (password.getText().toString().equals(Password) || password.getText().toString().equals(DEFAULT_PASSWORD)) {
                                Intent intent = new Intent(MainActivity.this, SecretActivity.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNeutralButton("修改密码", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            changePassword();
                        }
                    })
                    .show();
            changeDialogButtonColor(dialog);
        } else {
            dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Dark)
                    .setTitle("输入密码")
                    .setIcon(R.mipmap.orangenote_circle)
                    .setMessage("请输入您设置的私密便签密码.")
                    .setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText password = view.findViewById(R.id.dialog_main_inputpassword);
                            if (!(password.getText().toString().equals(Password)) && !(password.getText().toString().equals(DEFAULT_PASSWORD))) {
                                Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                                enterPassword();
                            } else if (password.getText().toString().equals(Password) || password.getText().toString().equals(DEFAULT_PASSWORD)) {
                                Intent intent = new Intent(MainActivity.this, SecretActivity.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNeutralButton("修改密码", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            changePassword();
                        }
                    })
                    .show();
            changeDialogColor(dialog);
        }

    }

    /**
     * 修改密码dialog
     */
    private void changePassword() {
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_main_input, null);
        final AlertDialog dialog;
        final AlertDialog[] dialogdelete = new AlertDialog[1];
        final AlertDialog[] dialogdelete2 = new AlertDialog[1];
        if (isTheme_Light) {
            dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Light)
                    .setTitle("请输入您的旧密码")
                    .setIcon(R.mipmap.orangenote_circle)
                    .setMessage("请输入您设置的私密便签密码.")
                    .setView(view)
                    .setPositiveButton("设置新密码", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText password = view.findViewById(R.id.dialog_main_inputpassword);
                            if (!(password.getText().toString().equals(Password))) {
                                Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                                changePassword();
                            } else if (password.getText().toString().equals(Password)) {
                                createPassword();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNeutralButton("忘记密码", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialogdelete[0] = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Light)
                                    .setTitle("忘记密码")
                                    .setIcon(R.mipmap.orangenote_circle)
                                    .setMessage("点击确定:\n清除所有私密便签并且重置密码.")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialogdelete2[0] = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Light)
                                                    .setTitle("删除私密便签并重置密码")
                                                    .setIcon(R.mipmap.orangenote_circle)
                                                    .setPositiveButton("重置", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            editor.putString("Password", null);
                                                            editor.apply();
                                                            //更新后重新赋值密码
                                                            Password = prefer.getString("Password", null);
                                                            //得到私密便签
                                                            List<Note> secretNote = DataSupport.where("isSecret = ?", "1").order("timeStamp desc").find(Note.class);
                                                            if (!(secretNote.isEmpty())) {
                                                                //删除私密便签
                                                                deleteNoteList(secretNote);
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    })
                                                    .show();
                                            changeDialogButtonColor(dialogdelete2[0]);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                            changeDialogButtonColor(dialogdelete[0]);
                        }
                    })
                    .show();
            changeDialogButtonColor(dialog);
        } else {
            dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Dark)
                    .setTitle("请输入您的旧密码")
                    .setIcon(R.mipmap.orangenote_circle)
                    .setMessage("请输入您设置的私密便签密码.")
                    .setView(view)
                    .setPositiveButton("设置新密码", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText password = view.findViewById(R.id.dialog_main_inputpassword);
                            if (!(password.getText().toString().equals(Password))) {
                                Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                                changePassword();
                            } else if (password.getText().toString().equals(Password)) {
                                createPassword();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNeutralButton("忘记密码", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialogdelete[0] = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Dark)
                                    .setTitle("忘记密码")
                                    .setIcon(R.mipmap.orangenote_circle)
                                    .setMessage("点击确定:\n清除所有私密便签并且重置密码.")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialogdelete2[0] = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Dark)
                                                    .setTitle("删除私密便签并重置密码")
                                                    .setIcon(R.mipmap.orangenote_circle)
                                                    .setPositiveButton("重置", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            editor.putString("Password", null);
                                                            editor.apply();
                                                            //更新后重新赋值密码
                                                            Password = prefer.getString("Password", null);
                                                            //得到私密便签
                                                            List<Note> secretNote = DataSupport.where("isSecret = ?", "1").order("timeStamp desc").find(Note.class);
                                                            if (!(secretNote.isEmpty())) {
                                                                //删除私密便签
                                                                deleteNoteList(secretNote);
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    })
                                                    .show();
                                            changeDialogColor(dialogdelete2[0]);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                            changeDialogColor(dialogdelete[0]);
                        }
                    })
                    .show();
            changeDialogColor(dialog);
        }
    }

    /**
     * 利用反射改变dialog按钮的颜色
     *
     * @param dialog
     */
    private void changeDialogButtonColor(AlertDialog dialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#F9d43a"));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#F9d43a"));
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#F9d43a"));
    }

    /**
     * 利用反射改变dialog的标题和内容颜色
     *
     * @param dialog
     */
    private void changeDialogColor(DialogInterface dialog) {
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(MainActivity.this.getResources().getColor(R.color.color_text_title_Dark));

            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextColor(MainActivity.this.getResources().getColor(R.color.color_text_title_Dark));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
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
        menu.findItem(R.id.secret_toolbar).setIcon(R.drawable.password_close);
        if (isListView) {
            menu.findItem(R.id.view_toolbar).setIcon(R.drawable.viewgallery);
        } else {
            menu.findItem(R.id.view_toolbar).setIcon(R.drawable.viewlist);
        }
        if (!isDelete) {
            menu.findItem(R.id.delete_toolbar).setVisible(false);
            menu.findItem(R.id.secret_toolbar).setVisible(false);
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
            //home键
            case android.R.id.home:
                drawerLayout_main.openDrawer(GravityCompat.START);
                break;
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
                //每次点击判断是否全选
                if (deleteNote.size() != noteList.size()) {
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
                    //遍历待删除列表 设为私密便签
                    for (Note note : deleteNote) {
                        note.setSecret(true);
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
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
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
            List<NoteImagePath> noteImagePaths = DataSupport.where("noteId = ?", note.getId() + "").find(NoteImagePath.class);
            if (!(noteImagePaths.isEmpty())) {
                Toast.makeText(MainActivity.this, "如果返回图片list不为空: ", Toast.LENGTH_SHORT).show();
                //循环删除当前NoteId下的图片文件
                for (NoteImagePath path : noteImagePaths) {
                    Toast.makeText(MainActivity.this, "删除图片: " + path.getImagePath(), Toast.LENGTH_SHORT).show();
                    File file = new File(path.getImagePath());
                    file.delete();
                }
            }
            //删除当前NoteId图片地址的数据库数据
            DataSupport.deleteAll(NoteImagePath.class, "noteId = ?", note.getId() + "");

            //如果设置了提醒功能
            if (note.isRemind()) {
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

        //查询倒序
        noteList = DataSupport.where("isSecret = ?", "0").order("timeStamp desc").find(Note.class);
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
                    exitDeleteMode();
                    return false;
                }
                //如果侧滑菜单处于打开, 关闭侧滑菜单, 并且消费事件
                if (drawerLayout_main.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout_main.closeDrawers();
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
        editor.putBoolean("isTheme_Light", isTheme_Light);
        editor.apply();
        exitDeleteMode();
    }
}
