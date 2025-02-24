package eu.faircode.xlua.x.ui.fragments;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.UINotifier;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.hook.HooksSettingsGlobal;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class SettingFragmentUtils {
    private static final String TAG = LibUtil.generateTag(SettingFragmentUtils.class);


    public static void initializeFragment(SettingSharedRegistry sharedRegistry, Context context, UserClientAppContext appCtx) {
        prepareAssignments(sharedRegistry, context, appCtx);
        prepareAppBind(sharedRegistry, context, appCtx);
        prepareChecked(sharedRegistry, context, appCtx);
    }

    public static void prepareAssignments(SettingSharedRegistry sharedRegistry, Context context, UserClientAppContext appCtx) {
        try {
            if(sharedRegistry == null)
                throw new Exception("Shared Registry is null...");

            if(context == null)
                throw new Exception("Context is null...");

            if(appCtx == null)
                throw new Exception("User App Context is null...");

            sharedRegistry.refreshAssignments(context, appCtx);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Refreshed Assignments!");

        }catch (Exception e) {
            Log.e(TAG, "Error Refreshing Assignments! Error=" + e);
        }
    }

    public static void prepareAppBind(SettingSharedRegistry sharedRegistry, Context context, UserClientAppContext appCtx) {
        try {
            if(sharedRegistry == null)
                throw new Exception("Shared Registry is null...");

            if(context == null)
                throw new Exception("Context is null...");

            if(appCtx == null)
                throw new Exception("User App Context is null...");

            //Hmm I dont like this as in we can just have "isKill(Shared)" ?
            sharedRegistry.bindToUserContext(appCtx);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished Binding User App Context with Shared Registry, to ensure is knows the Kill Flag!");

            if(!appCtx.isGlobal()) {
                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_KILL, appCtx.appPackageName, appCtx.kill);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Set App Kill For Pkg (" + appCtx.appPackageName + ") Kill=" + appCtx.kill);
            }
        }catch (Exception e) {
            Log.e(TAG, "Error Preparing the App CTX Binding, Error=" + e);
        }
    }

    public static void populateSharedRegistryChecked(SettingSharedRegistry sharedRegistry, List<String> checked) {
        if(sharedRegistry != null && ListUtil.isValid(checked)) {
            try {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Populating Shared Registry with (" + ListUtil.size(checked) + ") Checked!");

                for(String setting : checked)
                    sharedRegistry.setChecked(
                            SharedRegistry.STATE_TAG_SETTINGS,
                            UINotifier.settingName(setting),
                            true);

            }catch (Exception e) {
                Log.e(TAG, "Error Populating Shared Registry with Checked! Count=" + ListUtil.size(checked) + " Error=" + e);
            }
        }
    }

    public static void prepareChecked(SettingSharedRegistry sharedRegistry, Context context, UserClientAppContext appCtx) {
        try {
            if(sharedRegistry == null)
                throw new Exception("Shared Registry is null...");

            if(context == null)
                throw new Exception("Context is null...");

            if(appCtx == null)
                throw new Exception("User App Context is null...");

            PrefManager prefManager = sharedRegistry.ensurePrefsOpen(context, PrefManager.SETTINGS_NAMESPACE);
            if(prefManager == null)
                throw new Exception("Preference Manager is NULL!");

            if(DebugUtil.isDebug())
                Log.d(TAG, "Shared Preferences Opened!");

            List<String> globalChecked = prefManager.getStringList(PrefManager.nameForChecked(true), ListUtil.emptyList(), false);
            populateSharedRegistryChecked(sharedRegistry, globalChecked);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Global Checked Size=" + ListUtil.size(globalChecked));

            if(appCtx.isGlobal())
                return;  //Ignore global

            List<String> appChecked = prefManager.getStringList(PrefManager.nameForChecked(false, appCtx.appPackageName), ListUtil.emptyList(), false);
            populateSharedRegistryChecked(sharedRegistry, appChecked);
            if(DebugUtil.isDebug())
                Log.d(TAG, "App Checked Size=" + ListUtil.size(appChecked) + " PackageName=" + appCtx.appPackageName);


        }catch (Exception e) {
            Log.e(TAG, "Error Preparing the checks, Error=" + e);
        }
    }



    public static List<String> getHookIds(
            Context context,
            int uid,
            String packageName,
            LiveData<List<SettingsGroup>> liveData,
            SharedRegistry sharedRegistry) {
        List<String> hookIds = new ArrayList<>();
        try {
            ///            SharedRegistry.ItemState state = sharedRegistry.getItemState(SharedRegistry.STATE_TAG_CONTAINERS, item.getContainerName());
            List<SettingsGroup> data = liveData.getValue();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Size of Data Groups for Hook Ids=" + ListUtil.size(data));

            if(ListUtil.isValid(data)) {
                for(SettingsGroup g : data) {
                    for(SettingsContainer c : g.getContainers()) {
                        if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_CONTAINERS, c.getContainerName())) {
                            /*AssignmentData assignmentData = CoreUiUtils.ensureAssignmentDataInit(
                                    context,
                                    uid,
                                    packageName,
                                    sharedRegistry,
                                    c,
                                    false);

                            for(AssignmentState state : assignmentData.states) {
                                String id = state.hook.getSharedId();
                                if(!hookIds.contains(id)) {
                                    hookIds.add(id);
                                }
                            }*/

                            ListUtil.addAllIfValid(hookIds, HooksSettingsGlobal.getHookIdsForSettings(context, c.getAllNames()));
                        } else {
                            for(SettingHolder holder : c.getSettings()) {
                                if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, holder.getObjectId())) {
                                    /*AssignmentData assignmentData = CoreUiUtils.ensureAssignmentDataInit(
                                            context,
                                            uid,
                                            packageName,
                                            sharedRegistry,
                                            c,
                                            false);

                                    for(AssignmentState state : assignmentData.states) {
                                        String id = state.hook.getSharedId();
                                        if(!hookIds.contains(id))
                                            hookIds.add(id);
                                    }*/
                                    ListUtil.addAllIfValid(hookIds, HooksSettingsGlobal.getHookIdsForSettings(context, c.getAllNames()));
                                }
                            }
                        }
                    }
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Hook Id Count=" + ListUtil.size(hookIds));

            return hookIds;
        }catch (Exception e) {
            Log.e(TAG, "Error with Hook Ids, Error=" + e);
            return hookIds;
        }
    }

    public static List<String> settingsToNameList(List<SettingHolder> holders) {
        List<String> names = new ArrayList<>();
        if(!ListUtil.isValid(holders))
            return names;

        for(SettingHolder holder : holders) {
            String name = holder.getName();
            if(!Str.isEmpty(name) && !names.contains(name) && !name.contains(Str.COMMA))
                names.add(name);
        }

        return names;
    }

    public static List<SettingPacket> filterCheckedAsPackets(List<SettingHolder> holders, SharedRegistry sharedRegistry) {
        List<SettingPacket> list = new ArrayList<>();
        for(SettingHolder holder : holders) {
            if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, holder.getObjectId())) {
                SettingPacket packet = new SettingPacket(holder.getName(), holder.getValue());
                list.add(packet);
            }
        }

        return list;
    }

    public static List<SettingHolder> filterChecked(List<SettingHolder> holders, SharedRegistry sharedRegistry) {
        List<SettingHolder> list = new ArrayList<>();
        if(!ListUtil.isValid(holders))
            return list;

        for(SettingHolder holder : holders) {
            String id = holder.getObjectId();
            if(!Str.isEmpty(id) && sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, id))
                list.add(holder);
        }

        return list;
    }

    public static List<SettingHolder> getSettings(LiveData<List<SettingsGroup>> liveData) {
        List<SettingHolder> settings = new ArrayList<>();
        try {
            List<SettingsGroup> data = liveData.getValue();
            List<String> names = new ArrayList<>();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Live Data (2) Size=" + ListUtil.size(data));

            if(ListUtil.isValid(data)) {
                for(SettingsGroup g : data) {
                    List<SettingsContainer> containers = g.getContainers();
                    if(!ListUtil.isValid(containers)) {
                        Log.w(TAG, "Invalid amount of Containers, (2) WTF! Group Name=" + g.getGroupName());
                        continue;
                    }

                    for(SettingsContainer c : containers) {
                        List<SettingHolder> conSettings = c.getSettings();
                        if(!ListUtil.isValid(conSettings)) {
                            Log.w(TAG, "Invalid amount of Containers, (2) WTF! Group Name=" + g.getGroupName() + " Container = " + c.getSettings());
                            continue;
                        }

                        for(SettingHolder h : conSettings) {
                            String name = h.getName();
                            if(!Str.isEmpty(name) && !names.contains(name)) {
                                names.add(name);
                                settings.add(h);
                            }
                        }
                    }
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Get (" + ListUtil.size(settings) + ") Settings!");

            return settings;
        }catch (Exception e) {
            Log.e(TAG, "Error Getting Settings! Error=" + e);
            return settings;
        }
    }

    public static Map<SettingHolder, SettingPacket> getSettingPackets(LiveData<List<SettingsGroup>> liveData, SharedRegistry registry, UserClientAppContext context, ActionFlag flag) {
        Map<SettingHolder, SettingPacket> packets = new HashMap<>();
        try {
            List<SettingsGroup> data = liveData.getValue();
            List<String> added = new ArrayList<>();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Live Data Size=" + ListUtil.size(data));

            if(ListUtil.isValid(data)) {
                for(SettingsGroup g : data) {
                    if(!ListUtil.isValid(g.getContainers()))
                        Log.w(TAG, "Invalid amount of Containers, WTF! Group Name=" + g.getGroupName());

                    for(SettingsContainer c : g.getContainers()) {
                        if(!ListUtil.isValid(c.getSettings()))
                            Log.w(TAG, "Invalid amount of Containers, WTF! Group Name=" + g.getGroupName() + " Container = " + c.getSettings());

                        for(SettingHolder h : c.getSettings()) {
                            if(registry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, h.getObjectId())) {
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, "Setting Is Checked: " + h.getName() + " Value=" + h.getValue() + " New Value=" + h.getNewValue() + " Is Not Saved=" + h.isNotSaved() + " Id=" + h.getObjectId());

                                if(h.isNotSaved() && !added.contains(h.getName())) {
                                    added.add(h.getName());
                                    SettingPacket packet = new SettingPacket();
                                    packet.setUserIdentity(UserIdentity.fromUid(context.appUid, context.appPackageName));
                                    packet.setActionPacket(ActionPacket.create(flag, context.kill));
                                    packet.name = h.getName();
                                    packet.value = h.getNewValue();
                                    packets.put(h, packet);
                                }
                            }
                        }
                    }
                }
            }

            return packets;
        }catch (Exception e) {
            Log.e(TAG, "Error Getting Enabled Settings! Error=" + e);
            return packets;
        }
    }
}
