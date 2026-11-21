/*
    This file is part of XPrivacyLua.

    XPrivacyLua is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    XPrivacyLua is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with XPrivacyLua.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2017-2019 Marcel Bokhorst (M66B)
 */

package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xlua.call.CleanHooksCommand;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.utilities.PrefUtil;
import eu.faircode.xlua.utilities.AppLanguage;
import eu.faircode.xlua.utilities.UiInsets;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.FileDialogUtils;
import eu.faircode.xlua.x.ui.activities.HooksExActivity;
import eu.faircode.xlua.x.ui.activities.AdvancedSettingsActivity;
import eu.faircode.xlua.x.ui.activities.SettingsExActivity;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.dialogs.BackupDialog;
import eu.faircode.xlua.x.ui.dialogs.CollectionsDialog;
import eu.faircode.xlua.x.ui.dialogs.TaskProgressDialog;
import eu.faircode.xlua.x.ui.dialogs.utils.BackupDialogUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.DropTableCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutAssignmentCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutHookExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.XBackup;
import eu.faircode.xlua.x.xlua.settings.data.XScript;

public class ActivityMain extends ActivityBase {
    private final static String TAG = LibUtil.generateTag(ActivityMain.class);


    private FragmentMain fragmentMain = null;
    private DrawerLayout drawerLayout = null;
    private View drawerContainer;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle = null;

    private Menu menu = null;

    public static final int LOADER_DATA = 1;
    public static final String EXTRA_SEARCH_PACKAGE = "package";

    public static final PrefManager manager = PrefManager.create(null, PrefManager.SETTINGS_MAIN);

    private final XBackup backup = new XBackup();
    private final Handler navigationHandler = new Handler();
    private Runnable hideNavigation;
    private boolean filterPanelExpanded = true;

    @Override
    protected boolean useCustomToolbar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        manager.ensureIsOpen(this, PrefManager.SETTINGS_MAIN);
        // Startup is intentionally non-blocking. Module/database diagnostics are
        // available elsewhere, but first launch, update and module activation must
        // never force the user through a warning or confirmation dialog.

        // Set layout
        setContentView(R.layout.main);
        forceEdgeToEdgeUpdate();
        setupAppFilterBar();

        // Prepare action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        UiInsets.applyToolbarInsets(findViewById(R.id.app_bar_layout), findViewById(R.id.content_frame));

        // Show fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        androidx.fragment.app.Fragment restored = fragmentManager.findFragmentById(R.id.content_frame);
        if (restored instanceof FragmentMain) {
            fragmentMain = (FragmentMain) restored;
        } else {
            fragmentMain = new FragmentMain();
            fragmentTransaction.replace(R.id.content_frame, fragmentMain);
            fragmentTransaction.commit();
        }

        // Get drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerContainer = findViewById(R.id.drawer_container);
        View customScrim = findViewById(R.id.custom_scrim);
        drawerLayout.setScrimColor(android.graphics.Color.TRANSPARENT);
        UiInsets.applyDrawerInsets(findViewById(R.id.drawer_header_spacer), findViewById(R.id.drawer_content));
        customScrim.setOnClickListener(view -> drawerLayout.closeDrawer(drawerContainer));

        // Let the toolbar title scroll instead of being cut off with an ellipsis.
        // app_name carries the version suffix, so it does not fit between the
        // navigation icon and the action buttons on a phone in portrait.
        enableToolbarTitleMarquee();

        // Create drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(getString(R.string.app_name));
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(getString(R.string.app_name));
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                customScrim.setVisibility(slideOffset > 0f ? View.VISIBLE : View.GONE);
                customScrim.setAlpha(slideOffset);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Get drawer list
        drawerList = findViewById(R.id.drawer_list);

