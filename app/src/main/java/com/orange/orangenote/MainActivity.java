package com.orange.orangenote;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orange.orangenote.db.Note;
import com.orange.orangenote.db.NoteImagePath;
import com.orange.orangenote.fragment.ContentFragment;
import com.orange.orangenote.json.CheckUpDate;
import com.orange.orangenote.json.FirstOpen;
import com.orange.orangenote.util.APKVersionCodeUtils;
import com.orange.orangenote.util.DateUtil;
import com.orange.orangenote.util.HttpUtil;
import com.orange.orangenote.util.ThemeChangeUtil;
import com.yalantis.phoenix.PullToRefreshView;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import yalantis.com.sidemenu.interfaces.Resourceble;
import yalantis.com.sidemenu.interfaces.ScreenShotable;
import yalantis.com.sidemenu.model.SlideMenuItem;
import yalantis.com.sidemenu.util.ViewAnimator;


public class MainActivity extends AppCompatActivity implements ViewAnimator.ViewAnimatorListener, View.OnLayoutChangeListener {

    /**
     * 滑动菜单
     */
    private DrawerLayout drawerLayout_main;

    /**
     * 添加便签按钮
     */
    private FloatingActionButton floatingActionButton;

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

    /**
     * 侧滑菜单布局
     */
    private LinearLayout left_drawer_main;

    /**
     * 内容碎片(没用)
     */
    private ContentFragment contentFragment;

    /**
     * 侧滑菜单list
     */
    private List<SlideMenuItem> list = new ArrayList<>();

    /**
     * 侧滑菜单动画
     */
    private ViewAnimator viewAnimator;

    /**
     * 控制home侧滑开关
     */
    private ActionBarDrawerToggle drawerToggle;

    /**
     * 检查更新的毫秒值
     */
    private static long UpDateTimeStamp = 0;

    private EditText edit_main_search;

    /**
     * 屏幕高度
     */
    private int screenHeight = 0;

    /**
     * 软件盘弹起后所占高度阀值
     */
    private int keyHeight = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefer = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        isTheme_Light = prefer.getBoolean("isTheme_Light", true);
        ThemeChangeUtil.changeTheme(this, isTheme_Light);
        setContentView(R.layout.activity_main);

        //获取屏幕高度
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/3
        keyHeight = screenHeight / 3;

        edit_main_search = findViewById(R.id.edit_main_search);

        isListView = prefer.getBoolean("isListView", true);
        Password = prefer.getString("Password", null);

        toolbar_main = findViewById(R.id.toolbar_main);

        drawerLayout_main = findViewById(R.id.drawerlayout_main);

        floatingActionButton = findViewById(R.id.fab_main_add);

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

        edit_main_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 输入前的监听
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入的内容变化的监听
                //查询倒序
                noteList = LitePal.where("isSecret = ?", "0").order("timeStamp desc").find(Note.class);
                List<Note> searchNotes = new ArrayList<>();
                for (Note note : noteList) {
                    if (note.getContent().indexOf(edit_main_search.getText().toString()) != -1) {
                        searchNotes.add(note);
                    }
                }
                noteList = searchNotes;
                adapter = new NoteAdapter(MainActivity.this, noteList);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 输入后的监听
            }
        });

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


        contentFragment = ContentFragment.newInstance(R.drawable.content_music);
        getSupportFragmentManager().beginTransaction()
