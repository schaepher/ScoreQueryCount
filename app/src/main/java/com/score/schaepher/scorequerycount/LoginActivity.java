package com.score.schaepher.scorequerycount;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.nio.charset.Charset;

import Sql.MyScoreDatabase;


public class LoginActivity extends Activity
{
    private EditText userEdit = null;
    private EditText passwordEdit = null;
    private Button loginButton = null;
    private CheckBox savePasswdCheckBox;
    private CheckBox autoLoginCheckBox;
    private ProgressDialog dialog;
    private Button loginOutlineButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userEdit = (EditText) findViewById(R.id.user_input);
        passwordEdit = (EditText) findViewById(R.id.password_input);
        loginButton = (Button) findViewById(R.id.login);
        loginButton.setOnClickListener(loginListener);
        loginOutlineButton = (Button) findViewById(R.id.login_outline);
        loginOutlineButton.setOnClickListener(loginListener);

        savePasswdCheckBox = (CheckBox) findViewById(R.id.checkbox);
        autoLoginCheckBox = (CheckBox) findViewById(R.id.auto_login);
        readSettings();

    }

    //处理登录逻辑
    public void Login(String user, String password)
    {
        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setTitle("登录中……");
        dialog.show();

        PersistentCookieStore myCookieStore = new PersistentCookieStore(LoginActivity.this);
        HttpUtil.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        params.add("muser", user);
        params.add("passwd", password);
        try
        {
            HttpUtil.post(HttpUtil.URL_LOGIN, params, new AsyncHttpResponseHandler()
            {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response)
                {
                    dialog.dismiss();
                    Charset charset = Charset.forName("UTF-8");
                    String html = new String(response, charset);
                    int logResult = html.indexOf("alert");
                    //打印获得的网页
//                    Log.w("first post=", html);
                    if (logResult == -1)
                    {
                        int locateStart = html.indexOf("top.aspx?id=");
                        int locateEnd = html.indexOf("\"", locateStart);
                        String id = html.substring(locateStart + 12, locateEnd);
                        Log.i("id", id);
                        dialog.dismiss();
                        //跳转
                        Intent intent = new Intent(LoginActivity.this, ScoreActivity.class);
                        intent.putExtra("response", id);
                        LoginActivity.this.finish();
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "登陆失败",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      byte[] response, Throwable throwable)
                {
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, "请检查网络" + String.valueOf(statusCode),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e)
        {
            dialog.dismiss();
            Log.e("Login()", e.toString());
        }
    }


    private void readSettings()
    {
        //读取选中状态
        SharedPreferences settings = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        Boolean savePasswd = settings.getBoolean("savePasswd", false);
        savePasswdCheckBox.setChecked(savePasswd);
        Boolean autologin = settings.getBoolean("autologin", false);

        String user = settings.getString("user", null);
        String password = settings.getString("password", null);
        userEdit.setText(user);
        if (savePasswd == true)
        {
            passwordEdit.setText(password);
        }
        else
        {
            autologin = false;
        }

        if (autologin == true)
        {
            Login(user, password);
        }

        //如果选中自动登录，则自动选中记住账号密码
        autoLoginCheckBox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (autoLoginCheckBox.isChecked())
                {
                    savePasswdCheckBox.setChecked(true);
                }
            }
        });
        //如果记住账号密码没有被选中，则取消自动登录
        savePasswdCheckBox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (savePasswdCheckBox.isChecked() == false)
                {
                    autoLoginCheckBox.setChecked(false);
                }
            }
        });
    }


    //点击登录
    private View.OnClickListener loginListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            //加上trim，删除空格
            final String user = userEdit.getText().toString().trim();
            final String password = passwordEdit.getText().toString().trim();
            if (user.equals("") || password.equals(""))
            {
                Toast.makeText(LoginActivity.this, "学号或密码不能为空",
                        Toast.LENGTH_SHORT).show();
            }

            switch (v.getId())
            {
                case R.id.login:
                {
                    SharedPreferences settings = getSharedPreferences("settings",
                            Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    //判断用户名是否更换,如果更换，创建新表（如果没有该用户的表的话）
                    if (!user.equals(settings.getString("user", null)))
                    {
                        MyScoreDatabase db = new MyScoreDatabase(LoginActivity.this);
                        db.createTable(user);
                        db.closeDatabase();
                        Log.w("equals ", user);
                    }

                    //处理自动登录
                    if (autoLoginCheckBox.isChecked())
                    {
                        editor.putBoolean("autologin", true);
                        savePasswdCheckBox.setChecked(true);
                    }
                    else
                    {
                        editor.putBoolean("autologin", false);
                    }

                    editor.putString("user", user);
                    if (savePasswdCheckBox.isChecked())
                    {
                        editor.putString("password", password);
                        editor.putBoolean("savePasswd", true);
                    }
                    else
                    {
                        editor.putString("user", user);
                        editor.putString("password", password);
                        editor.putBoolean("savePasswd", false);
                    }
                    editor.apply();
                    Login(user, password);

                    break;
                }


                case R.id.login_outline:
                {
                    MyScoreDatabase mydb = new MyScoreDatabase(LoginActivity.this);
                    mydb.setTableName(user);
                    Cursor cursor = mydb.queryCPoint();
                    if (cursor.moveToFirst())
                    {
                        Intent intent = new Intent(LoginActivity.this, ScoreActivity.class);
                        LoginActivity.this.finish();
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "无该用户，请先登录",
                                Toast.LENGTH_SHORT).show();
                    }

                    break;
                }

                default:
                    break;
            }
        }
    };
}
