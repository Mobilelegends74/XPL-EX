package eu.faircode.xlua.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

import eu.faircode.xlua.AppGeneric;
import eu.faircode.xlua.R;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.SettingsQue;
import eu.faircode.xlua.ui.interfaces.ISettingUpdateEx;
import eu.faircode.xlua.utilities.StringUtil;

public class SettingDeleteDialogEx extends AppCompatDialogFragment{
    private Context context;
    private int adapterPosition;
    private ISettingUpdateEx callback;
    private AppGeneric application;
    private LuaSettingExtended setting;
    private SettingsQue que;
    private CheckBox cbDelete, cbDeleteDefaultMap, cbForceKill;

    public SettingDeleteDialogEx setContext(Context context) { this.context = context; return this; }
    public SettingDeleteDialogEx setAdapterPosition(int position) { this.adapterPosition = position;  return this; }
    public SettingDeleteDialogEx setCallback(ISettingUpdateEx callback) { this.callback = callback; return  this; }
    public SettingDeleteDialogEx setApplication(AppGeneric application) { this.application = application; return this; }
    public SettingDeleteDialogEx setSetting(LuaSettingExtended setting) { this.setting = setting; return this; }
    public SettingDeleteDialogEx setSettingsQue(SettingsQue que) { this.que = que; return this; }

    public SettingDeleteDialogEx() { }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.settingdelete, null);
        cbDelete = view.findViewById(R.id.cbDeleteSettingDeleteSetting);
        cbDeleteDefaultMap = view.findViewById(R.id.cbDeleteSettingDeleteDefault);
        cbForceKill = view.findViewById(R.id.cbDeleteSettingForceKill);

        boolean isValidSetting = !setting.isValueNull();
        cbDelete.setChecked(isValidSetting);
        cbDelete.setEnabled(isValidSetting);
        cbForceKill.setEnabled(!application.isGlobal());
        builder.setView(view)
                .setTitle(R.string.title_delete_setting)
                .setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { XLog.i("Setting Delete was cancelled!"); }
                })
                .setPositiveButton(R.string.option_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(application != null && setting != null) {
                            boolean deleteSetting = cbDelete.isChecked();
                            boolean deleteDefault = cbDeleteDefaultMap.isChecked();
                            boolean forceKill = cbForceKill.isChecked();
                            if(!deleteSetting && !deleteDefault) {
                                Toast.makeText(context, R.string.result_delete_setting_null, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            que.deleteSetting(context, setting, adapterPosition, deleteSetting, deleteDefault, forceKill, callback);
                        }else {
                            Toast.makeText(context, R.string.msg_error_generic_null, Toast.LENGTH_LONG).show();
                        }
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