//               .replace(R.id.content_frame, contentFragment)
                .commit();
        drawerLayout_main.setScrimColor(Color.TRANSPARENT);
        left_drawer_main = findViewById(R.id.left_drawer_main);
        left_drawer_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout_main.closeDrawers();
            }
        });

        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout_main,         /* DrawerLayout object */
                toolbar_main,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                left_drawer_main.removeAllViews();
                left_drawer_main.invalidate();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > 0.6 && left_drawer_main.getChildCount() == 0)
                    viewAnimator.showMenuContent();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout_main.setDrawerListener(drawerToggle);
        createMenuList();
        viewAnimator = new ViewAnimator<>(this, list, contentFragment, drawerLayout_main, this);

        //是否为第一次打开
        if (prefer.getBoolean("isFirstOpen", true)) {
            //创建说明文档
            createFirstNote();
            //保存这次的检查时间
            editor.putBoolean("isFirstOpen", false);
            editor.apply();
        }

        //检查更新
        checkUpDate();

    }

    /**
     * 创建第一条便签
     */
    private void createFirstNote() {
        HttpUtil.sendOkHttpRequest("http://www.wanandroid.com/tools/mockapi/6662/chiuluiorangenoteFirstOpen", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //访问数据失败
                Note note = new Note();
                note.setYear(DateUtil.getNowYear());
                note.setDate(DateUtil.getNowDate());
                note.setTime(DateUtil.getNowTiem());
                note.setTimeStamp(DateUtil.getTimeStamp());
                note.setContent("<div style=\"text-align: center;\"><b><font size=\"6\" style=\"color: rgb(255, 152, 0);\">使用说明</font></b></div><div style=\"text-align: center;\"><b><font size=\"6\" style=\"color: rgb(255, 152, 0);\"><br></font></b></div><ul><li style=\"font-weight: bold;\"><font size=\"5\"><b>主页面</b>：</font></li></ul><span style=\"background-color: rgb(255, 255, 0);\">搜索框：</span><span style=\"background-color: rgb(255, 255, 255);\">输入关键字搜索便签。</span><br><span style=\"background-color: rgb(255, 255, 0);\">长按便签</span>➡️进入编辑模式。<br>编辑模式：<br><ol><li>\uD83D\uDD12设为私密便签（在私密便签页面\uD83D\uDD13解除私密）</li><li>\uD83D\uDCE4置顶便签</li><li>\uD83D\uDDD1️删除便签</li><li>☑️全选便签</li></ol>\uD83D\uDE0F在编辑模式下点击条目选中才能才能使用哟～<br><br><span style=\"background-color: rgb(255, 255, 0);\">下拉</span>➡️进入私密便签页面。<br>\uD83D\uDD10第一次进入需创建密码。可以在主页面设置便签为私密便签，也可以在私密便签页面创建您的私密便签。<br><br><span style=\"background-color: rgb(255, 255, 0);\">侧滑</span>➡️打开菜单页面。<br>在菜单页面<br><ol><li>\uD83C\uDF1E\uD83C\uDF1A白天、黑夜主题切换</li><li>⚠️关于：可以在关于页面获取更新等信息。<br></li><li>\uD83C\uDFC3退出</li></ol><ul><li><b><font size=\"5\">便签页面：</font></b><br></li></ul>页面右上角分别是<br><ol><li>⏰设置提醒：❗当后台被杀有可能会提醒失效。</li><li>\uD83D\uDDD1️清空页面内容。</li></ol>调出键盘同时会弹出<span style=\"color: rgb(255, 235, 59);\">可左右滑动</span>的工具栏。<br><span style=\"background-color: rgb(255, 255, 255); color: rgb(255, 128, 128);\">工具栏功能</span>：撤回、重构、插入图片、居中、<b>粗体</b>、<i>斜体</i>、<u>下划线</u>、<font size=\"5\">字体大小</font>、<span style=\"color: rgb(103, 58, 183);\">字体颜色</span>、<span style=\"background-color: rgb(255, 255, 0);\">高<span style=\"color: rgb(103, 58, 183);\">亮</span></span>、<a href=\"\" title=\"\">链接</a>、无序任务条目、有序任务条目、<input type=\"checkbox\" name=\"1529659953079\" value=\"1529659953079\">&nbsp; 多选框。");
                note.save();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordAdapter();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //访问数据成功
                String responseText = response.body().string();
                Gson gson = new Gson();
                FirstOpen firstOpen = gson.fromJson(responseText, FirstOpen.class);
                Note note = new Note();
                note.setYear(DateUtil.getNowYear());
                note.setDate(DateUtil.getNowDate());
                note.setTime(DateUtil.getNowTiem());
                note.setTimeStamp(DateUtil.getTimeStamp());
                note.setContent("<div style=\"text-align: center;\"><b><font size=\"6\" style=\"color: rgb(255, 152, 0);\">使用说明</font></b></div><div style=\"text-align: center;\"><b><font size=\"6\" style=\"color: rgb(255, 152, 0);\"><br></font></b></div><ul><li style=\"font-weight: bold;\"><font size=\"5\"><b>主页面</b>：</font></li></ul><span style=\"background-color: rgb(255, 255, 0);\">搜索框：</span><span style=\"background-color: rgb(255, 255, 255);\">输入关键字搜索便签。<br></span><span style=\"background-color: rgb(255, 255, 0);\">切换视图：</span><span style=\"background-color: rgb(255, 255, 255);\">点击页面右上角切换视图按钮：<br><ul><li>\uD83D\uDCD1列表视图</li><li>\uD83D\uDCF0瀑布流视图<br></li></ul></span><span style=\"background-color: rgb(255, 255, 0);\">长按便签</span>➡️进入编辑模式。<br>编辑模式：<br><ol><li>\uD83D\uDD12设为私密便签（在私密便签页面\uD83D\uDD13解除私密）</li><li>\uD83D\uDCE4置顶便签</li><li>\uD83D\uDDD1️删除便签</li><li>☑️全选便签</li></ol>\uD83D\uDE0F在编辑模式下点击条目选中才能才能使用哟～<br><br><span style=\"background-color: rgb(255, 255, 0);\">下拉</span>➡️进入私密便签页面。<br>\uD83D\uDD10第一次进入需创建密码。可以在主页面设置便签为私密便签，也可以在私密便签页面创建您的私密便签。<br><br><span style=\"background-color: rgb(255, 255, 0);\">侧滑</span>➡️打开菜单页面。<br>在菜单页面<br><ol><li>\uD83C\uDF1E\uD83C\uDF1A白天、黑夜主题切换</li><li>⚠️关于：可以在关于页面获取更新等信息。<br></li><li>\uD83C\uDFC3退出</li></ol><ul><li><b><font size=\"5\">便签页面：</font></b><br></li></ul>页面右上角分别是<br><ol><li>⏰设置提醒：❗当后台被杀有可能会提醒失效。</li><li>\uD83D\uDDD1️清空页面内容。</li></ol>调出键盘同时会弹出<span style=\"color: rgb(255, 235, 59);\">可左右滑动</span>的工具栏。<br><span style=\"background-color: rgb(255, 255, 255); color: rgb(255, 128, 128);\">工具栏功能</span>：撤回、重构、插入图片、居中、<b>粗体</b>、<i>斜体</i>、<u>下划线</u>、<font size=\"5\">字体大小</font>、<span style=\"color: rgb(103, 58, 183);\">字体颜色</span>、<span style=\"background-color: rgb(255, 255, 0);\">高<span style=\"color: rgb(103, 58, 183);\">亮</span></span>、<a href=\"\" title=\"\">链接</a>、无序任务条目、有序任务条目、<input type=\"checkbox\" name=\"1529659953079\" value=\"1529659953079\">&nbsp; 多选框。<br><div style=\"text-align: center;\"><b><font size=\"5\" style=\"color: rgb(255, 169, 42);\">附一张我老婆的美照</font></b></div><img src=\"" +
                        firstOpen.getImagePath() +
                        "\" alt=\"GAKKI\" width=\"320px\"><br>\n");
                note.save();
                NoteImagePath noteImagePath = new NoteImagePath();
                noteImagePath.setNoteId(note.getId());
                noteImagePath.setImagePath(firstOpen.getImagePath());
                noteImagePath.save();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordAdapter();
                    }
                });
            }
        });
    }

    /**
     * 检查更新
     */
    private void checkUpDate() {
        //检查更新(如果上次检查更新的毫秒值大于一天, 就检查更新)
        UpDateTimeStamp = prefer.getLong("UpDateTimeStamp", 0);
        //timeDifference(时间差)
        long timeDifference = DateUtil.getTimeStamp() - UpDateTimeStamp;
        //如果 上次检查的毫秒值 和 当前的毫秒值 差 为一天就检查更新
        if (timeDifference > 86400000) {
            //保存这次的检查时间
            editor.putLong("UpDateTimeStamp", DateUtil.getTimeStamp());
            editor.apply();
            //开始检查更新
            HttpUtil.sendOkHttpRequest("http://www.wanandroid.com/tools/mockapi/6662/chiuluiorangenote", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //访问数据失败
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //访问数据成功
                    String responseText = response.body().string();
                    Gson gson = new Gson();
                    final CheckUpDate checkUpDate = gson.fromJson(responseText, CheckUpDate.class);
                    if (checkUpDate.getName().equals(APKVersionCodeUtils.getAppName(MainActivity.this))) {

                        if (checkUpDate.getVersion() <= APKVersionCodeUtils.getVersionCode(MainActivity.this)) {
                            //无更新
                        } else {
                            //有更新
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final AlertDialog dialog;
                                    if (isTheme_Light) {
                                        dialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Light)
                                                .setTitle("发现更新")
                                                .setIcon(R.mipmap.orange_ylo)
                                                .setMessage(checkUpDate.getContent())
                                                .setPositiveButton("去更新", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent();
                                                        intent.setAction("android.intent.action.VIEW");
                                                        Uri apk_url = Uri.parse(checkUpDate.getDownloadURL());
                                                        intent.setData(apk_url);
                                                        startActivity(intent);//打开浏览器
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
                                                .setTitle("发现更新")
                                                .setIcon(R.mipmap.orange_ylo)
                                                .setMessage(checkUpDate.getContent())
                                                .setPositiveButton("去更新", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent();
                                                        intent.setAction("android.intent.action.VIEW");
                                                        Uri apk_url = Uri.parse(checkUpDate.getDownloadURL());
                                                        intent.setData(apk_url);
                                                        startActivity(intent);//打开浏览器
                                                    }
                                                })
                                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .show();
                                        changeDialogColor(MainActivity.this, dialog);
                                    }
                                }
                            });
                        }

                    }
                }
            });
        }
    }

    private void createMenuList() {
        SlideMenuItem menuItem0 = new SlideMenuItem(ContentFragment.CLOSE, isTheme_Light == true ? R.drawable.icn_closedaytime : R.drawable.icn_closenight);
        list.add(menuItem0);
        SlideMenuItem menuItem = new SlideMenuItem(ContentFragment.THEME, isTheme_Light == true ? R.drawable.icn_night : R.drawable.icn_daytime);
        list.add(menuItem);
        SlideMenuItem menuItem2 = new SlideMenuItem(ContentFragment.INREGARDTO, R.drawable.icn_inregardto);
        list.add(menuItem2);
        SlideMenuItem menuItem3 = new SlideMenuItem(ContentFragment.EXIT, R.drawable.icn_exit);
        list.add(menuItem3);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * 监听滑动菜单点击
     *
     * @param slideMenuItem
     * @param screenShotable
     * @param position
     * @return
     */
    @Override
    public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable, int position) {
        switch (slideMenuItem.getName()) {
            //关闭
            case ContentFragment.CLOSE:
                return screenShotable;
            //切换主题
            case ContentFragment.THEME:
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
                drawerLayout_main.openDrawer(GravityCompat.START);
                return screenShotable;
            //关于
            case ContentFragment.INREGARDTO:
                Intent intent = new Intent(this, CopyrightActivity.class);
                startActivity(intent);
                return screenShotable;
            //退出
            case ContentFragment.EXIT:
                finish();
                return screenShotable;
            default:
                return screenShotable;
        }
    }

    @Override
    public void disableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(false);

    }

    @Override
    public void enableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout_main.closeDrawers();

    }

    @Override
    public void addViewToContainer(View view) {
        left_drawer_main.addView(view);
    }

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
                    .setIcon(R.mipmap.orange_ylo)
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
                                    editor.putString("Password", password.getText().toString());
                                    editor.apply();
                                    //更新后重新赋值密码
                                    Password = prefer.getString("Password", null);
                                    Intent intent = new Intent(MainActivity.this, SecretActivity.class);
                                    startActivity(intent);
                                }
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
                    .setIcon(R.mipmap.orange_ylo)
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
                                    editor.putString("Password", password.getText().toString());
                                    editor.apply();
                                    //更新后重新赋值密码
                                    Password = prefer.getString("Password", null);
                                    Intent intent = new Intent(MainActivity.this, SecretActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            changeDialogColor(MainActivity.this, dialog);
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
                    .setIcon(R.mipmap.orange_ylo)
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
                    .setIcon(R.mipmap.orange_ylo)
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
            changeDialogColor(MainActivity.this, dialog);
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
                    .setIcon(R.mipmap.orange_ylo)
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
                                    .setIcon(R.mipmap.orange_ylo)
                                    .setMessage("点击确定:\n清除所有私密便签并且重置密码.")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialogdelete2[0] = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Light)
                                                    .setTitle("删除私密便签并重置密码")
                                                    .setIcon(R.mipmap.orange_ylo)
                                                    .setPositiveButton("重置", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            editor.putString("Password", null);
                                                            editor.apply();
                                                            //更新后重新赋值密码
                                                            Password = prefer.getString("Password", null);
                                                            //得到私密便签
                                                            List<Note> secretNote = LitePal.where("isSecret = ?", "1").order("timeStamp desc").find(Note.class);
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
                    .setIcon(R.mipmap.orange_ylo)
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
                                    .setIcon(R.mipmap.orange_ylo)
                                    .setMessage("点击确定:\n清除所有私密便签并且重置密码.")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialogdelete2[0] = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_Dark)
                                                    .setTitle("删除私密便签并重置密码")
                                                    .setIcon(R.mipmap.orange_ylo)
                                                    .setPositiveButton("重置", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            editor.putString("Password", null);
                                                            editor.apply();
                                                            //更新后重新赋值密码
                                                            Password = prefer.getString("Password", null);
                                                            //得到私密便签
                                                            List<Note> secretNote = LitePal.where("isSecret = ?", "1").order("timeStamp desc").find(Note.class);
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
                                            changeDialogColor(MainActivity.this, dialogdelete2[0]);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                            changeDialogColor(MainActivity.this, dialogdelete[0]);
                        }
                    })
                    .show();
            changeDialogColor(MainActivity.this, dialog);
        }
    }

    /**
     * 利用反射改变dialog按钮的颜色
     *
     * @param dialog
     */
    public static void changeDialogButtonColor(AlertDialog dialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#F9d43a"));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#F9d43a"));
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#F9d43a"));
    }

    /**
     * 利用反射改变dialog的标题和内容颜色
     *
     * @param dialog
     */
    public static void changeDialogColor(Context context, DialogInterface dialog) {
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(context.getResources().getColor(R.color.color_text_title_Dark));

            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextColor(context.getResources().getColor(R.color.color_text_title_Dark));
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
                //如果待删除数组不为空
                if (deleteNote != null && deleteNote.size() > 0) {
                    //遍历待删除列表 设为私密便签
                    Toast.makeText(this, "将 " + deleteNote.size() + " 条便签设为私密", Toast.LENGTH_SHORT).show();
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
                //如果待删除数组不为空
                if (deleteNote != null && deleteNote.size() > 0) {
                    //遍历待删除列表 增加毫秒值
                    for (Note note : deleteNote) {
                        if (note.isTop() && isTop) {
                            Toast.makeText(this, "将 " + deleteNote.size() + " 条便签取消置顶", Toast.LENGTH_SHORT).show();
                            note.setTop(false);
                            note.setTimeStamp(note.getTimeStamp() - ADDTIMESTAMP);
                            note.save();
                        } else if (!(note.isTop()) && !(isTop)) {
                            Toast.makeText(this, "将 " + deleteNote.size() + " 条便签设为置顶", Toast.LENGTH_SHORT).show();
                            note.setTop(true);
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
        Toast.makeText(this, "删除 " + deleteNote.size() + " 条便签", Toast.LENGTH_SHORT).show();
        //遍历待删除列表
        for (Note note : deleteNote) {
            //如果插入了图片
            List<NoteImagePath> noteImagePaths = LitePal.where("noteId = ?", note.getId() + "").find(NoteImagePath.class);
            if (!(noteImagePaths.isEmpty())) {
                //如果返回图片list不为空:
                //循环删除当前NoteId下的图片文件
                for (NoteImagePath path : noteImagePaths) {
                    //删除图片(未实现)
                    File file = new File(path.getImagePath());
                    file.delete();
                }
            }
            //删除当前NoteId图片地址的数据库数据
            LitePal.deleteAll(NoteImagePath.class, "noteId = ?", note.getId() + "");

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
        drawerLayout_main.addOnLayoutChangeListener(this);
        recordAdapter();
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
        //现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            //软键盘开启
            edit_main_search.setCursorVisible(true);
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
            //软键盘关闭
            edit_main_search.setCursorVisible(false);
        }

    }

    /**
     * 刷新适配器
     */
    private void recordAdapter() {

        edit_main_search.setText("");
        edit_main_search.setCursorVisible(false);

        //查询倒序
        noteList = LitePal.where("isSecret = ?", "0").order("timeStamp desc").find(Note.class);
        adapter = new NoteAdapter(MainActivity.this, this.noteList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (isListView) {
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }

       for (Note note : noteList) {
           Log.e("TAG", "!!!!!!!!!!!!内容=" + note.getContent());
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
            //清空内容&隐藏光标
            edit_main_search.setText("");
            edit_main_search.setCursorVisible(false);
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
