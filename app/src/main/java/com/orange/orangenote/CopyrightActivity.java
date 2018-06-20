package com.orange.orangenote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orange.orangenote.json.CheckUpDate;
import com.orange.orangenote.util.APKVersionCodeUtils;
import com.orange.orangenote.util.HttpUtil;
import com.orange.orangenote.util.ThemeChangeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.orange.orangenote.MainActivity.isTheme_Light;

public class CopyrightActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 代替ActionBar的Toolbar
     */
    private Toolbar toolbar_main;

    /**
     * ActionBar对象
     */
    private ActionBar actionBar;

    /**
     * 取SP的对象
     */
    private SharedPreferences prefer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefer = PreferenceManager.getDefaultSharedPreferences(CopyrightActivity.this);
        isTheme_Light = prefer.getBoolean("isTheme_Light", true);
        ThemeChangeUtil.changeTheme(this, isTheme_Light);
        setContentView(R.layout.activity_copyright);

        init();

        initActionBar();

    }

    /**
     * 初始化ActionBar
     */
    private void initActionBar() {
        toolbar_main = findViewById(R.id.toolbar_main);
        //设置toolbar和Actionbar一样效果
        setSupportActionBar(toolbar_main);
        //得到ActionBar的实例
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.back);
            actionBar.setDisplayHomeAsUpEnabled(true);
            //设置Applogo
