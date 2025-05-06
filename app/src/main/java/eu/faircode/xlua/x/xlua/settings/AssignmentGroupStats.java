package eu.faircode.xlua.x.xlua.settings;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.ui.GroupHelper;
import eu.faircode.xlua.utilities.SettingUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;

public class AssignmentGroupStats {
    private static final String TAG = LibUtil.generateTag(AssignmentGroupStats.class);

    private int id;
    private String name;
    private String nameNice;
    private String groupId;

    private String lastException = null;

    public AssignmentGroupStats(String name, Context context) {
        this.name = name;
        this.nameNice = SettingUtil.cleanSettingName(this.name);


        if(context != null) {
            Resources resources = context.getResources();
            String nameLow = name.toLowerCase().replaceAll("[^a-z]", "_");
            this.id = resources.getIdentifier("group_" + nameLow, "string", context.getPackageName());
            //group.name = hook.group;
            //group.title = (group.id > 0 ? resources.getString(group.id) : hook.group);
            //group.groupId = GroupHelper.getGroupId(group.name);
            this.groupId = GroupHelper.getGroupId(this.name);
            //group.hasWarning = HookWarnings.hasWarning(context, group.name);
        } else {
            this.id = this.name.hashCode();
            this.groupId = GroupHelper.getGroupId(this.name);
        }
    }

    private final List<XHook> hooks = new ArrayList<>();
    private final Map<String, String> exceptions = new HashMap<>();


    public boolean allAssigned() {
        Log.d(TAG, "Assigned Count=" + assigned + " Hook Count=" + hooks.size() + " Installed=" + installed + " Group=" + name);
        return assigned == hooks.size();
    }
    public boolean hasAssigned() { return assigned > 0; }
    public boolean hasException() { return !Str.isEmpty(lastException); }
    public boolean hasInstalled() { return installed > 0; }
    public boolean allInstalled() { return installed == hooks.size(); }
    public long lastUsed() { return used; }

    public void setHooks(List<XHook> hooks, boolean clearOriginal) { ListUtil.addAll(this.hooks, hooks, clearOriginal); }

    private int installed = 0;
    private int optional = 0;
    private long used = -1;
    private int assigned = 0;

    public String getName() { return name;  }
    public String getNameNice() { return nameNice; }
    public String getGroupId() { return groupId; }

    public int getId() { return id; }

    public int getInstalled() { return installed; }
    public int getOptional() { return optional; }
    public long getUsed() { return used; }
    public int getAssigned() { return assigned; }

    public List<String> getHookIds() {
        List<String> ids = new ArrayList<>();
        for(XHook hook : hooks) {
            ids.add(hook.getObjectId());
        }

        return ids;
    }

    public void pushUpdate(AssignmentPacket assignment) {
        if(assignment != null) {
            //exceptions.put(assignment.getHookId(), assignment.exception);
            if(assignment.exception != null) {
                lastException = assignment.exception;
            }

            if (assignment.installed >= 0)
                installed++;
            //if(Boolean.TRUE.equals(assignment.hookObj.optional))
            //    optional++;
            if (assignment.restricted)
                used = Math.max(used, assignment.used);

            assigned++;
        }
    }

    public void init(Context context, String packageName, int uid) {

    }

    public void reset(boolean clearHooks) {
        installed = 0;
        optional = 0;
        used = -1;
        assigned = 0;
        exceptions.clear();
        lastException = null;
        if(clearHooks) hooks.clear();
    }

    @NonNull
    @Override
    public String toString() {
        return getNameNice();
    }
}
