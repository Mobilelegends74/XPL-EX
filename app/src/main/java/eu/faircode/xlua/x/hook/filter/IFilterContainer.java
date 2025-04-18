package eu.faircode.xlua.x.hook.filter;

import java.util.List;
import java.util.Map;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;

public interface IFilterContainer {
    String getGroupName();
    boolean hasSwallowedAsRule(XHook hook);
    boolean hasSwallowedAsDefinition(XHook hook);
    void initializeDefinitions(List<XHook> hooks, Map<String, String> settings);


    List<String> getDependencies();
    List<XHook> getRules();
    List<XHook> getFilterBases();

    boolean hasSettings();

    int appendSettings(Map<String, String> settings);
}
