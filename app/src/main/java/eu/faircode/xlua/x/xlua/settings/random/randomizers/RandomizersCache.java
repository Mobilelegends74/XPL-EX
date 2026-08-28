package eu.faircode.xlua.x.xlua.settings.random.randomizers;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomCellDisplayName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomCellDisplayNameSource;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomDataRoaming;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomOperatorCarrierId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomOperatorName;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.random.RandomGenericBool;
import eu.faircode.xlua.x.xlua.settings.random.RandomGenericBoolInt;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomDeviceProfile;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel.RandomAndroidKernelNodeName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel.RandomAndroidKernelRelease;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel.RandomAndroidKernelSysName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.kernel.RandomAndroidKernelVersion;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.apps.RandomAppCurrentFlag;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.apps.RandomAppTime;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.battery.RandomBatteryProfile;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomMCC;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomMNC;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomMSIN;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomOperatorTint;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomPhoneType;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomSIMCountyCode;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomSimCount;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomSimType;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomSubscriptionId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateEpocSeconds;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateISOThree;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateOne;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateTwo;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.generic.RandomDateZero;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.hardware.etc.RandomHardwareEfuse;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomDhcpServer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetAllowedList;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetDNS;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetDNSList;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetDomains;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetGateway;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetHostAddress;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetHostName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetNetmask;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetParentControl;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.network.RandomNetRoutes;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomLocCountryIso;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.settings.RandomBootCount;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomAndroidId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomBSSID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomBaiduId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomBluetoothAddress;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomBluetoothOrDeviceName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomDRMID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomEmail;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomGSFID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomICCID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomIMEI;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomMEID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomMac;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomNetSSID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.props_serials.RandomOnePlusSerial;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomPhone;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomSIMSerial;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomSerialNo;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.cell.RandomSubscriberId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomUUID;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomVbmetaDigest;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.RandomVoicemailId;

import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent.RandomRegionCountryName;
//import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent.RandomRegionCountryIso2;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent.RandomRegionCountryCode;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent.RandomRegionLanguageName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent.RandomRegionLanguageIso;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent.RandomRegionLanguageTag;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent.RandomRegionTimezoneOffset;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent.RandomRegionTimezoneId;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.region.RandomRegionParent.RandomRegionTimezoneDisplayName;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.props_serials.RandomSerialGeneric;


public class RandomizersCache {
    private static final String TAG = LibUtil.generateTag(RandomizersCache.class);


    public static boolean isSpecialSetting(SettingsContainer container) { return container != null && isSpecialSetting(container.getContainerName()); }
    public static boolean isSpecialSetting(String settingName) { return CoreUiUtils.isSpecialSetting(settingName); }

    public static final Class<?> SETTING_GENERIC_DATE_EPOC_TYPE = RandomDateEpocSeconds.class;
    public static final Class<?> SETTING_GENERIC_DATE_ZERO_TYPE = RandomDateZero.class;
    public static final Class<?> SETTING_GENERIC_DATE_ONE_TYPE = RandomDateOne.class;
    public static final Class<?> SETTING_GENERIC_DATE_TWO_TYPE = RandomDateTwo.class;
    public static final Class<?> SETTING_GENERIC_DATE_ISO_TYPE = RandomDateISOThree.class;

    public static final String SETTING_XP_FORCE_IS_WHITE_LIST = "xplex.force.settings.list.is.whitelist";
    public static final String SETTING_XP_DEFAULTS = "xplex.force.settings.list";
    public static final String SETTING_NETWORK_ALLOW_LIST = "network.allowed.list";

    //public static final Class<?> SETTING_XP_DEFAULTS_TYPE = RandomXPDefaultValue.class;


    public static final String SETTING_SETTING_BOOT_COUNT = "settings.boot.count";
    public static final Class<?> SETTING_SETTING_BOOT_COUNT_TYPE = RandomBootCount.class;

    public static final String SETTING_SETTING_MOCK_LOCATION = "settings.mock.location";
    public static final String SETTING_SETTING_MASS_STORAGE = "settings.usb.mass.storage";
    public static final String SETTING_SETTING_DEVICE_PROVISIONED = "settings.device.provisioned";
    public static final String SETTING_SETTING_STAY_ON_WHILE_PLUGGED = "settings.stay.on.while.plugged";
    public static final String SETTING_SETTING_ADB_ENABLED = "settings.adb.enabled";
    public static final String SETTING_SETTING_DEV_SETTINGS_ENABLED = "settings.dev.settings.enabled";

    public static final Class<?> SETTING_GENERIC_BOOL_INT_TYPE = RandomGenericBoolInt.class;