//            actionBar.setLogo(R.mipmap.orange_ylo);
            actionBar.setTitle("关于橙子便签");
        }
    }

    /**
     * 初始化各种控件
     */
    private void init() {
        TextView textView = findViewById(R.id.textView);
        textView.setText(APKVersionCodeUtils.getAppName(this));
        TextView textView2 = findViewById(R.id.textView2);
        textView2.setText("V" + APKVersionCodeUtils.getVerName(this));

        ImageView image_copyright_icon1 = findViewById(R.id.image_copyright_icon1);
        ImageView image_copyright_icon2 = findViewById(R.id.image_copyright_icon2);
        ImageView image_copyright_icon3 = findViewById(R.id.image_copyright_icon3);
        image_copyright_icon1.setImageResource(isTheme_Light == true? R.drawable.copyrigth_account_b : R.drawable.copyrigth_account_w);
        image_copyright_icon2.setImageResource(isTheme_Light == true? R.drawable.copyrigth_email_b : R.drawable.copyrigth_email_w);
        image_copyright_icon3.setImageResource(isTheme_Light == true? R.drawable.copyrigth_bug_b : R.drawable.copyrigth_bug_w);

        Button button_copyright_update = findViewById(R.id.button_copyright_update);
        Button button_copyright_evaluate = findViewById(R.id.button_copyright_evaluate);

        button_copyright_update.setOnClickListener(this);
        button_copyright_evaluate.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //点击回退键
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //更新
            case R.id.button_copyright_update:
                HttpUtil.sendOkHttpRequest("http://www.wanandroid.com/tools/mockapi/6662/chiuluiorangenote", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CopyrightActivity.this, "检查更新失败, 请检查网络", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseText = response.body().string();
                        Gson gson = new Gson();
                        final CheckUpDate checkUpDate = gson.fromJson(responseText, CheckUpDate.class);
                        if (checkUpDate.getName().equals(APKVersionCodeUtils.getAppName(CopyrightActivity.this))) {
                            if (checkUpDate.getVersion() <= APKVersionCodeUtils.getVersionCode(CopyrightActivity.this)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(CopyrightActivity.this, "当前是最新版本", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final AlertDialog dialog;
                                        if (isTheme_Light) {
                                            dialog = new AlertDialog.Builder(CopyrightActivity.this, R.style.AlertDialog_Light)
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
                                            MainActivity.changeDialogButtonColor(dialog);
                                        } else {
                                            dialog = new AlertDialog.Builder(CopyrightActivity.this, R.style.AlertDialog_Dark)
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
                                            MainActivity.changeDialogColor(CopyrightActivity.this, dialog);
                                        }
                                    }
                                });
                            }

                        }
                    }
                });
                break;
            //评价
            case R.id.button_copyright_evaluate:
                HttpUtil.sendOkHttpRequest("http://www.wanandroid.com/tools/mockapi/6662/chiuluiorangenote", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CopyrightActivity.this, "跳转失败, 请检查网络", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseText = response.body().string();
                        Gson gson = new Gson();
                        final CheckUpDate checkUpDate = gson.fromJson(responseText, CheckUpDate.class);
                        if (checkUpDate.getName().equals(APKVersionCodeUtils.getAppName(CopyrightActivity.this))) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final AlertDialog dialog;
                                    if (isTheme_Light) {
                                        dialog = new AlertDialog.Builder(CopyrightActivity.this, R.style.AlertDialog_Light)
                                                .setTitle("评价")
                                                .setMessage("好看的小哥哥小姐姐, 给个好评吧~~")
                                                .setIcon(R.mipmap.orange_ylo)
                                                .setPositiveButton("去好评", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //获取当前手机有的市场
                                                        ArrayList<String> myMarkets = queryInstalledMarketPkgs(CopyrightActivity.this);
                                                        for (String myMarket : myMarkets) {
                                                            Log.e("TAG", "!!!!!!当前手机有的市场="+myMarket);
                                                        }
                                                        for (String myMarket : myMarkets) {
                                                            //获取应用上架的市场
                                                            List<String> marketList = checkUpDate.getMarketList();
                                                            for (String market : marketList) {
                                                                //如果手机中有应用上架的市场
                                                                if (myMarket.equals(market)) {
                                                                    //跳转
                                                                    openApplicationMarket(CopyrightActivity.this.getPackageName(), myMarket, checkUpDate.getPageURL());
                                                                    return;
                                                                }
                                                            }
                                                        }
                                                        //如果没有
                                                        openApplicationMarket(CopyrightActivity.this.getPackageName(), null, checkUpDate.getPageURL());
                                                    }
                                                })
                                                .setNegativeButton("丑拒", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .show();
                                        MainActivity.changeDialogButtonColor(dialog);
                                    } else {
                                        dialog = new AlertDialog.Builder(CopyrightActivity.this, R.style.AlertDialog_Dark)
                                                .setTitle("评价")
                                                .setMessage("好看的小哥哥小姐姐, 给个好评吧~~")
                                                .setIcon(R.mipmap.orange_ylo)
                                                .setPositiveButton("好哒", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //获取当前手机有得市场
                                                        ArrayList<String> myMarkets = queryInstalledMarketPkgs(CopyrightActivity.this);
                                                        for (String myMarket : myMarkets) {
                                                            //获取应用上架的市场
                                                            List<String> marketList = checkUpDate.getMarketList();
                                                            for (String market : marketList) {
                                                                //如果手机中有应用上架的市场
                                                                if (myMarket.equals(market)) {
                                                                    //跳转
                                                                    openApplicationMarket(CopyrightActivity.this.getPackageName(), myMarket, checkUpDate.getPageURL());
                                                                    return;
                                                                }
                                                            }
                                                        }
                                                        //如果没有
                                                        openApplicationMarket(CopyrightActivity.this.getPackageName(), null, checkUpDate.getPageURL());
                                                    }
                                                })
                                                .setNegativeButton("丑拒", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .show();
                                        MainActivity.changeDialogColor(CopyrightActivity.this, dialog);
                                    }
                                }
                            });
                        }
                    }
                });
        }
    }

    /**
     * 获取当前手机上的应用商店
     *
     * @param context
     * @return
     */
    private static ArrayList<String> queryInstalledMarketPkgs(Context context) {
        ArrayList<String> pkgs = new ArrayList<>();
        if (context == null)
            return pkgs;
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.APP_MARKET");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        if (infos == null || infos.size() == 0)
            return pkgs;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            String pkgName = "";
            try {
                ActivityInfo activityInfo = infos.get(i).activityInfo;
                pkgName = activityInfo.packageName;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(pkgName))
                pkgs.add(pkgName);
        }
        return pkgs;
    }

    /**
     * 启动到应用商店App页面
     *
     * @param appPkg    目标app包名
     * @param marketPkg 目标市场包名
     * @param url 目标市场包名 为null跳转的网页
     */
    private void openApplicationMarket(String appPkg, String marketPkg, String url) {
        try {
            if (TextUtils.isEmpty(appPkg)) return;
            if (TextUtils.isEmpty(marketPkg)){
                openLinkBySystem(url);
                return;
            }
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(marketPkg);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            // 打开应用商店失败 可能是没有手机没有安装应用市场
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "打开应用商店失败", Toast.LENGTH_SHORT).show();
            // 调用系统浏览器进入商城
            openLinkBySystem(url);
        }
    }

    /**
     * 调用系统浏览器打开网页
     *
     * @param url 地址
     */
    private void openLinkBySystem(String url) {
        Uri apk_url = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, apk_url);
        startActivity(intent);
    }

}
