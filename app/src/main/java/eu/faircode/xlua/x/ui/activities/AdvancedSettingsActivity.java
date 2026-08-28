package eu.faircode.xlua.x.ui.activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

import eu.faircode.xlua.ActivityBase;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.utilities.UiInsets;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;

/** Clean source implementation of the Advanced Settings interface found in 1.5.8. */
public class AdvancedSettingsActivity extends ActivityBase {
    private static final int EASTER_EGG_CLICKS = 10;

    private SharedPreferences preferences;
    private View settingsView;
    private int toolbarClickCount;

    @Override
    protected boolean useCustomToolbar() {
        return true;
    }

    @Override
    protected boolean useAdvancedSettingsTheme() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.advanced_settings_activity);
        forceEdgeToEdgeUpdate();

        Toolbar toolbar = findViewById(R.id.toolbar_advanced);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.menu_advanced_settings);
        }

        toolbar.setOnClickListener(view -> {
            toolbarClickCount++;
            if (toolbarClickCount >= EASTER_EGG_CLICKS) {
                toolbarClickCount = 0;
                unlockExperimentalSettings();
            }
        });

        FrameLayout container = findViewById(R.id.content_container);
        settingsView = LayoutInflater.from(this)
                .inflate(R.layout.advanced_settings_content, container, false);
        container.addView(settingsView);

        UiInsets.applyToolbarInsets(
                findViewById(R.id.app_bar_layout_advanced),
                findViewById(R.id.nsv_advanced_settings));
        bindSettings();
    }

    private void bindSettings() {
        bindTheme();
        bindMonet();
        bindSimpleSwitches();
        bindSnackbarDuration();
        bindExperimentalSettings();
    }

    private void bindTheme() {
        MaterialButtonToggleGroup themes = settingsView.findViewById(R.id.theme_toggle_group);
        String selected = preferences.getString("theme", THEME_DARK);
        int checked = THEME_AUTO.equals(selected) ? R.id.btn_theme_auto
                : THEME_LIGHT.equals(selected) ? R.id.btn_theme_light : R.id.btn_theme_dark;
        themes.check(checked);
        themes.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked)
                return;
            String value = checkedId == R.id.btn_theme_auto ? THEME_AUTO
                    : checkedId == R.id.btn_theme_light ? THEME_LIGHT : THEME_DARK;
            if (value.equals(preferences.getString("theme", THEME_DARK)))
                return;
            preferences.edit().putString("theme", value).apply();
            restartForVisualChange();
        });
    }

    private void bindMonet() {
        View section = settingsView.findViewById(R.id.monet_section);
        View colorInfo = settingsView.findViewById(R.id.monet_color_info);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            section.setVisibility(View.GONE);
            colorInfo.setVisibility(View.GONE);
            return;
        }

        section.setVisibility(View.VISIBLE);
        colorInfo.setVisibility(View.VISIBLE);
        MaterialSwitch monet = settingsView.findViewById(R.id.switch_monet);
        monet.setChecked(GetSettingExCommand.isMonetEnabled(this));
        updatePrimaryColorPreview();
        monet.setOnCheckedChangeListener((button, checked) -> {
            preferences.edit().putBoolean("monet_enabled", checked).apply();
            restartForVisualChange();
        });
    }

    private void updatePrimaryColorPreview() {
        int color = getColorFromAttribute(com.google.android.material.R.attr.colorPrimary);
        TextView value = settingsView.findViewById(R.id.tv_monet_color_hex);
        value.setText(String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF));
        MaterialCardView preview = settingsView.findViewById(R.id.monet_color_preview);
        preview.setCardBackgroundColor(color);
    }

    private int getColorFromAttribute(int attribute) {
        android.util.TypedValue value = new android.util.TypedValue();
        return getTheme().resolveAttribute(attribute, value, true) ? value.data : Color.DKGRAY;
    }

    private void bindSimpleSwitches() {
        MaterialSwitch graySystemApps = settingsView.findViewById(R.id.switch_gray_system_apps);
        graySystemApps.setChecked(getSystemAppsColor());
        graySystemApps.setOnCheckedChangeListener((button, checked) -> {
            setSystemAppsColor(checked);
            showMessage(R.string.advanced_setting_applied);
        });

        MaterialSwitch forceEnglish = settingsView.findViewById(R.id.switch_force_english);
        forceEnglish.setChecked(getIsForceEnglish());
        forceEnglish.setOnCheckedChangeListener((button, checked) -> {
            setForceEnglish(checked);
            restartForVisualChange();
        });

        MaterialSwitch hideFab = settingsView.findViewById(R.id.switch_fab_hide_on_scroll);
        hideFab.setChecked(preferences.getBoolean("fab_hide_on_scroll", true));
        hideFab.setOnCheckedChangeListener((button, checked) -> {
            preferences.edit().putBoolean("fab_hide_on_scroll", checked).apply();
            showMessage(R.string.advanced_setting_applied);
        });

        MaterialSwitch debugLogs = settingsView.findViewById(R.id.switch_debug_logs);
        boolean verbose = false;
        try {
            verbose = GetSettingExCommand.getVerboseLogs(this);
        } catch (Throwable ignored) {
        }
        debugLogs.setChecked(verbose);
        debugLogs.setOnCheckedChangeListener((button, checked) -> {
            DebugUtil.setForceDebug(checked);
            PutSettingExCommand.putVerboseLogging(this, checked);
            showMessage(R.string.advanced_setting_applied);
        });

        MaterialSwitch skipWarnings = settingsView.findViewById(R.id.switch_skip_warning);
        skipWarnings.setChecked(getSkipWarning());
        skipWarnings.setOnCheckedChangeListener((button, checked) -> {
            setSkipWarning(checked);
            showMessage(R.string.advanced_setting_applied);
        });

        MaterialSwitch transparentNavbar = settingsView.findViewById(R.id.switch_transparent_navbar);
        transparentNavbar.setChecked(getTransparentNavbar());
        transparentNavbar.setOnCheckedChangeListener((button, checked) -> {
            setTransparentNavbar(checked);
            forceEdgeToEdgeUpdate();
            showMessage(R.string.advanced_setting_applied);
        });
    }

    private void bindSnackbarDuration() {
        MaterialButtonToggleGroup group = settingsView.findViewById(R.id.snackbar_duration_group);
        int duration = preferences.getInt("snackbar_duration_ms", 2750);
        int checked = duration == 500 ? R.id.btn_snackbar_500
                : duration == 1000 ? R.id.btn_snackbar_1000
                : duration == 1500 ? R.id.btn_snackbar_1500
                : duration == 2000 ? R.id.btn_snackbar_2000 : R.id.btn_snackbar_default;
        group.check(checked);
        group.addOnButtonCheckedListener((buttonGroup, checkedId, isChecked) -> {
            if (!isChecked)
                return;
            int selected = checkedId == R.id.btn_snackbar_500 ? 500
                    : checkedId == R.id.btn_snackbar_1000 ? 1000
                    : checkedId == R.id.btn_snackbar_1500 ? 1500
                    : checkedId == R.id.btn_snackbar_2000 ? 2000 : 2750;
            preferences.edit().putInt("snackbar_duration_ms", selected).apply();
            showMessage(R.string.advanced_setting_applied);
        });
    }

    private void bindExperimentalSettings() {
        View section = settingsView.findViewById(R.id.experimental_section);
        section.setVisibility(preferences.getBoolean("experimental_unlocked", false)
                ? View.VISIBLE : View.GONE);

        TextInputEditText input = settingsView.findViewById(R.id.edit_custom_color);
        input.setText(preferences.getString("custom_theme_color", ""));
        MaterialButton apply = settingsView.findViewById(R.id.btn_apply_custom_color);
        MaterialButton reset = settingsView.findViewById(R.id.btn_reset_custom_color);
        MaterialButton hide = settingsView.findViewById(R.id.btn_hide_experimental);

        apply.setOnClickListener(view -> {
            String candidate = input.getText() == null ? "" : input.getText().toString().trim();
            if (!isValidHexColor(candidate)) {
                showMessage(R.string.advanced_invalid_color);
                return;
            }
            preferences.edit().putString("custom_theme_color", candidate).apply();
            // 1.5.8 marks this as an experimental preparation. Keep it isolated
            // from hook and device-profile behavior, but preview it immediately.
            MaterialCardView preview = settingsView.findViewById(R.id.monet_color_preview);
            preview.setCardBackgroundColor(Color.parseColor(candidate));
            showMessage(R.string.advanced_setting_applied);
        });
        reset.setOnClickListener(view -> {
            preferences.edit().remove("custom_theme_color").apply();
            input.setText("");
            updatePrimaryColorPreview();
            showMessage(R.string.advanced_setting_applied);
        });
        hide.setOnClickListener(view -> {
            preferences.edit().putBoolean("experimental_unlocked", false).apply();
            section.setVisibility(View.GONE);
        });
    }

    private void unlockExperimentalSettings() {
        preferences.edit().putBoolean("experimental_unlocked", true).apply();
        View section = settingsView.findViewById(R.id.experimental_section);
        section.setVisibility(View.VISIBLE);
        section.setAlpha(0f);
        section.animate().alpha(1f).setDuration(300L).start();
        showMessage(R.string.advanced_easter_unlocked);
    }

    private boolean isValidHexColor(String value) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}"))
            return false;
        try {
            Color.parseColor(value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private void restartForVisualChange() {
        showMessage(R.string.advanced_restart_applied);
        settingsView.postDelayed(this::recreate, 180L);
    }

    private void showMessage(int textResource) {
        int duration = preferences.getInt("snackbar_duration_ms", 2750);
        Snackbar snackbar = Snackbar.make(settingsView, textResource, Snackbar.LENGTH_SHORT);
        snackbar.setDuration(duration);
        snackbar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
