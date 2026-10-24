package eu.faircode.xlua.xposed.api101;

import android.content.Context;
import android.database.Cursor;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

/** Module-app side only: the settings bridge executes in system_server. */
public final class LsposedScope {
    private static final String TAG = "XLua.Scope";
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private static final ScopeRequestQueue QUEUE = new ScopeRequestQueue();
    private static XposedService service;
    private static boolean initialized;

    private LsposedScope() { }

    public static synchronized void initialize() {
        if (initialized)
            return;
        initialized = true;
        XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
            @Override
            public void onServiceBind(XposedService bound) {
                WORKER.execute(() -> {
                    if (service != null && service != bound)
                        QUEUE.disconnected();
                    service = bound;
                    flush();
                });
            }

            @Override
            public void onServiceDied(XposedService dead) {
                WORKER.execute(() -> {
                    if (service == dead) {
                        service = null;
                        QUEUE.disconnected();
                    }
                });
            }
        });
    }

    public static void synchronize(Context context, int uid, String packageName) {
        // Service requests affect the calling Android user, not an arbitrary profile.
        if (!BuildConfig.APPLICATION_ID.equals(context.getPackageName())
                || uid < 0 || uid / 100000 != Process.myUid() / 100000)
            return;
        initialize();
        Context application = context.getApplicationContext();
        WORKER.execute(() -> {
            // Read the saved assignments, not the checkbox or the deletion
            // packet: deleting one hook must retain scope if others remain.
            try (Cursor assignments = XProxyContent.luaQuery(application, "getAssignments",
                    UserIdentity.createSnakeQueryUID(uid, packageName))) {
                if (assignments == null) {
                    Log.w(TAG, "Could not read assignments for " + packageName);
                    return;
                }
                if (assignments.getCount() > 0)
                    QUEUE.add(packageName);
                else
                    QUEUE.remove(packageName);
                flush();
            } catch (RuntimeException e) {
                Log.w(TAG, "Could not synchronize LSPosed scope for " + packageName, e);
            }
        });
    }

    private static void flush() {
        if (service == null)
            return;
        Set<String> removals = QUEUE.takeRemovals();
        if (!removals.isEmpty()) {
            try {
                service.removeScope(new ArrayList<>(removals));
                Log.i(TAG, "Scope removed: " + removals);
            } catch (RuntimeException e) {
                QUEUE.retryRemovals(removals);
                Log.w(TAG, "Could not remove LSPosed scope", e);
                return;
            }
        }
        Set<String> request = new HashSet<>();
        try {
            request.addAll(QUEUE.take(new HashSet<>(service.getScope())));
            if (request.isEmpty())
                return;
            Set<String> submitted = new HashSet<>(request);
            XposedService submittingService = service;
            service.requestScope(new ArrayList<>(request), new XposedService.OnScopeEventListener() {
                @Override
                public void onScopeRequestApproved(List<String> approved) {
                    WORKER.execute(() -> {
                        if (service == submittingService) {
                            QUEUE.complete(submitted);
                            flush();
                        }
                        Log.i(TAG, "Scope approved: " + approved);
                    });
                }

                @Override
                public void onScopeRequestFailed(String message) {
                    WORKER.execute(() -> {
                        if (service == submittingService) {
                            QUEUE.complete(submitted);
                            flush();
                        }
                        Log.w(TAG, "Scope request failed: " + message);
                    });
                }
            });
        } catch (RuntimeException e) {
            // Scope failure must not turn a successful hook assignment into a failure.
            QUEUE.complete(request);
            for (String scope : request)
                QUEUE.add(scope);
            Log.w(TAG, "Could not request LSPosed scope", e);
        }
    }
}
