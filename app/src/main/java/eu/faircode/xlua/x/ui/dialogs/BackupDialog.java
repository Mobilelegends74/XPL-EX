package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.ResultRequest;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.DropTableCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutAssignmentCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutHookExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignmentsCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetSettingsExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.XBackup;
import eu.faircode.xlua.x.xlua.settings.data.XScript;

public class BackupDialog extends AppCompatDialogFragment {
    private static final String TAG = LibUtil.generateTag(BackupDialog.class);

    private Context context;
    private XBackup backup;
    private boolean wasCreatedFromFile;
    private Button positiveButton;

    private EditText tiBackupName;
    private CheckBox cbDefinitions, cbAllHooks, cbSettings, cbAssignments, cbDeleteOld;

    private final List<XHook> allHooks = new ArrayList<>();
    private boolean keepDialogOpenAfterCompletion = true; // Default to keeping dialog open

    public interface IOnOperationExportOrApplied {
        void onFinishedSuccessfully(XBackup instance, boolean wasImported);
    }

    private IOnOperationExportOrApplied onOperationEvent;

    public static BackupDialog create() { return new BackupDialog(); }

    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        return sdf.format(new Date());
    }


    public BackupDialog setBackup(XBackup backup, boolean wasImportedFromFile) {
        this.backup = backup;
        this.wasCreatedFromFile = wasImportedFromFile;
        if(!this.wasCreatedFromFile && this.backup != null && Str.isEmpty(this.backup.getName()))
            backup.setName("XPL-ex_" + getCurrentTimestamp());

        return this;
    }



    public BackupDialog setKeepDialogOpenAfterCompletion(boolean keepOpen) {
        this.keepDialogOpenAfterCompletion = keepOpen;
        return this;
    }

    public BackupDialog setDefinitions(Context context) { return setDefinitions(GetHooksCommand.dump(context, true)); }
    public BackupDialog setDefinitions(List<XHook> definitions) {
        if (backup == null) {
            backup = new XBackup();
            backup.setName("XPL-ex_" + getCurrentTimestamp());
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting Definitions, Count=" + ListUtil.size(definitions));

        backup.getDefinitions().clear();
        if (ListUtil.isValid(definitions)) {
            backup.getDefinitions().addAll(definitions);
            updateCheckboxCounts();
        }
        return this;
    }

    public BackupDialog setAllHooks(Context context) { return setAllHooks(GetHooksCommand.getHooks(context, true, true));}
    public BackupDialog setAllHooks(List<XHook> hooks) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting All Hooks, Count=" + ListUtil.size(hooks));

        ListUtil.addAll(this.allHooks, hooks, true);
        updateCheckboxCounts();
        return this;
    }

    public BackupDialog setSettings(Context context) { return setSettings(GetSettingsExCommand.dump(context, true)); }
    public BackupDialog setSettings(List<SettingPacket> settings) {
        if (backup == null) {
            backup = new XBackup();
            backup.setName("XPL-ex_" + getCurrentTimestamp());
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting Settings, Count=" + ListUtil.size(settings));

        backup.getSettings().clear();
        if (ListUtil.isValid(settings)) {
            for(SettingPacket packet : settings)
                if(packet.value != null)
                    backup.addSetting(packet);

            updateCheckboxCounts();
        }
        return this;
    }

    public BackupDialog setAssignments(Context context) { return setAssignments(GetAssignmentsCommand.dump(context, false)); }
    public BackupDialog setAssignments(List<AssignmentPacket> assignments) {
        if (backup == null) {
            backup = new XBackup();
            backup.setName("XPL-ex_" + getCurrentTimestamp());
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Setting Assignments, Count=" + ListUtil.size(assignments));

        backup.getAssignments().clear();
        if (ListUtil.isValid(assignments)) {
            for(AssignmentPacket packet : assignments)
                if(!Str.isEmpty(packet.getHookId()))
                    backup.addAssignment(packet);

            updateCheckboxCounts();
        }
        return this;
    }

    public BackupDialog setOnOperationExportedOrApplied(IOnOperationExportOrApplied onEvent) {
        this.onOperationEvent = onEvent;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.backup_dialog, null);

        initializeViews(view);
        setupListeners();
        updateCheckboxCounts();

        builder.setView(view)
                .setTitle(R.string.title_backup)
                .setPositiveButton(wasCreatedFromFile ? R.string.option_apply : R.string.option_export, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do more init
                        if(!wasCreatedFromFile) {
                            backup.setDate(System.currentTimeMillis());
                            backup.setAppVersion(BuildConfig.VERSION_NAME);
                        }

                        backup = backup.createFilteredCopy(
                                cbDefinitions.isChecked(),
                                true,
                                cbAssignments.isChecked(),
                                cbSettings.isChecked());

                        backup.dropOld = cbDeleteOld.isChecked();

                        if(wasCreatedFromFile) {
                            // Use TaskProgressDialog for import operation
                            applyBackupWithProgress(backup);
                        } else {
                            // For export, just notify completion
                            notifyOperationComplete();
                        }
                    }
                })
                .setNegativeButton(R.string.option_cancel, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            updateCheckboxCounts();
            ensurePositiveButtonState();
        });

        return dialog;
    }

    private void applyBackupWithProgress(final XBackup backupToApply) {
        if(backupToApply == null || !isAdded()) return;

        // Cache the context to use throughout the background operation
        final Context safeContext = getContext();
        if (safeContext == null) return; // Exit if context is not available

        // Pre-cache string resources we'll need to prevent calls to getString() when detached
        final String titleApplyingBackup = getString(R.string.title_applying_backup);
        final String taskApplyingSettings = getString(R.string.task_applying_settings);
        final String taskApplyingDefinitions = getString(R.string.task_applying_definitions);
        final String taskApplyingAssignments = getString(R.string.task_applying_assignments);
        final String taskComplete = getString(R.string.task_complete);
        final String taskProgressFormat = getString(R.string.task_progress_format);
        final String taskDetailDroppingSettings = getString(R.string.task_detail_dropping_settings);
        final String taskDetailDroppingSettingsTable = getString(R.string.task_detail_dropping_settings_table);
        final String taskDetailDroppingHooks = getString(R.string.task_detail_dropping_hooks);
        final String taskDetailDroppingHooksTable = getString(R.string.task_detail_dropping_hooks_table);
        final String taskDetailDroppingAssignments = getString(R.string.task_detail_dropping_assignments);
        final String taskDetailDroppingAssignmentsTable = getString(R.string.task_detail_dropping_assignments_table);
        final String taskDetailProcessingScriptsStatus = getString(R.string.task_detail_processing_scripts_status);
        final String taskDetailProcessingScripts = getString(R.string.task_detail_processing_scripts);
        final String taskDetailApplyingSetting = getString(R.string.task_detail_applying_setting);
        final String taskDetailApplyingHook = getString(R.string.task_detail_applying_hook);
        final String taskDetailApplyingAssignment = getString(R.string.task_detail_applying_assignment);
        final String taskDetailSettingsComplete = getString(R.string.task_detail_settings_complete);
        final String taskDetailDefinitionsComplete = getString(R.string.task_detail_definitions_complete);
        final String taskDetailAssignmentsComplete = getString(R.string.task_detail_assignments_complete);
        final String taskCancelled = getString(R.string.task_cancelled);

        TaskProgressDialog dialog = TaskProgressDialog.create()
                .setTitle(titleApplyingBackup)
                .setCancelableFlag(false);

        int sleep = 5;
        // Add task for settings if needed
        if(ListUtil.isValid(backupToApply.getSettings()) && !backupToApply.getSettings().isEmpty()) {
            dialog.addTask(taskApplyingSettings, callback -> {
                if(Thread.interrupted()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isDetached()) {
                            callback.updateStatus(taskCancelled);
                        }
                    });
                    return false;
                }

                if(backupToApply.dropOld) {
                    // Direct update for dropping tables - use cached strings
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isDetached()) {
                            callback.updateStatus(taskDetailDroppingSettings);
                            callback.updateDetail(taskDetailDroppingSettingsTable);
                        }
                    });

                    DropTableCommand.drop(safeContext, SettingPacket.TABLE_NAME, "xlua");

                    // Sleep after operation
                    try { Thread.sleep(sleep); } catch (InterruptedException e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }
                }

                int total = backupToApply.getSettings().size();
                int success = 0;

                for(int i = 0; i < backupToApply.getSettings().size(); i++) {
                    if(Thread.interrupted()) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }

                    final SettingPacket packet = backupToApply.getSettings().get(i);
                    final int currentCount = i + 1;

                    // Update UI - must use a new runnable for each update
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Check if fragment is still attached before updating UI
                        if (!isDetached()) {
                            callback.updateStatus(String.format(taskProgressFormat, currentCount, total));
                            callback.updateDetail(String.format(taskDetailApplyingSetting, packet.name));
                        }
                    });

                    if(packet.value != null) {
                        packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
                        A_CODE code = PutSettingExCommand.call(safeContext, packet);
                        if(A_CODE.isSuccessful(code)) {
                            success++;
                        }
                    }

                    // Sleep after each setting is processed
                    try { Thread.sleep(sleep); } catch (InterruptedException e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }
                }

                final int finalSuccess = success;
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Check if fragment is still attached before updating UI
                    if (!isDetached()) {
                        callback.updateStatus(taskComplete);
                        callback.updateDetail(String.format(taskDetailSettingsComplete, finalSuccess, total));
                    }
                });

                return success > 0;
            });
        }

        // Add task for definitions/hooks if needed
        if(ListUtil.isValid(backupToApply.getDefinitions()) && backupToApply.getDefinitions().size() > 0) {
            dialog.addTask(taskApplyingDefinitions, callback -> {
                if(Thread.interrupted()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isDetached()) {
                            callback.updateStatus(taskCancelled);
                        }
                    });
                    return false;
                }

                if(backupToApply.dropOld) {
                    // Direct update for dropping tables - use cached strings
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isDetached()) {
                            callback.updateStatus(taskDetailDroppingHooks);
                            callback.updateDetail(taskDetailDroppingHooksTable);
                        }
                    });

                    DropTableCommand.drop(safeContext, XHook.TABLE_NAME, "xlua");

                    // Sleep after operation
                    try { Thread.sleep(sleep); } catch (InterruptedException e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }
                }

                // Prepare Lua scripts map
                Map<String, String> luaScripts = new HashMap<>();
                if(ListUtil.isValid(backupToApply.getScripts())) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isDetached()) {
                            callback.updateStatus(taskDetailProcessingScriptsStatus);
                            callback.updateDetail(taskDetailProcessingScripts);
                        }
                    });

                    for(XScript script : backupToApply.getScripts()) {
                        String name = ensureLuaScript(script.getName(), null);
                        String code = script.getCode();
                        if(Str.isEmpty(code) || Str.isEmpty(name))
                            continue;

                        luaScripts.put(name, code);
                    }

                    // Sleep after script processing
                    try { Thread.sleep(sleep); } catch (InterruptedException e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }
                }

                int total = backupToApply.getDefinitions().size();
                int success = 0;

                for(int i = 0; i < backupToApply.getDefinitions().size(); i++) {
                    if(Thread.interrupted()) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }

                    final XHook hook = backupToApply.getDefinitions().get(i);
                    final int currentCount = i + 1;
                    final String hookId = Str.ensureIsNotNullOrDefault(hook.getObjectId(), "unknown");

                    // Update UI - must use a new runnable for each update
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isDetached()) {
                            callback.updateStatus(String.format(taskProgressFormat, currentCount, total));
                            callback.updateDetail(String.format(taskDetailApplyingHook, hookId));
                        }
                    });

                    if(hook != null && !Str.isEmpty(hook.getObjectId())) {
                        String luaScript = hook.luaScript;
                        String resolved = Str.ensureIsNotNullOrDefault(
                                ensureLuaScript(luaScript, luaScripts), Str.EMPTY);
                        hook.luaScript = luaScript;

                        ResultRequest code = PutHookExCommand.putEx(safeContext, hook, false);
                        if(code.successful()) {
                            success++;
                        }
                    }

                    // Sleep after each hook is processed
                    try { Thread.sleep(sleep); } catch (InterruptedException e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }
                }

                final int finalSuccess = success;
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isDetached()) {
                        callback.updateStatus(taskComplete);
                        callback.updateDetail(String.format(taskDetailDefinitionsComplete, finalSuccess, total));
                    }
                });

                return success > 0;
            });
        }

        // Add task for assignments if needed
        if(ListUtil.isValid(backupToApply.getAssignments()) && backupToApply.getAssignments().size() > 0) {
            dialog.addTask(taskApplyingAssignments, callback -> {
                if(Thread.interrupted()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isDetached()) {
                            callback.updateStatus(taskCancelled);
                        }
                    });
                    return false;
                }

                if(backupToApply.dropOld) {
                    // Direct update for dropping tables - use cached strings
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isDetached()) {
                            callback.updateStatus(taskDetailDroppingAssignments);
                            callback.updateDetail(taskDetailDroppingAssignmentsTable);
                        }
                    });

                    DropTableCommand.drop(safeContext, AssignmentPacket.TABLE_NAME, "xlua");

                    // Sleep after operation
                    try { Thread.sleep(sleep); } catch (InterruptedException e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }
                }

                int total = backupToApply.getAssignments().size();
                int success = 0;

                for(int i = 0; i < backupToApply.getAssignments().size(); i++) {
                    if(Thread.interrupted()) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }

                    final AssignmentPacket assignmentPacket = backupToApply.getAssignments().get(i);
                    final int currentCount = i + 1;

                    // Update UI - must use a new runnable for each update
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isDetached()) {
                            callback.updateStatus(String.format(taskProgressFormat, currentCount, total));
                            callback.updateDetail(String.format(taskDetailApplyingAssignment,
                                    assignmentPacket.getHookId(), assignmentPacket.getCategory()));
                        }
                    });

                    if(!Str.isEmpty(assignmentPacket.getCategory()) && !Str.isEmpty(assignmentPacket.getHookId())) {
                        assignmentPacket.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
                        A_CODE code = PutAssignmentCommand.put(safeContext, assignmentPacket);
                        if(A_CODE.isSuccessful(code)) {
                            success++;
                        }
                    }

                    // Sleep after each assignment is processed
                    try { Thread.sleep(sleep); } catch (InterruptedException e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isDetached()) {
                                callback.updateStatus(taskCancelled);
                            }
                        });
                        return false;
                    }
                }

                final int finalSuccess = success;
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isDetached()) {
                        callback.updateStatus(taskComplete);
                        callback.updateDetail(String.format(taskDetailAssignmentsComplete, finalSuccess, total));
                    }
                });

                return success > 0;
            });
        }

        // Set completion listener to notify when all tasks are done
        dialog.setCompletionListener(results -> {
            // Check results and provide appropriate feedback
            boolean allSucceeded = true;
            for(TaskProgressDialog.TaskResult result : results) {
                if(!result.isSuccess()) {
                    allSucceeded = false;
                    break;
                }
            }

            // Only notify about operation completion if fragment is still attached
            if (!isDetached()) {
                notifyOperationComplete();
            }

            // Make dialog dismissible after completion but don't auto-dismiss
            if (dialog.getDialog() != null) {
                dialog.getDialog().setCancelable(true);
                dialog.getDialog().setCanceledOnTouchOutside(true);
            }
        });

        // Show the dialog
        dialog.show(getParentFragmentManager(), "apply_backup_progress");
    }

    // Helper method similar to ActivityMain.ensureLuaScript
    private static String ensureLuaScript(String name, Map<String, String> map) {
        if(Str.isEmpty(name))
            return Str.EMPTY;

        String trimmed = Str.trimEx(Str.trimEx(name.trim(), true, false, true, false, "@"),
                true, false, false, true, ".lua");

        if(map == null)
            return trimmed;

        if(name.startsWith("@") || name.endsWith(".lua")) {
            String code = map.get(trimmed);
            if(Str.isEmpty(code))
                return Str.EMPTY;

            return code;
        }

        return trimmed;
    }

    private void initializeViews(View view) {
        tiBackupName = view.findViewById(R.id.tiBackupName);
        cbDefinitions = view.findViewById(R.id.cbDefinitions);
        cbAllHooks = view.findViewById(R.id.cbAllHooks);
        cbSettings = view.findViewById(R.id.cbSettings);
        cbAssignments = view.findViewById(R.id.cbAssignments);
        cbDeleteOld = view.findViewById(R.id.cbDeleteOld);


        if (backup != null && !Str.isEmpty(backup.getName())) {
            tiBackupName.setText(backup.getName());
        }
    }

    final CompoundButton.OnCheckedChangeListener checkListener = (compoundButton, b) -> {
        updateCheckboxCounts();
        ensurePositiveButtonState();
    };

    private void checkboxWire(boolean wire) {
        try {
            cbAllHooks.setOnCheckedChangeListener(wire ? checkListener : null);
            cbDefinitions.setOnCheckedChangeListener(wire ? checkListener : null);
            cbSettings.setOnCheckedChangeListener(wire ? checkListener : null);
            cbAssignments.setOnCheckedChangeListener(wire ? checkListener : null);
            if(wasCreatedFromFile && cbDeleteOld != null) {
                cbDeleteOld.setOnCheckedChangeListener(wire ? checkListener : null);
            }
        }catch (Exception e) {
            Log.e(TAG, "Check Box Link Error Event, Error=" + e);
        }
    }

    private void setupListeners() {
        tiBackupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (backup != null)
                    backup.setName(s.toString());
            }
        });
    }

    private void updateCheckboxCounts() {
        if (!isAdded())
            return;

        new Handler(Looper.getMainLooper()).post(() -> {
            checkboxWire(false);
            if (backup != null) {
                ensureCheckBoxState(cbDefinitions, R.string.text_definitions_count, ListUtil.size(backup.getDefinitions()));
                ensureCheckBoxState(cbSettings, R.string.text_settings_count, ListUtil.size(backup.getSettings()));
                ensureCheckBoxState(cbAssignments, R.string.text_assignments_count, ListUtil.size(backup.getAssignments()));
            }

            ensureCheckBoxState(cbAllHooks, R.string.text_all_hooks, ListUtil.size(allHooks));
            if(cbDeleteOld != null) {
                try {
                    cbDeleteOld.setVisibility(wasCreatedFromFile ? View.VISIBLE : View.GONE);
                    cbDeleteOld.setEnabled(wasCreatedFromFile);
                }catch (Exception ignored) { }
            }
            checkboxWire(true);
        });
    }

    public void ensureCheckBoxState(CheckBox checkBox, int resId, int count) {
        if(checkBox != null) {
            try {
                checkBox.setText(getString(resId, count));
                checkBox.setEnabled(count > 0);
                if(count < 1)
                    checkBox.setChecked(false);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Ensure the Check Box State, Error=" + e);
            }
        }
    }

    private void ensurePositiveButtonState() { ensurePositiveButtonState(isPositiveEnabled()); }
    private void ensurePositiveButtonState(boolean enable) {
        if(positiveButton != null) {
            try {
                positiveButton.setEnabled(enable);
                positiveButton.setAlpha(enable ? 1.0f : 0.5f);
            }catch (Exception e) {
                Log.e(TAG, "Failed to Set Positive Button State! Error=" + e);
            }
        }
    }

    private boolean isPositiveEnabled() {
        if(backup != null) {
            try {
                return (ListUtil.isValid(backup.getAssignments()) && cbAssignments.isChecked()) ||
                        (ListUtil.isValid(backup.getSettings()) && cbSettings.isChecked()) ||
                        (ListUtil.isValid(backup.getDefinitions()) && cbDefinitions.isChecked()) ||
                        (ListUtil.isValid(allHooks) && cbAllHooks.isChecked());
            }catch (Exception e) {
                Log.e(TAG, "Failed to get is Positive Enabled, Error=" + e);
            }
        }

        return false;
    }

    private void notifyOperationComplete() {
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "on Finished Dump=" + backup.dumpLog());

            if (onOperationEvent != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    new Handler(Looper.getMainLooper())
                            .post(() -> onOperationEvent.onFinishedSuccessfully(backup, wasCreatedFromFile));
                } else {
                    onOperationEvent.onFinishedSuccessfully(backup, wasCreatedFromFile);
                }
            }
        } catch (Exception e) {
            if (DebugUtil.isDebug()) {
                Log.e(TAG, "Error notifying operation complete: " + e);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}