    // Parent control
    public static final String SETTING_ZONE_PARENT = "zone.parent.control.tz";
    public static final Class<?> SETTING_ZONE_PARENT_TYPE = RandomRegionParent.class;

    // Country settings
    public static final String SETTING_ZONE_COUNTRY_NAME = "zone.country.name";
    public static final Class<?> SETTING_ZONE_COUNTRY_NAME_TYPE = RandomRegionCountryName.class;

    //public static final String SETTING_ZONE_COUNTRY_ISO2 = "zone.country.iso2";
    //public static final Class<?> SETTING_ZONE_COUNTRY_ISO2_TYPE = RandomRegionCountryIso2.class;

    public static final String SETTING_ZONE_COUNTRY_CODE = "zone.country.code";
    public static final Class<?> SETTING_ZONE_COUNTRY_CODE_TYPE = RandomRegionCountryCode.class;

    // Language settings
    public static final String SETTING_ZONE_LANGUAGE_NAME = "zone.language.name";
    public static final Class<?> SETTING_ZONE_LANGUAGE_NAME_TYPE = RandomRegionLanguageName.class;

    public static final String SETTING_ZONE_LANGUAGE_ISO = "zone.language.iso";
    public static final Class<?> SETTING_ZONE_LANGUAGE_ISO_TYPE = RandomRegionLanguageIso.class;

    public static final String SETTING_ZONE_LANGUAGE_TAG = "zone.language.tag";
    public static final Class<?> SETTING_ZONE_LANGUAGE_TAG_TYPE = RandomRegionLanguageTag.class;

    // Timezone settings
    public static final String SETTING_ZONE_TIMEZONE_OFFSET = "zone.timezone.offset";
    public static final Class<?> SETTING_ZONE_TIMEZONE_OFFSET_TYPE = RandomRegionTimezoneOffset.class;

    public static final String SETTING_ZONE_TIMEZONE_ID = "zone.timezone.id";
    public static final Class<?> SETTING_ZONE_TIMEZONE_ID_TYPE = RandomRegionTimezoneId.class;

    public static final String SETTING_ZONE_TIMEZONE_DISPLAY_NAME = "zone.timezone.display.name";
    public static final Class<?> SETTING_ZONE_TIMEZONE_DISPLAY_NAME_TYPE = RandomRegionTimezoneDisplayName.class;


    //network.allowed.list
    public static final String SETTING_DEVICE_NAME = "device.unique.name";
    public static final String SETTING_BLUETOOTH_NAME = "device.unique.bluetooth.name";
    public static final Class<?> SETTING_BLUETOOTH_OR_DEVICE_NAME_TYPE = RandomBluetoothOrDeviceName.class;

    public static final String SETTING_BAIDU_DEVICE_ID = "settings.unique.baidu.device.id";
    public static final Class<?> SETTING_BAIDU_DEVICE_ID_TYPE = RandomBaiduId.class;


    public static final String SETTING_XI_MI_HEALTH_ID = "settings.xiaomi.mi.health.id";
    public static final String SETTING_XI_MI_GC_BOOSTER_UUID = "settings.xiaomi.gcbooster.uuid";
    public static final String SETTING_XI_MI_KEY_MQS_UUID = "settings.xiaomi.key.mqs.uuid";
    public static final String SETTING_XI_MI_MDM_UUID = "settings.xiaomi.mdm.uuid";
    public static final String SETTING_OP_SEC_UUID = "settings.one.plus.security.uuid";
    public static final String SETTING_XI_MI_EXTM_UUID = "settings.xiaomi.extm.uuid";

    /*
        Device
     */
    public static final String SETTING_PARENT_DEVICE = "device.parent.parent.control";

    // A single real profile owns every linked device/build/SoC setting below.
    public static final Class<?> SETTING_DEVICE_PROFILE_TYPE = RandomDeviceProfile.class;

    // Manual-only until a verified bootloader value is stored per profile.
    public static final String SETTING_DEVICE_BOOTLOADER = "device.bootloader";

    public static final String SETTING_DEVICE_BRAND = "device.brand";

    // Device Manufacturer
    public static final String SETTING_DEVICE_MANUFACTURER = "device.manufacturer";

    // Device Nickname
    public static final String SETTING_DEVICE_NICKNAME = "device.nick.name";

    // Device Model
    public static final String SETTING_DEVICE_MODEL = "device.model";

    // Device Codename
    public static final String SETTING_DEVICE_CODENAME = "device.codename";


    /*
        ANDROID
     */

    /*
            Random Date (W MT DY HR:MIN:SC TZ YR)
            Random Date One (YYYY.MM.DD)
            Random Date Two (YYYYMMDD)
            Random Date Three (YYYY-MM-DD)
     */

