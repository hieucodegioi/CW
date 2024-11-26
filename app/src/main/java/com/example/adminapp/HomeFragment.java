package com.example.adminapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private List<Course> courseList;
    private DatabaseManager databaseManager;
    private ProgressBar progressBar;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewCourses);
        progressBar = rootView.findViewById(R.id.progressBar);
        Button uploadButton = rootView.findViewById(R.id.uploadButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseManager = new DatabaseManager(getContext());
        databaseManager.open();
        loadCourses();

        uploadButton.setOnClickListener(v -> uploadDataToFirebase());

        return rootView;
    }

    private void loadCourses() {
        List<Map<String, Object>> rawCourseList = databaseManager.getAllCourses();
        courseList = new ArrayList<>();
        
        for (Map<String, Object> courseData : rawCourseList) {
            long courseId = (long) courseData.get("courseId");
            String courseName = (String) courseData.get("courseName");
            String courseDescription = (String) courseData.get("courseDescription");
            courseList.add(new Course(courseId, courseName, courseDescription));
        }

        courseAdapter = new CourseAdapter(
            courseList, 
            databaseManager,
            getParentFragmentManager()
        );
        recyclerView.setAdapter(courseAdapter);
    }

    private void uploadDataToFirebase() {
        if (databaseManager != null) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setAlpha(0.5f);

            new Thread(() -> {
                databaseManager.uploadAllDataToFirebase();
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setAlpha(1.0f);
                        Toast.makeText(getContext(), 
                            "Data uploaded successfully", 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void showClassList(long courseId) {
        ClassListFragment classListFragment = ClassListFragment.newInstance(courseId);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, classListFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deleteCourse(long courseId) {
        if (databaseManager != null) {
            databaseManager.deleteCourse(courseId);
        }
    }
}
