package com.score.schaepher.scorequerycount;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

/**
 * Created by Schaepher on 2015/7/16.
 */
public class HttpUtil
{
    // 实例化对象
    private static AsyncHttpClient client = new AsyncHttpClient();
    // Host地址
    public static final String HOST = "59.77.226.32";
    // 基础地址
    public static final String URL_BASE = "http://jwch.fzu.edu.cn/";
    // 登陆地址
    public static final String URL_LOGIN = "http://59.77.226.32/logincheck.asp";
    // 登录成功的首页
    public static String URL_SCORE =
            "http://59.77.226.35/student/xyzk/cjyl/score_sheet.aspx?id=ID";

    // 静态初始化
    static
    {
        client.addHeader("Referer", URL_BASE);
    }


    public static void post(String urlString, RequestParams params,
                            AsyncHttpResponseHandler res)
    {
        client.post(urlString, params, res);
    }

    public static void get(String urlString, RequestParams params,
                           AsyncHttpResponseHandler res)
    {
        client.get(urlString, params, res);
    }

    public static AsyncHttpClient getClient()
    {
        return client;
    }

    public static void setCookieStore(PersistentCookieStore cookieStore)
    {
        client.setCookieStore(cookieStore);
    }



}
