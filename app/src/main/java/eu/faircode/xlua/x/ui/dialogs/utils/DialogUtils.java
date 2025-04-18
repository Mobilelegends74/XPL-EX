package eu.faircode.xlua.x.ui.dialogs.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.TryRun;

public class DialogUtils {


    public static void snack_bar_format(final View v, final int resId, final Object... inserts) {
        if(v != null && resId > 0) {
            TryRun.onMain(() -> {
                Snackbar.make(v, Str.fm(v.getContext().getString(resId), inserts), Snackbar.LENGTH_LONG).show();
            });
        }
    }

    public static void snack_bar(View v, int resId) {
        if(v != null && resId > 0) {
            TryRun.onMain(() -> {
                Snackbar.make(v, v.getContext().getString(resId), Snackbar.LENGTH_LONG).show();
            });
        }
    }

    public static void snack_bar(View v, String message) {
        if(v != null) {
            TryRun.onMain(() -> {
                Snackbar.make(v, Str.toStringOrNull(message), Snackbar.LENGTH_LONG).show();
            });
        }
    }

    public static void showMessage(Context context, String message) {
        if(context != null) {
            TryRun.onMain(() -> {
                new AlertDialog.Builder(context)
                        .setMessage(message)
                        .setPositiveButton(R.string.option_ok, null)
                        .show();
            });
        }
    }
}
