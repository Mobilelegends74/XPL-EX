package eu.faircode.xlua.x.hook.interceptors.battery;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.StrConversionUtils;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class BatteryInterceptor {
    private static final String TAG = LibUtil.generateTag(BatteryInterceptor.class);

    public static final String ACTION_CHANGED = "android.intent.action.BATTERY_CHANGED";
    public static final String ACTION_CHANGED_NAME = "BATTERY_CHANGED";

    public static final String ACTION_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
    public static final String ACTION_DISCONNECTED = "android.intent.action.ACTION_POWER_DISCONNECTED";

    public static final String ACTION_CONNECTED_NAME = "ACTION_POWER_CONNECTED";
    public static final String ACTION_DISCONNECTED_NAME = "ACTION_POWER_DISCONNECTED";

    public static final String ACTION_LOW = "android.intent.action.BATTERY_LOW";
    public static final String ACTION_OK = "android.intent.action.BATTERY_OKAY";

    public static final String ACTION_OK_NAME = "BATTERY_OKAY";
    public static final String ACTION_LOW_NAME = "BATTERY_LOW";



    //android.os.extra.CYCLE_COUNT
    //cycle_count
    //charge_counter
    public static final String EXTRA_CYCLE_COUNT =
            ReflectUtil.useFieldValueOrDefaultString(BatteryManager.class, "EXTRA_CYCLE_COUNT", "android.os.extra.CYCLE_COUNT");

    public static final String CYCLE_COUNT_NAME = "cycle_count";

    public static final String EXTRA_CHARGE_COUNTER =
            ReflectUtil.useFieldValueOrDefaultString(BatteryManager.class, "EXTRA_CHARGE_COUNTER", "charge_counter");


    public static boolean interceptGet(XParam param) {
        //public int getIntProperty(int id)
        //public long getLongProperty(int id)
        //private long queryProperty(int id)
        /*
        if arg == bmn.BATTERY_PROPERTY_CHARGE_COUNTER then
		log("BATTERY_PROPERTY_CHARGE_COUNTER");
		fake = 0
		--Battery capacity in microampere-hours, as an integer.
	        elseif arg == bmn.BATTERY_PROPERTY_ENERGY_COUNTER then
		log("BATTERY_PROPERTY_ENERGY_COUNTER");
		fake = 150000
		--Battery remaining energy in nanowatt-hours, as a long integer.
         */
        try {
            Object pOne = param.getArgument(0);
            if(!(pOne instanceof Integer))
                return false;

            int flag = (int)pOne;
            Object res = param.getResult();
            if(res == null)
                return false;

            boolean isLong = res instanceof Long;
            long longRes = res instanceof Long ? (long)res : res instanceof Integer ? (long)(int)res : -1;
            if(longRes < 0)
                return false;

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Battery Get Property Flag=%s Result=%s",
                        flag,
                        longRes));

            switch (flag) {
                case BatteryManager.BATTERY_PROPERTY_CAPACITY:
                    int modifiedCapacity = param.getSettingInt(RandomizersCache.SETTING_BATTERY_PERCENT, -1);
                    if(modifiedCapacity > -1 && modifiedCapacity < 101 && ((long)modifiedCapacity != longRes)) {
                        param.setLogOld(String.valueOf(longRes));
                        param.setLogNew(String.valueOf(modifiedCapacity));
                        param.setLogExtra("Capacity");
                        param.setResult(isLong ? (long)modifiedCapacity : modifiedCapacity);
                        return true;
                    }
                    break;
                case BatteryManager.BATTERY_PROPERTY_STATUS:
                    int modifiedStatus = param.getSettingInt(RandomizersCache.SETTING_BATTERY_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
                    if(modifiedStatus == BatteryManager.BATTERY_STATUS_UNKNOWN) {
                        String isC = param.getSetting(RandomizersCache.SETTING_BATTERY_IS_CHARGING);
                        if(!Str.isEmpty(isC)) {
                            modifiedStatus = StrConversionUtils.tryParseBoolean(isC) ?
                                    BatteryManager.BATTERY_STATUS_CHARGING :
                                    BatteryManager.BATTERY_STATUS_DISCHARGING;
                        }
                    }

                    if(isValidStatus(modifiedStatus) && (long)modifiedStatus != longRes) {
                        param.setLogOld(String.valueOf(longRes));
                        param.setLogNew(String.valueOf(modifiedStatus));
                        param.setLogExtra("Status");
                        param.setResult(isLong ? (long)modifiedStatus : modifiedStatus);
                        return true;
                    }
                    break;
            }

            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Failed at Intercepting Batter get Function! Error=" + e);
            return false;
        }
    }

    public static boolean intercept(XParam param) {
        try {
            StrBuilder original = StrBuilder.create().ensureDelimiter(Str.NEW_LINE);
            StrBuilder modified = StrBuilder.create().ensureDelimiter(Str.NEW_LINE);

            Intent intent = param.tryGetResult(null);
            if (intent == null)
                return false;

            String action = intent.getAction();
            if (Str.isEmpty(action))
                return false;

            if(DebugUtil.isDebug() && action.toLowerCase().contains("battery"))
                Log.d(TAG, Str.fm("Intent Action is Related to Battery! [%s][%s] ",
                        action,
                        Str.ensureNoDoubleNewLines(intent.toString())));

            if (ACTION_CHANGED.equals(action) || ACTION_CHANGED_NAME.equals(action)) {
                Bundle bundle = intent.getExtras();
                if (bundle == null)
                    return false;

                boolean isFull = false;
                int scale = bundle.getInt(BatteryManager.EXTRA_SCALE, -1);

                //RandomizersCache.SETTING_BATTERY_IS_CHARGING

                int fakePercentage = Math.max(Math.min(param.getSettingInt(RandomizersCache.SETTING_BATTERY_PERCENT, 10), 100), 0);
                if (scale > 0) {
                    int fakeLevel = (int) Math.floor((fakePercentage / 100.0) * scale);
                    bundle.putInt(BatteryManager.EXTRA_LEVEL, fakeLevel);
                    // Handle the battery low flag based on the fake percentage
                    int lowThreshold = getLowThreshold(param.getApplicationContext());
                    boolean shouldBeLow = fakePercentage <= lowThreshold;
                    // Only modify if the key exists or we're forcing settings
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (bundle.containsKey(BatteryManager.EXTRA_BATTERY_LOW) ||
                                param.isForceSetting(RandomizersCache.SETTING_BATTERY_PERCENT)) {

                            boolean originalBatLow = bundle.getBoolean(BatteryManager.EXTRA_BATTERY_LOW, false);
                            if (shouldBeLow != originalBatLow) {
                                original.append("[BATTERY_LOW][" + originalBatLow + "]");
                                modified.append("[BATTERY_LOW][" + shouldBeLow + "]");
                                bundle.putBoolean(BatteryManager.EXTRA_BATTERY_LOW, shouldBeLow);
                            }

                            // Set full flag if percentage is 100%
                            isFull = fakePercentage == 100;
                        }
                    }
                }

                if(bundle.containsKey(BatteryManager.EXTRA_VOLTAGE)) {
                    original.append("[VOLTAGE(mV)][" + bundle.getInt(BatteryManager.EXTRA_VOLTAGE) + "]");
                    modified.append("[VOLTAGE(mV)][4200]");
                    // Set voltage to 4200mV (typical for a healthy battery)
                    bundle.putInt(BatteryManager.EXTRA_VOLTAGE, 4200);
                }

                if(bundle.containsKey(BatteryManager.EXTRA_TEMPERATURE)) {
                    original.append("[TEMPERATURE][" + bundle.getInt(BatteryManager.EXTRA_TEMPERATURE) + "]");
                    modified.append("[TEMPERATURE][25°C]");
                    // Set temperature to 250 (25°C in tenths of degrees - normal temperature)
                    bundle.putInt(BatteryManager.EXTRA_TEMPERATURE, 250);
                }

                if(bundle.containsKey(EXTRA_CHARGE_COUNTER)) {
                    original.append("[CHARGE_COUNTER(μAh)][" + bundle.getInt(EXTRA_CHARGE_COUNTER) + "]");
                    modified.append("[CHARGE_COUNTER(μAh)][0]");
                    // Set charge counter to 0 (current charge in μAh)
                    bundle.putInt(EXTRA_CHARGE_COUNTER, 0);
                }

                boolean isCharging = param.getSettingBool(RandomizersCache.SETTING_BATTERY_IS_CHARGING, false);
                if(bundle.containsKey(BatteryManager.EXTRA_STATUS)) {
                    int originalStatus = bundle.getInt(BatteryManager.EXTRA_STATUS);
                    isCharging = originalStatus == BatteryManager.BATTERY_STATUS_CHARGING;

                    if(originalStatus > -1 || param.isForceSetting(RandomizersCache.SETTING_BATTERY_STATUS)) {
                        int modifiedStatus = param.getSettingInt(RandomizersCache.SETTING_BATTERY_STATUS, originalStatus);
                        if(modifiedStatus != BatteryManager.BATTERY_STATUS_CHARGING && modifiedStatus != BatteryManager.BATTERY_STATUS_FULL && isFull)
                            modifiedStatus = BatteryManager.BATTERY_STATUS_FULL;

                        if(isValidStatus(modifiedStatus) && originalStatus != modifiedStatus) {
                            original.append("[STATUS][" + statusString(originalStatus) + "]");
                            modified.append("[STATUS][" + statusString(modifiedStatus) + "]");
                            // Set battery status to CHARGING
                            bundle.putInt(BatteryManager.EXTRA_STATUS, modifiedStatus);
                            isCharging = modifiedStatus == BatteryManager.BATTERY_STATUS_CHARGING;
                        }
                    }
                }

                if(bundle.containsKey(BatteryManager.EXTRA_PLUGGED)) {
                    int originalIsPlugged = bundle.getInt(BatteryManager.EXTRA_PLUGGED);
                    if(originalIsPlugged > -1 || param.isForceSetting(RandomizersCache.SETTING_BATTERY_IS_PLUGGED)) {
                        boolean fakeIsPlugged = param.getSettingBool(RandomizersCache.SETTING_BATTERY_IS_PLUGGED, false);
                        int modifiedIsPlugged = isCharging ? 1 : fakeIsPlugged ? 1 : 0;
                        if(modifiedIsPlugged != originalIsPlugged) {
                            original.append("[PLUGGED][" + (originalIsPlugged == 1 ? "true" : "false") + "]");
                            modified.append("[PLUGGED][" + (modifiedIsPlugged == 1 ? "true" : "false") + "]");
                            // Set plugged state to 0 (unplugged)
                            bundle.putInt(BatteryManager.EXTRA_PLUGGED, modifiedIsPlugged);
                        }
                    }
                }

                String fieldName = bundle.containsKey(EXTRA_CYCLE_COUNT) ? EXTRA_CYCLE_COUNT : CYCLE_COUNT_NAME;
                if(bundle.containsKey(fieldName)) {
                    int originalCycleCount = bundle.getInt(fieldName, -1);
                    if(originalCycleCount > -1 || param.isForceSetting(RandomizersCache.SETTING_BATTERY_CHARGING_CYCLES)) {
                        int modifiedCycleCount = Math.max(0, param.getSettingInt(RandomizersCache.SETTING_BATTERY_CHARGING_CYCLES, 5));
                        if(originalCycleCount != modifiedCycleCount) {
                            original.append("[CHARGE_CYCLES][" + originalCycleCount + "]");
                            modified.append("[CHARGE_CYCLES][" + modifiedCycleCount + "]");
                            bundle.putInt(fieldName, modifiedCycleCount);
                        }
                    }
                }

                intent.replaceExtras(bundle);
                param.setLogOld(original.toString());
                param.setLogNew(modified.toString());
                param.setLogExtra(action);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Updated Battery Status! [%s][%s] Original=[%s] Modified=[%s]",
                            action,
                            Str.ensureNoDoubleNewLines(intent.toString()),
                            original.toString(true).replaceAll(Str.NEW_LINE, Str.WHITE_SPACE),
                            modified.toString(true).replaceAll(Str.NEW_LINE, Str.WHITE_SPACE)));

                return true;
            }
            else if(ACTION_LOW.equals(action) || ACTION_OK.equals(action)) {
                int fakePercentage = param.getSettingInt(RandomizersCache.SETTING_BATTERY_PERCENT, -1);
                boolean isOk = fakePercentage <= -1 || fakePercentage >= 101 || getLowThreshold(param.getApplicationContext()) < fakePercentage;
                String modifiedState = isOk ? ACTION_OK : ACTION_LOW;
                if(!modifiedState.equals(action)) {
                    intent.setAction(modifiedState);
                    param.setLogOld(isOk ? ACTION_LOW_NAME : ACTION_OK_NAME);
                    param.setLogNew(isOk ? ACTION_OK_NAME : ACTION_LOW_NAME);
                    param.setLogExtra(action);
                    if(DebugUtil.isDebug()) Log.d(TAG, Str.fm("Updated Battery [%s] to => [%s]",
                            action,
                            modifiedState));

                    return true;
                }
            }
            else if(ACTION_CONNECTED.equals(action) || ACTION_DISCONNECTED.equals(action)) {
                boolean fakeIsPlugged = param.getSettingBool(RandomizersCache.SETTING_BATTERY_IS_PLUGGED, false);
                String modifiedState = fakeIsPlugged ? ACTION_CONNECTED : ACTION_DISCONNECTED;
                if(!modifiedState.equals(action)) {
                    intent.setAction(ACTION_DISCONNECTED);
                    param.setLogOld(fakeIsPlugged ? ACTION_DISCONNECTED_NAME : ACTION_CONNECTED_NAME);
                    param.setLogNew(fakeIsPlugged ? ACTION_CONNECTED_NAME : ACTION_DISCONNECTED_NAME);
                    param.setLogExtra(action);
                    if(DebugUtil.isDebug()) Log.d(TAG, Str.fm("Updated Battery [%s] to => [%s]",
                            action,
                            modifiedState));

                    return true;
                }
            }

            return false;
        }catch (Exception e) {
            Log.e(TAG, "Error Intercepting Battery Parcel! Error=" + e);
            return false;
        }
    }

    public static boolean isValidStatus(int status) { return status == BatteryManager.BATTERY_STATUS_DISCHARGING || status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL || status == BatteryManager.BATTERY_STATUS_UNKNOWN; }
    public static String statusString(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING: return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING: return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL: return "Full";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING: return "Not Charging";
            default: return "Unknown";
        }
    }

    public static int getLowThreshold(Context context) {
        return TryRun.getOrDefault(() -> {
            int resourceId = context.getResources().getIdentifier("config_lowBatteryWarningLevel", "integer", "android");
            return resourceId != 0 ? context.getResources().getInteger(resourceId) : 15;
        }, 15);
    }
}
