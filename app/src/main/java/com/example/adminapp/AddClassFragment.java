package com.example.adminapp;

import android.app.DatePickerDialog;
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

import java.util.Calendar;

public class AddClassFragment extends Fragment {

    private EditText teacherNameEditText, sessionCommentEditText;
    private TextView sessionDateTextView;
    private Button saveButton;
    private long courseId;

    private DatabaseManager databaseManager;

    // Default constructor
    public AddClassFragment() {
    }

    // Static method to create an instance of the fragment with courseId
    public static AddClassFragment newInstance(long courseId) {
        AddClassFragment fragment = new AddClassFragment();
        Bundle args = new Bundle();
        args.putLong("courseId", courseId); // Store courseId in arguments
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_class, container, false);

        // Initialize views
        teacherNameEditText = rootView.findViewById(R.id.teacherName);
        sessionDateTextView = rootView.findViewById(R.id.sessionDate);
        sessionCommentEditText = rootView.findViewById(R.id.sessionComment);
        saveButton = rootView.findViewById(R.id.saveButton);

        // Initialize DatabaseManager
        databaseManager = new DatabaseManager(getContext());

        // Retrieve courseId from arguments
        if (getArguments() != null) {
            courseId = getArguments().getLong("courseId");
        }

        // Set up date picker
        sessionDateTextView.setOnClickListener(v -> showDatePickerDialog());

        // Set OnClickListener for the save button
        saveButton.setOnClickListener(v -> saveClass());

        return rootView;
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Update the TextView with selected date
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    sessionDateTextView.setText(selectedDate);
                }, year, month, day);
        
        datePickerDialog.show();
    }

    private void saveClass() {
        // Retrieve input values
        String teacherName = teacherNameEditText.getText().toString();
        String sessionDate = sessionDateTextView.getText().toString();
        String sessionComment = sessionCommentEditText.getText().toString();

        // Validate input fields
        if (TextUtils.isEmpty(teacherName) || TextUtils.isEmpty(sessionDate)) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
        } else {
            // Insert class into database
            long classId = databaseManager.insertClass(courseId, teacherName, sessionDate, sessionComment);

            // Check if the class was inserted successfully
            if (classId != -1) {
                Toast.makeText(getContext(), "Class added successfully", Toast.LENGTH_SHORT).show();
                
                // Trở về ClassListFragment với courseId
                ClassListFragment classListFragment = ClassListFragment.newInstance(courseId);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, classListFragment)
                        .commit();
            } else {
                Toast.makeText(getContext(), "Failed to add class", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Clear input fields after class is added
    private void clearFields() {
        teacherNameEditText.setText("");
        sessionDateTextView.setText("");
        sessionCommentEditText.setText("");
    }
}
