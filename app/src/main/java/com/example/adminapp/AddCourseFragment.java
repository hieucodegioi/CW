package com.example.adminapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;

public class AddCourseFragment extends Fragment {

    private EditText courseNameEditText, descriptionEditText, courseTimeEditText, 
                     durationEditText, priceEditText;
    private Spinner courseDaySpinner;
    private Spinner classTypeSpinner;
    private Button saveButton;
    private DatabaseManager databaseManager;
    private String selectedClassType;
    private String selectedDayOfWeek;
    private View rootView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_course, container, false);

        initializeViews(rootView);
        setupSpinners();
        setupSaveButton();

        return rootView;
    }

    private void initializeViews(View rootView) {
        courseNameEditText = rootView.findViewById(R.id.courseName);
        descriptionEditText = rootView.findViewById(R.id.courseDescription);
        courseTimeEditText = rootView.findViewById(R.id.courseTime);
        durationEditText = rootView.findViewById(R.id.courseDuration);
        priceEditText = rootView.findViewById(R.id.coursePrice);
        saveButton = rootView.findViewById(R.id.saveButton);

        databaseManager = new DatabaseManager(getContext());
    }

    private void setupSpinners() {
        // Setup for Day of Week Spinner
        AutoCompleteTextView courseDayAutoComplete = rootView.findViewById(R.id.courseDay);
        ArrayAdapter<CharSequence> daysAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.days_of_week,
            android.R.layout.simple_dropdown_item_1line
        );
        courseDayAutoComplete.setAdapter(daysAdapter);
        courseDayAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            selectedDayOfWeek = parent.getItemAtPosition(position).toString();
        });
        // Set default value
        courseDayAutoComplete.setText(daysAdapter.getItem(0).toString(), false);
        selectedDayOfWeek = daysAdapter.getItem(0).toString();

        // Setup for Class Type Spinner
        AutoCompleteTextView classTypeAutoComplete = rootView.findViewById(R.id.classType);
        ArrayAdapter<CharSequence> classTypesAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.class_types,
            android.R.layout.simple_dropdown_item_1line
        );
        classTypeAutoComplete.setAdapter(classTypesAdapter);
        classTypeAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            selectedClassType = parent.getItemAtPosition(position).toString();
        });
        // Set default value
        classTypeAutoComplete.setText(classTypesAdapter.getItem(0).toString(), false);
        selectedClassType = classTypesAdapter.getItem(0).toString();
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> saveCourse());
    }

    private void saveCourse() {
        String courseName = courseNameEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String courseTime = courseTimeEditText.getText().toString();
        String duration = durationEditText.getText().toString();
        String priceString = priceEditText.getText().toString();

        if (validateInputs(courseName, description, courseTime, duration, priceString)) {
            try {
                double price = Double.parseDouble(priceString);
                Map<String, Object> courseData = createCourseData(
                    courseName, description, selectedDayOfWeek, courseTime, 
                    duration, price, selectedClassType
                );
                saveCourseToDatabase(courseData);
            } catch (NumberFormatException e) {
                showToast("Invalid price format");
            }
        }
    }

    private boolean validateInputs(String... inputs) {
        for (String input : inputs) {
            if (TextUtils.isEmpty(input)) {
                showToast("Please fill in all fields");
                return false;
            }
        }
        return true;
    }

    private Map<String, Object> createCourseData(String name, String description, 
            String dayOfWeek, String time, String duration, double price, String classType) {
        Map<String, Object> courseData = new HashMap<>();
        courseData.put(DatabaseHelper.COLUMN_COURSE_NAME, name);
        courseData.put(DatabaseHelper.COLUMN_COURSE_DESCRIPTION, description);
        courseData.put(DatabaseHelper.COLUMN_DAY_OF_WEEK, dayOfWeek);
        courseData.put(DatabaseHelper.COLUMN_COURSE_TIME, time);
        courseData.put(DatabaseHelper.COLUMN_MAX_CAPACITY, 30);
        courseData.put(DatabaseHelper.COLUMN_DURATION, duration);
        courseData.put(DatabaseHelper.COLUMN_PRICE, price);
        courseData.put(DatabaseHelper.COLUMN_CLASS_TYPE, classType);
        return courseData;
    }

    private void saveCourseToDatabase(Map<String, Object> courseData) {
        long courseId = databaseManager.insertCourse(courseData);
        if (courseId != -1) {
            showToast("Course added successfully");
            goToHomeFragment();
        } else {
            showToast("Failed to add course");
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void goToHomeFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .addToBackStack(null)
                .commit();
    }
}
