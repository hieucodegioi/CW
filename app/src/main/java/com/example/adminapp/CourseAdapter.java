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
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList; // Dùng List<Course> thay vì List<Map<String, Object>>
    private DatabaseManager databaseManager;
    private FragmentManager fragmentManager;

    // Constructor
    public CourseAdapter(List<Course> courseList, DatabaseManager databaseManager, FragmentManager fragmentManager) {
        this.courseList = courseList;
        this.databaseManager = databaseManager;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate layout item_course.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        Course course = courseList.get(position);

        // Kiểm tra null và gán giá trị mặc định nếu cần
        String courseName = course.getCourseName() != null ? course.getCourseName() : "Chưa có tên";
        String courseDescription = course.getCourseDescription() != null ? course.getCourseDescription() : "Chưa có mô tả";

        // Cập nhật View trong ViewHolder
        holder.courseNameTextView.setText(courseName);
        holder.courseDescriptionTextView.setText(courseDescription);

        // Sử dụng final int để tránh lỗi với lambda
        final int currentPosition = position;

        holder.btnMore.setOnClickListener(v -> {
            // Chuyển sang ClassListFragment với courseId
            showClassList(course.getCourseId());
        });

        holder.btnDelete.setOnClickListener(v -> {
            // Xử lý sự kiện click nút Delete
            // Ví dụ: xóa khóa học khỏi cơ sở dữ liệu và danh sách
            deleteCourse(currentPosition);  // Gọi hàm xóa khóa học
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    // Hàm xóa khóa học
    private void deleteCourse(int position) {
        try {
            if (position >= 0 && position < courseList.size()) {
                Course courseToDelete = courseList.get(position);
                
                // Xóa khỏi cơ sở dữ liệu trước
                if (databaseManager != null) {
                    databaseManager.deleteCourse(courseToDelete.getCourseId());
                }

                // Sau đó xóa khỏi danh sách và cập nhật UI
                courseList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, courseList.size());  // Cập nhật lại các vị trí sau item bị xóa
            }
        } catch (Exception e) {
            Log.e("CourseAdapter", "Error deleting course: " + e.getMessage());
        }
    }

    // Thêm phương thức để chuyển sang ClassListFragment
    private void showClassList(long courseId) {
        if (fragmentManager != null) {
            ClassListFragment classListFragment = ClassListFragment.newInstance(courseId);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, classListFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // ViewHolder Class
    public class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView courseNameTextView, courseDescriptionTextView;
        Button btnMore, btnDelete;

        public CourseViewHolder(View itemView) {
            super(itemView);
            // Ensure the views are not null
            courseNameTextView = itemView.findViewById(R.id.courseName);
            courseDescriptionTextView = itemView.findViewById(R.id.courseDescription);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            if (courseNameTextView == null || courseDescriptionTextView == null ||
                    btnMore == null || btnDelete == null) {
                throw new NullPointerException("One or more views in item_course.xml are null.");
            }
        }
    }
}