    // Android Build Date UTC
    public static final String SETTING_ANDROID_BUILD_DATE_EPOC = "android.build.date.utc";
    public static final String SETTING_ANDROID_BUILD_DATE = "android.build.date";
    public static final String SETTING_ANDROID_BUILD_DATE_ONE = "android.build.date.one";
    public static final String SETTING_ANDROID_BUILD_DATE_TWO = "android.build.date.two";

    // Android Build Version
    public static final String SETTING_ANDROID_BUILD_VERSION = "android.build.version";

    // Android Build Version SDK
    public static final String SETTING_ANDROID_BUILD_VERSION_SDK = "android.build.version.sdk";

    // Android Build Version Min SDK
    public static final String SETTING_ANDROID_BUILD_VERSION_MIN_SDK = "android.build.version.min.sdk";

    // Android Build Tags
    public static final String SETTING_ANDROID_BUILD_TAGS = "android.build.tags";

    // Android Build Incremental
    public static final String SETTING_ANDROID_BUILD_INCREMENTAL = "android.build.incremental";

    // Android Build Description
    public static final String SETTING_ANDROID_BUILD_DESCRIPTION = "android.build.description";

    // Android Build ID
    public static final String SETTING_ANDROID_BUILD_ID = "android.build.id";

    // Android Build Display ID
    // Manual-only: a fingerprint does not reliably provide DISPLAY, HOST or SECURITY_PATCH.
    public static final String SETTING_ANDROID_BUILD_DISPLAY_ID = "android.build.display.id";

    // Android Build Flavor
    public static final String SETTING_ANDROID_BUILD_FLAVOR = "android.build.flavor";

    // Android Build Host
    public static final String SETTING_ANDROID_BUILD_HOST = "android.build.host";

    // Android Build Patch
    public static final String SETTING_ANDROID_BUILD_PATCH = "android.build.patch";

    // Android Build Codename
    public static final String SETTING_ANDROID_BUILD_CODENAME = "android.build.codename";

    // Android Build Fingerprint
    public static final String SETTING_ANDROID_BUILD_FINGERPRINT = "android.build.fingerprint";

    // Android Build ETC - Base OS
    public static final String SETTING_ANDROID_ETC_BUILD_ROM_BASE_OS = "android.etc.build.rom.base.os";

    // Android Build ETC - ROM User
    public static final String SETTING_ANDROID_ETC_BUILD_ROM_USER = "android.etc.build.rom.user";

    // Android Build ETC - ROM Version Codename
    public static final String SETTING_ANDROID_ETC_BUILD_ROM_VERSION_CODENAME = "android.etc.build.rom.version.codename";

    // Android Build ETC - ROM Variant
    public static final String SETTING_ANDROID_ETC_BUILD_ROM_VARIANT = "android.etc.build.rom.variant";

    // Android Kernel - System Name
    public static final String SETTING_ANDROID_KERNEL_SYS_NAME = "android.kernel.sys.name";
    public static final Class<?> SETTING_ANDROID_KERNEL_SYS_NAME_TYPE = RandomAndroidKernelSysName.class;

    // Android Kernel - Version
    public static final String SETTING_ANDROID_KERNEL_VERSION = "android.kernel.version";
    public static final Class<?> SETTING_ANDROID_KERNEL_VERSION_TYPE = RandomAndroidKernelVersion.class;

    // Android Kernel - Release
    public static final String SETTING_ANDROID_KERNEL_RELEASE = "android.kernel.release";
    public static final Class<?> SETTING_ANDROID_KERNEL_RELEASE_TYPE = RandomAndroidKernelRelease.class;

    // Android Kernel - Node Name
    public static final String SETTING_ANDROID_KERNEL_NODE_NAME = "android.kernel.node.name";
    public static final Class<?> SETTING_ANDROID_KERNEL_NODE_NAME_TYPE = RandomAndroidKernelNodeName.class;


    // Hardware E-Fuse
    public static final String SETTING_HARDWARE_EFUSE = "hardware.efuse";
    public static final Class<?> SETTING_HARDWARE_EFUSE_TYPE = RandomHardwareEfuse.class;

    // Device-specific hardware values stay manually editable until verified profile data exists.
    // Hardware NFC Kind
    public static final String SETTING_HARDWARE_NFC_KIND = "hardware.nfc.kind";

    // Hardware NFC Controller Interface
    public static final String SETTING_HARDWARE_NFC_CONTROLLER_INTERFACE = "hardware.nfc.controller.interface";

