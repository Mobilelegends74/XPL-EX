package eu.faircode.xlua.x.ui.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import eu.faircode.xlua.ActivityMain;
import eu.faircode.xlua.AdapterApp;
import eu.faircode.xlua.FragmentSettings;
import eu.faircode.xlua.R;
import eu.faircode.xlua.loggers.LogHelper;
import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.ui.core.acitivty.ListBaseActivity;
import eu.faircode.xlua.x.ui.dialogs.HelpDialog;
import eu.faircode.xlua.x.ui.fragments.SettingExFragment;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.database.DatabasePathUtil;

public class SettingsExActivity extends ListBaseActivity {
    public static final String SHARED_TAG = "x_settings_x";

    private static final String TAG = LibUtil.generateTag(SettingsExActivity.class);
    public enum enumShow { none, all, unique, android }
    private Menu menu;
    public static final PrefManager manager = PrefManager.create(null, PrefManager.SETTINGS_NAMESPACE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager.ensureIsOpen(this, PrefManager.SETTINGS_NAMESPACE);
        setContentView(R.layout.settings_ex_activity);
        super.startFragmentTransaction(SettingExFragment.class, R.id.content_frame_settings_ex, true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) { super.onPostCreate(savedInstanceState); }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) { super.onConfigurationChanged(newConfig); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settingsmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        //getController().updatedSortedList();
        bindMenuSearch(menu.findItem(R.id.menu_search_settings));
        MenuItem help = menu.findItem(R.id.menu_help_settings);

        if(help != null) {
            help.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                    HelpDialog.create().show(getSupportFragmentManager(), getString(R.string.title_help));
                    return true;
                }
            });
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public SettingExFragment getSettingsFragment() {
        Fragment frag = getFragment();
        if(frag instanceof SettingExFragment) return (SettingExFragment) frag;
        return null;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        manager.ensureIsOpen(SettingsExActivity.this, PrefManager.SETTINGS_NAMESPACE);
        SettingExFragment fragment = getSettingsFragment();
        switch (item.getItemId()) {
            case R.id.menu_show_settings:
                enumShow show = (fragment == null ? enumShow.none : fragment.getShow());
                this.menu.findItem(R.id.menu_show_settings_all).setEnabled(show != enumShow.none);
                this.menu.findItem(R.id.menu_show_settings_unique).setEnabled(show != enumShow.none);
                this.menu.findItem(R.id.menu_show_settings_android).setEnabled(show != enumShow.none);
                switch (show) {
                    case all:
                        this.menu.findItem(R.id.menu_show_settings_all).setChecked(true);
                        break;
                    case unique:
                        this.menu.findItem(R.id.menu_show_settings_unique).setChecked(true);
                        break;
                    case android:
                        this.menu.findItem(R.id.menu_show_settings_android).setChecked(true);
                        break;
                }
                return true;
            case R.id.menu_show_settings_unique:
            case R.id.menu_show_settings_android:
            case R.id.menu_show_settings_all:
                item.setChecked(!item.isChecked());
                final enumShow set;
                switch (item.getItemId()) {
                    case R.id.menu_show_settings_unique:
                        set = enumShow.unique;
                        break;
                    case R.id.menu_show_settings_android:
                        set = enumShow.android;
                        break;
                    default:
                        set = enumShow.all;
                        break;
                }

                manager.putString(PrefManager.PREFERENCE_SHOW, set.name());
                fragment.setShow(set);
                return true;
            //case R.id.menu_help:
            //    menuHelp();
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected String getSharedTagId() {
        return SHARED_TAG;
    }
}
