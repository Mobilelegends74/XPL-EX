package eu.faircode.xlua.x.ui.core.model;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.loggers.LogHelper;
import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.ui.core.CoreUiGlobals;
import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.xlua.LibUtil;

/**
 * ToDo: Work more off of Filters / Criteria. I want to make a system that resolved the chips as these would mostly be bind to chips
 * @param <TElement>
 */
public abstract class ListPreferenceViewModel<TElement> extends ListBaseViewModel<TElement> {
    private static final String TAG = LibUtil.generateTag(ListPreferenceViewModel.class);

    private final PrefManager preferences;
    private FilterRequest filterRequest;

    public PrefManager getPreferencesManger() { return preferences; }

    //Do something with filters
    public ListPreferenceViewModel(Application application, String tag) {
        super(application, tag);
        preferences = PrefManager.create(application, tag);
        updateList(createRequest(false, true, false));
    }

    //When updating, also update prefs ?
    @Override
    public FilterRequest createRequest(boolean setLast, boolean initFromPreferences, boolean copyLastQuery) {
        FilterRequest req = new FilterRequest();
        if(initFromPreferences) {
            req.setShow(getShowValue());
            req.setIsReversed(isReversed());
            req.setOrder(getOrder());
            req.setFilterTags(getFilters());
        }

        if(copyLastQuery && filterRequest != null)
            req.setQuery(filterRequest.query);
        if(setLast)
            filterRequest = req;

        return req;
    }

    @Override
    protected String getShowValue() { return PrefManager.getString(preferences, PrefManager.PREFERENCE_SHOW, PrefManager.DEFAULT_SHOW); }

    @Override
    protected boolean isReversed() { return PrefManager.getBoolean(preferences, CoreUiGlobals.PREFERENCE_IS_REVERSED, CoreUiGlobals.PREFERENCE_IS_REVERSED_DEFAULT); }

    @Override
    protected String getOrder() { return PrefManager.getString(preferences, CoreUiGlobals.PREFERENCE_ORDER, CoreUiGlobals.PREFERENCE_ORDER_DEFAULT, true); }

    @Override
    protected List<String> getFilters() { return Collections.emptyList(); }
}
