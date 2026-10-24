package eu.faircode.xlua.xposed.api101;

import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Accessed only on the scope worker. Keeps assignments made before service delivery. */
final class ScopeRequestQueue {
    private final Set<String> pending = new LinkedHashSet<>();
    private final Set<String> inFlight = new HashSet<>();
    private final Set<String> removals = new LinkedHashSet<>();
    private final Map<String, Boolean> desired = new HashMap<>();

    void add(String packageName) {
        if (packageName == null || packageName.isEmpty() || "global".equals(packageName))
            return;
        String scope = "android".equals(packageName) ? "system" : packageName;
        desired.put(scope, true);
        removals.remove(scope);
        if (!inFlight.contains(scope))
            pending.add(scope);
    }

    void remove(String packageName) {
        // System Framework keeps the settings/database bridge available even
        // when it has no user-assigned hooks.
        if (packageName == null || packageName.isEmpty() || "global".equals(packageName)
                || "android".equals(packageName) || "system".equals(packageName))
            return;
        desired.put(packageName, false);
        pending.remove(packageName);
        removals.add(packageName);
    }

    Set<String> takeRemovals() {
        Set<String> request = new LinkedHashSet<>(removals);
        removals.clear();
        return request;
    }

    void retryRemovals(Set<String> request) {
        removals.addAll(request);
    }

    Set<String> take(Set<String> currentScope) {
        pending.removeAll(currentScope);
        Set<String> request = new LinkedHashSet<>(pending);
        pending.clear();
        inFlight.addAll(request);
        return request;
    }

    void complete(Set<String> request) {
        inFlight.removeAll(request);
        // Approval can arrive after the last hook was disabled. Remove the
        // newly granted scope again rather than letting that late result win.
        for (String scope : request)
            if (Boolean.FALSE.equals(desired.get(scope)))
                removals.add(scope);
    }

    void disconnected() {
        for (String scope : inFlight) {
            if (Boolean.FALSE.equals(desired.get(scope)))
                removals.add(scope);
            else
                pending.add(scope);
        }
        inFlight.clear();
    }
}
