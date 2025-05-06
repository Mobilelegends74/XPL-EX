package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;

public class TaskProgressDialog extends AppCompatDialogFragment {
    private static final String TAG = "XLua.TaskProgressDialog";

    private Context context;
    private TextView tvTaskStatus;
    private TextView tvTaskDetail;
    private ProgressBar pbTaskProgress;
    private String title = "Task Progress";
    private boolean cancelable = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isCancelling = false;

    private final List<TaskWrapper> tasks = new ArrayList<>();
    private TaskCompletionListener completionListener;

    public interface TaskExecutor {
        boolean execute(TaskProgressCallback callback);
    }

    public interface TaskProgressCallback {
        void updateStatus(String status);
        void updateDetail(String detail);
        void updateProgress(int progress, int max);
    }

    public interface TaskCompletionListener {
        void onAllTasksCompleted(List<TaskResult> results);
    }

    public static class TaskWrapper {
        private final String name;
        private final TaskExecutor executor;

        public TaskWrapper(String name, TaskExecutor executor) {
            this.name = name;
            this.executor = executor;
        }

        public String getName() {
            return name;
        }

        public TaskExecutor getExecutor() {
            return executor;
        }
    }

    public static class TaskResult {
        private final String taskName;
        private final boolean success;
        private final String message;

        public TaskResult(String taskName, boolean success, String message) {
            this.taskName = taskName;
            this.success = success;
            this.message = message;
        }

        public String getTaskName() {
            return taskName;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public static TaskProgressDialog create() {
        return new TaskProgressDialog();
    }

    public TaskProgressDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public TaskProgressDialog setCancelableFlag(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public TaskProgressDialog setTasks(List<TaskWrapper> tasks) {
        this.tasks.clear();
        if (tasks != null) {
            this.tasks.addAll(tasks);
        }
        return this;
    }

    public TaskProgressDialog addTask(String name, TaskExecutor executor) {
        this.tasks.add(new TaskWrapper(name, executor));
        return this;
    }

    public TaskProgressDialog setCompletionListener(TaskCompletionListener listener) {
        this.completionListener = listener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.task_progress_dialog, null);

        tvTaskStatus = view.findViewById(R.id.tvTaskStatus);
        tvTaskDetail = view.findViewById(R.id.tvTaskDetail);
        pbTaskProgress = view.findViewById(R.id.pbTaskProgress);

        builder.setView(view)
                .setTitle(title)
                .setNegativeButton(R.string.option_cancel, null);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);

        // Set custom cancel button behavior
        dialog.setOnShowListener(dialogInterface -> {
            Button cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            cancelButton.setOnClickListener(v -> {
                // Disable the button to prevent multiple clicks
                cancelButton.setEnabled(false);
                cancelButton.setText(R.string.cancelling);

                // Set flag and interrupt the executor
                isCancelling = true;
                executor.shutdownNow();

                // Update UI to show cancelling
                tvTaskStatus.setText(R.string.cancelling);

                // Start a timeout to dismiss the dialog if it takes too long to cancel
                new Handler().postDelayed(() -> {
                    if (isAdded() && getDialog() != null && getDialog().isShowing()) {
                        dismissAllowingStateLoss();
                    }
                }, 500); // Force dismiss after 500ms if tasks don't respond to interruption
            });
        });

        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!tasks.isEmpty()) {
            startTaskExecution();
        } else {
            tvTaskStatus.setText("No tasks to execute");
            pbTaskProgress.setVisibility(View.GONE);
        }
    }

    private void startTaskExecution() {
        executor.submit(() -> {
            final List<TaskResult> results = new ArrayList<>();
            final AtomicInteger taskCount = new AtomicInteger(0);
            final int totalTasks = tasks.size();

            for (int i = 0; i < tasks.size(); i++) {
                if (isRemoving() || !isAdded() || isCancelling) {
                    break; // Exit if dialog is being removed or not added or cancelling
                }

                TaskWrapper task = tasks.get(i);
                final String taskName = task.getName();

                // Update status on UI thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isDetached() && !isCancelling) {
                        tvTaskStatus.setText(String.format("Executing: %s (%d/%d)",
                                taskName, taskCount.get() + 1, totalTasks));
                        tvTaskDetail.setText("");
                    }
                });

                TaskProgressCallback callback = createCallbackForTask();

                boolean success = false;
                String message = "";

                try {
                    success = task.getExecutor().execute(callback);
                    message = success ? "Completed successfully" : "Failed to complete";
                } catch (Exception e) {
                    if (isCancelling) {
                        message = "Cancelled";
                    } else if (DebugUtil.isDebug()) {
                        Log.e(TAG, "Error executing task: " + taskName, e);
                        message = "Error: " + e.getMessage();
                    }
                }

                results.add(new TaskResult(taskName, success, message));
                taskCount.incrementAndGet();

                if (isCancelling) {
                    break; // Stop processing more tasks if cancelling
                }
            }

            // All tasks completed or cancelled, update UI and notify listener
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isDetached()) {
                    if (isCancelling) {
                        tvTaskStatus.setText(R.string.task_cancelled);

                        // Just dismiss after a short delay if cancelling
                        new Handler().postDelayed(() -> {
                            if (isAdded()) {
                                dismissAllowingStateLoss();
                            }
                        }, 300);
                    } else {
                        tvTaskStatus.setText("All tasks completed");

                        // Hide the progress bar when all tasks are completed
                        pbTaskProgress.setVisibility(View.GONE);

                        if (completionListener != null) {
                            completionListener.onAllTasksCompleted(results);
                        }

                        // Make dialog dismissible after completion
                        Dialog dialog = getDialog();
                        if (dialog != null) {
                            dialog.setCancelable(true);
                            dialog.setCanceledOnTouchOutside(true);
                        }
                    }
                }
            });
        });
    }

    private TaskProgressCallback createCallbackForTask() {
        return new TaskProgressCallback() {
            @Override
            public void updateStatus(String status) {
                if (isCancelling) return;

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isDetached() && !isCancelling) {
                        tvTaskStatus.setText(status);
                    }
                });
            }

            @Override
            public void updateDetail(String detail) {
                if (isCancelling) return;

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isDetached() && !isCancelling) {
                        tvTaskDetail.setText(detail);
                    }
                });
            }

            @Override
            public void updateProgress(int progress, int max) {
                if (isCancelling) return;

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isDetached() && !isCancelling) {
                        pbTaskProgress.setMax(max);
                        pbTaskProgress.setProgress(progress);
                    }
                });
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Ensure executor is shut down
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}