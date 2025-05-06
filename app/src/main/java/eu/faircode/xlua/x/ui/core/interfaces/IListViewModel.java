package eu.faircode.xlua.x.ui.core.interfaces;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.model.ListBaseViewModel;
import eu.faircode.xlua.x.ui.core.util.ListFragmentUtils;
import kotlin.Pair;
import kotlin.Triple;

public interface IListViewModel<TElement>  {
    FilterRequest createRequest(boolean setLast, boolean initFromPreferences, boolean copyLastQuery);

    void refresh();
    void updateList(FilterRequest request);
    LiveData<List<TElement>> getRawLiveData();
    void updateList(Triple<String, String, List<String>> filter, String query, boolean isReversed);
    IUserContext getAsUserContext();
    AndroidViewModel getAsViewModel();
}
