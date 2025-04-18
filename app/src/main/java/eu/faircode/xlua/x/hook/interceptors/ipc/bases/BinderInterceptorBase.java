package eu.faircode.xlua.x.hook.interceptors.ipc.bases;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.tools.BytesReplacer;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.hook.interceptors.ipc.InterfacesGlobal;
import eu.faircode.xlua.x.hook.interceptors.ipc.holders.InterfaceBinderData;
import eu.faircode.xlua.x.xlua.LibUtil;

public class BinderInterceptorBase implements IBinderInterceptor {
    private static final String TAG = LibUtil.generateTag(BinderInterceptorBase.class);
    protected String interfaceName;
    protected String settingName;
    protected String settingValueName;

    public String getSettingValueName() { return settingValueName; }

    public static BinderInterceptorBase create(String interfaceName, String settingName, String settingValueName) {
        BinderInterceptorBase b = new BinderInterceptorBase();
        b.interfaceName = interfaceName;
        b.settingName = settingName;
        b.settingValueName = settingValueName;
        return b;
    }

    @Override
    public boolean intercept(XParam param, InterfaceBinderData helper) {
        if(!Str.toBoolean(param.getSetting(settingName)))
            return false;

        if(!InterfacesGlobal.APPSET_INTERFACE.equalsIgnoreCase(helper.interfaceName)) {
            helper.readReplyException();
            try {
                String oldId = helper.readReplyString();
                if(!Str.isEmpty(oldId) && oldId.length() > 4) {
                    String newId = param.getSetting(settingValueName);    //Use this for now
                    if(!Str.isEmpty(newId)) {
                        if(DebugUtil.isDebug())
                            Log.w(TAG, "Is Target Interface [" + helper.code + "] Reply Size: " + helper.getReplySize() + " Data Size: " + helper.getDataSize() +  " Code=" + helper.code + " Setting=" + settingName + " Old=" + oldId + " New=" + newId);

                        //Have settings Control disable enable like shell for IPC
                        helper.resetParcelPositions();
                        helper.replaceReplyString(oldId, newId);
                        param.setLogOld(oldId);
                        param.setLogNew(newId);
                        param.setLogExtra(helper.interfaceName);
                        return true;
                    }
                    else {
                        Log.w(TAG, "Setting: " + settingName + " For Interface: " + helper.interfaceName + " Code=" + helper.code);
                        return false;
                    }
                } else {
                    Log.w(TAG, "Is Empty or Null or Bad Length! " + helper.interfaceName + " Code=" + helper.code);
                    return false;
                }
            }catch (Exception e) {
                Log.e(TAG, "Failed Intercepting Interface: " + helper.interfaceName + " Error=" + e);
                return false;
            } finally {
                helper.resetParcelPositions();
            }
        } else {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Got AppSetId Interface! Now Messing with the Data! Size=" + helper.getDataSize());

            if(helper.getDataSize() == 260) {
                int originalPosition = helper.getDataPosition();
                try {
                    byte[] bytes = helper.data.marshall();
                    int startIndex = 88;
                    int length = 36;

                    byte[] from = Arrays.copyOfRange(bytes, startIndex * 2, (startIndex * 2) + (length * 2));
                    String original = new String(from, StandardCharsets.UTF_16LE);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Got AppSetId Interface Bytes From=" + Str.bytesToHex(from) + " ID=" + original + " Setting Name Value=" + settingValueName);

                    String fake = param.getSetting(settingValueName);
                    if(Str.isEmpty(fake) || fake.length() != original.length()) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Fake AppSetId is Null or Empty or Not Length Equal to (%s) as it is (%s), Bytes Count=%s",
                                    Str.length(original),
                                    Str.length(fake),
                                    from.length));

                        return false;
                    }

                    byte[] to = fake.getBytes(StandardCharsets.UTF_16LE);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Replacing AppSetId (%s) bytes (%s)[%s] with new (%s) bytes (%s)[%s]",
                                original,
                                Str.bytesToHex(from),
                                from.length,
                                fake,
                                Str.bytesToHex(to),
                                to.length));

                    BytesReplacer bytesReplacer = new BytesReplacer(from, to);
                    byte[] newBytes = bytesReplacer.replace(bytes);
                    helper.data.unmarshall(newBytes, 0, newBytes.length);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Replaced AppSetId (Data) Bytes=" + Str.bytesToHex(newBytes)));

                    param.setLogOld(original);
                    param.setLogNew(fake);
                    param.setLogExtra(helper.interfaceName);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Failed to Intercept AppSetId Interface, Error=" + e);
                    return false;
                } finally {
                    helper.setDataPosition(originalPosition);
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public String getInterfaceName() { return interfaceName; }

    @Override
    public String getSettingName() { return settingName; }

    @Override
    public boolean isEnabled(XParam param) { return StringUtil.toBoolean(param.getSetting(settingName, "false"), false); }
}
