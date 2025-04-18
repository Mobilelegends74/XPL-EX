package eu.faircode.xlua.x.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.R;
import eu.faircode.xlua.databinding.HooksExFragmentBinding;
import eu.faircode.xlua.databinding.HooksExItemBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.ui.adapters.hooks.HookAdapter;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.ResultRequest;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.core.CoreUiColors;
import eu.faircode.xlua.x.ui.core.CoreUiLog;
import eu.faircode.xlua.x.ui.core.DataEventKind;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.fragment.ListFragment;
import eu.faircode.xlua.x.ui.core.util.ListFragmentUtils;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.dialogs.HookEditDialog;
import eu.faircode.xlua.x.ui.models.HooksExItemViewModel;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.PutHookExCommand;


import me.zhanghai.android.fastscroll.FastScrollerBuilder;


public class HooksExFragment
        extends
        ListFragment<XHook, HooksExFragmentBinding, HooksExItemBinding>
        implements
        CompoundButton.OnCheckedChangeListener {

    private static final String TAG = LibUtil.generateTag(HooksExFragment.class);
    public static HooksExFragment newInstance(UserClientAppContext context) { return ListFragmentUtils.newInstance(HooksExFragment.class, context); }

    private final SettingSharedRegistry sharedRegistry = new SettingSharedRegistry();

    @Override
    public SharedRegistry getSharedRegistry() { return sharedRegistry; }
    @Override
    public Fragment getAsFragment() {
        return this;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.createViewModel(HooksExItemViewModel.class, true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.ensureHasUserContext();
        super.setAdapter(new HookAdapter(requireContext(), null, this, getUserContext().bindShared(sharedRegistry)));

        super.initFloatingActions(
                binding.flSettingsButtonOne,
                binding.flSettingsButtonTwo,
                binding.flSettingsButtonThree);

        super.setProgressBar(binding.pbHookz);
        super.initSwipeRefreshLayout(binding.swipeRefreshHookz, CoreUiColors.getSwipeRefreshColor(requireContext()));
        super.initRecyclerView(binding.rvHookz);

        new FastScrollerBuilder(binding.rvHookz).useMd2Style().build();

        super.startObserver();
        wire();
    }

    @Override
    protected void dataEvent(DataEventKind kind, List<XHook> xHooks) {

    }

    @Override
    public void wire() {
        super.wire();
        binding.flSettingsButtonOne.setOnClickListener(this);
        binding.flSettingsButtonTwo.setOnClickListener(this);
        binding.flSettingsButtonThree.setOnClickListener(this);

        //binding.cbForceStop.setOnCheckedChangeListener(this);
        //binding.cbUseDefaultValues.setOnCheckedChangeListener(this);
        //binding.btAppIslandProfileDialog.setOnClickListener(this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || resultCode != Activity.RESULT_OK) return;
        Uri uri = data.getData();
        if (uri == null) return;
        switch (requestCode) {
            case ConfUtils.REQUEST_OPEN_CONFIG:
                TryRun.onMain(() -> {
                    List<XHook> hooks = ConfUtils.readHooks(requireContext(), uri);
                    if(!ListUtil.isValid(hooks)) {
                        return;
                    }

                    Log.d(TAG, "Reading Hooks=" + hooks.size());


                    HookAdapter adapter = (HookAdapter)getAdapter().getAsListAdapterOrNull();
                    List<XHook> fromList = adapter.getCurrentList();

                    Map<String, XHook> items = new HashMap<>();
                    for(XHook hook : fromList) {
                        if(hook.isValid()) {
                            items.put(hook.getObjectId(), hook);
                        }
                    }

                    Log.d(TAG, "Reading Hooks=" + hooks.size() + " Items Counts=" + items.size());
                    for(XHook imported : hooks) {
                        if(imported.isValid()) {
                            ResultRequest res = PutHookExCommand.putEx(getContext(), imported, false);
                            if(res.successful()) {
                                items.put(res.hook.getObjectId(), res.hook);
                            }
                        }
                    }

                    adapter.submitList(new ArrayList<>(items.values()));
                });
                break;
            case ConfUtils.REQUEST_SAVE_CONFIG:
                TryRun.onMain(() -> {
                    XHook hook =  sharedRegistry.pop(XHook.class);
                    ConfUtils.takePersistablePermissions(requireContext(), uri);
                    boolean success = ConfUtils.writeHookToUri(requireContext(), uri, hook);
                    Log.d(TAG, "Hook (" + hook.getObjectId() + " ) Success=" + success);
                });
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        int id = compoundButton.getId();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        final int id = CoreUiLog.getViewIdOnClick(v, TAG);
        final Context context = v.getContext();
        switch (id) {
            case R.id.flSettingsButtonOne:
                recyclerViewWrapper.getFloatingActionButtonContext().invokeFloatingActions();
                break;
            case R.id.flSettingsButtonTwo:
                ConfUtils.startConfigFilePicker(this);
                break;
            case R.id.flSettingsButtonThree:
                TryRun.onMain(() -> {
                    HookAdapter adapter = (HookAdapter)getAdapter().getAsListAdapterOrNull();
                    if(adapter != null) {
                        HookEditDialog.create()
                                .setEditListener((hook) -> {
                                    if(hook != null && hook.isValid() && !Str.isEmpty(hook.group)) {
                                        ResultRequest res = PutHookExCommand.putEx(context, hook, false);
                                        if(res.successful())
                                            adapter.onHookEdited(null, res.hook, false);
                                    }
                                }).show(getFragmentMan(), context.getString(R.string.title_hook_edit));
                    }
                });
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onLongClick(View v) {
        int id = CoreUiLog.getViewIdOnLongClick(v, TAG);
        return false;
    }

    public Context tryGetContext() {
        try {
            return requireContext();
        }catch (Exception ignored) {
            return getContext();
        }
    }
}