    // Hardware Fingerprint Sensor
    public static final String SETTING_HARDWARE_FP_SENSOR = "hardware.fp.sensor";

    // Hardware Fingerprint Sensor Name
    public static final String SETTING_HARDWARE_FP_SENSOR_NAME = "hardware.fp.sensor.name";

    // Hardware GPS Model Name
    public static final String SETTING_HARDWARE_GPS_MODEL_NAME = "hardware.gps.model.name";

    // Hardware GPS Model Year
    public static final String SETTING_HARDWARE_GPS_MODEL_YEAR = "hardware.gps.model.year";

    // Hardware Camera Count
    public static final String SETTING_HARDWARE_CAMERA_COUNT = "hardware.camera.count";

    // Hardware Camera App
    public static final String SETTING_HARDWARE_CAMERA_APP = "hardware.camera.app";

    // Hardware Memory Total
    public static final String SETTING_HARDWARE_MEMORY_TOTAL = "hardware.memory.total";

    // Hardware Memory Available
    public static final String SETTING_HARDWARE_MEMORY_AVAILABLE = "hardware.memory.available";

    public static final String SETTING_DISPLAY_WIDTH = "display.width";
    public static final String SETTING_DISPLAY_HEIGHT = "display.height";
    public static final String SETTING_DISPLAY_DENSITY_DPI = "display.density.dpi";
    public static final String SETTING_DISPLAY_REFRESH_RATE_HZ = "display.refresh.rate.hz";



    //SOC Info
    // SOC Board Model
    public static final String SETTING_SOC_BOARD_MODEL = "soc.board.model";

    // SOC Board Config Code Name
    public static final String SETTING_SOC_BOARD_CONFIG_CODE_NAME = "soc.board.config.code.name";

    // SOC Board Manufacturer
    public static final String SETTING_SOC_BOARD_MANUFACTURER = "soc.board.manufacturer";

    // SOC Board Manufacturer ID
    public static final String SETTING_SOC_BOARD_MANUFACTURER_ID = "soc.board.manufacturer.id";

    // SOC CPU Processor Count
    public static final String SETTING_SOC_CPU_PROCESSOR_COUNT = "soc.cpu.processor.count";

    // SOC CPU Architecture
    public static final String SETTING_SOC_CPU_ARCHITECTURE = "soc.cpu.architecture";

    // Core instruction-set names vary within heterogeneous SoCs; keep them manual-only.
    // SOC CPU Instruction Set 32-bit
    public static final String SETTING_SOC_CPU_INSTRUCTION_SET_32 = "soc.cpu.instruction.set.32";

    // SOC CPU Instruction Set 64-bit
    public static final String SETTING_SOC_CPU_INSTRUCTION_SET_64 = "soc.cpu.instruction.set.64";

    // SOC CPU ABI
    public static final String SETTING_SOC_CPU_ABI = "soc.cpu.abi";

    // SOC CPU ABI List
    public static final String SETTING_SOC_CPU_ABI_LIST = "soc.cpu.abi.list";

    // SOC CPU ABI List 32-bit
    public static final String SETTING_SOC_CPU_ABI_LIST_32 = "soc.cpu.abi.list.32";

    // SOC CPU ABI List 64-bit
    public static final String SETTING_SOC_CPU_ABI_LIST_64 = "soc.cpu.abi.list.64";

    // SOC CPU Info Dump
    public static final String SETTING_SOC_CPU_INFO_DUMP = "soc.cpu.info.dump";
    //ERROR user input required
    //public static final Class<?> SETTING_SOC_CPU_INFO_DUMP_TYPE = RandomSocCpuInfoDump.class;




    //CELL




    //
    //GPU
    //

    // GPU/baseband/Bluetooth implementation strings are device and driver specific. They remain
    // manual-only instead of being combined independently with a real DeviceProfile.

    // SOC GPU EGL Implementor
    public static final String SETTING_SOC_GPU_EGL_IMPLEMENTOR = "soc.gpu.egl.implementor";

    // SOC GPU Vulkan Implementor
    public static final String SETTING_SOC_GPU_VULKAN_IMPLEMENTOR = "soc.gpu.vulkan.implementor";

    // SOC GPU OpenGLES Version Encoded
    public static final String SETTING_SOC_GPU_OPEN_GLES_VERSION_ENCODED = "soc.gpu.open.gles.version.encoded";

    // SOC GPU OpenGLES Vendor
    public static final String SETTING_SOC_GPU_OPEN_GLES_VENDOR = "soc.gpu.open.gles.vendor";

    // SOC GPU OpenGLES Renderer
    public static final String SETTING_SOC_GPU_OPEN_GLES_RENDERER = "soc.gpu.open.gles.renderer";