        // Handle drawer list click
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerItem item = (DrawerItem) parent.getAdapter().getItem(position);
                //Log.i(TAG, "Drawer selected " + item.getTitle());
                item.onClick();
                if (!item.isCheckable())
                    drawerLayout.closeDrawer(drawerContainer);
            }
        });

        boolean notifyNew = GetSettingExCommand.notifyOnNewApps(this);
        boolean restrictNew = GetSettingExCommand.restrictNewApps(this);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Notify New=%s Restrict New=%s", notifyNew, restrictNew));

        final ArrayAdapterDrawer drawerArray = new ArrayAdapterDrawer(ActivityMain.this, R.layout.draweritem);

        if (!XposedUtil.isVirtualXposed())
            drawerArray.add(new DrawerItem(this, R.string.menu_notify_new, notifyNew, new DrawerItem.IListener() {
                @Override
                public void onClick(DrawerItem item) {
                    handleCodeToSnack(PutSettingExCommand.putNotifyNewApps(ActivityMain.this, item.isChecked()), getString(R.string.result_prefix_notify) + "=" + item.isChecked());
                    drawerArray.notifyDataSetChanged();
                }
            }));

        //Remove this for now, how can something like this be implemented ?
        /*if (!XposedUtil.isVirtualXposed())
            drawerArray.add(new DrawerItem(this, R.string.menu_restrict_new, restrictNew, new DrawerItem.IListener() {
                @Override
                public void onClick(DrawerItem item) {
                    handleCodeToSnack(PutSettingExCommand.putRestrictNewApps(ActivityMain.this, item.isChecked()), getString(R.string.result_prefix_restrict) + "=" + item.isChecked());
                    drawerArray.notifyDataSetChanged();
                }
            }));*/


        drawerArray.add(new DrawerItem(this, R.string.menu_donate, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/0bbedCode/XPL-EX/?tab=readme-ov-file#donations"));
                if (browse.resolveActivity(getPackageManager()) == null)
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_no_browser), Snackbar.LENGTH_LONG).show();
                else
                    startActivity(browse);
            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_whats_new_button, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                whatsNew();
            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_collections, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                new CollectionsDialog()
                        .set(ActivityMain.this)
                        .setOnDialogCloseListener(() -> { if(fragmentMain != null) fragmentMain.loadData(); })
                        .show(getSupportFragmentManager(), getString(R.string.menu_collections));
            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_settings, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                menuSettings();
            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_hooks, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                menuHooks();
            }
        }));


        // Add after other drawer items
        drawerArray.add(new DrawerItem(this, R.string.menu_import_settings, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                BackupDialogUtils.startBackupFilePicker(ActivityMain.this);


                // Create and show the dialog


            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_export_settings, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                final Context context = ActivityMain.this;
                backup.reset();
                //ConfUtils.startConfigSavePicker(this, checked.name);
                BackupDialog.create()
                        .setBackup(backup, false)  // Not from file
                        .setDefinitions(context)
                        .setSettings(context)
                        .setAssignments(context)
                        .setAllHooks(context)
                        .setOnOperationExportedOrApplied((v, wasImported) -> {
                            if(v != null) {
                                backup.copyFrom(v);
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, "Was Imported: " + wasImported + " Name=" + backup.getName());

                                if(!wasImported)
                                    BackupDialogUtils.startSavePicker(ActivityMain.this, backup.getName());
                            }
                        })
                        .show(getSupportFragmentManager(), getString(R.string.title_backup));
            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_advanced_settings, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                startActivity(new Intent(ActivityMain.this, AdvancedSettingsActivity.class));
            }
        }));


        drawerList.setAdapter(drawerArray);
        //whatsNew
        initCore();
        ModuleUpdateChecker.check(this);
    }

    private void setupAppFilterBar() {
        View bar = findViewById(R.id.bottom_app_filter);
        bar.setVisibility(View.VISIBLE);
        int selected = R.id.filter_user;
        manager.ensureIsOpen(this, PrefManager.SETTINGS_MAIN);
        {
            switch (PrefManager.settingToShow(manager.getString(PrefManager.SETTING_APPS_SHOW, "show_user", true))) {
                case icon: selected = R.id.filter_user; break;
                case all: selected = R.id.filter_all; break;
                case hook: selected = R.id.filter_hook; break;
                case system: selected = R.id.filter_system; break;
                default: break;
            }
        }
        int[] buttons = {R.id.filter_user, R.id.filter_all, R.id.filter_hook, R.id.filter_system};
        updateFilterSelection(selected, buttons);
        for (int buttonId : buttons) {
            View button = findViewById(buttonId);
            button.setOnClickListener(v -> {
            if (fragmentMain == null) return;
            int id = v.getId();
            AdapterApp.enumShow value;
            String pref;
            if (id == R.id.filter_all) { value = AdapterApp.enumShow.all; pref = "show_all"; }
            else if (id == R.id.filter_hook) { value = AdapterApp.enumShow.hook; pref = "show_hook"; }
            else if (id == R.id.filter_system) { value = AdapterApp.enumShow.system; pref = "show_system"; }
            else { value = AdapterApp.enumShow.user; pref = "show_user"; }
            manager.putString(PrefManager.SETTING_APPS_SHOW, pref);
            fragmentMain.setShow(value);
            fragmentMain.loadData();
            updateFilterSelection(id, buttons);
            showNavigation(true);
            });
        }
        // Keep the last list item above the floating navigation bar.
        View content = findViewById(R.id.content_frame);
        if (content != null) {
            content.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                        || event.getActionMasked() == MotionEvent.ACTION_MOVE
                        || event.getActionMasked() == MotionEvent.ACTION_UP
                        || event.getActionMasked() == MotionEvent.ACTION_CANCEL)
                    showNavigation(event.getActionMasked() != MotionEvent.ACTION_UP
                            && event.getActionMasked() != MotionEvent.ACTION_CANCEL);
                return false;
            });
            content.post(() -> {
                RecyclerView list = findRecyclerView(content);
                if (list != null) {
                    int bottom = Math.round(76 * getResources().getDisplayMetrics().density);
                    list.setClipToPadding(false);
                    list.setPadding(list.getPaddingLeft(), list.getPaddingTop(),
                            list.getPaddingRight(), bottom);
                    list.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int state) {
                        if (state == RecyclerView.SCROLL_STATE_DRAGGING)
                            showNavigation(true);
                        else if (state == RecyclerView.SCROLL_STATE_IDLE)
                            showNavigation(false);
                    }
                    });
                }
            });
        }
        View barView = findViewById(R.id.bottom_app_filter);
        if (barView != null) {
            barView.addOnLayoutChangeListener((view, left, top, right, bottom,
                                                oldLeft, oldTop, oldRight, oldBottom) -> centerNavigation(view));
            barView.setOnApplyWindowInsetsListener((view, insets) -> {
                int inset = 0;
                if (android.os.Build.VERSION.SDK_INT >= 23)
                    inset = insets.getSystemWindowInsetBottom();
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                lp.bottomMargin = Math.round(12 * getResources().getDisplayMetrics().density) + inset;
                view.setLayoutParams(lp);
                centerNavigation(view);
                return insets;
            });
            barView.post(() -> centerNavigation(barView));
            barView.requestApplyInsets();
        }
        showNavigation(false);
    }

    private RecyclerView findRecyclerView(View view) {
        if (view instanceof RecyclerView) return (RecyclerView) view;
        if (!(view instanceof ViewGroup)) return null;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            RecyclerView result = findRecyclerView(group.getChildAt(i));
            if (result != null) return result;
        }
        return null;
    }

    /** Centers the measured capsule against the full overlay parent, independent of OEM CoordinatorLayout gravity. */
    private void centerNavigation(View bar) {
        if (!(bar.getParent() instanceof View)) return;
        View parent = (View) bar.getParent();
        if (parent.getWidth() <= 0 || bar.getWidth() <= 0) return;
        bar.setX(Math.round((parent.getWidth() - bar.getWidth()) / 2f));
    }

    private void showNavigation(boolean interacting) {
        View bar = findViewById(R.id.bottom_app_filter);
        if (bar == null) return;
        if (hideNavigation != null) navigationHandler.removeCallbacks(hideNavigation);
        bar.setVisibility(View.VISIBLE);
        bar.animate().withEndAction(null).cancel();
        if (bar.getAlpha() < 1f) {
            bar.setTranslationY(Math.round(24 * getResources().getDisplayMetrics().density));
            bar.animate().alpha(1f).translationY(0f).setDuration(220).start();
        } else bar.setAlpha(1f);
        hideNavigation = () -> bar.animate().alpha(0f)
                .translationY(Math.round(24 * getResources().getDisplayMetrics().density))
                .setDuration(200).withEndAction(() -> bar.setVisibility(View.GONE)).start();
        if (!interacting) navigationHandler.postDelayed(hideNavigation, 3000L);
    }

    private void updateFilterSelection(int selected, int[] buttons) {
        for (int id : buttons) {
            View button = findViewById(id);
            if (button != null)
                button.setSelected(id == selected);
        }
    }

    public void handleCodeToSnack(A_CODE code, String extraIfSucceeded) {
        Snackbar.make(findViewById(android.R.id.content),
                A_CODE.isSuccessful(code) ?
                        Str.combine(getString(R.string.msg_task_finished_command), TextUtils.isEmpty(extraIfSucceeded) ? "" :  " >> " + extraIfSucceeded) :
                        Str.combine(getString(R.string.msg_task_failure), " >> " + code.name(), false) , Snackbar.LENGTH_LONG).show();
    }

    public static String ensureLuaScript(String name, Map<String, String> map) {
        if(Str.isEmpty(name))
            return Str.EMPTY;

        String trimmed = Str.trimEx(Str.trimEx(name.trim(), true, false, true, false, "@"),
                true, false, false, true, ".lua");

        if(map == null)
            return trimmed;

        if(name.startsWith("@") || name.endsWith(".lua")) {
            String code = map.get(trimmed);
            if(Str.isEmpty(code))
                return Str.EMPTY;

            return code;
        }

        return trimmed;
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || resultCode != Activity.RESULT_OK)
            return;
        Uri uri = data.getData();
        if (uri == null)
            return;

        if (requestCode == BackupDialogUtils.REQUEST_OPEN_BACKUP) {
            XBackup backup = BackupDialogUtils.readBackupFromUri(this, uri);
            if (backup != null) {
                BackupDialog.create()
                        .setBackup(backup, true)  // From file
                        .setOnOperationExportedOrApplied((v, wasImported) -> {
                            if (DebugUtil.isDebug())
                                Log.d(TAG, "Backup import completed");

                            // Refresh UI after import
                            //if (fragmentMain != null)
                            //    fragmentMain.loadData();
                            if(v != null && wasImported) {
                                if(ListUtil.isValid(v.getSettings())) {
                                    if(v.dropOld)
                                        DropTableCommand.drop(this, SettingPacket.TABLE_NAME, "xlua");

                                    for(SettingPacket packet : v.getSettings()) {
                                        if(packet.value != null) {
                                            packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
                                            PutSettingExCommand.call(this, packet);
                                        }
                                    }
                                }

                                if(ListUtil.isValid(v.getDefinitions())) {
                                    if(v.dropOld)
                                        DropTableCommand.drop(this, XLuaHook.TABLE_NAME, "xlua");

                                    Map<String, String> luaScripts = new HashMap<>();
                                    if(ListUtil.isValid(v.getScripts())) {
                                        for(XScript script : v.getScripts()) {
                                            String name = ensureLuaScript(script.getName(), null);
                                            String code = script.getCode();
                                            if(Str.isEmpty(code) || Str.isEmpty(name))
                                                continue;

                                            luaScripts.put(name, code);
                                        }
                                    }

                                    if(DebugUtil.isDebug())
                                        Log.d(TAG, "Lua Scripts Loaded=" + ListUtil.size(luaScripts));

                                    //Resolve the Script
                                    for(XHook hook : v.getDefinitions()) {
                                        if(hook != null && !Str.isEmpty(hook.getObjectId())) {
                                            //This is our resolver for Lua Scripts ?? how ? double check this pls
                                            String luaScript = hook.luaScript;
                                            String resolved = Str.ensureIsNotNullOrDefault(ensureLuaScript(luaScript, luaScripts), Str.EMPTY);
                                            //hook.setScript(resolved);
                                            hook.luaScript = luaScript;
                                            //ToDo: Make this more advance, perhaps checks ?
                                            PutHookExCommand.putEx(this, hook, false);
                                        }
                                    }
                                }

                                if(ListUtil.isValid(v.getAssignments())) {
                                    if(v.dropOld)
                                        DropTableCommand.drop(this, AssignmentPacket.TABLE_NAME, "xlua");

                                    for(AssignmentPacket assignmentPacket : v.getAssignments()) {
                                        if(!Str.isEmpty(assignmentPacket.getCategory()) && !Str.isEmpty(assignmentPacket.getHookId())) {
                                            assignmentPacket.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
                                            PutAssignmentCommand.put(this, assignmentPacket);
                                            if(DebugUtil.isDebug())
                                                Log.d(TAG, "Pushed Assignment=" + Str.ensureNoDoubleNewLines(Str.toStringOrNull(assignmentPacket)));
                                        }
                                    }
                                }
                            }

                            Snackbar.make(findViewById(android.R.id.content),
                                    getString(R.string.msg_settings_imported),
                                    Snackbar.LENGTH_LONG).show();
                        })
                        .show(getSupportFragmentManager(), getString(R.string.title_backup));
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.msg_error_reading_backup,
                        Snackbar.LENGTH_LONG).show();
            }
        }
        else if (requestCode == BackupDialogUtils.REQUEST_SAVE_BACKUP) {
            try {
                String json = backup.toJSON();
                if (DebugUtil.isDebug())
                    Log.d(TAG, "Save backup request received, Name=" + backup.getName() + " Json=" + Str.ensureNoDoubleNewLines(json));

                if(Str.isEmpty(json))
                    throw new Exception("XLua Backup JSON is null or Empty!");

                FileDialogUtils.takePersistablePermissions(ActivityMain.this, uri);
                boolean success = FileDialogUtils.writeFileContent(ActivityMain.this, uri, json);
                Snackbar.make(findViewById(android.R.id.content),
                        success ? getString(R.string.msg_backup_exported) : getString(R.string.msg_backup_exported_failed),
                        Snackbar.LENGTH_LONG).show();
            }catch (Exception e) {
                Log.e(TAG, "Error Saving XLua Backup! Error=" + e + " Name=" + backup.getName());
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle != null)
            drawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (this.menu != null)
            updateMenu(this.menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null)
            drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        navigationHandler.removeCallbacksAndMessages(null);
        View bar = findViewById(R.id.bottom_app_filter);
        if (bar != null) bar.animate().withEndAction(null).cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerContainer != null && drawerLayout.isDrawerOpen(drawerContainer))
            drawerLayout.closeDrawer(drawerContainer);
        else
            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();
        if(searchView == null)
            return false;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (fragmentMain != null) {
                    fragmentMain.filter(query);
                    searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (fragmentMain != null) fragmentMain.filter(newText);
                return true;
            }
        });

        menuSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) { return true; }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                // Search uid once
                Intent intent = getIntent();
                intent.removeExtra(EXTRA_SEARCH_PACKAGE);
                setIntent(intent);
                return true;
            }
        });

        updateMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item))
            return true;

        manager.ensureIsOpen(ActivityMain.this, PrefManager.SETTINGS_MAIN);
        switch (item.getItemId()) {
            case R.id.menu_show:
                setFilterPanelExpanded(!filterPanelExpanded, true);
                return true;
            case R.id.menu_help:
                menuHelp();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void menuHooks() {
        Intent settingIntent = new Intent(this, HooksExActivity.class);
        settingIntent.putExtra(UserClientAppContext.FIELD_APP_PACKAGE_NAME,  UserClientAppContext.GLOBAL_NAME_SPACE);
        startActivity(settingIntent);
    }
    private void menuHelp() {
        startActivity(new Intent(this, ActivityHelp.class));
    }
    private void menuProps() { startActivity(new Intent(this, ActivityProperties.class)); }
    private void menuDBs() { startActivity(new Intent(this, ActivityDatabase.class)); }
    private void menuCPU() { startActivity(new Intent(this, ActivityCpu.class)); }
    private void menuConfig() { startActivity(new Intent(this, ActivityConfig.class)); }
    private void menuSettings() {
        Intent settingIntent = new Intent(this, SettingsExActivity.class);
        settingIntent.putExtra(UserClientAppContext.FIELD_APP_PACKAGE_NAME,  UserClientAppContext.GLOBAL_NAME_SPACE);
        startActivity(settingIntent);
    }


    public void updateMenu(Menu menu) {
        MenuItem panelToggle = menu.findItem(R.id.menu_show);
        if (panelToggle != null) {
            panelToggle.setIcon(filterPanelExpanded
                    ? R.drawable.ic_expand_less_white_24dp
                    : R.drawable.ic_expand_more_white_24dp);
            panelToggle.setTitle(filterPanelExpanded ? getString(R.string.menu_filter_panel_hide) : getString(R.string.menu_filter_panel_show));
        }
        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();
        if (searchView != null) {
            String pkg = getIntent().getStringExtra(EXTRA_SEARCH_PACKAGE);
            if (pkg != null) {
                menuSearch.expandActionView();
                searchView.setQuery(pkg, true);
            }
        }
    }

    private void setFilterPanelExpanded(boolean expanded, boolean animate) {
        filterPanelExpanded = expanded;
        if (fragmentMain != null)
            fragmentMain.setFilterPanelVisible(expanded, animate);
        if (menu != null) {
            MenuItem panelToggle = menu.findItem(R.id.menu_show);
            if (panelToggle != null) {
                panelToggle.setIcon(expanded
                        ? R.drawable.ic_expand_less_white_24dp
                        : R.drawable.ic_expand_more_white_24dp);
                panelToggle.setTitle(expanded ? getString(R.string.menu_filter_panel_hide) : getString(R.string.menu_filter_panel_show));
            }
        }
    }

    public void whatsNew() {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.thankyou, null, false);
            TextView tvLicence = view.findViewById(R.id.tvThankYouObbed);
            tvLicence.setMovementMethod(LinkMovementMethod.getInstance());
            //int year = Calendar.getInstance().get(Calendar.YEAR);
            //tvLicence.setText(
            //        Html.fromHtml(getString(R.string.whats_new, year)));

            //int year = Calendar.getInstance().get(Calendar.YEAR);
            tvLicence.setText(Html.fromHtml(getString(R.string.whats_new)));

            new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_whats_new_button)
                    .setView(view)
                    .setCancelable(true)
                    .setPositiveButton(R.string.option_thanks, null)
                    .show();
        } catch (Exception ignored) { }
    }

    /**
     * Makes the toolbar title scroll (marquee) when it is wider than the space
     * left between the navigation icon and the action buttons. MaterialToolbar
     * builds its title as a plain AppCompatTextView with a single line and
     * ellipsis, so the title has to be reconfigured after the toolbar inflated
     * it. setSelected(true) is what actually starts a marquee, which is why a
     * clickable/focusable flag is not enough here.
     */
    private void enableToolbarTitleMarquee() {
        // toolbar is a local variable in onCreate, not a field, so resolve it again.
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null)
            return;

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (!(child instanceof TextView))
                continue;
            // Only the title, not the subtitle or any custom view the toolbar may hold.
            CharSequence text = ((TextView) child).getText();
            if (text == null || !text.equals(getString(R.string.app_name)))
                continue;

            TextView title = (TextView) child;
            title.setSingleLine(true);
            title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            title.setMarqueeRepeatLimit(-1); // marquee_forever
            title.setHorizontallyScrolling(true);
            title.setSelected(true);
        }
    }

    private void showLanguageDialog() {
        String[] values = { AppLanguage.SYSTEM, AppLanguage.ENGLISH, AppLanguage.RUSSIAN, AppLanguage.CHINESE };
        String selected = AppLanguage.getSelection(this);
        int checked = AppLanguage.ENGLISH.equals(selected) ? 1
                : AppLanguage.RUSSIAN.equals(selected) ? 2
                : AppLanguage.CHINESE.equals(selected) ? 3 : 0;
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_language)
                .setSingleChoiceItems(R.array.app_language_options, checked, (dialog, which) -> {
                    AppLanguage.setSelection(this, values[which]);
                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton(R.string.option_cancel, null)
                .show();
    }

    public void initCore() {
        String lastHash = PrefUtil.getString(this, PrefUtil.PREF_LAST_RUN);
        String curHash = String.valueOf(BuildConfig.VERSION_NAME.hashCode());
        if(lastHash == null || lastHash.equals(curHash)) {
            try {
                XResult res = CleanHooksCommand.invokeEx(this);
                if(res.succeeded()) {
                    PrefUtil.setString(this, PrefUtil.PREF_LAST_RUN, curHash);
                    for(int i = 0; i < 6; i++)
                        CleanHooksCommand.invokeEx(this);
                }
            }catch (Exception e) {
                XLog.e("Failed to Clean PEX Hooks", e, true);
            }
        }

        markStartupPromptsHandled();
    }

    private void markStartupPromptsHandled() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("firstrun", true))
            prefs.edit().putBoolean("firstrun", false).apply();
        if (!PrefUtil.getBoolean(this, "welcome", false, true))
            PrefUtil.setBoolean(this, "welcome", true);
    }

    private static class DrawerItem {
        private final int id;
        private final String title;
        private final boolean checkable;
        private boolean checked;
        private final IListener listener;

        DrawerItem(Context context, int title, IListener listener) {
            this.id = title;
            this.title = context.getString(title);
            this.checkable = false;
            this.checked = false;
            this.listener = listener;
        }

        DrawerItem(Context context, int title, boolean checked, IListener listener) {
            this.id = title;
            this.title = context.getString(title);
            this.checkable = true;
            this.checked = checked;
            this.listener = listener;
        }

        public int getId() {
            return this.id;
        }

        public String getTitle() {
            return this.title;
        }

        boolean isCheckable() {
            return this.checkable;
        }

        boolean isChecked() {
            return this.checked;
        }

        void onClick() {
            if (this.checkable)
                this.checked = !this.checked;
            if (this.listener != null)
                this.listener.onClick(this);
        }

        interface IListener {
            void onClick(DrawerItem item);
        }
    }

    private static class ArrayAdapterDrawer extends ArrayAdapter<DrawerItem> {
        private final int resource;

        ArrayAdapterDrawer(@NonNull Context context, int resource) {
            super(context, resource);
            this.resource = resource;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row;
            if (null == convertView)
                row = LayoutInflater.from(getContext()).inflate(this.resource, null);
            else
                row = convertView;

            DrawerItem item = getItem(position);

            TextView tv = row.findViewById(R.id.tvItem);
            CheckBox cb = row.findViewById(R.id.cbItem);
            tv.setText(item.getTitle());
            cb.setVisibility(item.isCheckable() ? View.VISIBLE : View.GONE);
            cb.setChecked(item.isChecked());

            return row;
        }
    }
}
