package eu.faircode.xlua.x.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.core.acitivty.ListBaseActivity;
import eu.faircode.xlua.x.ui.dialogs.HelpDialog;
import eu.faircode.xlua.x.ui.fragments.HooksExFragment;

public class HooksExActivity extends ListBaseActivity {
    public static final String SHARED_TAG = "x_hooks_x";

    @Override
    protected boolean useCustomToolbar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hooks_ex_activity);
        setupCustomToolbar(R.id.toolbar_hooks, R.id.app_bar_layout_hooks,
                R.id.content_frame_hooks_ex, R.string.menu_hooks);
        super.startFragmentTransaction(HooksExFragment.class, R.id.content_frame_hooks_ex, true);
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
        bindMenuSearch(menu.findItem(R.id.menu_search_settings));

        MenuItem help = menu.findItem(R.id.menu_help_settings);
        try {
            MenuItem sort = menu.findItem(R.id.menu_show_settings);
            sort.setVisible(false);
        }catch (Exception e) { }

        if(help != null) {
            help.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                    HelpDialog.create()
                            .setIsSetting(false)
                            .show(getSupportFragmentManager(), getString(R.string.title_help));
                    return true;
                }
            });
        }


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getSharedTagId() {
        return SHARED_TAG;
    }
}
