package eu.faircode.xlua.x.ui.models;

import android.app.Application;

import eu.faircode.xlua.x.ui.adapters.hooks.HooksRepository;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.core.model.ListRepoViewModel;
import eu.faircode.xlua.x.xlua.repos.SettingsGroupRepository;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;

public class HooksExItemViewModel extends ListRepoViewModel<XHook> {
    public HooksExItemViewModel(Application application) {
        super(application, "xlua_hooks_ex_hooks", HooksRepository.INSTANCE);
    }
}