    // SOC GPU OpenGLES Version
    public static final String SETTING_SOC_GPU_OPEN_GLES_VERSION = "soc.gpu.open.gles.version";



    // SOC GPU GFX Driver Name 1
    public static final String SETTING_SOC_GPU_GFX_DRIVER_NAME_1 = "soc.gpu.gfx.driver.name.1";
    //public static final Class<?> SETTING_SOC_GPU_GFX_DRIVER_NAME_1_TYPE = RandomSocGpuGfxDriverName1.class;
    // SOC GPU GFX Driver Name 2
    public static final String SETTING_SOC_GPU_GFX_DRIVER_NAME_2 = "soc.gpu.gfx.driver.name.2";
    //public static final Class<?> SETTING_SOC_GPU_GFX_DRIVER_NAME_2_TYPE = RandomSocGpuGfxDriverName2.class;



    // SOC Baseband Board Config Name
    public static final String SETTING_SOC_BASEBAND_BOARD_CONFIG_NAME = "soc.baseband.board.config.name";

    // SOC Baseband Board Radio Version
    public static final String SETTING_SOC_BASEBAND_BOARD_RADIO_VERSION = "soc.baseband.board.radio.version";

    // SOC Baseband Board Implementor
    public static final String SETTING_SOC_BASEBAND_BOARD_IMPLEMENTOR = "soc.baseband.board.implementor";

    // SOC Bluetooth Board Config Name
    public static final String SETTING_SOC_BLUETOOTH_BOARD_CONFIG_NAME = "soc.bluetooth.board.config.name";


    public static final String SETTING_UNIQUE_BLUETOOTH = "unique.bluetooth.address";
    public static final Class<?> SETTING_UNIQUE_BLUETOOTH_TYPE = RandomBluetoothAddress.class;

    public static final String SETTING_UNIQUE_MAC = "unique.network.mac.address";
    public static final Class<?> SETTING_UNIQUE_MAC_TYPE = RandomMac.class;


    //public static final String SETTING_UNIQUE_SUB_ID = "unique.gsm.subscription.id";


    public static final String INSTALL_CURRENT_OFFSET_SETTING = "apps.current.install.time.offset";
    public static final String UPDATE_CURRENT_OFFSET_SETTING = "apps.current.update.time.offset";
    public static final String INSTALL_OFFSET_SETTING = "apps.install.time.offset";
    public static final String UPDATE_OFFSET_SETTING = "apps.update.time.offset";

    public static final String SETTING_FILE_MODIFY_OFFSET = "files.time.modify.offset";
    public static final String SETTING_FILE_ACCESS_OFFSET = "files.time.access.offset";
    public static final String SETTING_FILE_CREATION_OFFSET = "files.time.created.offset";

    public static final String SETTING_FILES_OFFSET_SUBTRACT = "files.offset.subtract.bool";
    public static final String SETTING_FILE_SYNC_TIME = "files.time.sync.bool";



    public static final String SETTING_APP_SYNC_TIME = "apps.time.sync.bool";
    public static final String SETTING_APP_OFFSET_SUBTRACT = "apps.offset.subtract.bool";


    public static final Class<?> SETTING_GENERIC_BOOL_TYPE = RandomGenericBool.class;
    //Delete this
    public static final String SETTING_BATTERY_IS_CHARGING = "battery.is.charging.bool";
    public static final Class<?> SETTING_BATTERY_IS_CHARGING_TYPE = RandomBatteryProfile.class;
    public static final String SETTING_BATTERY_IS_POWER_SAVE_MODE = "battery.is.power.save.mode.bool";

    public static final String SETTING_BATTERY_CHARGING_CYCLES = "battery.charging.cycles";
    public static final Class<?> SETTING_BATTERY_CHARGING_CYCLES_TYPE = RandomBatteryProfile.class;

    public static final String SETTING_BATTERY_PERCENT = "battery.charge.percent";
    public static final Class<?> SETTING_BATTERY_PERCENT_TYPE = RandomBatteryProfile.class;

    public static final String SETTING_BATTERY_STATUS = "battery.status";
    public static final Class<?> SETTING_BATTERY_STATUS_TYPE = RandomBatteryProfile.class;

