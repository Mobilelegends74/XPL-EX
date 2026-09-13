package eu.faircode.xlua;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Checks the project's latest GitHub release and offers the newer module APK. */
public final class ModuleUpdateChecker {
    static final String LATEST_RELEASE_API =
            "https://api.github.com/repos/Mobilelegends74/XPL-EX/releases/latest";
    private static final String TAG = "XPL-EX.Update";
    private static final String PREF_LAST_CHECK = "module_update_last_check";
    private static final long CHECK_INTERVAL_MS = 12L * 60L * 60L * 1000L;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private ModuleUpdateChecker() { }

    public static void check(Activity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        long now = System.currentTimeMillis();
        if(now - preferences.getLong(PREF_LAST_CHECK, 0L) < CHECK_INTERVAL_MS)
            return;
        preferences.edit().putLong(PREF_LAST_CHECK, now).apply();

        EXECUTOR.execute(() -> {
            try {
                RemoteRelease release = loadLatestRelease();
                if(release == null || compareVersions(release.versionName, BuildConfig.VERSION_NAME) <= 0)
                    return;
                activity.runOnUiThread(() -> showUpdate(activity, release));
            } catch (Throwable e) {
                Log.w(TAG, "Unable to check for module updates", e);
            }
        });
    }

    private static RemoteRelease loadLatestRelease() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(LATEST_RELEASE_API).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("User-Agent", "XPL-EX-NEXT/" + BuildConfig.VERSION_NAME);
        try {
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;
            StringBuilder json = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while((line = reader.readLine()) != null)
                    json.append(line);
            }
            return parseRelease(json.toString());
        } finally {
            connection.disconnect();
        }
    }

    static RemoteRelease parseRelease(String json) throws Exception {
        JSONObject object = new JSONObject(json);
        String tag = object.optString("tag_name", "");
        if(!tag.startsWith("XPL-EX-NEXT-v"))
            return null;

        String version = tag.substring("XPL-EX-NEXT-v".length());
        String updateUrl = object.optString("html_url", "");
        JSONArray assets = object.optJSONArray("assets");
        if(assets != null) {
            for(int index = 0; index < assets.length(); index++) {
                JSONObject asset = assets.optJSONObject(index);
                if(asset == null || !asset.optString("name", "").endsWith(".apk"))
                    continue;
                String apkUrl = asset.optString("browser_download_url", "");
                if(!apkUrl.isEmpty()) {
                    updateUrl = apkUrl;
                    break;
                }
            }
        }
        return updateUrl.isEmpty() ? null : new RemoteRelease(version, updateUrl);
    }

    static int compareVersions(String left, String right) {
        int[] first = parseVersion(left);
        int[] second = parseVersion(right);
        for(int index = 0; index < Math.max(first.length, second.length); index++) {
            int a = index < first.length ? first[index] : 0;
            int b = index < second.length ? second[index] : 0;
            if(a != b)
                return Integer.compare(a, b);
        }
        return 0;
    }

    private static int[] parseVersion(String version) {
        String[] components = version == null ? new String[0] : version.split("\\.");
        int[] result = new int[components.length];
        for(int index = 0; index < components.length; index++) {
            String digits = components[index].replaceFirst("^(\\d+).*$", "$1");
            try {
                result[index] = Integer.parseInt(digits);
            } catch (NumberFormatException ignored) {
                result[index] = 0;
            }
        }
        return result;
    }

    private static void showUpdate(Activity activity, RemoteRelease release) {
        if(activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && activity.isDestroyed()))
            return;
        new AlertDialog.Builder(activity)
                .setTitle(R.string.module_update_available_title)
                .setMessage(activity.getString(
                        R.string.module_update_available_message,
                        release.versionName))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.option_update, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(release.updateUrl));
                    if(intent.resolveActivity(activity.getPackageManager()) != null)
                        activity.startActivity(intent);
                })
                .show();
    }

    static final class RemoteRelease {
        final String versionName;
        final String updateUrl;

        RemoteRelease(String versionName, String updateUrl) {
            this.versionName = versionName;
            this.updateUrl = updateUrl;
        }
    }
}
