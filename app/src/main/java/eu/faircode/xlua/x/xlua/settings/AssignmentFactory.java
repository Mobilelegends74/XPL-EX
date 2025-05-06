package eu.faircode.xlua.x.xlua.settings;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.AssignHooksCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetHookCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignmentsCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.hook.AssignmentsPacket;

public class AssignmentFactory {
    private static final String TAG = LibUtil.generateTag(AssignmentFactory.class);

    private AppXpPacket initializedApp;

    //Have event links, so when init is full done it will notify something ?
    //Can have it on the bind shit, so we can inline check box shit whatever

    private int totalHookCount = 0;

    public int getUid() { return initializedApp != null ? initializedApp.uid : 0; }
    public String getPackage() { return initializedApp != null ? initializedApp.packageName : null; }
    public boolean getForceStop() { return initializedApp != null && initializedApp.forceStop; }

    private final Map<String, AssignmentGroupStats> groups = new HashMap<>();

    public final List<AssignmentGroupStats> groupList = new ArrayList<>();

    public int getGroupCount() { return groupList.size(); }
    public AssignmentGroupStats getGroupAt(int index) { return groupList.get(index); }

    private final Map<String, AssignmentPacket> assigned = new HashMap<>();
    private final List<AssignmentGroupStats> modified = new ArrayList<>();


    public boolean areAllAssigned() { return totalHookCount == assigned.size(); }
    public boolean areAnyAssigned() { return !assigned.isEmpty(); }

    public boolean isAssigned(String id) {
        return assigned.containsKey(id);
    }

