package com.orange.orangenote;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.orange.orangenote.db.Note;

public class NewNote extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private FloatingActionButton floatingActionButton;

    private String nowTime;

    private String nowDate;

    private String nowYear;

    private Toolbar toolbar;

    private EditText editText;

    private SharedPreferences.Editor editor;

    private SharedPreferences prefer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        final Intent intent = getIntent();
        nowYear = intent.getStringExtra("nowYear");
        nowDate = intent.getStringExtra("nowDate");
        nowTime = intent.getStringExtra("nowTime");

        collapsingToolbarLayout = findViewById(R.id.collapsingtoolbar_newnote);
        collapsingToolbarLayout.setTitle(nowDate + "-" + nowTime);

        editText = findViewById(R.id.edit_newnote_content);

        editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        prefer = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = findViewById(R.id.toolbar_newnote);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.back);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        floatingActionButton = findViewById(R.id.fab_nwenote_ok);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((editText.getText()).length() != 0) {
                    Note note = new Note();
                    note.setYear(nowYear);
                    note.setDate(nowDate);
                    note.setTime(nowTime);
                    note.setContent(editText.getText().toString());
                    note.save();
                    Toast.makeText(NewNote.this, "保存到数据库", Toast.LENGTH_SHORT).show();
                }

                finish();
            }
        });

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
            case android.R.id.home:
                editText.setText("");
                finish();
                break;
            case R.id.delete_toolbar:
                editor.putString("delete", String.valueOf(editText.getText()));
                editor.apply();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                editText.setText("");
                Snackbar.make(collapsingToolbarLayout, "已清空文本", Snackbar.LENGTH_LONG).setAction("恢复", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editText.setText(prefer.getString("delete", null));
                    }
                }).show();

                break;
        }
        return true;
    }


}
