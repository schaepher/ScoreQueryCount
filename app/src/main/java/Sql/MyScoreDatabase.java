package Sql;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Schaepher on 2015/7/17.
 */
public class MyScoreDatabase {

    //添加_id属性，会方便很多。
    public static final String KEY_ID = "_id";

    public static final String KEY_SEMESTER_COLUMN =
            "SEMESTER_COLUMN";
    public static final String KEY_COURSE_NAME_COLUMN =
            "COURSE_NAME_COLUMN";
    public static final String KEY_COURSE_CREDIT_COLUMN =
            "COURSE_CREDIT_COLUMN";
    public static final String KEY_COURSE_SCORE_COLUMN =
            "COURSE_SCORE_COLUMN";
    public static final String KEY_COURSE_SCORE_POINT_COLUMN =
            "COURSE_SCORE_POINT_COLUMN";
    public static final String KEY_COURSE_TEACHER_COLUMN =
            "COURSE_TEACHER_COLUMN";
    public static final String KEY_COURSE_TYPE_COLUMN =
            "COURSE_TYPE_COLUMN";
    public static final String TABLE_COURSE_POINT =
            "CORE_POINT";

    private String dataBaseScoreTable;
    private SQLiteDatabase db;

    public String getKeySemesterColumn() {
        return KEY_SEMESTER_COLUMN;
    }

    public String getKeyCourseNameColumn() {
        return KEY_COURSE_NAME_COLUMN;
    }

    public String getKeyCourseCreditColumn() {
        return KEY_COURSE_CREDIT_COLUMN;
    }

    public String getKeyCourseScoreColumn() {
        return KEY_COURSE_SCORE_COLUMN;
    }

    public String getKeyCourseScorePointColumn() {
        return KEY_COURSE_SCORE_POINT_COLUMN;
    }

    public String getKeyCourseTeacherColumn() {
        return KEY_COURSE_TEACHER_COLUMN;
    }

    public String getKeyCourseTypeColumn() {
        return KEY_COURSE_TYPE_COLUMN;
    }


    public MyScoreDatabase(Context context) {
        ScoreDBOpenHelper scoreDBOpenHelper = new ScoreDBOpenHelper(context,
                ScoreDBOpenHelper.DATABASE_NAME, null, ScoreDBOpenHelper.DATABASE_VERSION);
        db = scoreDBOpenHelper.getWritableDatabase();
        SharedPreferences settings = context.getSharedPreferences("settings",
                Activity.MODE_PRIVATE);
        dataBaseScoreTable = "S" + settings.getString("user", null);
    }


    public void closeDatabase() {
        db.close();
    }

    public void setTableName(String tableName) {
        dataBaseScoreTable = tableName;
    }


    public Cursor query(String[] result_columns, String where, String whereArgs[],
                        String groupBy, String having, String order) {
        return db.query(dataBaseScoreTable, result_columns, where, whereArgs, groupBy, having, order);
    }


//    public Cursor queryAll()
//    {
//        String[] result_columns = new String[]{KEY_ID + " as _id"};
//        String where = null;
//        String whereArgs[] = null;
//        String groupBy = null;
//        String having = null;
//        String order = null;
//        Cursor cursor = query(result_columns, where, whereArgs, groupBy, having, order);
//        return cursor;
//    }

    public Cursor querySemesterDigit() {
        String[] result_columns = new String[]
                {
                        KEY_SEMESTER_COLUMN
                };
        String where = null;
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;
        return db.query(true, dataBaseScoreTable, result_columns, where, whereArgs, groupBy, having, order, null);
    }

    public Cursor queryCourse(String cname) {
        String[] result_columns = new String[]
                {
                        KEY_ID + " as _id",
                        KEY_COURSE_SCORE_COLUMN,
                        KEY_COURSE_SCORE_POINT_COLUMN
                };
        String where = KEY_COURSE_NAME_COLUMN + "=" + "\"" + cname + "\"";
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;
        return query(result_columns, where, whereArgs, groupBy, having, order);
    }

    public Cursor querySemester(String semester) {
        String[] result_columns = new String[]
                {
                        KEY_ID + " as _id",
                        KEY_COURSE_NAME_COLUMN,
                        KEY_SEMESTER_COLUMN, KEY_COURSE_CREDIT_COLUMN,
                        KEY_COURSE_SCORE_COLUMN, KEY_COURSE_SCORE_POINT_COLUMN,
                        KEY_COURSE_TYPE_COLUMN
                };
        String where = KEY_SEMESTER_COLUMN + "=" + "\"" + semester + "\"";
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;
        return query(result_columns, where, whereArgs, groupBy, having, order);
    }


