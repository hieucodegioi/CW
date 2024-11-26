package com.example.adminapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class EditClassFragment extends Fragment {
    private EditText teacherNameEditText, sessionCommentEditText;
    private TextView sessionDateTextView;
    private Button saveButton;
    private long classId;
    private DatabaseManager databaseManager;

    public static EditClassFragment newInstance(long classId, String className, String teacher, 
                                              String date, String comments) {
        EditClassFragment fragment = new EditClassFragment();
        Bundle args = new Bundle();
        args.putLong("classId", classId);
        args.putString("className", className);
        args.putString("teacher", teacher);
        args.putString("date", date);
        args.putString("comments", comments);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_class, container, false);

        initializeViews(rootView);
        loadClassData();
        setupListeners();

        return rootView;
    }

    private void initializeViews(View rootView) {
        teacherNameEditText = rootView.findViewById(R.id.teacherName);
        sessionDateTextView = rootView.findViewById(R.id.sessionDate);
        sessionCommentEditText = rootView.findViewById(R.id.sessionComment);
        saveButton = rootView.findViewById(R.id.saveButton);
        databaseManager = new DatabaseManager(getContext());
    }

    private void loadClassData() {
        if (getArguments() != null) {
            classId = getArguments().getLong("classId");
            teacherNameEditText.setText(getArguments().getString("teacher"));
            sessionDateTextView.setText(getArguments().getString("date"));
            sessionCommentEditText.setText(getArguments().getString("comments"));
        }
    }

    private void setupListeners() {
        sessionDateTextView.setOnClickListener(v -> showDatePickerDialog());
        saveButton.setOnClickListener(v -> updateClass());
    }

    private void showDatePickerDialog() {
        // Giống như trong AddClassFragment
    }

    private void updateClass() {
        String teacherName = teacherNameEditText.getText().toString();
        String sessionDate = sessionDateTextView.getText().toString();
        String sessionComment = sessionCommentEditText.getText().toString();

        if (validateInputs(teacherName, sessionDate)) {
            boolean success = databaseManager.updateClass(classId, teacherName, sessionDate, sessionComment);
            if (success) {
                Toast.makeText(getContext(), "Class updated successfully", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Failed to update class", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInputs(String teacherName, String sessionDate) {
        if (TextUtils.isEmpty(teacherName) || TextUtils.isEmpty(sessionDate)) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
} 