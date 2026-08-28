package eu.faircode.xlua.utilities;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** Small, reusable edge-to-edge adapter for the custom 1.5.8 toolbars. */
public final class UiInsets {
    private UiInsets() {
    }

    public static void enableEdgeToEdge(Activity activity, boolean lightTheme, boolean transparentNavbar) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        updateSystemBarAppearance(activity, lightTheme, transparentNavbar);
    }

    public static void updateSystemBarAppearance(Activity activity, boolean lightTheme, boolean transparentNavbar) {
        Window window = activity.getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(transparentNavbar ? Color.TRANSPARENT : Color.BLACK);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            // All redesigned screens use a red toolbar behind the status bar.
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(lightTheme && transparentNavbar);
        }
    }

    public static void applyToolbarInsets(View appBar, View content) {
        if (appBar != null) {
            final int left = appBar.getPaddingLeft();
            final int top = appBar.getPaddingTop();
            final int right = appBar.getPaddingRight();
            final int bottom = appBar.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(appBar, (view, insets) -> {
                Insets status = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                view.setPadding(left, top + status.top, right, bottom);
                return insets;
            });
            ViewCompat.requestApplyInsets(appBar);
        }

        if (content != null) {
            final int left = content.getPaddingLeft();
            final int top = content.getPaddingTop();
            final int right = content.getPaddingRight();
            final int bottom = content.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(content, (view, insets) -> {
                Insets navigation = insets.getInsets(
                        WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.displayCutout());
                view.setPadding(left, top, right, bottom + navigation.bottom);
                return insets;
            });
            ViewCompat.requestApplyInsets(content);
        }
    }

    public static void applyDrawerInsets(View spacer, View drawerContent) {
        if (spacer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(spacer, (view, insets) -> {
                Insets status = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = status.top;
                view.setLayoutParams(params);
                return insets;
            });
            ViewCompat.requestApplyInsets(spacer);
        }
        if (drawerContent != null)
            applyToolbarInsets(null, drawerContent);
    }
}
