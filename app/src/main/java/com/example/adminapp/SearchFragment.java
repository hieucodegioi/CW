package com.example.adminapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private ClassAdapter searchAdapter;
    private List<Class> searchResults;
    private DatabaseHelper dbHelper;
    private DatabaseManager databaseManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        
        initializeViews(rootView);
        setupRecyclerView();
        setupSearchListener();
        
        return rootView;
    }

    private void initializeViews(View rootView) {
        searchEditText = rootView.findViewById(R.id.search_edit_text);
        searchResultsRecyclerView = rootView.findViewById(R.id.recycler_view_search_results);
        dbHelper = new DatabaseHelper(requireContext());
        databaseManager = new DatabaseManager(requireContext());
        databaseManager.open();
    }

    private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        searchAdapter = new ClassAdapter(
            searchResults,
            databaseManager,
            getParentFragmentManager()
        );
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsRecyclerView.setAdapter(searchAdapter);
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            private final Handler searchHandler = new Handler();
            private static final long SEARCH_DELAY_MS = 300;
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search if any
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Create new search with delay
                searchRunnable = () -> filterClassesByTeacher(s.toString());
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterClassesByTeacher(String query) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.query(
                     DatabaseHelper.TABLE_CLASSES,
                     null,
                     DatabaseHelper.COLUMN_TEACHER + " LIKE ?",
                     new String[]{"%" + query + "%"},
                     null,
                     null,
                     DatabaseHelper.COLUMN_DATE + " DESC")) {

            searchResults.clear();

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    searchResults.add(createClassFromCursor(cursor));
                }
            }

            updateUIWithResults();

        } catch (Exception e) {
            Log.e("SearchFragment", "Error searching classes: " + e.getMessage());
            showError("Error searching classes");
        }
    }

    private Class createClassFromCursor(Cursor cursor) {
        return new Class(
            cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_NAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEACHER)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMMENTS))
        );
    }

    private void updateUIWithResults() {
        if (isAdded()) {  // Check if fragment is still attached
            requireActivity().runOnUiThread(() -> {
                searchAdapter.notifyDataSetChanged();
                if (searchResults.isEmpty()) {
                    showNoResults();
                }
            });
        }
    }

    private void showNoResults() {
        // Implement UI feedback for no results
    }

    private void showError(String message) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> 
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}
