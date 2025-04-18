/*******************************************************************************
 * Copyright (c) 2009-2011 Luaj.org. All rights reserved.
 *
 * [License block unchanged]
 ******************************************************************************/
package org.luaj.vm2.lib.jse;

import android.util.Log;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaLong;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

/**
 * [Class docstring unchanged]
 */
public class CoerceJavaToLua {

	static interface Coercion {
		LuaValue coerce(Object javaValue);
	}

	private static final class BoolCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			Boolean b = (Boolean) javaValue;
			return b.booleanValue() ? LuaValue.TRUE : LuaValue.FALSE;
		}
	}

	private static final class IntCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			Number n = (Number) javaValue;
			return LuaInteger.valueOf(n.intValue());
		}
	}

	private static final class LongCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			return new LuaLong((Long) javaValue);
		}
	}

	private static final class CharCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			Character c = (Character) javaValue;
			return LuaInteger.valueOf(c.charValue());
		}
	}

	private static final class DoubleCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			Number n = (Number) javaValue;
			return LuaDouble.valueOf(n.doubleValue());
		}
	}

	private static final class StringCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			return LuaString.valueOf(javaValue.toString());
		}
	}

	private static final class BytesCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			return LuaValue.userdataOf(javaValue); // Preserve raw byte[]
		}
	}

	private static final class ClassCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			return JavaClass.forClass((Class) javaValue);
		}
	}

	private static final class InstanceCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			return new JavaInstance(javaValue);
		}
	}

	private static final class ArrayCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			return new JavaArray(javaValue);
		}
	}

	private static final class LuaCoercion implements Coercion {
		public LuaValue coerce(Object javaValue) {
			return (LuaValue) javaValue;
		}
	}

	static final Map COERCIONS = Collections.synchronizedMap(new HashMap());

	static {
		Coercion boolCoercion = new BoolCoercion();
		Coercion intCoercion = new IntCoercion();
		Coercion charCoercion = new CharCoercion();
		Coercion doubleCoercion = new DoubleCoercion();
		Coercion stringCoercion = new StringCoercion();
		Coercion bytesCoercion = new BytesCoercion();
		Coercion classCoercion = new ClassCoercion();
		Coercion longCoercion = new LongCoercion();

		COERCIONS.put(Boolean.class, boolCoercion);
		COERCIONS.put(Byte.class, intCoercion);
		COERCIONS.put(Character.class, charCoercion);
		COERCIONS.put(Short.class, intCoercion);
		COERCIONS.put(Integer.class, intCoercion);
		COERCIONS.put(Float.class, doubleCoercion);
		COERCIONS.put(Double.class, doubleCoercion);
		COERCIONS.put(String.class, stringCoercion);
		COERCIONS.put(byte[].class, bytesCoercion);
		COERCIONS.put(Class.class, classCoercion);
		COERCIONS.put(Long.class, longCoercion);
		COERCIONS.put(long.class, longCoercion);
	}

	public static LuaValue coerce(Object o) {
		if (o == null)
			return LuaValue.NIL;

		Class clazz = o.getClass();
		Coercion c = (Coercion) COERCIONS.get(clazz);

		if (c != null) {
			return c.coerce(o);
		}

		// ✅ Fix fallback for unregistered Number types like boxed Long, BigDecimal, etc.
		if (o instanceof Long) {
			return new LuaLong((Long) o);
		}

		if (o instanceof Number) {
			Number n = (Number) o;
			if (n.longValue() == n.doubleValue()) {
				return LuaValue.valueOf(n.longValue());
			} else {
				return LuaValue.valueOf(n.doubleValue());
			}
		}

		if (clazz.isArray()) {
			return arrayCoercion.coerce(o);
		}

		if (o instanceof LuaValue) {
			return luaCoercion.coerce(o);
		}

		return instanceCoercion.coerce(o);
	}

	static final Coercion instanceCoercion = new InstanceCoercion();
	static final Coercion arrayCoercion = new ArrayCoercion();
	static final Coercion luaCoercion = new LuaCoercion();
}
