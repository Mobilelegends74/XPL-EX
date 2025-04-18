package eu.faircode.xlua.x.hook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Member;
import java.util.List;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;

public class HookDefinition {
    public XHook blob;
    public Class<?> resolvedClazz;

    public Member member;

    protected String getName() { return ""; }

    public String getBlobId() { return blob == null ? null : blob.getObjectId(); }

    public String getClassName() { return resolvedClazz == null ? null : resolvedClazz.getName(); }

    protected void setAccessible(boolean a) { }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof HookDefinition))
            return false;

        HookDefinition otherBase = (HookDefinition) obj;
        if((this instanceof HookDefinitionField && obj instanceof HookDefinitionField) ||
                (this instanceof HookDefinitionAll && obj instanceof HookDefinitionAll) ||
                (this instanceof HookDefinitionMember && obj instanceof HookDefinitionMember))
            return Str.areEqualIgnoreCase(getName(), otherBase.getName()) &&
                    Str.areEqualIgnoreCase(getClassName(), otherBase.getClassName()) &&
                    Str.areEqualIgnoreCase(getBlobId(), otherBase.getBlobId());

        return false;
    }

    @NonNull
    @Override
    public String toString() { return
            "Class=" + Str.toStringOrNull(resolvedClazz) +
                    " Name=" + getName() +
                    " Hook=" + (blob == null ? "null" : blob.getObjectId()) +
                    " This=" + this.getClass().getName(); }
}
