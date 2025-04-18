package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.ui.adapters.SettingsSearchAdapter;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;

public class SettingsSearchDialog extends AppCompatDialogFragment {
    private static final String TAG = LibUtil.generateTag(SettingsSearchDialog.class);

    private Context context;
    private SettingHolder parentSetting;
    private final List<SettingHolder> settings = new ArrayList<>();
    private final List<String> checkedSettings = new ArrayList<>();
    private SettingsSearchAdapter adapter;
    private OnFinishListener onFinishListener;

    public interface OnFinishListener {  void onFinish(List<SettingHolder> allSettings, List<String> checkedSettings); }

    public static SettingsSearchDialog create() { return new SettingsSearchDialog(); }

    public SettingsSearchDialog removeParentSettingFromList() {
        if(this.parentSetting != null) {
            if(ListUtil.isValid(this.settings)) {
                for (int i = this.settings.size() - 1; i >= 0; i--) {
                    SettingHolder item = this.settings.get(i);
                    //ToDo: Make Cool Linq functions for this shit
                    if(item.getName().equalsIgnoreCase(this.parentSetting.getName()))
                        this.settings.remove(i);
                }
            }

            if(ListUtil.isValid(this.checkedSettings)) {
                for (int i = this.checkedSettings.size() - 1; i >= 0; i--) {
                    String item = this.checkedSettings.get(i);
                    if(item.equalsIgnoreCase(this.parentSetting.getName()))
                        this.checkedSettings.remove(i);
                }
            }
        }

        return this;
    }

    public SettingsSearchDialog setSettings(List<SettingHolder> settings) {
        this.settings.clear();
        if(ListUtil.isValid(settings)) {
            for(SettingHolder setting : settings) {
                if(!this.settings.contains(setting))
                    this.settings.add(setting);
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Finished Setting Settings Count (%s) From Count (%s)", ListUtil.size(this.settings), ListUtil.size(settings)));

        return this;
    }

    public SettingsSearchDialog setChecked(List<String> checked) {
        this.checkedSettings.clear();
        if(ListUtil.isValid(checked)) {
            for(String c : checked) {
                String trimmed = Str.trimOriginal(c);
                if(!Str.isEmpty(trimmed) && !this.checkedSettings.contains(trimmed))
                    this.checkedSettings.add(trimmed);
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Finished Setting Checked Settings Count (%s) From Count (%s)", ListUtil.size(this.checkedSettings), ListUtil.size(checked)));

        return this;
    }

    public SettingsSearchDialog setCheckedFromValue(SettingHolder settingHolder) {
        if(settingHolder != null) {
            this.parentSetting = settingHolder;
            String value = settingHolder.getNewValue();
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Setting Checked Settings From Setting (%s) Value=%s", settingHolder.getName(), Str.toStringOrNull(value)));

            if(Str.isValid(value)) {
                String decoded = Str.fromBase64String(value, Str.CHAR_SET_UTF_8);
                if(!Str.isEmpty(decoded)) {
                    List<String> parts = Str.splitToList(decoded, Str.NEW_LINE);
                    setChecked(parts);
                }
            }
        }

        return this;
    }

    public SettingsSearchDialog setOnFinishListener(OnFinishListener listener) {
        this.onFinishListener = listener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.settings_search_dialog, null);

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Inflating/Creating Settings Search Dialog with (%s) Settings and (%s) Checked Settings From (%s)",
                    ListUtil.size(this.settings),
                    ListUtil.size(this.checkedSettings),
                    TryRun.getOrDefault(() -> this.parentSetting.getName(), Str.EMPTY)));


        EditText etSearch = view.findViewById(R.id.etSearchSettings);
        ListView lvSettings = view.findViewById(R.id.lvSettings);

        // Initialize adapter with settings and checked items
        adapter = new SettingsSearchAdapter(context, settings, checkedSettings);
        lvSettings.setAdapter(adapter);

        // Set up search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s.toString());
            }
        });

        builder.setView(view)
                .setTitle(R.string.title_settings_search)
                .setNegativeButton(R.string.option_cancel, null)
                .setPositiveButton(R.string.option_ok, (dialog, which) -> {
                    if (onFinishListener != null) {
                        onFinishListener.onFinish(settings, adapter.getCheckedSettings());
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}