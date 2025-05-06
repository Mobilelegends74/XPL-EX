package eu.faircode.xlua.x.xlua.repos;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.GroupStats;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;

public class SettingsGroupRepository implements IXLuaRepo<SettingsGroup> {
    private static final String TAG = LibUtil.generateTag(SettingsGroupRepository.class);

    public static final IXLuaRepo<SettingsGroup> INSTANCE = new SettingsGroupRepository();
    public static final String DEFAULT_SORT_FIELD = "name";

    @Override
    public List<SettingsGroup> get() {
        return Collections.emptyList();
    }

    @Override
    public List<SettingsGroup> get(
            Context context,
            UserClientAppContext userContext) {
        return SettingsGroup.categorizeIntoGroups(SettingsRepository.INSTANCE.get(context, userContext));
    }

    @Override
    public List<SettingsGroup> filterAndSort(List<SettingsGroup> items, FilterRequest request) {
        return ListUtil.forEachFilter(
                items,
                request,
                SettingsGroupRepository::isMatchingCriteria,
                getComparator(request.getOrderOrDefault(DEFAULT_SORT_FIELD), request.isReversed, request.show),
                true);
    }

    public static boolean isMatchingCriteria(SettingsGroup group, FilterRequest request) {
        //Make this more advance
        if(request == null || group == null)
            return true;

        //do first wave
        if(!Str.isEmpty(request.show)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Filtering Groups By Show=[" + request.show + "]");

            switch (request.show) {
                case "unique":
                case "android":
                    boolean isUnique = request.show.equals("unique");
                    //Show only ones with invoked assignments, aka used
                    List<SettingsContainer> allowedContainers = new ArrayList<>();
                    for(SettingsContainer container : group.getContainers()) {
                        List<SettingHolder> allowedHolders = new ArrayList<>();
                        for(SettingHolder holder : container.getSettings()) {
                            if(isUnique) {
                                if(GroupStats.isSettingUnique(holder.getName()))
                                    allowedHolders.add(holder);
                            } else {
                                if(GroupStats.isSettingAndroid(holder.getName()))
                                    allowedHolders.add(holder);
                            }
                        }

                        if(!allowedHolders.isEmpty()) {
                            container.setSettings(allowedHolders);
                            allowedContainers.add(container);
                        }
                    }

                    if(allowedContainers.isEmpty())
                        return false;
                    else {
                        group.setContainers(allowedContainers);
                    }

                    break;
            }
        }

        if(request.isEmptyOrClearQuery())
            return true;

        String qLow = request.query.toLowerCase();
        if(group.getGroupName().toLowerCase().contains(qLow))
            return true;

        for(SettingsContainer container : group.getContainers()) {
            if(container.getContainerName().toLowerCase().contains(qLow))
                return true;
            if(container.getDescription() != null && container.getDescription().toLowerCase().contains(qLow))
                return true;
            for(SettingHolder setting : container.getSettings())
                if(setting.getName().toLowerCase().contains(qLow))
                    return true;
        }

        return false;
    }



    public static Comparator<SettingsGroup> getComparator(String sortBy, boolean isReverse, String show) {
        Comparator<SettingsGroup> comparator;
        switch (sortBy) {
            default:
                comparator = new Comparator<SettingsGroup>() {
                    @Override
                    public int compare(SettingsGroup a1, SettingsGroup a2) { return String.CASE_INSENSITIVE_ORDER.compare(a1.getGroupName(), a2.getGroupName()); }
                };
                break;
        }

        if (isReverse) {
            final Comparator<SettingsGroup> finalComparator = comparator;
            comparator = new Comparator<SettingsGroup>() {
                @Override
                public int compare(SettingsGroup a1, SettingsGroup a2) { return finalComparator.compare(a2, a1); }
            };
        }

        return comparator;
    }
}
