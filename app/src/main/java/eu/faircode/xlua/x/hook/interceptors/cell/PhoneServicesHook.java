package eu.faircode.xlua.x.hook.interceptors.cell;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Binder;
import android.os.Process;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.hook.interceptors.cell.stubs.SubscriptionControllerHooks;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class PhoneServicesHook {
    private static final String TAG = LibUtil.generateTag(PhoneServicesHook.class);


    /*
         cd /data/local/tmp
        ./frida-server -D
        cd /data/data/com.termux/files/usr/bin
        ./frida-inject -n com.android.phone -s /sdcard/FridaEx/BinderHook.js
        ./frida -H 127.0.0.1 -f com.einnovation.temu -l /sdcard/FridaEx/BinderHook.js
     */



    // public List<SubscriptionInfo> getActiveSubscriptionInfoList()

    public static void deployHook(final XC_LoadPackage.LoadPackageParam lpparam, Context context) {

        SubscriptionControllerHooks.deployHook_getActiveSubscriptionInfoList(lpparam, context);
    }



    public static void deployHook_getImeiForSlot(final XC_LoadPackage.LoadPackageParam lpparam, Context context) {
        /*

            PHONE => *#06#
            public String getImeiForSlot(int slotIndex, String callingPackage, String callingFeatureId)
            java.lang.Exception
            at android.os.BinderProxy.transactNative(Native Method)
            at android.os.BinderProxy.transact(BinderProxy.java:584)
            at android.permission.ILegacyPermissionManager$Stub$Proxy.checkDeviceIdentifierAccess(ILegacyPermissionManager.java:292)
            at android.permission.LegacyPermissionManager.checkDeviceIdentifierAccess(LegacyPermissionManager.java:86)
            at com.android.internal.telephony.TelephonyPermissions.checkPrivilegedReadPermissionOrCarrierPrivilegePermission(TelephonyPermissions.java:373)
            at com.android.internal.telephony.TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(TelephonyPermissions.java:285)
            at com.android.phone.PhoneInterfaceManager.getImeiForSlot(PhoneInterfaceManager.java:3305)
            at com.android.internal.telephony.ITelephony$Stub.onTransact$getImeiForSlot$(ITelephony.java:15324)
            at com.android.internal.telephony.ITelephony$Stub.onTransact(ITelephony.java:5620)
            at android.os.Binder.execTransactInternal(Binder.java:1263)
            at android.os.Binder.execTransact(Binder.java:1222)
        */
        /*try {
            if("com.android.phone".equalsIgnoreCase(lpparam.packageName)) {
                @SuppressLint("PrivateApi")
                Class<?> clazz = Class.forName("com.android.phone.PhoneInterfaceManager", false, lpparam.classLoader);
                XposedBridge.hookAllMethods(clazz, "getImeiForSlot", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            int slotIndex = (int)param.args[0];
                            String callingPkg = tryGetArgAsString(param.args, 1, "");
                            String callingFeatureId = tryGetArgAsString(param.args, 2, "");
                            String res = (String)param.getResult();
                            String fake = getSettingValue(context, "unique.gsm.imei", 1, callingPkg);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Phone Service [getImeiForSlot] Hook Invoked! Slot Index=" + slotIndex +
                                        " Calling Package=" + callingPkg +
                                        " Calling Feature Id=" + callingFeatureId +
                                        " Result=" + res +
                                        " Fake Result=" + fake);

                            if(!TextUtils.isEmpty(fake) && !TextUtils.isEmpty(res))
                                param.setResult(fake);
                        }catch (Exception e) {
                            Log.e(TAG, "Error Inner Service Hook [getImeiForSlot] Error=" + e);
                        }
                        super.afterHookedMethod(param);
                    }
                });
            }
        }catch (Exception e) {
            XposedUtility.logE_xposed(TAG, "Error Deploying Telephony Hook [getImeiForSlot]! Error=" + e);
        }*/
    }
    /*

        java.lang.Exception
            at android.os.BinderProxy.transactNative(Native Method)
            at android.os.BinderProxy.transact(BinderProxy.java:584)
            at android.app.IActivityManager$Stub$Proxy.checkPermission(IActivityManager.java:4875)
            at android.permission.PermissionManager.checkPermissionUncached(PermissionManager.java:1541)
            at android.permission.PermissionManager.-$$Nest$smcheckPermissionUncached(Unknown Source:0)
            at android.permission.PermissionManager$1.recompute(PermissionManager.java:1609)
            at android.permission.PermissionManager$1.recompute(PermissionManager.java:1606)
            at android.app.PropertyInvalidatedCache.query(PropertyInvalidatedCache.java:999)
            at android.permission.PermissionManager.checkPermission(PermissionManager.java:1615)
            at android.app.ContextImpl.checkPermission(ContextImpl.java:2174)
            at android.app.ContextImpl.enforcePermission(ContextImpl.java:2251)
            at android.content.ContextWrapper.enforcePermission(ContextWrapper.java:937)
            at android.content.ContextWrapper.enforcePermission(ContextWrapper.java:937)
            at com.android.internal.telephony.TelephonyPermissions.checkReadPhoneState(TelephonyPermissions.java:163)
            at com.android.internal.telephony.TelephonyPermissions.checkCallingOrSelfReadPhoneState(TelephonyPermissions.java:94)
            at com.android.internal.telephony.SubscriptionController.getActiveSubscriptionInfoForSimSlotIndex(SubscriptionController.java:883)
            at com.android.internal.telephony.ISub$Stub.onTransact(ISub.java:731)
            at android.os.Binder.execTransactInternal(Binder.java:1258)
            at android.os.Binder.execTransact(Binder.java:1222)

     */

    /*
        java.lang.Exception
            at android.os.BinderProxy.transactNative(Native Method)
            at android.os.BinderProxy.transact(BinderProxy.java:584)
            at com.android.internal.app.IAppOpsService$Stub$Proxy.checkPackage(IAppOpsService.java:1419)
            at android.app.AppOpsManager.checkPackage(AppOpsManager.java:8802)
            at com.android.phone.PhoneInterfaceManager.getUiccCardsInfo(PhoneInterfaceManager.java:8798)
            at com.android.internal.telephony.ITelephony$Stub.onTransact(ITelephony.java:5960)
            at android.os.Binder.execTransactInternal(Binder.java:1263)
            at android.os.Binder.execTransact(Binder.java:1222)
     */

    /*
    [BpBinder]
    android.permission.ILegacyPermissionManager
    java.lang.Exception
            at android.os.BinderProxy.transactNative(Native Method)
            at android.os.BinderProxy.transact(BinderProxy.java:584)
            at android.permission.ILegacyPermissionManager$Stub$Proxy.checkDeviceIdentifierAccess(ILegacyPermissionManager.java:292)
            at android.permission.LegacyPermissionManager.checkDeviceIdentifierAccess(LegacyPermissionManager.java:86)
            at com.android.internal.telephony.TelephonyPermissions.checkPrivilegedReadPermissionOrCarrierPrivilegePermission(TelephonyPermissions.java:373)
            at com.android.internal.telephony.TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(TelephonyPermissions.java:285)
            at com.android.phone.PhoneInterfaceManager.getImeiForSlot(PhoneInterfaceManager.java:3305)
            at com.android.internal.telephony.ITelephony$Stub.onTransact$getImeiForSlot$(ITelephony.java:15324)
            at com.android.internal.telephony.ITelephony$Stub.onTransact(ITelephony.java:5620)
            at android.os.Binder.execTransactInternal(Binder.java:1263)
            at android.os.Binder.execTransact(Binder.java:1222)
     */
}
