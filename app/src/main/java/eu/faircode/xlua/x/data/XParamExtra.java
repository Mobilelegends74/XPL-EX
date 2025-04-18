package eu.faircode.xlua.x.data;

import android.os.Looper;
import android.os.Process;
import android.util.Base64;
import android.util.Log;

import org.luaj.vm2.attributes.NonInvokable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import eu.faircode.xlua.R;
import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.FileUtil;
import eu.faircode.xlua.utilities.ReflectUtilEx;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.StrConversionUtils;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomStringKind;
import eu.faircode.xlua.x.runtime.HiddenApi;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;
import eu.faircode.xlua.x.xlua.LibUtil;

/*
    This is the Layer that is for Logging Data
    This will or can Require String related Functions so it will Inherit XParamUtils

    Add some Attributes or Something that make certain functions not invokable
 */

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class XParamExtra {
    private static final String TAG = LibUtil.generateTag(XParamExtra.class);

    private String _logOldResult = "";
    private String _logNewResult = "";
    private String _logExtra = "";
    private boolean __do_logging = true;
    private final List<String> _lastSettingsCache = new ArrayList<>();

    @NonInvokable
    @SuppressWarnings("unused")
    public void setLoggingFlag(boolean shouldLog) { this.__do_logging = shouldLog; }
    @SuppressWarnings("unused")
    public boolean getLoggingFlag() { return this.__do_logging; }

    @SuppressWarnings("unused")
    public void setLastSetting(String lastSetting) { if(!Str.isEmpty(lastSetting)) _lastSettingsCache.add(lastSetting); }
    @SuppressWarnings("unused")
    public void setLastSetting(String lastSetting, boolean flag) { if(flag && !Str.isEmpty(lastSetting)) _lastSettingsCache.add(lastSetting); }
    @SuppressWarnings("unused")
    public String getLastSetting() { return ListUtil.getLast(_lastSettingsCache, Str.EMPTY); }

    @SuppressWarnings("unused")
    public void setLogOld(String oldResult) { if(__do_logging) this._logOldResult = oldResult; }
    @SuppressWarnings("unused")
    public String getLogOld() { return this._logOldResult; }
    @SuppressWarnings("unused")
    public void setLogNew(String newResult) { if(__do_logging) this._logNewResult = newResult; }
    @SuppressWarnings("unused")
    public String getLogNew() { return this._logNewResult; }
    @SuppressWarnings("unused")
    public void setLogExtra(String settingResult) { if(__do_logging) this._logExtra = settingResult; }
    @SuppressWarnings("unused")
    public String getLogExtra() { return "Setting:" + Str.toStringOrNull(this._logExtra); } //ToDO: Change so its not Constrained as "Setting:"

    @SuppressWarnings("unused")
    public String safe(Object o) { return o == null ? "null" : Str.toStringOrNull(o); }

    @SuppressWarnings("unused")
    public StackTraceElement[] getStackTrace() { return RuntimeUtils.getStackTraceSafe(new Exception()); }
    @SuppressWarnings("unused")
    public StackTraceElement[] getStackTrace(Throwable t) { return RuntimeUtils.getStackTraceSafe(t); }
    @SuppressWarnings("unused")
    public String getStackTraceString() { return RuntimeUtils.getStackTraceSafeString(new Exception()); }
    @SuppressWarnings("unused")
    public String getStackTraceString(Throwable t) { return RuntimeUtils.getStackTraceSafeString(t); }
    @SuppressWarnings("unused")
    public void printStack() { Log.w(TAG, RuntimeUtils.getStackTraceSafeString(new Throwable())); }
    @SuppressWarnings("unused")
    public void printFileContents(String filePath) { FileUtil.printContents(filePath); }

    @SuppressWarnings("unused")
    public Throwable createException() { return new Exception();}
    @SuppressWarnings("unused")
    public Throwable createException(String msg) { return !Str.isValid(msg) ? new Exception() : new Exception(msg); }
    @SuppressWarnings("unused")
    public void throwException() throws Exception { throw new Exception(); }
    @SuppressWarnings("unused")
    public void throwException(String msg) throws Exception { throw new Exception(Str.getNonNullString(msg, Str.EMPTY)); }


    @SuppressWarnings("unused")
    public static boolean isNumericString(String s) { return Str.isNumeric(s); }
    @SuppressWarnings("unused")
    public boolean stringContains(String s, String containing) { return s != null && containing != null && s.contains(containing); }
    @SuppressWarnings("unused")
    public boolean stringStartsWith(String s, String startsWith) { return s != null && startsWith != null && s.startsWith(startsWith); }
    @SuppressWarnings("unused")
    public boolean stringEndsWith(String s, String endsWith) { return s != null && endsWith != null && s.endsWith(endsWith); }
    @SuppressWarnings("unused")
    public int stringLength(String s) { return s == null ? -1 : s.length(); }
    @SuppressWarnings("unused")
    public boolean stringIsValid(String s) { return s != null && !s.isEmpty(); }
    @SuppressWarnings("unused")
    public boolean stringIsNull(String s) { return  s == null; }
    @SuppressWarnings("unused")
    public boolean stringIsEmpty(String s) { return !stringIsValid(s); }
    @SuppressWarnings("unused")
    public String stringReplaceAll(String s, String regex, String replaceWith) { return !stringIsValid(s) || regex == null || replaceWith == null ? s : s.replaceAll(regex, replaceWith); }
    @SuppressWarnings("unused")
    public String stringTrim(String s) { return !stringIsValid(s) ? s.trim() : s; }

    @SuppressWarnings("unused")
    public String stringSubString(String s, int startIndexInclusive, int endIndexExclusive) { return Str.subString(s, startIndexInclusive, endIndexExclusive); }
    @SuppressWarnings("unused")
    public String stringSubString(String s, int startIndexInclusive) { return Str.subString(s, startIndexInclusive);   }

    @SuppressWarnings("unused")
    public String stringToLowerCase(String s) { return s == null ? null : s.toLowerCase(); }
    @SuppressWarnings("unused")
    public String stringToUpperCase(String s) { return  s == null ? null : s.toUpperCase(); }

    @SuppressWarnings("unused")
    public char[] stringToCharArray(String s) { return s == null ? null : s.toCharArray(); }
    @SuppressWarnings("unused")
    public String charArrayToString(char[] chars) { return chars == null ? null : new String(chars); }
    @SuppressWarnings("unused")
    public char stringCharAt(String s, int index) { return Str.charAt(s, index); }
    @SuppressWarnings("unused")
    public char stringFirstChar(String s) { return Str.charAt(s, 0); }
    @SuppressWarnings("unused")
    public char stringLastChar(String s) { return Str.charAt(s, Str.length(s) - 1); }
    @SuppressWarnings("unused")
    public String charToString(Character c) { return c == null ? null : Character.toString(c); }
    @SuppressWarnings("unused")
    public boolean stringHasNumbersChars(String s) { return Str.hasNumericChars(s); }
    @SuppressWarnings("unused")
    public boolean stringHasAlphabeticChars(String s) { return Str.hasAlphabeticChars(s); }

    @SuppressWarnings("unused")
    public byte[] stringToBytes(String s) { return s == null ? null : s.getBytes(StandardCharsets.UTF_8); }
    @SuppressWarnings("unused")
    public byte[] stringToBytesUnicode(String s) { return s == null ? null : s.getBytes(StandardCharsets.UTF_16); }
    @SuppressWarnings("unused")
    public byte[] stringToBytes(String s, String encoding) { return s == null ? null : TryRun.getOrDefault(() -> s.getBytes(encoding), stringToBytes(s)); }
    @SuppressWarnings("unused")
    public byte[] stringToRawBytes(String s) { return s == null ? null : Str.stringToRawBytes(s); }


    @SuppressWarnings("unused")
    public byte[] stringBase64ToBytes(String s) { return s == null ? null : TryRun.get(() -> Base64.decode(s, Base64.DEFAULT)); }
    @SuppressWarnings("unused")
    public String bytesToString(byte[] bytes) { return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8); }
    @SuppressWarnings("unused")
    public String bytesToStringUnicode(byte[] bytes) {  return bytes == null ? null : new String(bytes, StandardCharsets.UTF_16); }
    @SuppressWarnings("unused")
    public String bytesToString(byte[] bytes, String encoding) { return bytes == null ? null :  TryRun.getOrDefault(() -> new String(bytes, encoding), bytesToString(bytes)); }
    @SuppressWarnings("unused")
    public String bytesToHexString(byte[] bytes) { return bytes == null ? null : Str.bytesToHexString(bytes, true); }
    @SuppressWarnings("unused")
    public String bytesToHexString(byte[] bytes, boolean addSpaces) { return bytes == null ? null : Str.bytesToHexString(bytes, addSpaces); }
    @SuppressWarnings("unused")
    public String bytesToBase64String(byte[] bytes) { return bytes == null ? null : TryRun.get(() -> Base64.encodeToString(bytes, Base64.DEFAULT)); }
    @SuppressWarnings("unused")
    public String stringToBase64String(String s) { return s == null ? null : bytesToBase64String(stringToBytes(s)); }
    @SuppressWarnings("unused")
    public String stringToBase64String(String s, String encoding) { return  s == null ? null : bytesToBase64String(stringToBytes(s, encoding)); }

    @SuppressWarnings("unused")
    public String[] stringSplitToArray(String s) { return s == null ? null : Str.split(s, Str.COMMA, true, false); }
    @SuppressWarnings("unused")
    public String[] stringSplitToArray(String s, String delimiter) { return s == null ? null : Str.split(s, delimiter, true, false); }
    @SuppressWarnings("unused")
    public String joinStringArray(String[] array) { return array == null ? Str.EMPTY : Str.joinArray(array, Str.COMMA); }
    @SuppressWarnings("unused")
    public String joinStringArray(String[] array, String delimiter) { return array == null ? Str.EMPTY : Str.joinArray(array, delimiter); }
    @SuppressWarnings("unused")
    public Collection<String> stringSplitToList(String s) { return Str.splitToList(s, Str.COMMA); }
    @SuppressWarnings("unused")
    public Collection<String> stringSplitToList(String s, String delimiter) { return Str.splitToList(s, delimiter); }
    @SuppressWarnings("unused")
    public String joinStringList(Collection<String> list) { return Str.joinList(list, Str.COMMA);}
    @SuppressWarnings("unused")
    public String joinStringList(Collection<String> list, String delimiter) { return Str.joinList(list, delimiter); }
    @SuppressWarnings("unused")
    public static int getContainerSize(Object o) { return CollectionUtil.getSize(o); }


    @SuppressWarnings("unused")
    public boolean stringToBoolean(String s) { return stringToBoolean(s, false); }
    @SuppressWarnings("unused")
    public boolean stringToBoolean(String s, boolean defaultValue) { return Boolean.TRUE.equals(StrConversionUtils.tryParseBoolean(s, defaultValue)); }
    @SuppressWarnings("unused")
    public int stringToInt(String s) { return stringToInt(s, 0); }
    @SuppressWarnings("unused")
    public int stringToInt(String s, int defaultValue) { return StrConversionUtils.tryParseInt(s, defaultValue); }

    @SuppressWarnings("unused")
    public boolean objectIsArray(Object o) { return o != null && o.getClass().isArray(); }
    @SuppressWarnings("unused")
    public boolean objectIsCollection(Object o) { return o instanceof Collection; }
    @SuppressWarnings("unused")
    public String objectToString(Object o) { return objectToString(o, null); }
    @SuppressWarnings("unused")
    public String objectToString(Object o, String defaultIfNull) { return o == null ? defaultIfNull : String.valueOf(o); }
    @SuppressWarnings("unused")
    public String objectTypeToString(Object o) { return o == null ? null : o.getClass().getName(); }
    @SuppressWarnings("unused")
    public boolean objectsAreEqual(Object a, Object b) { return Objects.equals(a, b); }
    @SuppressWarnings("unused")
    public boolean objectsAreEqualDeep(Object a, Object b) { return Objects.deepEquals(a, b); }
    @SuppressWarnings("unused")
    public int objectToHashCode(Object o) { return o == null ? 0 : o.hashCode(); }

    @SuppressWarnings("unused")
    public Object createArrayEmpty() { return createArray(0); }
    @SuppressWarnings("unused")
    public Object createArray(int size) { return createArray(Object.class, size); }
    @SuppressWarnings("unused")
    public Object createArray(String elementKind, int size) { return createArray(ObjectUtils.valueNonNull(classForName(elementKind), Object.class), size); }
    @SuppressWarnings("unused")
    public Object createArray(Class<?> elementKind, int size) { return TryRun.get(() -> Array.newInstance(elementKind, Math.max(size, 0))); }

    @SuppressWarnings("unused")
    public Object[] createArrayObject(int size) { return new Object[Math.max(size, 0)]; }
    @SuppressWarnings("unused")
    public byte[] createArrayByte(int size) { return new byte[Math.max(size, 0)]; }
    @SuppressWarnings("unused")
    public char[] createArrayChar(int size) { return new char[Math.max(size, 0)]; }
    @SuppressWarnings("unused")
    public short[] createArrayShort(int size) { return new short[Math.max(size, 0)]; }
    @SuppressWarnings("unused")
    public int[] createArrayInt(int size) { return new int[Math.max(size, 0)]; }
    @SuppressWarnings("unused")
    public long[] createArrayLong(int size) { return new long[Math.max(size, 0)]; }
    @SuppressWarnings("unused")
    public String[] createArrayString(int size) { return new String[Math.max(size, 0)]; }
    @SuppressWarnings("unused")
    public Object createReflectArray(String className, int size) { return ReflectUtilEx.createArray(className, size); }
    @SuppressWarnings("unused")
    public Object createReflectArray(Class<?> classType, int size) { return ReflectUtilEx.createArray(classType, size); }

    @SuppressWarnings("unused")
    public Object reverseArray(Object o) { return o == null ? null : ArrayUtils.reverseAnyArray(o); }

    @SuppressWarnings("unused")
    public int arraySize(Object o) { return ArrayUtils.safeLength(o); }
    @SuppressWarnings("unused")
    public boolean arrayHasMinimumIndex(Object o, int minimumIndex) { return ArrayUtils.isMinimumIndex(o, minimumIndex); }
    @SuppressWarnings("unused")
    public boolean arrayHasMinimumSize(Object o, int minimumSize) { return ArrayUtils.isMinimumSize(o, minimumSize); }

    @SuppressWarnings("unused")
    public Object createListEmpty() { return ListUtil.emptyList(); }
    @SuppressWarnings("unused")
    public Object createList(int size) { return new ArrayList<>(Math.max(size, 0)); }
    @SuppressWarnings("unused")
    public Object arrayToList(Object e) { return ListUtil.arrayToList(e); }
    @SuppressWarnings("unused")
    public Object listToArray(Object l) { return ArrayUtils.toArray(l); }
    @SuppressWarnings("unused")
    public Object listToArray(Object l, String elementKind) { return listToArray(l, classForName(elementKind)); }
    @SuppressWarnings("unused")
    public Object listToArray(Object l, Class<?> elementKind) { return ArrayUtils.toArrayObject(l, ObjectUtils.valueNonNull(elementKind, Object.class)); }

    @SuppressWarnings("unused")
    public int sizeList(Object o) { return ListUtil.safeLength(o); }
    @SuppressWarnings("unused")
    public boolean listHasMinimumIndex(Object o, int minimumIndex) { return ListUtil.isMinimumIndex(o, minimumIndex); }
    @SuppressWarnings("unused")
    public boolean listHasMinimumSize(Object o, int minimumSize) { return ListUtil.isMinimumSize(o, minimumSize); }

    @SuppressWarnings("unused")
    public Object arrayElementAt(Object a, int index) { return ArrayUtils.getElementAtSafe(a, index);  }
    @SuppressWarnings("unused")
    public Object listElementAt(Object a, int index) { return ListUtil.getElementAtSafe(a, index); }

    @SuppressWarnings("unused")
    public boolean arrayContains(Object a, Object c) { return ArrayUtils.contains(a, c); }
    @SuppressWarnings("unused")
    public boolean listContain(Object l, Object c) { return ListUtil.contains(l, c); }

    @SuppressWarnings("unused")
    public Object addElementToArray(Object a, Object e) { return ArrayUtils.addElement(a, e); }
    @SuppressWarnings("unused")
    public void addElementToList(Object a, Object e) { ListUtil.addElement(a, e); }
    @SuppressWarnings("unused")
    public void setArrayElementAtIndex(Object a, Object e, int index) { ArrayUtils.setElementAt(a, e, index); }
    @SuppressWarnings("unused")
    public void setListElementAtIndex(Object l, Object e, int index) { ListUtil.setElementAt(l, e, index); }
    @SuppressWarnings("unused")
    public void removeListElementAtIndex(Object l, int index) { ListUtil.removeElementAt(l, index); }
    @SuppressWarnings("unused")
    public void removeArrayElementAtIndex(Object a, int index) { ArrayUtils.removeElementAt(a, index); }
    @SuppressWarnings("unused")
    public int elementIndexArray(Object a, Object e) { return ArrayUtils.getIndexOfElement(a, e);  }
    @SuppressWarnings("unused")
    public int elementIndexList(Object l, Object e) { return ListUtil.getIndexOfElement(l, e); }

    @SuppressWarnings("unused")
    public boolean clearList(Object l) { return ListUtil.clear(l);  }
    @SuppressWarnings("unused")
    public boolean clearArray(Object a) { return ArrayUtils.clear(a); }
    @SuppressWarnings("unused")
    public boolean removeDuplicateListElements(Object l) { return ListUtil.removeDuplicates(l); }
    @SuppressWarnings("unused")
    public Object removeDuplicateArrayElements(Object a) { return ArrayUtils.removeDuplicates(a); }
    @SuppressWarnings("unused")
    public boolean removeNullListElements(Object l) { return ListUtil.removeNulls(l); }
    @SuppressWarnings("unused")
    public Object removeNullArrayElements(Object a) { return ArrayUtils.removeNulls(a); }

    @SuppressWarnings("unused")
    public Object getFirstListElement(Object l) { return ListUtil.getFirst(l); }
    @SuppressWarnings("unused")
    public Object getLastListElement(Object l) { return ListUtil.getLast(l); }
    @SuppressWarnings("unused")
    public Object getFirstArrayElement(Object a) { return ArrayUtils.getFirst(a); }
    @SuppressWarnings("unused")
    public Object getLastArrayElement(Object a) { return ArrayUtils.getLast(a); }

    @SuppressWarnings("unused")
    public Class<?> classForName(String className) { return className == null ? null : ReflectUtil.tryGetClassForName(className); }
    @SuppressWarnings("unused")
    public Class<?> classFromObject(Object o) { return o == null ? null : o.getClass(); }

    //ToDo: Make Invoke Versions ?, also make / use the Resolver / Ability to Create Services easily, maybe Unbox/Conversion Functions ?
    //  Also maybe String is Android ID Kind or UUID kind Functions ? also expose Dynamic Wrapper Functions for better use with Methods/Fields ?
    //  Also do more with Clearing Cat logs (avoid the hooked app from getting weird logs) this will only Clear Logs from Current App whatever can See, maybe Something on the Script Def defining clear after use
    // MAYBE MAYBE PackageInfo / ApplicationInfo Utils, also maybe Make Copy Utils make Copies of Arrays and Lists

    @SuppressWarnings("unused")
    public Class<?> classForObject() { return Object.class; }
    @SuppressWarnings("unused")
    public Class<?> classForByte() { return Byte.class; }
    @SuppressWarnings("unused")
    public Class<?> classForChar() { return Character.class; }
    @SuppressWarnings("unused")
    public Class<?> classForShort() { return Short.class; }
    @SuppressWarnings("unused")
    public Class<?> classForInt() { return Integer.class; }
    @SuppressWarnings("unused")
    public Class<?> classForLong() { return Long.class; }
    @SuppressWarnings("unused")
    public Class<?> classForString() { return Byte.class; }

    @SuppressWarnings("unused")
    public boolean isObjectObject(Object o) { return  o != null && o.getClass().equals(Object.class); }
    @SuppressWarnings("unused")
    public boolean isObjectByte(Object o) { return o instanceof Byte; }
    @SuppressWarnings("unused")
    public boolean isObjectShort(Object o) { return o instanceof Short; }
    @SuppressWarnings("unused")
    public boolean isObjectInt(Object o) { return o instanceof Integer; }
    @SuppressWarnings("unused")
    public boolean isObjectChar(Object o) { return o instanceof Character; }
    @SuppressWarnings("unused")
    public boolean isObjectCollection(Object o) { return o instanceof Collection; }
    @SuppressWarnings("unused")
    public boolean isObjectArray(Object o) { return o instanceof Array || (o != null && o.getClass().isArray()); }
    @SuppressWarnings("unused")
    public boolean isObjectString(Object o) { return o instanceof String; }


    @SuppressWarnings("unused")
    public String randomString() { return RandomGenerator.nextString(); }
    @SuppressWarnings("unused")
    public String randomString(int length) { return RandomGenerator.nextString(length); }
    @SuppressWarnings("unused")
    public String randomString(int origin, int bound) { return RandomGenerator.nextString(origin, bound); }

    @SuppressWarnings("unused")
    public String randomHexString() { return RandomGenerator.nextString(RandomStringKind.HEX); }
    @SuppressWarnings("unused")
    public String randomHexString(int length) { return RandomGenerator.nextString(RandomStringKind.HEX, length); }
    @SuppressWarnings("unused")
    public String randomHexString(int origin, int bound) { return RandomGenerator.nextString(RandomStringKind.HEX, origin, bound); }

    @SuppressWarnings("unused")
    public String randomAlphabeticString() { return RandomGenerator.nextString(RandomStringKind.ALPHA_NUMERIC); }
    @SuppressWarnings("unused")
    public String randomAlphabeticString(int length) { return RandomGenerator.nextString(RandomStringKind.ALPHA_NUMERIC, length); }
    @SuppressWarnings("unused")
    public String randomAlphabeticString(int origin, int bound) { return RandomGenerator.nextString(RandomStringKind.ALPHA_NUMERIC, origin, bound); }

    @SuppressWarnings("unused")
    public String randomNumericString() { return RandomGenerator.nextString(RandomStringKind.NUMERIC); }
    @SuppressWarnings("unused")
    public String randomNumericString(int length) { return RandomGenerator.nextString(RandomStringKind.NUMERIC, length); }
    @SuppressWarnings("unused")
    public String randomNumericString(int origin, int bound) { return RandomGenerator.nextString(RandomStringKind.NUMERIC, origin, bound); }

    @SuppressWarnings("unused")
    public String randomUuid() { return UUID.randomUUID().toString(); }


    @SuppressWarnings("unused")
    public short randomShort() { return RandomGenerator.nextShort(); }
    @SuppressWarnings("unused")
    public short randomShort(short bound) { return RandomGenerator.nextShort(bound); }
    @SuppressWarnings("unused")
    public short randomShort(short origin, short bound) { return RandomGenerator.nextShort(origin, bound); }

    @SuppressWarnings("unused")
    public int randomInt() { return RandomGenerator.nextInt(); }
    @SuppressWarnings("unused")
    public int randomInt(int bound) { return RandomGenerator.nextInt(bound); }
    @SuppressWarnings("unused")
    public int randomInt(int origin, int bound) { return RandomGenerator.nextInt(origin, bound); }

    @SuppressWarnings("unused")
    public float randomFloat() { return RandomGenerator.nextFloat(); }
    @SuppressWarnings("unused")
    public float randomFloat(float bound) { return RandomGenerator.nextFloat(bound); }
    @SuppressWarnings("unused")
    public float randomFloat(float origin, float bound) { return RandomGenerator.nextFloat(origin, bound); }

    @SuppressWarnings("unused")
    public double randomDouble() { return RandomGenerator.nextDouble(); }
    @SuppressWarnings("unused")
    public double randomDouble(double bound) { return RandomGenerator.nextDouble(bound); }
    @SuppressWarnings("unused")
    public double randomDouble(double origin, double bound) { return RandomGenerator.nextDouble(origin, bound); }

    @SuppressWarnings("unused")
    public byte randomByte() { return RandomGenerator.nextByte(); }

    @SuppressWarnings("unused")
    public boolean randomBool() { return RandomGenerator.nextBoolean(); }
    @SuppressWarnings("unused")
    public boolean randomChance() { return RandomGenerator.chance(); }
    @SuppressWarnings("unused")
    public boolean randomChance(int chance) { return RandomGenerator.chance(chance); }
    @SuppressWarnings("unused")
    public byte[] randomBytes() { return RandomGenerator.nextBytes(); }
    @SuppressWarnings("unused")
    public byte[] randomBytes(int bound) { return RandomGenerator.nextBytes(bound); }
    @SuppressWarnings("unused")
    public byte[] randomBytes(int origin, int bound) { return RandomGenerator.nextBytes(origin, bound); }



    @SuppressWarnings("unused")
    public boolean isAssignableFrom(Object o, String className) { return isAssignableFrom(o, classForName(className)); }
    @SuppressWarnings("unused")
    public boolean isAssignableFrom(Object o, Class<?> clazz) { return o != null && o.getClass().isAssignableFrom(clazz); }

    @SuppressWarnings("unused")
    public Object newInstance(String className, Object... args) { return ReflectUtil.tryCreateNewInstance(classForName(className), args); }
    @SuppressWarnings("unused")
    public Object newInstance(Class<?> clazz, Object... args) { return ReflectUtil.tryCreateNewInstance(clazz, args); }
    @SuppressWarnings("unused")
    public Object newInstance(String className) { return TryRun.get(() -> Class.forName(className).newInstance()); }
    @SuppressWarnings("unused")
    public Object newInstance(Class<?> clazz) { return TryRun.get(clazz::newInstance); }

    @SuppressWarnings("unused")
    public boolean hasClass(String className) { return ReflectUtil.tryGetClassForName(className) != null; }
    @SuppressWarnings("unused")
    public boolean hasMethod(String className, String methodName) { return ReflectUtil.tryGetMethod(classForName(className), methodName) != null; }
    @SuppressWarnings("unused")
    public boolean hasMethod(Class<?> clazz, String methodName) { return ReflectUtil.tryGetMethod(clazz, methodName) != null;  }
    @SuppressWarnings("unused")
    public boolean hasMethod(String className, String methodName, int argCount) { return ReflectUtil.tryGetMethodWilCard(classForName(className), methodName, argCount) != null; }
    @SuppressWarnings("unused")
    public boolean hasMethod(Class<?> clazz, String methodName, int argCount) { return ReflectUtil.tryGetMethodWilCard(clazz, methodName, argCount) != null; }
    @SuppressWarnings("unused")
    public boolean hasField(String className, String fieldName) { return ReflectUtil.tryGetField(className, fieldName) != null; }
    @SuppressWarnings("unused")
    public boolean hasField(Class<?> clazz, String fieldName) { return ReflectUtil.tryGetField(clazz, fieldName) != null; }

    @SuppressWarnings("unused")
    public Method methodForName(String className, String methodName) { return ReflectUtil.tryGetMethod(className, methodName); }
    @SuppressWarnings("unused")
    public Method methodForName(Class<?> clazz, String methodName) { return ReflectUtil.tryGetMethod(clazz, methodName); }
    @SuppressWarnings("unused")
    public Method methodForName(Class<?> clazz, String methodName, int argCount) { return ReflectUtil.tryGetMethodWilCard(clazz, methodName, argCount); }
    @SuppressWarnings("unused")
    public Method methodForName(String className, String methodName, int argCount) { return ReflectUtil.tryGetMethodWilCard(classForName(className), methodName, argCount); }

    @SuppressWarnings("unused")
    public Field fieldForName(String className, String fieldName) { return ReflectUtil.tryGetField(className, fieldName); }
    @SuppressWarnings("unused")
    public Field fieldForName(Class<?> clazz, String fieldName) { return ReflectUtil.tryGetField(clazz, fieldName); }

    //ToDO: Add Signatures Options, PS Side Note by Default Hidden Api Is already bypassed for Everything for target app from XLua (I know messy)
    @SuppressWarnings("unused")
    public boolean bypassHiddenApiRestrictions() { return HiddenApi.bypassHiddenApiRestrictions(); }
    @SuppressWarnings("unused")
    public boolean bypassHiddenApiRestrictions(ClassLoader classLoader) { return HiddenApi.bypassHiddenApiRestrictionsClassLoader(classLoader); }

    @SuppressWarnings("unused")
    public int myPid() { return Process.myPid(); }
    @SuppressWarnings("unused")
    public int myUid() { return Process.myUid(); }
    @SuppressWarnings("unused")
    public int myTid() { return Process.myTid(); }
    @SuppressWarnings("unused")
    public void exit() { System.exit(0); }
    @SuppressWarnings("unused")
    public void exit(int status) { System.exit(status); }
    @SuppressWarnings("unused")
    public void exitCurrentThread() { Thread.currentThread().interrupt(); }

    @SuppressWarnings("unused")
    public java.lang.Process exec(String command) { return TryRun.get(() -> Runtime.getRuntime().exec(command)); }
    @SuppressWarnings("unused")
    public java.lang.Process execEcho(String msg) { return TryRun.get(() -> Runtime.getRuntime().exec(new String[] { "sh", "-c", "echo " + msg})); }
    @SuppressWarnings("unused")
    public Runtime getRuntime() { return Runtime.getRuntime(); }

    @SuppressWarnings("unused")
    public Thread getCurrentThread() { return Thread.currentThread(); }
    @SuppressWarnings("unused")
    public boolean isCurrentThreadMain() { return Looper.getMainLooper().getThread() == Thread.currentThread(); }

    @SuppressWarnings("unused")
    public void clearLogCatLogs() { TryRun.silent(() -> Runtime.getRuntime().exec("logcat -c")); }


    @SuppressWarnings("unused")
    public long gigabytesToBytes(int gigabytes) { return (long) gigabytes * 1073741824L; }

}
