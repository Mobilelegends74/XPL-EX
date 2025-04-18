package eu.faircode.xlua.x.ui.adapters.hooks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.databinding.HooksExItemBinding;
import eu.faircode.xlua.databinding.SettingsExGroupBinding;
import eu.faircode.xlua.utilities.UiUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.ResultRequest;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHookIO;
import eu.faircode.xlua.x.ui.adapters.settings.ContainersListManager;
import eu.faircode.xlua.x.ui.core.UINotifier;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.adapter.EnhancedListAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IGenericElementEvent;
import eu.faircode.xlua.x.ui.core.interfaces.IListAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.core.view_registry.ChangedStatesPacket;
import eu.faircode.xlua.x.ui.core.view_registry.CheckBoxState;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.ui.core.view_registry.IStateChanged;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.dialogs.HookEditDialog;
import eu.faircode.xlua.x.ui.dialogs.MessageDialog;
import eu.faircode.xlua.x.ui.fragments.ConfUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.PutHookExCommand;
import eu.faircode.xlua.x.xlua.settings.GroupStats;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.databinding.SettingsExGroupBinding;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.UINotifier;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.view_registry.ChangedStatesPacket;
import eu.faircode.xlua.x.ui.core.view_registry.CheckBoxState;
import eu.faircode.xlua.x.ui.core.view_registry.IStateChanged;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.core.adapter.EnhancedListAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IGenericElementEvent;
import eu.faircode.xlua.x.ui.core.interfaces.IListAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.dialogs.MessageDialog;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.GroupStats;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.test.EventTrigger;
import eu.faircode.xlua.x.xlua.settings.test.SharedViewControl;
import eu.faircode.xlua.x.xlua.settings.test.interfaces.IUIViewControl;