    public void addNewScore(String csemester, String cname, Float ccredit, String cscore,
                            Float cspoint, String cteacher, String ctype) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_SEMESTER_COLUMN, csemester);
        newValues.put(KEY_COURSE_NAME_COLUMN, cname);
        newValues.put(KEY_COURSE_CREDIT_COLUMN, ccredit);
        newValues.put(KEY_COURSE_SCORE_COLUMN, cscore);
        newValues.put(KEY_COURSE_SCORE_POINT_COLUMN, cspoint);
        newValues.put(KEY_COURSE_TEACHER_COLUMN, cteacher);
        newValues.put(KEY_COURSE_TYPE_COLUMN, ctype);

        db.insert(dataBaseScoreTable, null, newValues);
    }

    public void updateScoreValue(String cname, String cscore, Float cspoint) {
        ContentValues updataValues = new ContentValues();

        updataValues.put(KEY_COURSE_SCORE_COLUMN, cscore);
        updataValues.put(KEY_COURSE_SCORE_POINT_COLUMN, cspoint);

        String where = KEY_COURSE_NAME_COLUMN + "=" + "\"" + cname + "\"";
        String whereArgs[] = null;

        db.update(dataBaseScoreTable, updataValues, where, whereArgs);
    }


    public void createTable(String tableName) {
        tableName = "S" + tableName;
        String DATABASE_CREATE =
                "create table if not exists " + tableName + " ( " +
                        KEY_ID + " integer primary key autoincrement, " +
                        KEY_SEMESTER_COLUMN + " smallint not null, " +
                        KEY_COURSE_NAME_COLUMN + " char(20), " +
                        KEY_COURSE_CREDIT_COLUMN + " real not null, " +
                        KEY_COURSE_SCORE_COLUMN + " char(10) not null, " +
                        KEY_COURSE_SCORE_POINT_COLUMN + " real, " +
                        KEY_COURSE_TEACHER_COLUMN + " char(20), " +
                        KEY_COURSE_TYPE_COLUMN + " char(20) );";
        db.execSQL(DATABASE_CREATE);
    }

    public void updateCPoint(String semester, Float value) {
        ContentValues updataValues = new ContentValues();

        updataValues.put(KEY_COURSE_SCORE_POINT_COLUMN, value);

        String where = KEY_SEMESTER_COLUMN + "=" + "\"" + semester + "\"";
        String whereArgs[] = null;

        db.update(TABLE_COURSE_POINT, updataValues, where, whereArgs);
    }

    public void addCPoint(String csemester, Float cspoint) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_SEMESTER_COLUMN, csemester);
        newValues.put(KEY_COURSE_SCORE_POINT_COLUMN, cspoint);

        db.insert(TABLE_COURSE_POINT, null, newValues);
    }

    public Cursor queryCPoint() {
        String[] strings = new String[]{
                KEY_ID + " as _id",
                KEY_SEMESTER_COLUMN,
                KEY_COURSE_SCORE_POINT_COLUMN
        };
        Cursor cursor = db.query(TABLE_COURSE_POINT, strings, null, null, null, null, null);
        return cursor;
    }

    public Cursor queryCPoint(String semester) {
        String[] strings = new String[]{
                KEY_ID + " as _id",
                KEY_SEMESTER_COLUMN,
                KEY_COURSE_SCORE_POINT_COLUMN
        };
        String where = KEY_SEMESTER_COLUMN + "=" + "\"" + semester + "\"";
        Cursor cursor = db.query(TABLE_COURSE_POINT, strings, where, null, null, null, null);
        return cursor;
    }

    private static class ScoreDBOpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "myDatabase.db";
        private static final int DATABASE_VERSION = 1;


        public ScoreDBOpenHelper(Context context, String name,
                                 SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }


        public void onCreate(SQLiteDatabase db) {
            String DATABASE_CREATE =
                    "create table if not exists " + TABLE_COURSE_POINT + " ( " +
                            KEY_ID + " integer primary key autoincrement, " +
                            KEY_SEMESTER_COLUMN + " smallint not null, " +
                            KEY_COURSE_SCORE_POINT_COLUMN + " real );";
            db.execSQL(DATABASE_CREATE);
            Log.i("rebuild", "oncreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("rebuild", "onupgrade");
            onCreate(db);
        }

    }
}