    public void bind(Context context, AppXpPacket app, boolean attachAssignments, Map<String, List<XHook>> hooks) {
        if(app != null)
            initializedApp = app;

        if(DebugUtil.isDebug())
            Log.d(TAG, "Binding App=" + app.packageName + " Attach=" + attachAssignments + " Hook Groups =" + hooks.size() + " App Assignments=" + app.assignments.size());

        if(hooks != null) {
            groups.clear();
            groupList.clear();
            initializeFromHooks(hooks, context);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Binding App (2)=" + app.packageName + " Attach=" + attachAssignments + " Group Count=" + groups.size() + " Group List Count=" + groupList.size());

        if(attachAssignments && initializedApp != null) {
            modified.clear();
            assigned.clear();
            if(ListUtil.isValid(initializedApp.assignments)) {
                for(AssignmentPacket assignmentPacket : initializedApp.assignments) {
                    XHook hook = assignmentPacket.hookObj == null ?
                            GetHookCommand.getEx(context, assignmentPacket.getHookId()) :
                            assignmentPacket.hookObj;

                    if(hook != null) {
                        AssignmentGroupStats groupStats = groups.get(hook.group);
                        if(groupStats != null) {
                            //groupStats.reset(false);
                            groupStats.pushUpdate(assignmentPacket);
                            assigned.put(assignmentPacket.getHookId(), assignmentPacket);
                            modified.add(groupStats);
                        }
                    }
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished Binding App=" + app.packageName + " This=" + toString());
    }

    public boolean assign(Context context, String groupName, boolean assign) {
        if(context == null)
            return false;

        boolean status = false;
        if(!Str.isEmpty(groupName)) {
            AssignmentGroupStats group = groups.get(groupName);
            if(group != null) {
                List<String> hookIds = group.getHookIds();
                status = A_CODE.isSuccessful(AssignHooksCommand.call(context, AssignmentsPacket.create(getUid(), getPackage(), hookIds, !assign, getForceStop())));
                Log.d(TAG, "Assigned For Group=" + groupName + " Assign=" + assign + " Hook Id Count=" + hookIds.size() + " Return=" + status);
                if(status) {
                    refreshStats(context, groupName);
                }
            }
        }

        return status;
    }

    public void initializeFromHooks(Map<String, List<XHook>> groupsRaw, Context context) {
        //Then and if more hooks ?????????
        if(groups.isEmpty()) {
            for(String group : groupsRaw.keySet()) {
                List<XHook> hooks = groupsRaw.get(group);
                if(ListUtil.isValid(hooks)) {
                    List<XHook> filtered = new ArrayList<>();
                    for(XHook hook : hooks) {
                        if(hook.isAvailable(getPackage(), null, false, true))
                            filtered.add(hook);
                    }

                    if(!filtered.isEmpty()) {
                        totalHookCount += filtered.size();
                        //Attach the name!
                        AssignmentGroupStats groupStats = new AssignmentGroupStats(group, context);
                        groupStats.setHooks(filtered, true);
                        groups.put(group, groupStats);
                        groupList.add(groupStats);
                    }
                }
            }
        }
    }

    public void refreshStats(Context context, String targetGroup) {
        if(!groups.isEmpty()) {
            List<AssignmentPacket> assignments = GetAssignmentsCommand.get(context, true, getUid(), getPackage());
            if(targetGroup == null) {
                if(ListUtil.isValid(assignments)) {
                    for(AssignmentPacket assignmentPacket : assignments) {
                        XHook hook = assignmentPacket.hookObj == null ?
                                GetHookCommand.getEx(context, assignmentPacket.getHookId()) :
                                assignmentPacket.hookObj;
                        if(hook != null) {
                            AssignmentGroupStats groupStats = groups.get(hook.group);
                            if(groupStats != null) {
                                groupStats.pushUpdate(assignmentPacket);
                                assigned.put(assignmentPacket.getHookId(), assignmentPacket);
                                modified.add(groupStats);
                            }
                        }
                    }
                }
            } else {
                AssignmentGroupStats groupStats = groups.get(targetGroup);
                if(groupStats != null) {
                    Log.d(TAG, "Updating Group=" + targetGroup + " Stats=" + Str.toStringOrNull(groupStats) + " Assignment Count=" + assignments.size() + " All Assigned=" + groupStats.allAssigned() + " Group Hook Count=" + groupStats.getHookIds().size() + " Id=" + Str.joinList(groupStats.getHookIds())) ;

                    groupStats.reset(false);
                    if(ListUtil.isValid(assignments)) {
                        List<String> hookIds = groupStats.getHookIds();
                        for(AssignmentPacket assignment : assignments) {
                            if(hookIds.contains(assignment.getHookId())) {
                                try {
                                    Log.d(TAG, "Found Group=" + targetGroup + " From Hook=" + assignment.getHookId());
                                    groupStats.pushUpdate(assignment);
                                    Log.d(TAG, "Updated Done for Group=" + targetGroup + " From Hook=" + assignment.getHookId() + " Assigned Count=" + groupStats.getAssigned());
                                }catch (Exception e) {
                                    Log.d(TAG, "Error Pushing Update=" + e);
                                }
                            } else {
                                Log.d(TAG, "Bad=" + assignment.getHookId() + " Not From Group: " + targetGroup);
                            }

                            /*XHook hook = assignment.hookObj == null ?
                                    GetHookCommand.getEx(context, assignment.getHookId()) :
                                    assignment.hookObj;

                            if(hook != null) {
                                //Check if hook
                                if(targetGroup.equalsIgnoreCase(hook.group)) {
                                    groupStats.pushUpdate(assignment);
                                    Log.d(TAG, "Found Group=" + targetGroup + " From Hook=" + hook.group);
                                }
                            }*/
                        }
                    }
                }
            }
        }
    }

    public void resetModified() {
        if(!modified.isEmpty()) {
            for(AssignmentGroupStats group : modified) {
                group.reset(false);
            }
        }
    }


    //Invoke Change Flag (will be on its own thread in background)
    //Update the Cache here of any changes
    //Then notify caller,

    //We should not cache in labels, instead we should notify some event ?
    //Use the DIFF checking ???
    //We can also maybe just link event for each assignment, when they are set triggers event updates count,


    //Update what is needed
    //then we can trigger mass update event ?


    @NonNull
    @Override
    public String toString() {
        return "Hook Count=" + totalHookCount + " Group Count=" + groups.size() + " Group List Count=" + groupList.size() + " Assigned=" + assigned.size() + " Modified=" + modified.size() + " App=" + getPackage();
    }
}
