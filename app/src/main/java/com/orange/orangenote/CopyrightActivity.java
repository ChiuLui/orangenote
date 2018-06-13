package com.orange.orangenote;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.orange.orangenote.db.Note;
import com.orange.orangenote.util.ThemeChangeUtil;

import static com.orange.orangenote.MainActivity.isTheme_Light;

public class CopyrightActivity extends AppCompatActivity implements View.OnClickListener {

    /** 代替ActionBar的Toolbar */
    private Toolbar toolbar_main;

    /** ActionBar对象 */
    private ActionBar actionBar;

    /** 取SP的对象 */
    private SharedPreferences prefer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefer = PreferenceManager.getDefaultSharedPreferences(CopyrightActivity.this);
        isTheme_Light = prefer.getBoolean("isTheme_Light", true);
        ThemeChangeUtil.changeTheme(this, isTheme_Light);
        setContentView(R.layout.activity_copyright);

        initButton();

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

    private void initButton() {
        Button button_copyright_update = findViewById(R.id.button_copyright_update);
        Button button_copyright_evaluate = findViewById(R.id.button_copyright_evaluate);

        button_copyright_update.setOnClickListener(this);
        button_copyright_evaluate.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //点击回退键
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_copyright_update:
                Toast.makeText(this, "未完成检查更新", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_copyright_evaluate:
                Toast.makeText(this, "未完成评价一下吧", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
