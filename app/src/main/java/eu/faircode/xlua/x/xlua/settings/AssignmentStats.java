package eu.faircode.xlua.x.xlua.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.box.XAssignment;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.hook.IAssignListener;

public class AssignmentStats implements IIdentifiableObject {
    private XHook hook;
    private AssignmentPacket assignment;


    //private final Map<String, IAssignListener> events = new HashMap<>();

    @Override
    public String getObjectId() {
        if(hook != null) return hook.getObjectId();
        if(assignment != null) return assignment.getObjectId();
        return Str.EMPTY;
    }
}
