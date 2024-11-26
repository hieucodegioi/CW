package com.example.adminapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private List<Class> classList;
    private DatabaseManager databaseManager;
    private FragmentManager fragmentManager;

    public ClassAdapter(List<Class> classList, DatabaseManager databaseManager, FragmentManager fragmentManager) {
        this.classList = classList;
        this.databaseManager = databaseManager;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassViewHolder holder, int position) {
        Class currentClass = classList.get(position);
        holder.className.setText(currentClass.getClassName());
        holder.teacher.setText(currentClass.getTeacher());
        holder.date.setText(currentClass.getDate());
        holder.comments.setText(currentClass.getComments());

        final int currentPosition = position;

        holder.editButton.setOnClickListener(v -> {
            editClass(currentClass);
        });

        holder.deleteButton.setOnClickListener(v -> {
            deleteClass(currentPosition);
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    private void editClass(Class currentClass) {
        EditClassFragment editFragment = EditClassFragment.newInstance(
            currentClass.getClassId(),
            currentClass.getClassName(),
            currentClass.getTeacher(),
            currentClass.getDate(),
            currentClass.getComments()
        );

        if (fragmentManager != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, editFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void deleteClass(int position) {
        try {
            if (position >= 0 && position < classList.size()) {
                Class classToDelete = classList.get(position);
                
                if (databaseManager != null) {
                    databaseManager.deleteClass(classToDelete.getClassId());
                }

                classList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, classList.size());
            }
        } catch (Exception e) {
            Log.e("ClassAdapter", "Error deleting class: " + e.getMessage());
        }
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView className, teacher, date, comments;
        Button editButton, deleteButton;

        public ClassViewHolder(View itemView) {
            super(itemView);
            className = itemView.findViewById(R.id.text_class_name);
            teacher = itemView.findViewById(R.id.text_teacher);
            date = itemView.findViewById(R.id.text_date);
            comments = itemView.findViewById(R.id.text_comments);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
