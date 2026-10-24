package eu.faircode.xlua.xposed.api101;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.api.XProxyContent;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

/** Module-app side only. All policy and Binder operations run on WORKER. */
public final class LsposedScope {
    private static final String TAG = "XLua.Scope";
    private static final ScheduledExecutorService WORKER = Executors.newSingleThreadScheduledExecutor();
    private static final ScopeRequestQueue QUEUE = new ScopeRequestQueue();
    private static final Map<String, Long> REQUESTS = new HashMap<>();
    private static XposedService service;
    private static Context application;
    private static long sequence;
    private static boolean initialized;
    private LsposedScope() { }

    public static synchronized void initialize() {
        if (initialized) return;
        try {
            XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
                @Override public void onServiceBind(XposedService bound) {
                    WORKER.execute(() -> {
                        if (service != bound) {
                            QUEUE.disconnected();
                            REQUESTS.clear();
                        }
                        service = bound;
                        diagnostic("service bound");
                        flush();
                    });
                }
                @Override public void onServiceDied(XposedService dead) {
                    WORKER.execute(() -> {
                        if (service == dead) {
                            service = null;
                            QUEUE.disconnected();
                            REQUESTS.clear();
                            diagnostic("service disconnected");
                        }
                    });
                }
            });
            initialized = true;
        } catch (RuntimeException e) {
            Log.w(TAG, "Could not register LSPosed service listener", e);
        }
    }

    private static boolean isModule(Context context) {
        return context != null && BuildConfig.APPLICATION_ID.equals(context.getPackageName());
    }

    /** Observe live state on resume, without overriding a manual scope removal. */
    public static void refresh(Context context) {
        if (!isModule(context)) return;
        initialize();
        Context app = context.getApplicationContext();
        WORKER.execute(() -> {
            application = app;
            flush();
        });
    }

    public static void synchronize(Context context, int uid, String packageName) {
        if (!isModule(context) || uid < 0 || uid / 100000 != Process.myUid() / 100000) return;
        initialize();
        Context app = context.getApplicationContext();
        WORKER.execute(() -> {
            application = app;
            try (Cursor assignments = XProxyContent.luaQuery(application, "getAssignments",
                    new String[]{String.valueOf(uid), packageName})) {
                if (assignments == null) {
                    diagnostic("assignment query unavailable package=" + packageName);
                    return; // Unknown is not zero: never remove scope on query failure.
                }
                int count = assignments.getCount();
                diagnostic("assignments package=" + packageName + " uid=" + uid + " count=" + count);
                if (count > 0) QUEUE.add(packageName);
                else QUEUE.remove(packageName);
                flush();
            } catch (RuntimeException e) {
                diagnostic("synchronize failed package=" + packageName + " error=" + e);
            }
        });
    }

    private static void flush() {
        if (service == null) {
            diagnostic("scope queued: service unavailable");
            return;
        }
        Set<String> request = new HashSet<>();
        try {
            Set<String> current = new HashSet<>(service.getScope());
            diagnostic("scope observed=" + current);
            QUEUE.reconcile(current);
            Set<String> removals = QUEUE.takeRemovals();
            removals.retainAll(current);
            if (!removals.isEmpty()) {
                try {
                    service.removeScope(new ArrayList<>(removals));
                    current = new HashSet<>(service.getScope());
                    Set<String> remaining = new HashSet<>(removals);
                    remaining.retainAll(current);
                    QUEUE.retryRemovals(remaining);
                    diagnostic("remove requested=" + removals + " stillPresent=" + remaining);
                } catch (RuntimeException e) {
                    QUEUE.retryRemovals(removals);
                    diagnostic("remove failed=" + removals + " error=" + e);
                    return;
                }
            }
            request.addAll(QUEUE.take(current));
            if (request.isEmpty()) return;
            Set<String> submitted = new HashSet<>(request);
            XposedService submittingService = service;
            long ticket = ++sequence;
            for (String scope : submitted) REQUESTS.put(scope, ticket);
            diagnostic("add requested=" + submitted + " ticket=" + ticket);
            service.requestScope(new ArrayList<>(submitted), new XposedService.OnScopeEventListener() {
                @Override public void onScopeRequestApproved(List<String> approved) {
                    WORKER.execute(() -> finish(submittingService, submitted, ticket, "approved=" + approved));
                }
                @Override public void onScopeRequestFailed(String message) {
                    WORKER.execute(() -> finish(submittingService, submitted, ticket, "failed=" + message));
                }
            });
            // Missing callbacks must not block later explicit retries. No auto-repeat.
            WORKER.schedule(() -> finish(submittingService, submitted, ticket, "callback timeout"),
                    45, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            QUEUE.complete(request);
            for (String scope : request) REQUESTS.remove(scope);
            diagnostic("scope operation failed=" + request + " error=" + e);
        }
    }

    private static void finish(XposedService origin, Set<String> submitted, long ticket, String outcome) {
        if (service != origin) return;
        Set<String> matching = new HashSet<>();
        for (String scope : submitted)
            if (Long.valueOf(ticket).equals(REQUESTS.get(scope))) {
                matching.add(scope);
                REQUESTS.remove(scope);
            }
        if (matching.isEmpty()) {
            // A late approval must still be removed if the last hook was disabled.
            if (!outcome.equals("callback timeout")) flush();
            return;
        }
        QUEUE.complete(matching);
        diagnostic("request result ticket=" + ticket + " packages=" + matching + " " + outcome);
        flush();
    }

    private static void diagnostic(String message) {
        Log.i(TAG, message);
        if (application == null) return;
        Bundle data = new Bundle();
        data.putString("message", message);
        XProxyContent.luaCall(application, "scopeDiagnostic", data);
    }
}
