package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHookIO;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;

public class HookEditDialog extends AppCompatDialogFragment {
    public static HookEditDialog create() { return new HookEditDialog(); }

    private final XHook hook = new XHook();
    private OnHookEditListener editListener;
    private boolean expandedScriptView;

    // UI
    private ImageView ivExpander;
    private TextView tvExpander;
    private ScrollView scroll;
    private EditText etScript;
    private EditText etCollection, etGroup, etName, etAuthor, etVersion, etDescription;
    private CheckBox cbEnabled, cbOptional, cbUsage, cbNotify;
    private EditText etClass, etMethod, etParams, etReturn;
    private EditText etMinSdk, etMaxSdk, etMinApk, etMaxApk;
    private EditText etExclude, etTarget, etSettings;

    public interface OnHookEditListener {  void onHookEdited(XHook hook); }

    public HookEditDialog setHook(XHook hook) {
        if(hook != null) XHookIO.copy(hook, this.hook);
        return this;
    }

    public HookEditDialog setEditListener(OnHookEditListener listener) {
        this.editListener = listener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.define, null);

        initializeViews(view);
        populateFields();
        setupExpanderLogic();

        builder.setView(view)
                .setTitle(R.string.title_hook_edit)
                .setPositiveButton(R.string.option_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveHookData();
                        if (editListener != null) {
                            TryRun.silent(() -> editListener.onHookEdited(hook));
                        }
                    }
                })
                .setNegativeButton(R.string.option_cancel, null);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make the dialog full-width / wrap-content height
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            d.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void initializeViews(View view) {
        ivExpander   = view.findViewById(R.id.ivExpander);
        tvExpander   = view.findViewById(R.id.tvExpander);
        scroll       = view.findViewById(R.id.scroll);
        etScript     = view.findViewById(R.id.etScript);

        etCollection = view.findViewById(R.id.etCollection);
        etGroup      = view.findViewById(R.id.etGroup);
        etName       = view.findViewById(R.id.etName);
        etAuthor     = view.findViewById(R.id.etAuthor);
        etVersion    = view.findViewById(R.id.etVersion);
        etDescription= view.findViewById(R.id.etDescription);

        cbEnabled    = view.findViewById(R.id.cbEnabled);
        cbOptional   = view.findViewById(R.id.cbOptional);
        cbUsage      = view.findViewById(R.id.cbUsage);
        cbNotify     = view.findViewById(R.id.cbNotify);

        etClass      = view.findViewById(R.id.etClass);
        etMethod     = view.findViewById(R.id.etMethod);
        etParams     = view.findViewById(R.id.etParams);
        etReturn     = view.findViewById(R.id.etReturn);

        etMinSdk     = view.findViewById(R.id.etMinSdk);
        etMaxSdk     = view.findViewById(R.id.etMaxSdk);
        etMinApk     = view.findViewById(R.id.etMinApk);
        etMaxApk     = view.findViewById(R.id.etMaxApk);

        etExclude    = view.findViewById(R.id.etExclude);
        etTarget     = view.findViewById(R.id.etTarget);
        etSettings   = view.findViewById(R.id.etSettings);
    }

    private void populateFields() {
        // start collapsed
        expandedScriptView = false;
        scroll.setVisibility(View.VISIBLE);
        etScript.setVisibility(View.GONE);

        etCollection.setText(Str.getNonNullOrEmptyString(hook.collection, Str.EMPTY));
        etGroup.setText(Str.getNonNullOrEmptyString(hook.group, Str.EMPTY));
        etName.setText(Str.getNonNullOrEmptyString(hook.name, Str.EMPTY));
        etAuthor.setText(Str.getNonNullOrEmptyString(hook.author, Str.EMPTY));
        etVersion.setText(hook.version != null ? hook.version.toString() : "0");
        etDescription.setText(Str.getNonNullOrEmptyString(hook.description, Str.EMPTY));

        cbEnabled.setChecked(Boolean.TRUE.equals(hook.enabled));
        cbOptional.setChecked(Boolean.TRUE.equals(hook.optional));
        cbUsage.setChecked(Boolean.TRUE.equals(hook.usage));
        cbNotify.setChecked(Boolean.TRUE.equals(hook.notify));

        etClass.setText(Str.getNonNullOrEmptyString(hook.className, Str.EMPTY));
        etMethod.setText(Str.getNonNullOrEmptyString(hook.methodName, Str.EMPTY));

        CoreUiUtils.setEditTextText(etParams, Str.joinList(hook.parameterTypes, Str.NEW_LINE));

        etReturn.setText(Str.getNonNullOrEmptyString(hook.returnType, Str.EMPTY));

        etMinSdk.setText(String.valueOf(hook.minSdk));
        etMaxSdk.setText(String.valueOf(hook.maxSdk));
        etMinApk.setText(String.valueOf(hook.minApk));
        etMaxApk.setText(String.valueOf(hook.maxApk));

        CoreUiUtils.setEditTextText(etExclude, Str.joinList(hook.excludePackages, Str.NEW_LINE));
        CoreUiUtils.setEditTextText(etTarget, Str.joinList(hook.targetPackages, Str.NEW_LINE));
        CoreUiUtils.setEditTextText(etSettings, Str.joinList(hook.settings, Str.NEW_LINE));

        etScript.setText(Str.getNonNullOrEmptyString(hook.luaScript, XHook.DEFAULT_SCRIPT));
    }

    private void setupExpanderLogic() {
        View.OnClickListener toggle = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandedScriptView = !expandedScriptView;
                scroll.setVisibility(expandedScriptView ? View.GONE : View.VISIBLE);
                etScript.setVisibility(expandedScriptView ? View.VISIBLE : View.GONE);
                ivExpander.setImageLevel(expandedScriptView ? 1 : 0);
            }
        };
        ivExpander.setOnClickListener(toggle);
        tvExpander.setOnClickListener(toggle);
    }

    private void saveHookData() {
        //ToDO: future make sure things are being set properly, for now this will do
        hook.collection = etCollection.getText().toString().trim();
        hook.group      = etGroup.getText().toString().trim();
        hook.name       = etName.getText().toString().trim();
        hook.author     = etAuthor.getText().toString().trim();

        try { hook.version = Integer.parseInt(etVersion.getText().toString().trim()); }
        catch (NumberFormatException e) { hook.version = 0; }

        hook.description = etDescription.getText().toString().trim();
        hook.enabled     = cbEnabled.isChecked();
        hook.optional    = cbOptional.isChecked();
        hook.usage       = cbUsage.isChecked();
        hook.notify      = cbNotify.isChecked();

        hook.className   = etClass.getText().toString().trim();
        hook.methodName  = etMethod.getText().toString().trim();

        ListUtil.addAll(hook.parameterTypes,
                Str.splitAdvance(CoreUiUtils.getInputTextText(etParams), true, true, Str.NEW_LINE, Str.COMMA),
                false, true);

        hook.returnType = etReturn.getText().toString().trim();

        try { hook.minSdk = Integer.parseInt(etMinSdk.getText().toString().trim()); }
        catch (NumberFormatException e) { hook.minSdk = 0; }
        try { hook.maxSdk = Integer.parseInt(etMaxSdk.getText().toString().trim()); }
        catch (NumberFormatException e) { hook.maxSdk = Integer.MAX_VALUE; }
        try { hook.minApk = Integer.parseInt(etMinApk.getText().toString().trim()); }
        catch (NumberFormatException e) { hook.minApk = 0; }
        try { hook.maxApk = Integer.parseInt(etMaxApk.getText().toString().trim()); }
        catch (NumberFormatException e) { hook.maxApk = Integer.MAX_VALUE; }

        ListUtil.addAll(hook.excludePackages,
                Str.splitAdvance(CoreUiUtils.getInputTextText(etExclude), true, true, Str.NEW_LINE, Str.COMMA),
                true, true);
        ListUtil.addAll(hook.targetPackages,
                Str.splitAdvance(CoreUiUtils.getInputTextText(etTarget), true, true, Str.NEW_LINE, Str.COMMA),
                true, true);
        ListUtil.addAll(hook.settings,
                Str.splitAdvance(CoreUiUtils.getInputTextText(etSettings), true, true, Str.NEW_LINE, Str.COMMA),
                false, true);

        String lua = etScript.getText().toString();
        hook.luaScript = lua.isEmpty() ? XHook.DEFAULT_SCRIPT : lua;
    }
}
