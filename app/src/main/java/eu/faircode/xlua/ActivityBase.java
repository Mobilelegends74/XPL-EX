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

import android.content.SharedPreferences;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import eu.faircode.xlua.utilities.UiInsets;
import eu.faircode.xlua.utilities.AppLanguage;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;

public class ActivityBase extends AppCompatActivity {
    private static final String TAG = "XLua.ActivityBase";
    public static final String THEME_AUTO = "system";
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";

    private String theme = THEME_DARK;
    private String resolvedTheme = THEME_DARK;
    private String language = AppLanguage.ENGLISH;
    private boolean monetEnabled;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLanguage.wrap(newBase));
    }

    /** Screens ported to the 1.5.8 MaterialToolbar layout override this. */
    protected boolean useCustomToolbar() {
        return false;
    }

    /** The Advanced Settings screen uses the full Material 3 surface palette. */
    protected boolean useAdvancedSettingsTheme() {
        return false;
    }

    private SharedPreferences preferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private String readThemePreference() {
        SharedPreferences prefs = preferences();
        if (!prefs.contains("theme")) {
            String legacyTheme = THEME_DARK;
            try {
                legacyTheme = GetSettingExCommand.getTheme(this, Process.myUid());
            } catch (Throwable ignored) {
                // The service may not be available on the first start. Dark is the safe default.
            }
            if (!THEME_LIGHT.equals(legacyTheme))
                legacyTheme = THEME_DARK;
            prefs.edit().putString("theme", legacyTheme).apply();
        }

        String selected = prefs.getString("theme", THEME_DARK);
        if (!THEME_AUTO.equals(selected) && !THEME_LIGHT.equals(selected))
            selected = THEME_DARK;
        return selected;
    }

    private String resolveTheme(String selected) {
        if (!THEME_AUTO.equals(selected))
            return selected;
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES ? THEME_DARK : THEME_LIGHT;
    }

    private int getThemeStyle() {
        boolean dark = THEME_DARK.equals(resolvedTheme);
        boolean dynamic = monetEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;

        if (useAdvancedSettingsTheme()) {
            if (dynamic)
                return dark ? R.style.AdvancedSettingsThemeDarkMonet : R.style.AdvancedSettingsThemeLightMonet;
            return dark ? R.style.AdvancedSettingsThemeDark : R.style.AdvancedSettingsThemeLight;
        }

        if (useCustomToolbar()) {
            if (dynamic)
                return dark ? R.style.AppThemeDark_NoActionBar_Monet : R.style.AppThemeLight_NoActionBar_Monet;
            return dark ? R.style.AppThemeDark_NoActionBar : R.style.AppThemeLight_NoActionBar;
        }

        return dark ? R.style.AppThemeDark : R.style.AppThemeLight;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        language = AppLanguage.resolveLanguage(this);
        theme = readThemePreference();
        resolvedTheme = resolveTheme(theme);
        monetEnabled = preferences().getBoolean("monet_enabled", false);
        setTheme(getThemeStyle());

        if (DebugUtil.isDebug())
            Log.d(TAG, "Theme=" + theme + " resolved=" + resolvedTheme + " monet=" + monetEnabled);

        super.onCreate(savedInstanceState);
        applyNavigationBarPreference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String selected = readThemePreference();
        String resolved = resolveTheme(selected);
        boolean dynamic = preferences().getBoolean("monet_enabled", false);
        String currentLanguage = AppLanguage.resolveLanguage(this);
        if (!selected.equals(theme) || !resolved.equals(resolvedTheme)
                || dynamic != monetEnabled || !currentLanguage.equals(language))
            recreate();
    }

    public void forceEdgeToEdgeUpdate() {
        UiInsets.enableEdgeToEdge(this, THEME_LIGHT.equals(resolvedTheme), getTransparentNavbar());
    }

    /** Connects the 1.5.8-style in-layout toolbar without duplicating inset code. */
    protected void setupCustomToolbar(int toolbarId, int appBarId, int contentId, int titleId) {
        forceEdgeToEdgeUpdate();
        Toolbar toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (titleId != 0)
                getSupportActionBar().setTitle(titleId);
        }
        UiInsets.applyToolbarInsets(findViewById(appBarId), findViewById(contentId));
    }

    private void applyNavigationBarPreference() {
        UiInsets.updateSystemBarAppearance(this, THEME_LIGHT.equals(resolvedTheme), getTransparentNavbar());
    }

    public String getThemeName() {
        return theme;
    }

    public String getCurrentResolvedTheme() {
        return resolvedTheme;
    }

    public boolean isMonetEnabled() {
        return monetEnabled;
    }

    public void setSystemAppsColor(boolean enabled) {
        preferences().edit().putBoolean("system_apps_color", enabled).apply();
    }

    public boolean getSystemAppsColor() {
        return preferences().getBoolean("system_apps_color", false);
    }

    public void setTransparentNavbar(boolean enabled) {
        preferences().edit().putBoolean("transparent_navbar", enabled).apply();
        applyNavigationBarPreference();
    }

    public boolean getTransparentNavbar() {
        return preferences().getBoolean("transparent_navbar", false);
    }
}
