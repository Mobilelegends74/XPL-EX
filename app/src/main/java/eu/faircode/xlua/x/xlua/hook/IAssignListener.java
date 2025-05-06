package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;

import eu.faircode.xlua.AdapterApp;

public interface IAssignListener {
    void setAssigned(Context context, String group, boolean assign);
   /* void updateAssigned(
            Context context,
            String groupName,
            boolean assign,
            boolean notifyChangeMain,
            AdapterApp.IOnAssignmentFinished onFinish);*/
}
