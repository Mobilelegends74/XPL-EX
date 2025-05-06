package eu.faircode.xlua.x.runtime.reflect;

import eu.faircode.xlua.x.file.ModePermission;

public enum TypeBind {
    NONE(0),
    SHORT(1),
    INTEGER(2),
    LONG(3),
    BYTE(4),
    CHAR(5),
    DOUBLE(6),
    BOOLEAN(7),
    FLOAT(8),
    STRING(9),
    CHAR_SEQUENCE(10);

    private final int value;

    TypeBind(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
