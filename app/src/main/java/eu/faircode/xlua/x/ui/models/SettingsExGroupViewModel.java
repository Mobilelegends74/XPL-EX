package eu.faircode.xlua.x.ui.models;

import android.app.Application;

import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.ui.core.model.ListRepoViewModel;
import eu.faircode.xlua.x.xlua.repos.SettingsGroupRepository;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;

public class SettingsExGroupViewModel extends ListRepoViewModel<SettingsGroup> {
    public SettingsExGroupViewModel(Application application) {
        super(application, PrefManager.SETTINGS_NAMESPACE, SettingsGroupRepository.INSTANCE);
    }
}
