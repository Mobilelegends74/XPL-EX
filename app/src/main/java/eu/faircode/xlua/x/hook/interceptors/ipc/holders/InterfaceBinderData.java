package eu.faircode.xlua.x.hook.interceptors.ipc.holders;

import android.os.IBinder;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.tools.BytesReplacer;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.runtime.HiddenApi;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.xlua.LibUtil;

//android.os.Binder
//android.os.BinderProxy
//android.os.IBinder

public class InterfaceBinderData {
    private static final String TAG = LibUtil.generateTag(InterfaceBinderData.class);

    public static InterfaceBinderData create(XParam param, boolean getResult) { return new InterfaceBinderData(param, getResult); }

    public String interfaceName;
    public int code;
    public Parcel data;
    public Parcel reply;
    public int flags;
    public boolean result = true;

    private int mDataPosition = 0;
    private int mReplyPosition = 0;

    public boolean hasInterfaceName() { return !TextUtils.isEmpty(interfaceName); }
    public boolean hasData() { return data != null && data.dataSize() > 12; }       //Check if it has more than 12 Bytes (being generous)
    public boolean hasReply() { return reply != null && reply.dataSize() > 12; }    //Check if it has more than 12 Bytes (being generous)
    public boolean hasSucceeded() { return result; }

    public int getDataSize() { return data != null ? data.dataSize() : -1; }
    public int getReplySize() { return reply != null ? reply.dataSize() : -1; }

    public boolean isInterfaceName(String targetInterface) { return interfaceName.equalsIgnoreCase(targetInterface); }
    public boolean isCode(int targetCode) { return code == targetCode; }

    private static Method mthInt = null;
    private static Method mthLong = null;

    static {
        try {
            HiddenApi.bypassHiddenApiRestrictions();
            Method[] methods = Parcel.class.getDeclaredMethods();
            for(Method m : methods) {
                if(m.getName().equals("obtain")) {
                    int pCount = m.getParameterCount();
                    if(pCount < 1)
                        continue;

                    Class<?>[] params = m.getParameterTypes();
                    if(!ArrayUtils.isValid(params) || params.length != 1)
                        continue;

                    if(params[0] == long.class || params[0] == Long.class) {
                        if(mthLong == null) {
                            mthLong = m;
                            m.setAccessible(true);
                            continue;
                        }
                    }

                    if(params[0] == int.class || params[0] == Integer.class) {
                        if(mthInt == null) {
                            mthInt = m;
                            m.setAccessible(true);
                            continue;
                        }
                    }

                    if(mthInt != null && mthLong != null)
                        break;
                }
            }

        }catch (Exception e) {
            Log.e(TAG, "Failed to Static Init, Error=" + e);
        }
    }

    public static Parcel obtain(int p) {
        try {
            return (Parcel) mthInt.invoke(null, p);
        }catch (Exception e) {
            Log.e(TAG, "Failed to obtain Parcel(int): Error=" + e);
            return null;
        }
    }

    public static Parcel obtain(long p) {
        try {
            return (Parcel) mthLong.invoke(null, p);

           // DynamicMethod mth = new DynamicMethod(Parcel.class, "obtain", Long.class);
           // mth.setHiddenApis();
           // mth.setAccessible(true);
           // return (Parcel) mth.tryStaticInvoke(p);

            //static protected final Parcel obtain(long obj)
            //Method methodObtain = Parcel.class.getDeclaredMethod("obtain", long.class);
            //methodObtain.setAccessible(true);
            //return (Parcel) methodObtain.invoke(null, p);
        }catch (Exception e) {
            Log.e(TAG, "Failed to obtain Parcel(long): Error=" + e);
            return null;
        }
    }

    public InterfaceBinderData(XParam param, boolean getResult) {
        try {
            this.interfaceName = getInterfaceDescriptor(param.getThis());
            this.code = param.tryGetArgument(0, 0);

            //this.data = param.tryGetArgument(1, null);
            //this.reply = param.tryGetArgument(2, null);

            Object vOne = param.getArgument(1);
            if(vOne instanceof Long) {
                this.data = obtain((long) vOne);
            }
            else if(vOne instanceof Integer) {
                this.data = obtain((int)vOne);
            }
            else if(vOne instanceof Parcel) {
                this.data = (Parcel) vOne;
            }


            Object vTwo = param.getArgument(2);
            if(vTwo instanceof Long) {
                this.reply = obtain((long) vTwo);
            }
            else if(vTwo instanceof Integer) {
                this.reply = obtain((int)vTwo);
            }
            else if(vTwo instanceof Parcel) {
                this.reply = (Parcel) vTwo;
            }

            this.flags = param.tryGetArgument(3, 0);
            if(getResult)
                this.result = param.tryGetResult(false);

            cache();
            ensureHasInterfaceName();
        }catch (Exception e) {
            Log.e(TAG, "Error in Constructor<InterfaceBinderData>. Error:" + e);
        }
    }

    public InterfaceBinderData(String interfaceName, int code, Parcel data, Parcel reply, int flags) {
        this.interfaceName = interfaceName;
        this.code = code;
        this.data = data;
        this.reply = reply;
        this.flags = flags;
        cache();
    }

    private void cache() {
        cacheDataPosition();
        cacheReplyPosition();
    }

