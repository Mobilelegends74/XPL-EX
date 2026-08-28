package eu.faircode.xlua;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ActivityCpu extends ActivityBase {
    private static final String TAG = "XLua.ActivityCpu";
    private FragmentCpu fragmentCpu;
    private Menu menu;

    @Override
    protected boolean useCustomToolbar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cpuview);
        setupCustomToolbar(R.id.toolbar_cpu, R.id.app_bar_layout_cpu,
                R.id.content_frame_cpu, 0);

        //Show Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentCpu = new FragmentCpu();

        Log.i(TAG, "Created Fragment, now replacing...");

        fragmentTransaction.replace(R.id.content_frame_cpu, fragmentCpu);
        fragmentTransaction.commit();

        Log.i(TAG, "Finished Creating Fragment/Activity");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        //this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Selected option " + item.getTitle());
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            //break;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