    public static final String SETTING_BATTERY_IS_PLUGGED = "battery.is.plugged.in.bool";
    public static final Class<?> SETTING_BATTERY_IS_PLUGGED_TYPE = RandomBatteryProfile.class;
    public static final String SETTING_BATTERY_CAPACITY_MAH = "battery.capacity.mah";
    public static final Class<?> SETTING_BATTERY_CAPACITY_MAH_TYPE = RandomBatteryProfile.class;
    public static final String SETTING_BATTERY_VOLTAGE_MV = "battery.voltage.mv";
    public static final Class<?> SETTING_BATTERY_VOLTAGE_MV_TYPE = RandomBatteryProfile.class;
    public static final String SETTING_BATTERY_TEMPERATURE_TENTHS_C = "battery.temperature.tenths.celsius";
    public static final Class<?> SETTING_BATTERY_TEMPERATURE_TENTHS_C_TYPE = RandomBatteryProfile.class;

    public static final String SETTING_UNIQUE_VB_META_DIGEST = "props.unique.vbmeta.digest";
    public static final Class<?> SETTING_UNIQUE_VB_META_DIGEST_TYPE = RandomVbmetaDigest.class;

    public static final String SETTING_PROP_ONE_PLUS_UNIQUE_SERIAL = "props.unique.one.plus.serialno";
    public static final Class<?> SETTING_PROP_ONE_PLUS_UNIQUE_SERIAL_TYPE = RandomOnePlusSerial.class;

    //"props.unique.ril.serialnumber" "props.unique.ap_serial" "props.unique.em.did" "props.unique.lite.uid", "props.unique.persist.radio.serialno"

    //public static final String SETTING_PROP_ONE_PLUS_UNIQUE_SERIAL = "props.unique.ril.serialnumber";
    //public static final String SETTING_PROP_ONE_PLUS_UNIQUE_SERIAL = "props.unique.ap_serial";
    //public static final String SETTING_PROP_ONE_PLUS_UNIQUE_SERIAL = "props.unique.em.did";
    public static final Class<?> SETTING_PROP_UNIQUE_NO_GENERIC_TYPE = RandomSerialGeneric.class;

    public static final String SETTING_UNIQUE_GSF_ID = "unique.gsf.id";
    public static final Class<?> SETTING_UNIQUE_GSF_ID_TYPE = RandomGSFID.class;

    public static final String SETTING_EMAIL = "unique.account.email";
    public static final Class<?> SETTING_EMAIL_TYPE = RandomEmail.class;


    public static final String SETTING_UNIQUE_SERIAL_NO = "unique.serial.no";
    public static final Class<?> SETTING_UNIQUE_SERIAL_NO_TYPE = RandomSerialNo.class;

    public static final String SETTING_NET_ALLOWED_LIST = "network.allowed.list";
    public static final Class<?> SETTING_NET_ALLOWED_TYPE = RandomNetAllowedList.class;

    public static final String SETTING_UNIQUE_NET_SSID = "unique.network.ssid";
    public static final Class<?> SETTING_UNIQUE_NET_SSID_TYPE = RandomNetSSID.class;

    public static final String SETTING_UNIQUE_NET_BSSID = "unique.network.bssid";
    public static final Class<?> SETTING_UNIQUE_NET_BSSID_TYPE = RandomBSSID.class;

    public static final String SETTING_UNIQUE_UUID = "unique.guid.uuid";
    public static final String SETTING_UNIQUE_VA_ID = "unique.app.va.id";
    public static final String SETTING_UNIQUE_ANON_ID = "unique.app.anon.id";
    public static final String SETTING_UNIQUE_OPEN_ANON_ID = "unique.open.anon.advertising.id";
    public static final String SETTING_UNIQUE_BOOT_ID = "unique.boot.id";
    public static final String SETTING_UNIQUE_FACEBOOK_ID = "unique.facebook.advertising.id";
    public static final String SETTING_UNIQUE_GOOGLE_ID = "unique.google.advertising.id";
    public static final String SETTING_UNIQUE_PAY_SESSION_ID = "settings.unique.pay.session.id";

    public static final String SETTING_UNIQUE_GOOGLE_APP_SET_ID = "unique.google.app.set.id";

    //unique.facebook.advertising.id

    public static final Class<?> SETTING_UNIQUE_UUID_TYPE = RandomUUID.class;

    public static final String SETTING_UNIQUE_ICC_ID = "cell.unique.sim.icc.id";
    public static final String SETTING_UNIQUE_MEID = "cell.unique.meid";
    public static final String SETTING_UNIQUE_IMEI = "cell.unique.imei";

    public static final Class<?> SETTING_UNIQUE_ICC_ID_TYPE = RandomICCID.class;
    public static final Class<?> SETTING_UNIQUE_MEID_TYPE = RandomMEID.class;
    public static final Class<?> SETTING_UNIQUE_IMEI_TYPE = RandomIMEI.class;

    public static final String SETTING_UNIQUE_ANDROID_ID = "unique.android.id";
    public static final Class<?> SETTING_UNIQUE_ANDROID_ID_TYPE = RandomAndroidId.class;