    private void ensureHasInterfaceName() {
        if(TextUtils.isEmpty(interfaceName) && hasData()) {
            String possibleString = data.readString();
            //Possibly Stored in Data (before) Hook as it was written with "writeInterfaceToken"
            if(possibleString != null && possibleString.contains("."))
                interfaceName = possibleString;

            resetParcelPositions();
        }
    }

    public InterfaceBinderData resetParcelPositions() {
        setDataPosition();
        setReplyPosition();
        return this;
    }

    public InterfaceBinderData readDataException() {
        if(data != null) data.readException();
        return this;
    }

    public InterfaceBinderData readReplyException() {
        if(reply != null) reply.readException();
        return this;
    }

    public InterfaceBinderData replaceDataString(String oldString, String newString) { return replaceDataString(oldString, newString, StandardCharsets.UTF_16); }
    public InterfaceBinderData replaceDataString(String oldString, String newString, Charset set) {
        int oldDataPosition = getDataPosition();
        try {
            byte[] oldBytes = oldString.getBytes(set);
            byte[] newBytes = newString.getBytes(set);
            if(set == StandardCharsets.UTF_16) {
                //We need to skip the first (2) bytes the UTF_16 (BOM)
                oldBytes = Arrays.copyOfRange(oldBytes, 2, oldBytes.length);
                newBytes = Arrays.copyOfRange(newBytes, 2, newBytes.length);
            }

            byte[] bytes = data.marshall();
            BytesReplacer bytesReplacer = new BytesReplacer(oldBytes, newBytes);
            byte[] newDataBytes = bytesReplacer.replace(bytes);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Replacing Data Parcel [" + interfaceName + "]\n" +
                        "Old String=" + oldString + "\n" +
                        "New String=" + newString + "\n" +
                        "Old Bytes=" + Str.bytesToHex(oldBytes) + "\n" +
                        "New Bytes=" + Str.bytesToHex(newBytes));

            data.unmarshall(newDataBytes, 0, newDataBytes.length);
        }catch (Exception e) {
            Log.e(TAG, "Error Replacing String in Data Parcel, Old String: " + oldString + " New String: " + newString + " Error: " + e);
        } finally {
            setDataPosition(oldDataPosition);
        }
        return this;
    }

    public InterfaceBinderData replaceReplyString(String oldString, String newString) { return replaceReplyString(oldString, newString, StandardCharsets.UTF_16); }
    public InterfaceBinderData replaceReplyString(String oldString, String newString, Charset set) {
        int oldReplyPosition = getReplyPosition();
        try {
            byte[] oldBytes = oldString.getBytes(set);
            byte[] newBytes = newString.getBytes(set);
            if(set == StandardCharsets.UTF_16) {
                //We need to skip the first (2) bytes the UTF_16 (BOM)
                oldBytes = Arrays.copyOfRange(oldBytes, 2, oldBytes.length);
                newBytes = Arrays.copyOfRange(newBytes, 2, newBytes.length);
            }

            byte[] bytes = reply.marshall();
            BytesReplacer bytesReplacer = new BytesReplacer(oldBytes, newBytes);
            byte[] newReplyBytes = bytesReplacer.replace(bytes);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Replacing Reply Parcel [" + interfaceName + "]\n" +
                        "Old String=" + oldString + "\n" +
                        "New String=" + newString + "\n" +
                        "Old Bytes=" + Str.bytesToHex(oldBytes) + "\n" +
                        "New Bytes=" + Str.bytesToHex(newBytes));

            reply.unmarshall(newReplyBytes, 0, newReplyBytes.length);
        }catch (Exception e) {
            Log.e(TAG, "Error Replacing String in Reply Parcel, Old String: " + oldString + " New String: " + newString + " Error: " + e);
        } finally {
            setReplyPosition(oldReplyPosition);
        }
        return this;
    }

    public String readDataString() {
        return data != null ? data.readString() : null;
    }

    public String readReplyString() {
        return reply != null ? reply.readString() : null;
    }

    public InterfaceBinderData cacheReplyPosition() {
        if(reply != null) this.mReplyPosition = reply.dataPosition();
        return this;
    }

    public int getReplyPosition() { return reply != null ? reply.dataPosition() : -1; }
    public InterfaceBinderData setReplyPosition() { return setReplyPosition(mReplyPosition); }
    public InterfaceBinderData setReplyPosition(int position) {
        if(reply != null && position >= 0) reply.setDataPosition(position);
        return this;
    }

    public InterfaceBinderData cacheDataPosition() {
        if(data != null) this.mDataPosition = data.dataPosition();
        return this;
    }

    public int getDataPosition() { return data != null ? data.dataPosition() : -1; }
    public InterfaceBinderData setDataPosition() { return setDataPosition(mDataPosition); }
    public InterfaceBinderData setDataPosition(int position) {
        if(data != null && position >= 0) data.setDataPosition(position);
        return this;
    }

    public static String getInterfaceDescriptor(Object obj) {
        if(obj instanceof IBinder) {
            try {
                IBinder binder = (IBinder) obj;
                return binder.getInterfaceDescriptor();
            }catch (Exception e) {
                Log.e(TAG, "Failed to Get Interface Descriptor! Error: " + e);
                return Str.EMPTY;
            }
        }

        return Str.EMPTY;
    }
}
