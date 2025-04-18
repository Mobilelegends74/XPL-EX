package eu.faircode.xlua.x.hook.interceptors.cell.stubs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.hook.interceptors.cell.PhoneHookUtils;
import eu.faircode.xlua.x.runtime.reflect.DynType;
import eu.faircode.xlua.x.runtime.reflect.DynamicType;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;

public class SubscriptionControllerHooks {
    private static final String TAG = LibUtil.generateTag(SubscriptionControllerHooks.class);


    private static final String METHOD_GET_SUBS = "getActiveSubscriptionInfoList";


    public static List<SubscriptionInfo> getSubsResult(XC_MethodHook.MethodHookParam param) {
        try {
            return  (List<SubscriptionInfo>) param.getResult();
        } catch (Exception ignored) {  }
        return null;
    }

    public static void deployHook_getActiveSubscriptionInfoList(final XC_LoadPackage.LoadPackageParam lpparam, Context context) {
        //com.android.internal.telephony.SubscriptionController.getActiveSubscriptionInfoList
        //https://android.googlesource.com/platform/frameworks/opt/telephony/+/aa1b0618a847ee1365c1e9810f085214c717a27e/src/java/com/android/internal/telephony/SubscriptionController.java
        /*
            at android.os.BinderProxy.transactNative(Native Method)
            at android.os.BinderProxy.transact(BinderProxy.java:584)
            at android.permission.ILegacyPermissionManager$Stub$Proxy.checkPhoneNumberAccess(ILegacyPermissionManager.java:314)
            at android.permission.LegacyPermissionManager.checkPhoneNumberAccess(LegacyPermissionManager.java:116)
            at com.android.internal.telephony.TelephonyPermissions.checkReadPhoneNumber(TelephonyPermissions.java:540)
            at com.android.internal.telephony.TelephonyPermissions.checkCallingOrSelfReadPhoneNumber(TelephonyPermissions.java:518)
            at com.android.internal.telephony.SubscriptionController.hasPhoneNumberAccess(SubscriptionController.java:498)
            at com.android.internal.telephony.SubscriptionController.getSubscriptionInfoListFromCacheHelper(SubscriptionController.java:4353)
            at com.android.internal.telephony.SubscriptionController.getActiveSubscriptionInfoList(SubscriptionController.java:1003)
            at com.android.internal.telephony.ISub$Stub.onTransact(ISub.java:743)
            at android.os.Binder.execTransactInternal(Binder.java:1263)
            at android.os.Binder.execTransact(Binder.java:1222)
         */

        /*
            at android.os.BinderProxy.transactNative(Native Method)
            at android.os.BinderProxy.transact(Unknown Source:174)
            at android.permission.ILegacyPermissionManager$Stub$Proxy.checkPhoneNumberAccess(Unknown Source:36)
            at android.permission.LegacyPermissionManager.checkPhoneNumberAccess(Unknown Source:7)
            at com.android.internal.telephony.TelephonyPermissions.checkReadPhoneNumber(Unknown Source:23)
            at com.android.internal.telephony.TelephonyPermissions.checkCallingOrSelfReadPhoneNumber(Unknown Source:14)
            at com.android.internal.telephony.subscription.SubscriptionManagerService.hasPhoneNumberAccess(Unknown Source:2)
            at com.android.internal.telephony.subscription.SubscriptionManagerService.conditionallyRemoveIdentifiers(Unknown Source:14)
            at com.android.internal.telephony.subscription.SubscriptionManagerService.lambda$getActiveSubscriptionInfoList$16(Unknown Source:6)
            at com.android.internal.telephony.subscription.SubscriptionManagerService.$r8$lambda$Q90LYYoiyuY55HEyHtKc2l0la48(Unknown Source:0)
            at com.android.internal.telephony.subscription.SubscriptionManagerService$$ExternalSyntheticLambda4.apply(Unknown Source:8)
            at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:205)
            at java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:187)
            at java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1642)
            at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:513)
            at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:503)
            at java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:708)
            at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:236)
            at java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:519)
            at com.android.internal.telephony.subscription.SubscriptionManagerService.getActiveSubscriptionInfoList(Unknown Source:104)
            at com.android.internal.telephony.ISub$Stub.onTransact(Unknown Source:1095)
            at android.os.Binder.execTransactInternal(Unknown Source:109)
            at android.os.Binder.execTransact(Unknown Source:39)
         */



        /*
            [1] CLIENT_APP
            [2] BIND_TO_INTERFACE(com.android.internal.telephony.ISub) [com.android.phone] /binds ? "com.android.internal.telephony.subscription.SubscriptionManagerService.getActiveSubscriptionInfoList"
            [3] com.android.phone BINDS_INTERFACE(SubscriptionManagerService)
            [4] com.android.providers.telephony (com.android.internal.telephony.subscription.SubscriptionManagerService.getActiveSubscriptionInfoList)
                                                        => query => /database/telephony.db
                                             Has to load in the "telephony-common.jar" some point before providers process
         */

        //com.android.internal.telephony.SubscriptionController
        //com.android.internal.telephony.subscription.SubscriptionManagerService
        //  /data/user_de/0/com.android.providers.telephony/databases


        List<String> classes = Arrays.asList(
                "com.android.internal.telephony.SubscriptionController",
                "com.android.internal.telephony.subscription.SubscriptionManagerService");

        for(String className : classes) {
            try {
                Class<?> clazz = Class.forName(className, false, lpparam.classLoader);
                try {
                    XposedBridge.hookAllMethods(clazz, METHOD_GET_SUBS, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            List<SubscriptionInfo> subs = getSubsResult(param);
                            if(subs != null) {

                            }

                            super.afterHookedMethod(param);
                        }
                    });
                }catch (Exception e) {
                    Log.e(TAG,  Str.fm("[%s] Failed, Exception=%s", METHOD_GET_SUBS, e));
                }

            }catch (Exception e) {
                XposedUtility.logE_xposed(TAG, "Error Intercepting Class: " + className + " Error=" + e);
            }
        }



        try {
            if(PhoneHookUtils.isPhoneService(lpparam.packageName)) {
                @SuppressLint("PrivateApi")
                Class<?> clazz = Class.forName("com.android.internal.telephony.SubscriptionController", false, lpparam.classLoader);
                XposedBridge.hookAllMethods(clazz, "getActiveSubscriptionInfoList", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            List<SubscriptionInfo> result = (List<SubscriptionInfo>) param.getResult();
                            int resultSize = ListUtil.size(result);
                            if(DebugUtil.isDebug())
                                for(SubscriptionInfo sub : result)
                                    Log.d(TAG, "[getActiveSubscriptionInfoList] Sub=" + Str.toStringOrNull(sub));

                            int fakeCount = PhoneHookUtils.getActiveSimCount(context, resultSize);
                            if(fakeCount == 0) {
                                param.setResult(ListUtil.emptyList());
                                return;
                            } else if(fakeCount <= 2) {
                                List<SubscriptionInfo> subs = new ArrayList<>(fakeCount);
                                for(int i = 0; i < Math.min(resultSize, fakeCount); i++) {
                                    SubscriptionInfo sub = result.get(i);
                                    subs.set(i, sub);
                                }

                                for(int i = 0; i < fakeCount; i++) {
                                    SubscriptionInfo sub = subs.get(i);
                                    if(sub == null) {

                                    } else {

                                    }
                                }
                            }
                        }catch (Exception e) {
                            Log.e(TAG, "Error Inner Service Hook [getActiveSubscriptionInfoList] Error=" + e);
                        }
                        super.afterHookedMethod(param);
                    }
                });
            }
        }catch (Exception e) {
            XposedUtility.logE_xposed(TAG, "Error Deploying Telephony Hook [getActiveSubscriptionInfoList]! Error=" + e);
        }
    }


    public static void handleSubscriptionInfoObject(SubscriptionInfo subscriptionInfo, int index, String callingPackage, Context context) {
        try {
            int resolvedIndexSetting = index++;
            //SubscriptionInfo sub =


        }catch (Exception e) {
            Log.e(TAG, "Failed to Handle Sub! Index=" + index + " Calling Package=" + callingPackage + " Error=" + e);
        }
    }
}
