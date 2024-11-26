package com.example.adminapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ClassAdapter classAdapter;
    private List<Class> classList;
    private DatabaseManager databaseManager;
    private long courseId;
    private Button addClassButton;

    public static ClassListFragment newInstance(long courseId) {
        ClassListFragment fragment = new ClassListFragment();
        Bundle args = new Bundle();
        args.putLong("courseId", courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_list, container, false);

        if (getArguments() != null) {
            courseId = getArguments().getLong("courseId");
        }

        recyclerView = view.findViewById(R.id.recyclerViewClasses);
        addClassButton = view.findViewById(R.id.buttonAddClass);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        databaseManager = new DatabaseManager(getContext());
        databaseManager.open();

        loadClasses();

        addClassButton.setOnClickListener(v -> {
            AddClassFragment addClassFragment = AddClassFragment.newInstance(courseId);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addClassFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadClasses() {
        if (databaseManager != null) {
            List<Map<String, Object>> classDataList = databaseManager.getClassesForCourse(courseId);
            classList = new ArrayList<>();
            
            for (Map<String, Object> classData : classDataList) {
                long classId = (long) classData.get("classId");
                String className = (String) classData.get("className");
                String teacher = (String) classData.get("teacherName");
                String date = (String) classData.get("sessionDate");
                String comments = (String) classData.get("sessionComment");
                
                classList.add(new Class(classId, className, teacher, date, comments));
            }

            classAdapter = new ClassAdapter(classList, databaseManager, getParentFragmentManager());
            recyclerView.setAdapter(classAdapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}
