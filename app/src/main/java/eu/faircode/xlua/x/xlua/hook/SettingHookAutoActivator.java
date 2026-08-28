package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.AssignHooksCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignmentsCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;

/**
 * Assigns the hooks that consume values saved from the Setting Values screen.
 * The dependency table is built by {@link HooksSettingsGlobal} from each hook's
 * settings metadata and setting reads found in its Lua script.
 */
public final class SettingHookAutoActivator {
    private static final String TAG = LibUtil.generateTag(SettingHookAutoActivator.class);

    private SettingHookAutoActivator() { }

    public static List<String> activate(
            Context context,
            UserClientAppContext app,
            List<String> changedSettingNames) {
        if(context == null || app == null || app.isGlobal() || app.appUid <= 0 ||
                Str.isEmpty(app.appPackageName) || !ListUtil.isValid(changedSettingNames))
            return Collections.emptyList();

        try {
            List<String> requiredHookIds = HooksSettingsGlobal.getHookIdsForSettingGroups(
                    context,
                    app.appUid,
                    app.appPackageName,
                    changedSettingNames);
            if(!ListUtil.isValid(requiredHookIds))
                return Collections.emptyList();

            List<String> assignedHookIds = new ArrayList<>();
            for(AssignmentPacket assignment : GetAssignmentsCommand.get(
                    context,
                    true,
                    app.appUid,
                    app.appPackageName)) {
                if(assignment != null && !Str.isEmpty(assignment.getHookId()))
                    assignedHookIds.add(assignment.getHookId());
            }

            List<String> missingHookIds = getMissingHookIds(requiredHookIds, assignedHookIds);
            List<String> staleManufacturerHookIds = changesDeviceIdentity(changedSettingNames)
                    ? HooksSettingsGlobal.getMismatchedManufacturerHookIds(
                    context, app.appUid, app.appPackageName, assignedHookIds)
                    : Collections.emptyList();

            if(ListUtil.isValid(staleManufacturerHookIds)) {
                A_CODE removeResult = AssignHooksCommand.call(
                        context,
                        AssignmentsPacket.create(
                                app.appUid,
                                app.appPackageName,
                                staleManufacturerHookIds,
                                true,
                                false));
                if(!A_CODE.isSuccessful(removeResult))
                    Log.e(TAG, Str.fm(
                            "Failed to remove mismatched manufacturer hooks for %s, hooks=[%s], result=%s",
                            app.appPackageName,
                            Str.joinList(staleManufacturerHookIds),
                            removeResult));
            }

            if(!ListUtil.isValid(missingHookIds))
                return Collections.emptyList();

            A_CODE result = AssignHooksCommand.call(
                    context,
                    AssignmentsPacket.create(
                            app.appUid,
                            app.appPackageName,
                            missingHookIds,
                            false,
                            false));

            if(!A_CODE.isSuccessful(result)) {
                Log.e(TAG, Str.fm(
                        "Failed to auto-activate hooks for %s, settings=[%s], hooks=[%s], result=%s",
                        app.appPackageName,
                        Str.joinList(changedSettingNames),
                        Str.joinList(missingHookIds),
                        result));
                return Collections.emptyList();
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm(
                        "Auto-activated hooks for %s, settings=[%s], hooks=[%s]",
                        app.appPackageName,
                        Str.joinList(changedSettingNames),
                        Str.joinList(missingHookIds)));

            return missingHookIds;
        } catch (Exception e) {
            // The setting itself has already been saved. Do not report it as a
            // failed save if a separate hook assignment could not be completed.
            Log.e(TAG, Str.fm(
                    "Failed to auto-activate hooks for %s, error=%s",
                    app.appPackageName,
                    e));
            return Collections.emptyList();
        }
    }

    static List<String> getMissingHookIds(
            List<String> requiredHookIds,
            List<String> assignedHookIds) {
        if(!ListUtil.isValid(requiredHookIds))
            return Collections.emptyList();

        Set<String> assigned = new LinkedHashSet<>();
        if(ListUtil.isValid(assignedHookIds)) {
            for(String hookId : assignedHookIds)
                if(!Str.isEmpty(hookId))
                    assigned.add(hookId);
        }

        Set<String> missing = new LinkedHashSet<>();
        for(String hookId : requiredHookIds)
            if(!Str.isEmpty(hookId) && !assigned.contains(hookId))
                missing.add(hookId);

        return new ArrayList<>(missing);
    }

    static boolean changesDeviceIdentity(List<String> settingNames) {
        if(!ListUtil.isValid(settingNames))
            return false;
        for(String settingName : settingNames)
            if(!Str.isEmpty(settingName)
                    && settingName.trim().toLowerCase(Locale.ROOT).startsWith("device."))
                return true;
        return false;
    }
}
