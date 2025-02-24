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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.ui.core.UINotifier;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class SettingsProgressDialog extends AppCompatDialogFragment {
    private static final String TAG = LibUtil.generateTag(SettingsProgressDialog.class);

    private Context context;
    private Context context2;
    private TextView tvProgressData;
    private ProgressBar pbProgress;
    private UINotifier notifier;
    private Map<SettingHolder, SettingPacket> data;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static SettingsProgressDialog create() {
        return new SettingsProgressDialog();
    }

    public Context getContext() {
        return context == null ? context2 : context;
    }

    public SettingsProgressDialog setNotifier(UINotifier notifier) {
        this.notifier = notifier;
        return this;
    }

    public SettingsProgressDialog setData(Map<SettingHolder, SettingPacket> data, Context context) {
        this.data = data;
        this.context2 = context;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.hookdeplyload, null);

        pbProgress = view.findViewById(R.id.pbDeployHooks);
        tvProgressData = view.findViewById(R.id.tvDeployHookName);

        builder.setView(view)
                .setTitle(getString(R.string.title_deploy_settings))
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
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
        if (!isProcessing.get()) {
            isProcessing.set(true);
            new Thread(this::processSettings).start();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainHandler.removeCallbacksAndMessages(null);
        context = null;
        context2 = null;
    }

    private void processSettings() {
        if (data == null || getContext() == null) {
            dismissSafely();
            return;
        }

        List<String> succeeded = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        try {
            if (DebugUtil.isDebug()) {
                Log.d(TAG, "Sending Settings Size=" + data.size());
            }

            for (Map.Entry<SettingHolder, SettingPacket> entry : data.entrySet()) {
                if (!isAdded()) {
                    return;
                }

                SettingPacket packet = entry.getValue();
                updateProgress(packet.name);

                A_CODE code = PutSettingExCommand.call(getContext(), packet);
                if (DebugUtil.isDebug()) {
                    Log.d(TAG, "Send Setting Packet, Name=" + packet.name +
                            " Result=" + code + " Value=" + packet.value +
                            " UID=" + packet.getUid() + " Category=" + packet.getCategory());
                }

                SettingHolder holder = entry.getKey();
                if (code == A_CODE.SUCCESS) {
                    holder.setValue(holder.getNewValue(), true);
                    holder.setNameLabelColor(getContext());
                    holder.notifyUpdate(notifier);
                    succeeded.add(holder.getName());
                } else {
                    failed.add(holder.getName());
                }
            }

            showResults(succeeded, failed);
        } catch (Exception e) {
            Log.e(TAG, "Error processing settings", e);
            mainHandler.post(() -> {
                if (isAdded()) {
                    tvProgressData.setText(R.string.msg_error_processing);
                    dismissSafely();
                }
            });
        }
    }

    private void updateProgress(final String progressText) {
        mainHandler.post(() -> {
            if (isAdded() && tvProgressData != null) {
                tvProgressData.setText(progressText);
            }
        });
    }

    private void showResults(List<String> succeeded, List<String> failed) {
        if (!isAdded()) return;

        final StrBuilder sb = new StrBuilder();
        sb.appendLine(getString(R.string.msg_succeeded_count));
        sb.newLine();
        for (String s : succeeded) {
            sb.appendLine(s);
        }

        if (!failed.isEmpty()) {
            sb.appendLine(getString(R.string.msg_failed_count));
            sb.newLine();
            for (String f : failed) {
                sb.appendLine(f);
            }
        }

        mainHandler.post(() -> {
            if (isAdded()) {
                if (tvProgressData != null) {
                    tvProgressData.setText(sb.toString());
                }
                if (pbProgress != null) {
                    pbProgress.setVisibility(View.GONE);
                }
                dismissSafely();
            }
        });
    }

    private void dismissSafely() {
        mainHandler.postDelayed(() -> {
            try {
                if (isAdded()) {
                    dismissAllowingStateLoss();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error dismissing dialog", e);
            }
        }, 2000); // Give user time to see results before dismissing
    }
}