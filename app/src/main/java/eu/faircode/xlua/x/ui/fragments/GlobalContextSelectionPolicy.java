package eu.faircode.xlua.x.ui.fragments;

import java.util.List;

/** Decides whether hook-derived defaults may extend the checked settings list. */
public final class GlobalContextSelectionPolicy {
    private GlobalContextSelectionPolicy() {
    }

    public static boolean hasUserDefinedTemplate(List<String> globalChecked) {
        return globalChecked != null && !globalChecked.isEmpty();
    }

    public static boolean shouldAutoSelectAssignedSettings(
            boolean isGlobalContext,
            boolean hasUserDefinedTemplate) {
        return !isGlobalContext && !hasUserDefinedTemplate;
    }
}
