package com.example.adminapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "ANDROID_ADMIN_APP.DB";
    private static final int DATABASE_VERSION = 3; // Cập nhật phiên bản

    // Course Table
    public static final String TABLE_COURSES = "courses";
    public static final String COLUMN_COURSE_ID = "id"; // Dùng COLUMN_COURSE_ID để tránh nhầm lẫn
    public static final String COLUMN_COURSE_NAME = "course_name";
    public static final String COLUMN_COURSE_DESCRIPTION = "course_description";
    public static final String COLUMN_DAY_OF_WEEK = "day_of_week";
    public static final String COLUMN_COURSE_TIME = "course_time";
    public static final String COLUMN_MAX_CAPACITY = "max_capacity";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_CLASS_TYPE = "class_type";

    // Class Table
    public static final String TABLE_CLASSES = "classes";
    public static final String COLUMN_CLASS_ID = "id";
    public static final String COLUMN_CLASS_NAME = "class_name"; // Thêm cột class_name
    public static final String COLUMN_TEACHER = "teacher";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_COMMENTS = "comments";

    // SQL Queries to create tables
    private static final String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + " (" +
            COLUMN_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_COURSE_NAME + " TEXT, " +
            COLUMN_COURSE_DESCRIPTION + " TEXT, " +
            COLUMN_DAY_OF_WEEK + " TEXT, " +
            COLUMN_COURSE_TIME + " TEXT, " +
            COLUMN_MAX_CAPACITY + " INTEGER, " +
            COLUMN_DURATION + " TEXT, " +
            COLUMN_PRICE + " REAL, " +
            COLUMN_CLASS_TYPE + " TEXT" +
            ");";

    private static final String CREATE_CLASSES_TABLE = "CREATE TABLE " + TABLE_CLASSES + " (" +
            COLUMN_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_COURSE_ID + " INTEGER, " +
            COLUMN_CLASS_NAME + " TEXT, " +
            COLUMN_TEACHER + " TEXT, " +
            COLUMN_DATE + " TEXT, " +
            COLUMN_COMMENTS + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + COLUMN_COURSE_ID + ")" +
            ");";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng courses và classes
        db.execSQL(CREATE_COURSES_TABLE);
        db.execSQL(CREATE_CLASSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        
        // Tạo lại bảng mới
        onCreate(db);
    }
}