    public static final String SETTING_UNIQUE_SIM_SERIAL = "unique.gsm.sim.serial";
    public static final Class<?> SETTING_UNIQUE_SIM_SERIAL_TYPE = RandomSIMSerial.class;




    public static final String SETTING_CELL_SUBSCRIBER_ID = "cell.unique.subscriber.id";
    public static final Class<?> SETTING_UNIQUE_SUB_ID_TYPE = RandomSubscriberId.class;

    //cell.phone.sim.card.count
    public static final String SETTING_CELL_SIM_COUNT = "cell.phone.sim.card.count";
    public static final Class<?> SETTING_CELL_SIM_COUNT_TYPE = RandomSimCount.class;

    public static final String SETTING_CELL_PHONE_NUMBER = "cell.unique.phone.number";
    public static final Class<?> SETTING_CELL_UNIQUE_PHONE_TYPE = RandomPhone.class;
    public static final String SETTING_CELL_OPERATOR_NAME = "cell.operator.name";
    public static final Class<?> SETTING_CELL_CARRIER_NAME_TYPE = RandomOperatorName.class;
    public static final String SETTING_CELL_DISPLAY_NAME = "cell.display.name";
    public static final Class<?> SETTING_CELL_DISPLAY_NAME_TYPE = RandomCellDisplayName.class;
    public static final String SETTING_CELL_DISPLAY_NAME_SOURCE = "cell.display.name.source";
    public static final Class<?> SETTING_CELL_DISPLAY_NAME_SOURCE_TYPE = RandomCellDisplayNameSource.class;

    public static final String SETTING_OPERATOR_ICON_TINT = "cell.operator.icon.tint";                      //"telephony.db" => 'siminfo' [color] example (-6728704 / FF99CC00) or
    public static final Class<?> SETTING_OPERATOR_ICON_TINT_TYPE = RandomOperatorTint.class;

    public static final String SETTING_SIM_KIND = "cell.sim.type";

    public static final String SETTING_SIM_SUBSCRIPTION_ID = "cell.sim.subscription.id";
    public static final Class<?> SETTING_SIM_SUB_TYPE = RandomSubscriptionId.class;

    public static final String SETTING_PHONE_KIND = "cell.phone.type";

    public static final String SETTING_SIM_COUNTRY_ISO = "cell.sim.country.iso";                      //or zone.country.iso2 / cell.zone.provioder ? //cell.sim.country.iso
    public static final String SETTING_CELL_OPERATOR_E_SIM = "cell.sim.is.embedded";//cell.sim.is.embedded


    public static final Class<?> SETTING_PHONE_TYPE = RandomPhoneType.class;

    public static final Class<?> SETTING_SIM_KIND_TYPE = RandomSimType.class;

    public static final Class<?> SETTING_PROVIDER_COUNTRY_ISO_TYPE = RandomLocCountryIso.class;

    //mIsOpportunistic

    //cell.data.is.opportunistic
    public static final String SETTING_CELL_DATA_IS_OPPORTUNISTIC = "cell.data.is.opportunistic";


    public static final String SETTING_CELL_DATA_ROAMING = "cell.data.roaming.enabled.flag";

    public static final Class<?> SETTING_CELL_DATA_ROAMING_TYPE = RandomDataRoaming.class;



    public static final String SETTING_CELL_SIM_COUNTRY_CODE = "cell.sim.country.numeric.code";
    public static final Class<?> SETTING_CELL_SIM_COUNTRY_CODE_TYPE = RandomSIMCountyCode.class;


    public static final String SETTING_CELL_OPERATOR_ID = "cell.operator.id";                               //"carrierIdentification.db" => carrier_id [carrier id] Example (1839)
    public static final Class<?> SETTING_CELL_OPERATOR_ID_TYPE = RandomOperatorCarrierId.class;



    public static final String SETTING_CELL_OPERATOR_MCC = "cell.operator.mcc";
    public static final Class<?> SETTING_CELL_OPERATOR_MCC_TYPE = RandomMCC.class;

    public static final String SETTING_CELL_OPERATOR_MNC = "cell.operator.mnc";
    public static final Class<?> SETTING_CELL_OPERATOR_MNC_TYPE = RandomMNC.class;

    public static final String SETTING_CELL_MSIN = "cell.unique.msin";
    public static final Class<?> SETTING_CELL_MSIN_TYPE = RandomMSIN.class;








    public static final String SETTING_UNIQUE_DRM_ID = "unique.drm.id";
    public static final Class<?> SETTING_UNIQUE_DRM_ID_TYPE = RandomDRMID.class;

