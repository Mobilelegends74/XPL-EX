package eu.faircode.xlua.x.ui.adapters.hooks;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.repos.IXLuaRepo;

public class HooksRepository implements IXLuaRepo<XHook> {
    private static final String TAG = LibUtil.generateTag(HooksRepository.class);

    public static final String DEFAULT_COMPARE = "name";

    public static final IXLuaRepo<XHook> INSTANCE = new HooksRepository();

    @Override
    public List<XHook> get() {
        return Collections.emptyList();
    }

    @Override
    public List<XHook> get(Context context, UserClientAppContext userContext) { return GetHooksCommand.getHooks(context, true, true); }

    @Override
    public List<XHook> filterAndSort(List<XHook> items, FilterRequest request) {
        List<XHook> queryHooks = new ArrayList<>();
        if(!ObjectUtils.anyNull(items, request)) {
            if(!request.isEmptyOrClearQuery()) {
                for(XHook hook : items)
                    if(isMatchingCriteria(hook, request))
                        queryHooks.add(hook);
            } else {
                queryHooks.addAll(items);
            }

            Collections.sort(queryHooks, getComparator(request.getOrderOrDefault(DEFAULT_COMPARE), request.isReversed));
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Filtered and Sorted through Hooks, Original Size=" + ListUtil.size(items) + " Filtered Size=" + ListUtil.size(queryHooks) + " Request=" + Str.toStringOrNull(request));

        return queryHooks;
    }

    public static boolean isMatchingCriteria(XHook hook, FilterRequest request) {
        String qLow = Str.toLowerCase(request.query);
        if(Str.isEmpty(qLow))
            return true;
        if(hook != null) {
            if(Str.contains(Str.toLowerCase(hook.group), qLow, false))
                return true;
            if(Str.contains(Str.toLowerCase(hook.getObjectId()), qLow, false))
                return true;
            if(Str.contains(Str.toLowerCase(hook.className), qLow, false) || Str.contains(Str.toLowerCase(hook.methodName), qLow, false))
                return true;
            if(Str.contains(Str.toLowerCase(hook.author), qLow, false))
                return true;
            if(Str.contains(Str.toLowerCase(hook.description), qLow, false))
                return true;
            if(ListUtil.stringListContains(hook.settings, qLow, true, false))
                return true;
        }

        return false;
    }

    public static Comparator<XHook> getComparator(String sortBy, boolean isReverse) {
        //ToDo: Asap when can, Core UI just like your logic to insert what not for HookEdit whatever, implement that shit including comparing submit list things etc
        Comparator<XHook> comparator;
        switch (sortBy) {
            default:
                comparator = (a1, a2) -> String.CASE_INSENSITIVE_ORDER.compare(a1.getObjectId(), a2.getObjectId());
                break;
        }

        if (isReverse) {
            final Comparator<XHook> finalComparator = comparator;
            comparator = (a1, a2) -> finalComparator.compare(a2, a1);
        }

        return comparator;
    }
}
