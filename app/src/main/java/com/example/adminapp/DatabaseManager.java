package com.example.adminapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private SQLiteDatabase database;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference coursesRef, classesRef;
    private Context context;
    private DatabaseHelper dbHelper;  // Chỉ khai báo DatabaseHelper, sẽ khởi tạo sau

    public DatabaseManager(Context context) {
        // Kiểm tra context trước khi khởi tạo DatabaseHelper
        if (context != null) {
            this.context = context.getApplicationContext();  // Sử dụng context ứng dụng để tránh memory leak
            this.firebaseDatabase = FirebaseDatabase.getInstance();
            this.coursesRef = firebaseDatabase.getReference("courses");
            this.classesRef = firebaseDatabase.getReference("classes");
            this.dbHelper = new DatabaseHelper(this.context);  // Khởi tạo DatabaseHelper sau khi context hợp lệ
        } else {
            Log.e("DatabaseManager", "Context is null in constructor");
        }
    }

    // Mở kết nối với SQLite database
    public void open() {
        if (database == null || !database.isOpen()) {
            if (dbHelper != null) {
                database = dbHelper.getWritableDatabase();  // Mở cơ sở dữ liệu
            } else {
                Log.e("DatabaseManager", "DatabaseHelper is null");
            }
        }
    }

    // Đóng kết nối SQLite
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    // Gộp hai phương thức insertCourse thành một
    public long insertCourse(Map<String, Object> courseData) {
        if (database == null || !database.isOpen()) {
            open();
        }

        // Thêm vào SQLite trước
        long courseId = database.insert(DatabaseHelper.TABLE_COURSES, null, convertToContentValues(courseData));
        
        if (courseId != -1) {
            // Nếu thêm vào SQLite thành công, thêm ID vào dữ liệu
            courseData.put("courseId", courseId);
            
            // Đồng bộ lên Firebase
            coursesRef.child(String.valueOf(courseId)).setValue(courseData)
                .addOnSuccessListener(aVoid -> 
                    Log.d("Firebase", "Course synced successfully"))
                .addOnFailureListener(e -> 
                    Log.e("Firebase", "Error syncing course: " + e.getMessage()));
        }
        
        return courseId;
    }

    // Phương thức đồng bộ realtime với Firebase khi thêm Class
    public long insertClass(long courseId, String teacher, String date, String comments) {
        if (database == null || !database.isOpen()) {
            open();
        }

        try {
            // Kiểm tra xem courseId có tồn tại không
            String[] columns = {DatabaseHelper.COLUMN_COURSE_ID};
            String selection = DatabaseHelper.COLUMN_COURSE_ID + " = ?";
            String[] selectionArgs = {String.valueOf(courseId)};
            
            Cursor cursor = database.query(
                DatabaseHelper.TABLE_COURSES,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // CourseId tồn tại, tiến hành thêm class
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_COURSE_ID, courseId);  // Đảm bảo thêm course_id vào values
                values.put(DatabaseHelper.COLUMN_CLASS_NAME, "Class " + System.currentTimeMillis());
                values.put(DatabaseHelper.COLUMN_TEACHER, teacher);
                values.put(DatabaseHelper.COLUMN_DATE, date);
                values.put(DatabaseHelper.COLUMN_COMMENTS, comments);

                // Log để debug
                Log.d("DatabaseManager", "Inserting class with courseId: " + courseId);

                // Thêm vào SQLite
                long classId = database.insert(DatabaseHelper.TABLE_CLASSES, null, values);
                
                if (classId != -1) {
                    // Nếu thêm vào SQLite thành công, tạo Map để đồng bộ lên Firebase
                    Map<String, Object> classData = new HashMap<>();
                    classData.put("classId", classId);
                    classData.put("courseId", courseId);  // Đảm bảo thêm courseId vào Firebase
                    classData.put("className", "Class " + System.currentTimeMillis());
                    classData.put("teacher", teacher);
                    classData.put("date", date);
                    classData.put("comments", comments);
                    
                    // Log để debug
                    Log.d("DatabaseManager", "Class data before Firebase sync: " + classData.toString());
                    
                    // Đồng bộ lên Firebase
                    classesRef.child(String.valueOf(classId)).setValue(classData)
                        .addOnSuccessListener(aVoid -> 
                            Log.d("Firebase", "Class synced successfully with courseId: " + courseId))
                        .addOnFailureListener(e -> 
                            Log.e("Firebase", "Error syncing class: " + e.getMessage()));
                }

                cursor.close();
                return classId;
            } else {
                // CourseId không tồn tại
                if (cursor != null) {
                    cursor.close();
                }
                Log.e("DatabaseManager", "Course ID " + courseId + " does not exist");
                return -1;
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error inserting class: " + e.getMessage());
            return -1;
        }
    }

    public List<Map<String, Object>> getAllCourses() {
        List<Map<String, Object>> courses = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DatabaseHelper.TABLE_COURSES, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, Object> courseData = new HashMap<>();
                    courseData.put("courseId", cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_ID)));
                    courseData.put("courseName", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME)));
                    courseData.put("courseDescription", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_DESCRIPTION)));
                    courseData.put("dayOfWeek", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DAY_OF_WEEK)));
                    courseData.put("courseTime", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_TIME)));
                    courseData.put("maxCapacity", cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MAX_CAPACITY)));
                    courseData.put("duration", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION)));
                    courseData.put("price", cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE)));
                    courseData.put("classType", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_TYPE)));

                    courses.add(courseData);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d("CourseDetails", "Number of courses: " + courses.size());

        return courses;
    }

    // Lấy tất cả các lớp học cho một khóa học
    public List<Map<String, Object>> getClassesForCourse(long courseId) {
        List<Map<String, Object>> classes = new ArrayList<>();
        
        // Đảm bảo database được mở
        if (database == null || !database.isOpen()) {
            open();
        }

        try {
            String selection = DatabaseHelper.COLUMN_COURSE_ID + " = ?";
            String[] selectionArgs = {String.valueOf(courseId)};
            Cursor cursor = database.query(DatabaseHelper.TABLE_CLASSES, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, Object> classData = new HashMap<>();
                    classData.put("classId", cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_ID)));
                    classData.put("className", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_NAME)));
                    classData.put("teacherName", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEACHER)));
                    classData.put("sessionDate", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)));
                    classData.put("sessionComment", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMMENTS)));

                    classes.add(classData);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return classes;
    }

    // Lắng nghe thay đổi khóa học từ Firebase và đồng bộ với SQLite
    public void listenForCourseChanges() {
        coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> courseData = (Map<String, Object>) courseSnapshot.getValue();
                    if (courseData != null) {
                        updateOrInsertCourse(courseData);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    // Cập nhật hoặc thêm mới khóa học vào SQLite
    private void updateOrInsertCourse(Map<String, Object> courseData) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_COURSE_NAME, (String) courseData.get("courseName"));
        values.put(DatabaseHelper.COLUMN_COURSE_DESCRIPTION, (String) courseData.get("courseDescription"));
        values.put(DatabaseHelper.COLUMN_DAY_OF_WEEK, (String) courseData.get("dayOfWeek"));
        values.put(DatabaseHelper.COLUMN_COURSE_TIME, (String) courseData.get("courseTime"));
        values.put(DatabaseHelper.COLUMN_MAX_CAPACITY, (Integer) courseData.get("maxCapacity"));
        values.put(DatabaseHelper.COLUMN_DURATION, (String) courseData.get("duration"));
        values.put(DatabaseHelper.COLUMN_PRICE, (Double) courseData.get("price"));
        values.put(DatabaseHelper.COLUMN_CLASS_TYPE, (String) courseData.get("classType"));

        // Kiểm tra xem khóa học đã tồn tại chưa, nếu có thì cập nhật, nếu không thì thêm mới
        int rowsUpdated = database.update(DatabaseHelper.TABLE_COURSES, values,
                DatabaseHelper.COLUMN_COURSE_ID + " = ?", new String[]{String.valueOf(courseData.get("courseId"))});

        if (rowsUpdated == 0) {
            // Nếu không có khóa học nào được cập nhật, thực hiện chèn mới
            database.insert(DatabaseHelper.TABLE_COURSES, null, values);
        }
    }

    public Cursor getAllCoursesCursor() {
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_COURSES;
        return database.rawQuery(query, null);
    }
    public void deleteCourse(long courseId) {
        if (database == null || !database.isOpen()) {
            open();
        }

        try {
            // Bắt đầu transaction để đảm bảo tính nhất quán của dữ liệu
            database.beginTransaction();

            // 1. Xóa tất cả các class thuộc về course này
            String classWhereClause = DatabaseHelper.COLUMN_COURSE_ID + " = ?";
            String[] classWhereArgs = {String.valueOf(courseId)};
            
            // Lấy danh sách các class ID trước khi xóa để xóa khỏi Firebase
            Cursor classCursor = database.query(
                DatabaseHelper.TABLE_CLASSES,
                new String[]{DatabaseHelper.COLUMN_CLASS_ID},
                classWhereClause,
                classWhereArgs,
                null, null, null
            );

            if (classCursor != null) {
                while (classCursor.moveToNext()) {
                    long classId = classCursor.getLong(
                        classCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_ID)
                    );
                    // Xóa class khỏi Firebase
                    classesRef.child(String.valueOf(classId)).removeValue();
                }
                classCursor.close();
            }

            // Xóa tất cả các class từ SQLite
            database.delete(DatabaseHelper.TABLE_CLASSES, classWhereClause, classWhereArgs);

            // 2. Xóa course
            String courseWhereClause = DatabaseHelper.COLUMN_COURSE_ID + " = ?";
            String[] courseWhereArgs = {String.valueOf(courseId)};
            database.delete(DatabaseHelper.TABLE_COURSES, courseWhereClause, courseWhereArgs);

            // Xóa course khỏi Firebase
            coursesRef.child(String.valueOf(courseId)).removeValue()
                .addOnSuccessListener(aVoid -> 
                    Log.d("Firebase", "Course and related classes deleted successfully"))
                .addOnFailureListener(e -> 
                    Log.e("Firebase", "Error deleting course from Firebase: " + e.getMessage()));

            // Đánh dấu transaction thành công
            database.setTransactionSuccessful();
            
            Log.d("DatabaseManager", "Course and all related classes deleted successfully");
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error deleting course and classes: " + e.getMessage());
        } finally {
            // Kết thúc transaction
            if (database.inTransaction()) {
                database.endTransaction();
            }
        }
    }
    public void deleteClass(long classId) {
        // Xóa lớp học từ Firebase
        classesRef.child(String.valueOf(classId)).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("DatabaseManager", "Lớp học đã bị xóa khỏi Firebase.");
                    } else {
                        Log.e("DatabaseManager", "Không thể xóa lớp học khỏi Firebase.");
                    }
                });

        // Xóa lớp học từ cơ sở dữ liệu SQLite
        String whereClause = DatabaseHelper.COLUMN_CLASS_ID + " = ?";
        String[] whereArgs = {String.valueOf(classId)};
        database.delete(DatabaseHelper.TABLE_CLASSES, whereClause, whereArgs);
    }

    public boolean updateClass(long classId, String teacherName, String date, String comments) {
        if (database == null || !database.isOpen()) {
            open();
        }

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_TEACHER, teacherName);
            values.put(DatabaseHelper.COLUMN_DATE, date);
            values.put(DatabaseHelper.COLUMN_COMMENTS, comments);

            // Cập nhật trong SQLite
            int rowsAffected = database.update(
                DatabaseHelper.TABLE_CLASSES,
                values,
                DatabaseHelper.COLUMN_CLASS_ID + " = ?",
                new String[]{String.valueOf(classId)}
            );

            // Cập nhật trong Firebase
            if (classesRef != null) {
                classesRef.child(String.valueOf(classId)).updateChildren(new HashMap<String, Object>() {{
                    put("teacher", teacherName);
                    put("date", date);
                    put("comments", comments);
                }});
            }

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error updating class: " + e.getMessage());
            return false;
        }
    }

    // Phương thức upload toàn bộ dữ liệu lên Firebase
    public void uploadAllDataToFirebase() {
        // Upload Courses
        uploadCoursesToFirebase();
        // Upload Classes
        uploadClassesToFirebase();
    }

    private void uploadCoursesToFirebase() {
        List<Map<String, Object>> courses = getAllCourses();
        for (Map<String, Object> course : courses) {
            long courseId = (long) course.get("courseId");
            coursesRef.child(String.valueOf(courseId)).setValue(course)
                .addOnSuccessListener(aVoid -> 
                    Log.d("Firebase", "Course uploaded successfully: " + courseId))
                .addOnFailureListener(e -> 
                    Log.e("Firebase", "Error uploading course: " + e.getMessage()));
        }
    }

    private void uploadClassesToFirebase() {
        Cursor cursor = database.query(DatabaseHelper.TABLE_CLASSES, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Map<String, Object> classData = new HashMap<>();
                long classId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_ID));
                classData.put("classId", classId);
                classData.put("courseId", cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_ID)));
                classData.put("className", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_NAME)));
                classData.put("teacher", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEACHER)));
                classData.put("date", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)));
                classData.put("comments", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMMENTS)));

                classesRef.child(String.valueOf(classId)).setValue(classData)
                    .addOnSuccessListener(aVoid -> 
                        Log.d("Firebase", "Class uploaded successfully: " + classId))
                    .addOnFailureListener(e -> 
                        Log.e("Firebase", "Error uploading class: " + e.getMessage()));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private ContentValues convertToContentValues(Map<String, Object> data) {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) values.put(key, (String) value);
            else if (value instanceof Integer) values.put(key, (Integer) value);
            else if (value instanceof Long) values.put(key, (Long) value);
            else if (value instanceof Double) values.put(key, (Double) value);
        }
        return values;
    }
}