    public static final String SETTING_UNIQUE_VOICEMAIL_ID = "cell.voice.mail.alpha.tag";
    public static final Class<?> SETTING_UNIQUE_VOICEMAIL_ID_TYPE = RandomVoicemailId.class;


    public static final String SETTING_NET_HOST_NAME = "network.host.name";
    public static final Class<?> SETTING_NET_HOST_NAME_TYPE = RandomNetHostName.class;

    public static final String SETTING_NET_GATEWAY = "network.gateway";
    public static final Class<?> SETTING_NET_GATEWAY_TYPE = RandomNetGateway.class;

    public static final String SETTING_NET_DNS = "network.dns";
    public static final Class<?> SETTING_NET_DNS_TYPE = RandomNetDNS.class;

    public static final String SETTING_NET_DNS_LIST = "network.dns.list";
    public static final Class<?> SETTING_NET_DNS_LIST_TYPE = RandomNetDNSList.class;

    public static final String SETTING_NET_ROUTES = "network.routes";
    public static final Class<?> SETTING_NET_ROUTES_TYPE = RandomNetRoutes.class;

    public static final String SETTING_NET_HOST = "network.host.address";
    public static final Class<?> SETTING_NET_HOST_TYPE = RandomNetHostAddress.class;

    public static final String SETTING_NET_DHCP = "network.dhcp.server";
    public static final Class<?> SETTING_NET_DHCP_TYPE = RandomDhcpServer.class;


    public static final String SETTING_NET_NETMASK = "network.netmask";
    public static final Class<?> SETTING_NET_NETMASK_TYPE = RandomNetNetmask.class;

    public static final String SETTING_NET_DOMAINS = "network.domains";
    public static final Class<?> SETTING_NET_DOMAINS_TYPE = RandomNetDomains.class;

    public static final String SETTING_NET_PARENT_CONTROL = "network.parent.control.isp";
    public static final Class<?> SETTING_NET_PARENT_CONTROL_TYPE = RandomNetParentControl.class;

    public static final String SETTING_APP_INSTALL_TIME_OFFSET = "apps.install.time.offset";
    public static final String SETTING_APP_UPDATE_TIME_OFFSET = "apps.update.time.offset";

    public static final String SETTING_APP_CURRENT_INSTALL_TIME_OFFSET = "apps.current.install.time.offset";
    public static final String SETTING_APP_CURRENT_UPDATE_TIME_OFFSET = "apps.current.update.time.offset";


    //public static final String SETTING_APP_TIME_CURRENT_ONLY = "apps.time.spoof.current";

    public static final Class<?> SETTING_APP_INSTALL_TIME_OFFSET_TYPE = RandomAppTime.class;
    public static final Class<?> SETTING_APP_TIME_CURRENT_ONLY_TYPE = RandomAppCurrentFlag.class;


    private static final Map<String, IRandomizer> randomizers = new HashMap<>();

    //Use View Registry or something Store Copy of Randomizers
    //WHENEVER the Container Binds, it find it in the copy
    //and bind it there

    //Hmm but if they dont "extend" then it will be an issue ?
    //Well lets focus on ..
    //No just set the randomizer object

    //From the UI top we

    public static Map<String, IRandomizer> getCopy() {
        init();
        Map<String, IRandomizer> copy = new HashMap<>(randomizers.size());
        copy.putAll(randomizers);
        //add the more unique ones ?
        return copy;
    }

    public static void init() {
        try {
            synchronized (randomizers) {
                if(randomizers.isEmpty()) {
                    Field[] fields = RandomizersCache.class.getDeclaredFields();
                    for(Field field : fields) {
                        if(!Modifier.isStatic(field.getModifiers()))
                            continue;

                        String name = field.getName().toLowerCase();
                        Class<?> ret = field.getType();
                        if(name.startsWith("setting_") && name.endsWith("_type") && ret == Class.class) {
                            try {
                                Object value = field.get(null);
                                if(!(value instanceof Class<?>))
                                    throw new Exception("Object Value is not Instance of Class<?>");

                                Class<?> classType = (Class<?>) value;
                                IRandomizer randomizer = (IRandomizer) classType.newInstance();

                                for(String setting : randomizer.getSettings())
                                    randomizers.put(setting, randomizer);

                                //Perhaps do some logic here to determine if Special ?

                            }catch (Exception eI) {
                                Log.e(TAG, "Failed to Reflect Field: " + name + " Error=" + eI);
                            }
                        }
                    }
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed to Init Randomizers, error=" + e);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Exiting Init Randomizers, Count=" + randomizers.size());
    }
}
