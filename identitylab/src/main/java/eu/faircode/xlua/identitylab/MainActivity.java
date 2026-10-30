package eu.faircode.xlua.identitylab;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * A standalone test app. Do not add it to LSPosed scope.
 */
public final class MainActivity extends Activity {
    private TextView output;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (18 * getResources().getDisplayMetrics().density);
        layout.setPadding(p, p, p, p);

        TextView heading = new TextView(this);
        heading.setText("XPL-EX Settings API Lab");
        heading.setTextSize(22);
        layout.addView(heading);

        Button refresh = new Button(this);
        refresh.setText("Refresh readings");
        layout.addView(refresh);

        output = new TextView(this);
        output.setTextSize(16);
        output.setTextIsSelectable(true);
        layout.addView(output);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        setContentView(scroll);

        refresh.setOnClickListener(v -> readValues());
        readValues();
    }

    private void readValues() {
        String sdk;
        String direct;
        try {
            sdk = String.valueOf(Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID));
        } catch (Throwable t) {
            sdk = "ERROR: " + t.getClass().getSimpleName();
        }

        try {
            Bundle response = getContentResolver().call(
                    Settings.Secure.CONTENT_URI,
                    "GET_secure", "android_id", null);
            direct = response == null ? "<null>" :
                    String.valueOf(response.getString("value"));
        } catch (Throwable t) {
            direct = "ERROR: " + t.getClass().getSimpleName();
        }

        output.setText("Package: " + getPackageName()
                + "\nUID: " + Process.myUid()
                + "\n\nSettings.Secure.getString: " + sdk
                + "\nSettingsProvider.call: " + direct
                + "\nBuild.MODEL (control): " + Build.MODEL
                + "\n\nLab demonstration ID: 1122334455667788"
                + "\n\nDo not include IdentityLab in LSPosed scope."
                + "\nEnable only System Framework for XPL-EX."
                + "\nRestart app after changing LSPosed settings.");
    }
}
