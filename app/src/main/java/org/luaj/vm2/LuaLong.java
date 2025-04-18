package org.luaj.vm2;

import org.luaj.vm2.lib.TwoArgFunction;

public class LuaLong extends LuaValue {

    public final long value;

    public LuaLong(long value) {
        this.value = value;
    }

    @Override
    public int type() {
        return LuaValue.TNUMBER;
    }

    @Override
    public String typename() {
        return "long";
    }

    @Override
    public boolean isnumber() {
        return true;
    }

    @Override
    public boolean islong() {
        return true;
    }

    @Override
    public long tolong() {
        return value;
    }

    @Override
    public int toint() {
        return (int) value;
    }

    @Override
    public double todouble() {
        return (double) value;
    }

    @Override
    public LuaValue tonumber() {
        return this;
    }

    @Override
    public boolean isLong() { return true; }

    @Override
    public LuaValue add(LuaValue rhs) {
        return LuaValue.valueOf(this.value + rhs.tolong());
    }

    @Override
    public LuaValue sub(LuaValue rhs) {
        return LuaValue.valueOf(this.value - rhs.tolong());
    }

    @Override
    public LuaValue mul(LuaValue rhs) {
        return LuaValue.valueOf(this.value * rhs.tolong());
    }

    @Override
    public LuaValue div(LuaValue rhs) {
        return LuaValue.valueOf(this.value / rhs.tolong());
    }

    @Override
    public LuaValue mod(LuaValue rhs) {
        return LuaValue.valueOf(this.value % rhs.tolong());
    }

    @Override
    public LuaValue neg() {
        return LuaValue.valueOf(-this.value);
    }

    @Override
    public boolean eq_b(LuaValue val) {
        return val.islong() && this.value == val.tolong();
    }

    @Override
    public boolean lt_b(LuaValue val) {
        return this.value < val.tolong();
    }

    @Override
    public boolean lteq_b(LuaValue val) {
        return this.value <= val.tolong();
    }

    @Override
    public String tojstring() {
        return Long.toString(value);
    }

    @Override
    public LuaValue tostring() {
        return LuaString.valueOf(tojstring());
    }

    @Override
    public LuaString optstring(LuaString defval) {
        return LuaString.valueOf(Long.toString(value));
    }

    @Override
    public String optjstring(String defval) {
        return Long.toString(value);
    }

    @Override
    public LuaString strvalue() {
        return LuaString.valueOf(Long.toString(value));
    }

    @Override
    public String checkjstring() {
        return String.valueOf(value);
    }

    @Override
    public LuaString checkstring() {
        return valueOf(String.valueOf(value));
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public LuaValue concat(LuaValue rhs)      {
        //return LuaValue.valueOf(this.tojstring().concat(rhs.tojstring()));
        return rhs.concatTo(this);
    }

    @Override
    public Buffer   concat(Buffer rhs)        { return rhs.concatTo(this); }

    @Override
    public LuaValue concatTo(LuaNumber lhs)   { return strvalue().concatTo(lhs.strvalue()); }

    @Override
    public LuaValue concatTo(LuaString lhs)   { return strvalue().concatTo(lhs); }

    @Override
    public LuaValue metatag(LuaValue tag) {
        if (tag == LuaValue.CONCAT) {
            return new TwoArgFunction() {
                @Override
                public LuaValue call(LuaValue a, LuaValue b) {
                    return LuaValue.valueOf(a.tojstring() + b.tojstring());
                }
            };
        }
        return super.metatag(tag);
    }


    @Override
    public boolean equals(Object o) {
        return (o instanceof LuaLong) && ((LuaLong) o).value == value;
    }
}
