package com.score.schaepher.scorequerycount;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import Sql.MyScoreDatabase;

/**
 * Created by Schaepher on 2015/7/17.
 */

public class ScoreActivity extends Activity
{

    private Button refreshButton = null;
    private ListView listView1 = null;
    private Button getPointButton = null;
    private Button logoutButton;
    private ProgressDialog dialog;
    private String idFromLogin = "";
    private List<String> semesterStrCn;
    private static final String[] semesterStr = {"201301", "201302", "201401", "201402", "201501"};

    private Spinner mySpinner;
    private ArrayAdapter<String> adapter;


    private android.os.Handler handler = new android.os.Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            String data = (String) msg.obj;
            myHtmlFilterTask htmlFilterTask = new myHtmlFilterTask();
            htmlFilterTask.execute(data);
        }
    };


    @Override
    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.score_query);


        refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(clickListener);

        getPointButton = (Button) findViewById(R.id.pointButton);
        getPointButton.setOnClickListener(clickListener);

        logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(clickListener);

        listView1 = (ListView) findViewById(R.id.list_view);
        listView1.setOnItemClickListener(new ItemClickListener());

        dialog = new ProgressDialog(ScoreActivity.this);
//        dialog.setCancelable(false);

        semesterStrCn = new ArrayList<>();
        semesterStrCn.add(getString(R.string.s201301));
        semesterStrCn.add(getString(R.string.s201302));
        semesterStrCn.add(getString(R.string.s201401));
        semesterStrCn.add(getString(R.string.s201402));
        semesterStrCn.add(getString(R.string.s201501));
        //设置学期选项选择控件
        mySpinner = (Spinner) findViewById(R.id.mySpinner);
        adapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, semesterStrCn);
        adapter.setDropDownViewResource(R.layout.myspinner);
        mySpinner.setAdapter(adapter);
        mySpinner.setSelection(semesterStr.length - 1);
        mySpinner.setOnItemSelectedListener(spinnerListener);

    }


    //请求并获取html文件，转换为String后返回。
    public void getHtml(String id)
    {
        dialog.setTitle(getString(R.string.get_html));
        dialog.show();

        //URL_SCORE需要修改成网站给的id，不然请求的页面出错
        String httpUrlScore = HttpUtil.URL_SCORE.replace("ID", id);

        try
        {
            HttpUtil.get(httpUrlScore, null, new AsyncHttpResponseHandler()
            {
                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes)
                {
                    dialog.dismiss();
                    Charset charset = Charset.forName("UTF-8");
                    String html = new String(bytes, charset);
                    //打印获得的网页
//                    Log.w("ScoreActivity html=", html);
                    //htmlFilter的定义在下面
                    Message message = Message.obtain();
                    message.obj = html;
                    handler.sendMessage(message);
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable)
                {

                }
            });
        }
        catch (Exception e)
        {
            Log.w("getScoreHtml", e.toString());
        }

    }


    //解析收到的HTML文件，并放到数据库里。
    public class myHtmlFilterTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            dialog.setTitle(getString(R.string.parse_html));
            dialog.show();
        }

        @Override
        protected Void doInBackground(String... params)
        {
            MyScoreDatabase mydb = new MyScoreDatabase(ScoreActivity.this);
            boolean lastSemesterUpdate = false;

            String column = mydb.getKeyCourseScorePointColumn();
            //将网页以字符串形式传入Jsoup
            Document doc = Jsoup.parse(params[0]);
            //获取标签元素style为指定值的标签内容
            Elements trs = doc.getElementsByAttributeValue("style",
                    "height:30px; border-bottom:1px solid gray; " +
                            "border-left:1px solid gray; vertical-align:middle;");

            for (Element tr : trs)
            {

                //获取每个tr里的td元素集
                Elements tds = tr.select("td");
                //分别对td元素集中的每个td进行转换
                String semester = tds.get(1).text();
                String cname = tds.get(2).text();
//                Log.w("courseName", cname);
                Float ccredit = Float.parseFloat(tds.get(3).text());
                String cscore = tds.get(4).text();
                Float cspoint = null;
                //绩点一栏可能出现空字符串，在成绩录入之前会这样，parseFloat不能对空字符串进行转换
                if (!tds.get(5).text().equals(""))
                {
                    cspoint = Float.parseFloat(tds.get(5).text());
                }

                String cteacher = tds.get(6).text();
                String ctype = tds.get(7).text();

                //查询数据库里是否已经有了该课程，如果有，那么更新。如果没有，则添加
                Cursor cursor = mydb.queryCourse(cname);
                if (cursor.moveToFirst())
                {
                    //只更新没有得到学分的课程
                    int index = cursor.getColumnIndex(column);
                    Float dataBasePoint = cursor.getFloat(index);

                    if (cspoint != null && (dataBasePoint == null || dataBasePoint == 0))
                    {
                        mydb.updateScoreValue(cname, cscore, cspoint);
//                        Log.w("update", "newest update ok");
                        if (semester.equals(semesterStr[semesterStr.length - 1]))
                        {
                            lastSemesterUpdate = true;
                        }
                    }
                }
                else
                {
                    mydb.addNewScore(semester, cname, ccredit, cscore, cspoint, cteacher, ctype);
                    Log.d("add", "add ok");
                }
                cursor.close();
            }
            mydb.closeDatabase();
            //如果只更新了最新学期，则重新计算最新学期的绩点。否则更新全部
            if (lastSemesterUpdate)
            {
                int last = semesterStr.length;
                Log.e("last", String.valueOf(last));
                scorePoint(semesterStr[last - 1]);
            }
            else
            {
                for (String semester : semesterStr)
                {
                    scorePoint(semester);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            MyScoreDatabase mydb = new MyScoreDatabase(ScoreActivity.this);
            Cursor cursor = mydb.querySemester(semesterStr[semesterStr.length - 1]);
            show(mydb, cursor);
            mydb.closeDatabase();
        }
    }


    //计算指定学期绩点
    public void scorePoint(String semester)
    {
        MyScoreDatabase mydb = new MyScoreDatabase(ScoreActivity.this);
        Cursor cursor = mydb.querySemester(semester);

        String pointColumn = mydb.getKeyCourseScorePointColumn();
        int pointIndex = cursor.getColumnIndex(pointColumn);

        String creditColumn = mydb.getKeyCourseCreditColumn();
        int creditIndex = cursor.getColumnIndex(creditColumn);

        String courseTypeColumn = mydb.getKeyCourseTypeColumn();
        int courseTypeIndex = cursor.getColumnIndex(courseTypeColumn);

        Float totalPoint = 0.0f;
        Float totalCredit = 0.0f;

        while (cursor.moveToNext())
        {
            String courseType = cursor.getString(courseTypeIndex);
            Float point = cursor.getFloat(pointIndex);
            Float credit = cursor.getFloat(creditIndex);
            if (!(courseType.contains("类")) && point > 0.0f && credit > 0.0f)
            {
                Log.w("绩点", courseType + String.valueOf(point));

                totalPoint += point * credit;

                totalCredit += credit;
            }
        }

        Float average;
        if (totalCredit != 0.0f)
        {
            average = totalPoint / totalCredit;
        }
        else
        {
            average = 0.0f;
        }
        Log.w(semester + " average", String.valueOf(average));


        if (mydb.queryCPoint(semester).moveToFirst())
        {
            mydb.updateCPoint(semester, average);
        }
        else
        {
            mydb.addCPoint(semester, average);
        }
//        SharedPreferences points = getSharedPreferences("ScorePoint", Activity.MODE_PRIVATE);
//        SharedPreferences.Editor editor = points.edit();
//        editor.putFloat(semester, average);
//        editor.apply();
        mydb.closeDatabase();
    }

    //显示成绩选项
    private Spinner.OnItemSelectedListener spinnerListener = new Spinner.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            MyScoreDatabase mydb = new MyScoreDatabase(ScoreActivity.this);
            Cursor cursor = mydb.querySemester(semesterStr[position]);
            if (cursor.moveToFirst())
            {
                show(mydb, cursor);
                Log.i("mySpinner", "moveToFirst");
            }
            else
            {
                Intent intent = getIntent();
                idFromLogin = intent.getStringExtra("response");
                Log.i("onCreate", "getHtml");
                getHtml(idFromLogin);
            }
            parent.setVisibility(View.VISIBLE);
            mydb.closeDatabase();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {

        }
    };


    //点击事件
    private View.OnClickListener clickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                //点击退出登陆
                case R.id.logout:
                {
                    Intent intent = new Intent(ScoreActivity.this, LoginActivity.class);
                    ScoreActivity.this.finish();
                    startActivity(intent);
                    break;
                }
                //点击刷新
                case R.id.refresh:
                {
                    Intent intent = getIntent();

                    idFromLogin = intent.getStringExtra("response");
                    if (idFromLogin != null)
                    {
                        getHtml(idFromLogin);
                    }
                    else
                    {
                        Toast.makeText(ScoreActivity.this, "当前为【离线登陆】，无法刷新。\n" +
                                        "请【注销】并用【登陆】",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                }
                //点击显示绩点
                case R.id.pointButton:
                {
                    AlertDialog.Builder pointDialog = new AlertDialog.Builder(ScoreActivity.this);
                    pointDialog.setTitle(getString(R.string.show_point));

//                    SharedPreferences points = getSharedPreferences("ScorePoint", Activity.MODE_PRIVATE);
//
//                    Map<String, ?> myMap = points.getAll();
//
//                    Iterator entryIterator = myMap.entrySet().iterator();
//                    StringBuffer message = new StringBuffer();
//                    Log.w("显示map", myMap.toString());
//
//                    while (entryIterator.hasNext())
//                    {
//                        Map.Entry entry = (Map.Entry) entryIterator.next();
//                        String key = (String) entry.getKey();
//                        Float value = (Float) entry.getValue();
//                        message.append(key + ": " + value + "\n");
//                    }
                    StringBuffer message = new StringBuffer();
                    Float totalScorePoint = 0.0f;
                    int count = 0;
                    MyScoreDatabase mydb = new MyScoreDatabase(ScoreActivity.this);
                    Cursor cursor = mydb.queryCPoint();
                    while (cursor.moveToNext())
                    {
                        String semester = cursor.getString(1);
                        int index = 0;
                        for (String s : semesterStr)
                        {
                            if (s.equals(semester))
                            {
                                break;
                            }
                            index++;
                        }
                        Float scorePoint = cursor.getFloat(2);
                        if (scorePoint > 0)
                        {
                            count++;
                        }
                        totalScorePoint += scorePoint;
                        message.append(semesterStrCn.get(index) + ": " + scorePoint + "\n");
                    }
                    message.append("总平均绩点: " + totalScorePoint / count);
                    pointDialog.setMessage(message);
                    pointDialog.setPositiveButton("确定", null);
                    pointDialog.show();
                    break;
                }
                default:
                    break;
            }
        }
    };


    //获取listview里的点击事件，并进行处理
    private final class ItemClickListener implements AdapterView.OnItemClickListener
    {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            ListView listView = (ListView) parent;
            //getItemAtPosition返回Cursor对象
            Cursor cursor = (Cursor) listView.getItemAtPosition(position);
            int idIndex = cursor.getColumnIndex("_id");
            String personid = String.valueOf(cursor.getInt(idIndex));
            Toast.makeText(getApplicationContext(), personid, Toast.LENGTH_SHORT).show();
        }
    }


    //显示成绩页面
    public void show(MyScoreDatabase mydb, Cursor cursor)
    {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.item, cursor,
                new String[]{mydb.getKeyCourseNameColumn(), mydb.getKeyCourseCreditColumn(),
                        mydb.getKeyCourseScorePointColumn(), mydb.getKeyCourseScoreColumn()},
                new int[]{R.id.course_name, R.id.course_credit,
                        R.id.course_score_point, R.id.course_score});
        listView1.setAdapter(adapter);
    }


}
