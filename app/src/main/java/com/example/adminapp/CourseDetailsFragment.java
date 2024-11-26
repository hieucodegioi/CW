package com.example.adminapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CourseDetailsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Course> courseList;
    private DatabaseManager databaseManager;

    public static CourseDetailsFragment newInstance() {
        return new CourseDetailsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_course_details, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewClasses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseManager = new DatabaseManager(getContext());
        databaseManager.open();

        // Lắng nghe sự thay đổi khóa học từ Firebase và đồng bộ dữ liệu với SQLite
        databaseManager.listenForCourseChanges();

        // Lấy danh sách khóa học từ SQLite (hoặc Firebase)
        List<Map<String, Object>> rawCourseList = databaseManager.getAllCourses();

        // Chuyển đổi từ List<Map<String, Object>> sang List<Course>
        courseList = new ArrayList<>();
        for (Map<String, Object> courseData : rawCourseList) {
            long courseId = (long) courseData.get("courseId");
            String courseName = (String) courseData.get("courseName");
            String courseDescription = (String) courseData.get("courseDescription");
            courseList.add(new Course(courseId, courseName, courseDescription));
        }

        // Thiết lập RecyclerView và truyền thêm DatabaseManager và FragmentManager
        CourseAdapter courseAdapter = new CourseAdapter(
            courseList, 
            databaseManager,
            getParentFragmentManager()
        );
        recyclerView.setAdapter(courseAdapter);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseManager.close();
    }
}

