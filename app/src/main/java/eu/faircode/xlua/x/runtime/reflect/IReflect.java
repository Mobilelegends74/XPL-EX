package eu.faircode.xlua.x.runtime.reflect;

import eu.faircode.xlua.x.data.interfaces.IValidator;

public interface IReflect extends IValidator {
    String getName();
    DynClass getClazz();
    void setAccessible(boolean accessible);
    Exception getLastException();
    boolean wasSuccessful();
}