public class HookAdapter
        extends EnhancedListAdapter<XHook, HooksExItemBinding, HookAdapter.GroupViewHolder>
        implements IListAdapter<XHook, HooksExItemBinding> {


    private static final String TAG = LibUtil.generateTag(HookAdapter.class);
    private static final int PREFETCH_COUNT = 10;
    private final RecyclerView.RecycledViewPool sharedPool;
    private final UserClientAppContext userContext;

    public HookAdapter(Context context,
                                        IGenericElementEvent<XHook, HooksExItemBinding> events,
                                        IStateManager stateManager,
                                        UserClientAppContext userContext) {
        super(context, events, stateManager, new RecyclerView.RecycledViewPool());
        setHasStableIds(true);
        this.sharedPool = new RecyclerView.RecycledViewPool();
        this.sharedPool.setMaxRecycledViews(0, 15); // Adjust pool size as needed
        this.userContext = userContext;
    }


    @Override
    public long getItemId(int position) { return getItem(position).getObjectId().hashCode(); }


    public void onHookDeleted(XHook hook, ResultRequest request) { if(request != null && request.successful()) onHookDeleted(hook, request.hook); }
    public void onHookDeleted(XHook hook, XHook newHook) {
        if(hook != null) {
            TryRun.onMain(() -> {
                List<XHook> currentList = getCurrentList();
                List<XHook> updatedList = new ArrayList<>(currentList);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("On Delete Hook UI Event Invoked! Hook (%s) New Result (%s) First=[%s] Second=[%s]",
                            Str.toObjectId(hook),
                            Str.toObjectId(newHook),
                            Str.ensureNoDoubleNewLines(XHookIO.toJsonString(hook)),
                            Str.ensureNoDoubleNewLines(XHookIO.toJsonString(newHook))));

                //Maybe check if built in ???
                if(newHook != null && hook.getObjectId().equalsIgnoreCase(newHook.getObjectId())) {
                    for (int i = 0; i < updatedList.size(); i++) {
                        XHook updatedListItem = updatedList.get(i);
                        if(updatedListItem != null && Str.areEqual(newHook.getObjectId(), updatedListItem.getObjectId(), false)) {
                            updatedList.set(i, newHook);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("On Delete Updated Entry from (%s) to (%s) was Originally Give=[%s]",
                                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(updatedListItem)),
                                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(newHook)),
                                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(hook))));
                            break;
                        }
                    }
                } else {
                    for (int i = updatedList.size() - 1; i >= 0; i--) {
                        XHook item = updatedList.get(i);
                        if(item != null && Str.areEqual(hook.getObjectId(), item.getObjectId(), false)) {
                            updatedList.remove(i);
                            break;
                        }
                    }
                }

                submitList(updatedList);
            });
        }
    }

    public void onHookEdited(XHook oldHook, XHook newHook, boolean partnerNextToEdited, ResultRequest res) { if(res != null && res.successful()) onHookEdited(oldHook, newHook, partnerNextToEdited); }
    public void onHookEdited(XHook oldHook, XHook newHook, boolean partnerNextToEdited) {
        TryRun.onMain(() -> {
            List<XHook> currentList = getCurrentList();
            List<XHook> updatedList = new ArrayList<>(currentList);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("On Update Event UI Invoked for ID [%s] and New ID [%s] Partner=%s JSON(1)=[%s] JSON(2)=[%s]",
                        Str.toObjectId(oldHook),
                        Str.toObjectId(newHook),
                        partnerNextToEdited,
                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(oldHook)),
                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(newHook))));

            // Guard nulls: if hooks or IDs missing, append newHook
            if (oldHook == null || newHook == null
                    || oldHook.getObjectId() == null || newHook.getObjectId() == null) {
                updatedList.add(newHook);
                submitList(updatedList);
                return;
            }

            String oldId = Str.toObjectId(oldHook);
            String newId = Str.toObjectId(newHook);

            // If name unchanged: replace in place
            if (Str.areEqual(oldHook.name, newHook.name)) {
                for (int i = 0; i < updatedList.size(); i++) {
                    XHook hook = updatedList.get(i);
                    if (hook != null && Str.areEqual(hook.getObjectId(), oldId, false)) {
                        updatedList.set(i, newHook);
                        break;
                    }
                }

            } else {
                // Name changed: decide placement strategy
                if (partnerNextToEdited) {
                    // Place adjacent to the oldHook based on alphabetical comparison
                    boolean added = false;
                    for (int i = 0; i < updatedList.size(); i++) {
                        XHook hook = updatedList.get(i);
                        if (hook != null && Str.areEqual(hook.getObjectId(), oldId, true)) {
                            int cmp = newId.compareToIgnoreCase(oldId);
                            if (cmp < 0) {
                                // newHook comes before oldHook
                                updatedList.add(i, newHook);
                            } else {
                                // newHook comes after oldHook
                                updatedList.add(i + 1, newHook);
                            }
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        // fallback: append at end
                        updatedList.add(newHook);
                    }

                } else {
                    // Full-list alphabetical insertion
                    boolean added = false;
                    for (int i = 0; i < updatedList.size(); i++) {
                        XHook hook = updatedList.get(i);
                        if (hook != null && hook.getObjectId() != null) {
                            // compare newId to each existing ID
                            if (newId.compareToIgnoreCase(hook.getObjectId()) <= 0) {
                                updatedList.add(i, newHook);
                                added = true;
                                break;
                            }
                        }
                    }
                    if (!added) {
                        // no larger element found: append at end
                        updatedList.add(newHook);
                    }
                }
            }

            submitList(updatedList);
        });
    }


    @NonNull
    @Override
    public HookAdapter.GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HooksExItemBinding binding = HooksExItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HookAdapter.GroupViewHolder(
                binding,
                events,
                stateManager,
                sharedPool,
                userContext,
                this);
    }

    public static class GroupViewHolder extends BaseViewHolder<XHook, HooksExItemBinding>
            implements
            View.OnClickListener,
            View.OnLongClickListener,
            CompoundButton.OnCheckedChangeListener {

        private static final String TAG = LibUtil.generateTag(GroupViewHolder.class);

        private boolean isInitialized = false;
        private final UserClientAppContext userContext;
        private final HookAdapter adapter;

        private final GroupStats groupStats = new GroupStats();
        private final String[] actionButtons = {"Edit", "Delete", "Export"};

        public GroupViewHolder(HooksExItemBinding binding,
                               IGenericElementEvent<XHook, HooksExItemBinding> events,
                               IStateManager stateManager,
                               RecyclerView.RecycledViewPool sharedPool,
                               UserClientAppContext userContext,
                               HookAdapter adapter) {
            super(binding, events, stateManager);
            this.userContext = userContext;
            this.adapter = adapter;
            initializeViews();
        }

        private boolean isGlobal() { return userContext == null || userContext.isGlobal(); }

        private void initializeViews() {
            if (isInitialized) return;
            isInitialized = true;
        }

        @Override
        public void bind(XHook item) {
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Binding from [%s]   to   [%s]",
                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(currentItem)),
                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(item))));

            currentItem = item;
            binding.tvHookId.setText(item.getObjectId());
            binding.tvHookId.setSelected(true);
            binding.tvHookSubInfo.setText(Str.combineEx(item.group, " / ", item.collection));

            // Populate expanded details
            populateExpandedDetails(item);

            // Initialize action buttons
            setupActionButtons();

            SharedRegistry.ItemState state = sharedRegistry.getItemState(SharedRegistry.STATE_TAG_HOOKS, item.getObjectId());


            updateExpandedStateForGroup(state.isExpanded);

            initTextColor(getContext(), item);
            initCheckbox(isGlobal(), false);
            wireGroupEvents(true);
        }

        private void initTextColor(Context context, XHook hook) {
            if(hook != null) {
                boolean isAvail = hook.isAvailable(null, null, false, true);
                boolean isBuilt = Boolean.TRUE.equals(hook.builtin);
                int attr = isBuilt ? R.attr.colorTextOne : R.attr.colorAccent;
                int c = XUtil.resolveColor(context, attr);
                CoreUiUtils.setTextColor(binding.tvHookId, c);
                binding.tvHookId.setEnabled(isAvail);
                binding.tvHookId.setAlpha(isAvail ? 1.0f : 0.5f);

                int attr2 = (Build.VERSION.SDK_INT < hook.minSdk || Build.VERSION.SDK_INT > hook.maxSdk) ?
                        R.attr.colorUnsavedSetting : R.attr.colorAccent;
                int c2 = XUtil.resolveColor(context, attr2);
                CoreUiUtils.setTextColor(binding.tvHookSdk, c2);
            }
        }


        private void populateExpandedDetails(XHook hook) {
            if (hook == null)
                return;

            // Set name
            binding.tvHookName.setText(Str.getNonNullOrEmptyString(hook.name, "-"));

            // Set author
            binding.tvHookAuthor.setText(Str.getNonNullOrEmptyString(hook.author, "-"));

            // Set class and method separately
            binding.tvHookClass.setText(Str.getNonNullOrEmptyString(hook.getClassName(), "-"));
            binding.tvHookMethod.setText(Str.getNonNullOrEmptyString(hook.methodName, "-"));

            // Set SDK range
            String sdkRange = String.format("%d - %d",
                    hook.minSdk > 0 ? hook.minSdk : 1,
                    hook.maxSdk < 999 ? hook.maxSdk : 999);
            binding.tvHookSdk.setText(sdkRange);

            binding.tvHookVersion.setText(hook.version != null ? hook.version.toString() : "0");

            binding.tvHookDescription.setText(Str.getNonNullOrEmptyString(hook.description, "-"));

            //wireGroupEvents(false);
            CoreUiUtils.setChecked(binding.cbHookOptional, Boolean.TRUE.equals(hook.optional), this);
            CoreUiUtils.setChecked(binding.cbHookUsage, Boolean.TRUE.equals(hook.usage), this);
            CoreUiUtils.setChecked(binding.cbHookNotify, Boolean.TRUE.equals(hook.notify), this);
            //binding.cbHookOptional.setChecked(Boolean.TRUE.equals(hook.optional));
            //binding.cbHookUsage.setChecked(Boolean.TRUE.equals(hook.usage));
            //binding.cbHookNotify.setChecked(Boolean.TRUE.equals(hook.notify));
            //wireGroupEvents(true);
        }

        private void setupActionButtons() {
            binding.buttonContainer.removeAllViews();
            binding.flexButtons.removeAllViews();
            int totalWidth = 0;
            int parentWidth = binding.horizontalButtonScroll.getWidth() - CoreUiUtils.dpToPx(getContext(), 40);
            int i = 0;
            for (final String actionName : actionButtons) {
                if(i == 1 && Boolean.TRUE.equals(currentItem.builtin)) {
                    i++;
                    continue;
                }

                final int ix = i;
                Button scrollButton = createStyledButton(actionName);
                Button flexButton = createStyledButton(actionName);

                scrollButton.setOnClickListener(v -> handleActionButtonClick(v.getContext(), ix));
                flexButton.setOnClickListener(v -> handleActionButtonClick(v.getContext(), ix));

                // Measure button width
                scrollButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                totalWidth += scrollButton.getMeasuredWidth();

                binding.buttonContainer.addView(scrollButton);
                binding.flexButtons.addView(flexButton);
                i++;
            }

            boolean needsExpander = totalWidth > parentWidth;
            binding.ivButtonsExpander.setVisibility(needsExpander ? View.VISIBLE : View.GONE);

            // Handle expander click
            binding.ivButtonsExpander.setOnClickListener(v -> {
                boolean isExpanded = binding.flexButtons.getVisibility() == View.VISIBLE;
                binding.flexButtons.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                binding.horizontalButtonScroll.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                binding.ivButtonsExpander.setRotation(isExpanded ? 0 : 180); // Rotate arrow
            });
        }

        private Button createStyledButton(String text) {
            Button button = new Button(getContext());
            button.setText(text);
            button.setBackgroundResource(R.drawable.rounded_corner);
            button.setPadding(10, 0, 10, 0);
            button.setTextSize(10);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(12, 12, 12, 12);
            params.height = CoreUiUtils.dpToPx(getContext(), 30);
            button.setLayoutParams(params);
            return button;
        }

        private void handleActionButtonClick(Context context, int index) {
            if (currentItem == null)
                return;

            if(manager != null && manager.getFragmentMan() != null && currentItem != null) {
                final XHook copy = XHook.copy(currentItem);
                switch (index) {
                    case 0:
                        HookEditDialog.create()
                                .setHook(copy)
                                .setEditListener((hook) -> {
                                    ResultRequest res = PutHookExCommand.putEx(context, hook, false);
                                    if(res.successful()) {
                                        adapter.onHookEdited(currentItem, res.hook, false);
                                    }
                                }).show(manager.getFragmentMan(), context.getString(R.string.title_hook_edit));
                        break;
                    case 1:
                        TryRun.background(() -> {
                            //Do a "are you sure" prompt...
                            ResultRequest res =  PutHookExCommand.putEx(context, copy, true);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Deletion Result! Result Flag [%s] Result [%s] Copy [%s] Current [%s]",
                                        res.successful(),
                                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(res.hook)),
                                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(copy)),
                                        Str.ensureNoDoubleNewLines(XHookIO.toJsonString(currentItem))));
                            Snackbar.make(itemView, Str.fm(context.getString(
                                    res.flag ? R.string.hook_delete_success : R.string.hook_delete_error),
                                    res.flag ? res.exception : copy.getObjectId()), Snackbar.LENGTH_LONG).show();
                            TryRun.onMain(() -> {
                                adapter.onHookDeleted(currentItem, res);
                            });
                        });
                        break;
                    case 2:
                        TryRun.onMain(() -> {
                            //This came in clutch ?
                            sharedRegistry.push(currentItem);
                            ConfUtils.startConfigSavePicker(manager.getAsFragment(),
                                    Str.replaceAll(Str.replaceAll(currentItem.getObjectId(),
                                            Str.FORWARD_SLASH, "_"), Str.WHITE_SPACE, "_"));
                        });
                        break;
                }
            }
        }

        private void initCheckbox(boolean isGlobal, boolean isAssigned) {
            boolean showCheckbox = !isGlobal;
            if(showCheckbox) {
                binding.cbHookItemEnabled.setVisibility(View.VISIBLE);
                CoreUiUtils.setChecked(binding.cbHookItemEnabled, isAssigned, this);
                binding.cbHookItemEnabled.setClickable(true);

                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) binding.tvHookId.getLayoutParams();

                if (binding.ivActionNeeded.getVisibility() == View.VISIBLE) params.endToStart = binding.ivActionNeeded.getId();
                else params.endToStart = binding.cbHookItemEnabled.getId();

                params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
                binding.tvHookId.setLayoutParams(params);
            } else {
                binding.cbHookItemEnabled.setVisibility(View.INVISIBLE);

                CoreUiUtils.setChecked(binding.cbHookItemEnabled, false, this);
                binding.cbHookItemEnabled.setClickable(false);
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) binding.tvHookId.getLayoutParams();
                params.endToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
                params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                binding.tvHookId.setLayoutParams(params);
            }

            TryRun.onMain(() -> binding.clHookItemLayout.requestLayout());
        }

        @Override
        public void bind(XHook item, List<Object> payloads) {
            if (payloads.isEmpty()) {
                bind(item);
                return;
            }

            currentItem = item;
            // Handle payloads if needed
        }

        public void handleExpandClickForGroup(XHook item) {
            boolean expanded = sharedRegistry.toggleExpanded(SharedRegistry.STATE_TAG_HOOKS, item.getObjectId());
            updateExpandedStateForGroup(expanded);
        }

        private void updateExpandedStateForGroup(boolean isExpanded) {
            CoreUiUtils.setViewsVisibility(binding.ivExpanderHookItem, isExpanded, binding.llHookExpandedContent);
        }

        @Override
        protected void onViewDetached() {
            wireGroupEvents(false);
            if(currentItem != null)
                currentItem = null;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            if(currentItem == null)
                return;

            final int id = view.getId();
            switch (id) {
                case R.id.ivExpanderHookItem:
                case R.id.tvHookId:
                    handleExpandClickForGroup(currentItem);
                    break;
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View view) {
            Resources res = view.getResources();
            // Handle long clicks if needed
            return false;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(currentItem == null)
                return;
            final int id = compoundButton.getId();
            pauseContent(false);
            final Context context = compoundButton.getContext();
            final XHook copy = XHook.copy(currentItem);
            switch (id) {
                case R.id.cbHookOptional:
                    copy.optional = isChecked;
                    break;
                case R.id.cbHookUsage:
                    copy.usage = isChecked;
                    break;
                case R.id.cbHookNotify:
                    copy.notify = isChecked;
                    break;
                default:
                    pauseContent(true);
                    return;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG ,"Is being Invoked for Check=" + Str.ensureNoDoubleNewLines(RuntimeUtils.getStackTraceSafeString(new Exception())));

            final ResultRequest res = PutHookExCommand.putEx(context, copy, false);
            if(res.successful()) {
                if(this.currentItem != null) {
                    XHookIO.copy(res.hook, this.currentItem);
                } else {
                    this.currentItem = new XHook();
                    XHookIO.copy(res.hook, this.currentItem);
                }

                //case R.id.flSettingsButtonOne:
                //    recyclerViewWrapper.getFloatingActionButtonContext().invokeFloatingActions();
                //    break;

                populateExpandedDetails(this.currentItem);
                initTextColor(getContext(), this.currentItem);
                initCheckbox(isGlobal(), false);
                wireGroupEvents(true);
            }

            // Populate expanded details
            /*populateExpandedDetails(item);

            // Initialize action buttons
            setupActionButtons();

            SharedRegistry.ItemState state = sharedRegistry.getItemState(SharedRegistry.STATE_TAG_HOOKS, item.getObjectId());


            updateExpandedStateForGroup(state.isExpanded);

            initTextColor(getContext(), item);
            initCheckbox(isGlobal(), false);
            wireGroupEvents(true);*/




            adapter.onHookEdited(currentItem, res.hook, false, res);
            pauseContent(true);
            Snackbar.make(compoundButton, res.successful() ?
                            Str.fm(context.getString(R.string.hook_update_success), copy.getObjectId()) :
                            Str.fm(context.getString(R.string.hook_update_error), Str.toStringOrNull(res.exception)),
                    Snackbar.LENGTH_LONG).show();
        }

        private void pauseContent(boolean isAvail) {
            try {
                //wireGroupEvents(isAvail);
                binding.llHookExpandedContent.setClickable(isAvail);
                binding.llHookExpandedContent.setEnabled(isAvail);
                binding.llHookExpandedContent.setAlpha(isAvail ? 1.0f : 0.5f);
            }catch (Exception e) {

            }
        }

        private void wireGroupEvents(boolean wire) {
            if (binding != null) {
                // Main elements
                binding.ivExpanderHookItem.setOnClickListener(wire ? this : null);
                binding.tvHookId.setOnClickListener(wire ? this : null);
                binding.cbHookItemEnabled.setOnCheckedChangeListener(wire ? this : null);
                binding.ivActionNeeded.setOnClickListener(wire ? this : null);
                binding.ivActionNeeded.setOnLongClickListener(wire ? this : null);
                binding.ivButtonsExpander.setOnClickListener(wire ? this : null);
                // Expanded view checkboxes
                binding.cbHookOptional.setOnCheckedChangeListener(wire ? this : null);
                binding.cbHookUsage.setOnCheckedChangeListener(wire ? this : null);
                binding.cbHookNotify.setOnCheckedChangeListener(wire ? this : null);
            }
        }

        public Context getContext() {
            if(binding != null) {
                try {
                    LinearLayout v = binding.getRoot();
                    Context ctx = v.getContext();
                    if(ctx != null)
                        return ctx;
                }catch (Exception ignored) {  }
            }
            return itemView.getContext();
        }
    }
}