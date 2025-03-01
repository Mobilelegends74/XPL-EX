package eu.faircode.xlua.x.hook.interceptors.network.utils;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;


import android.net.wifi.ScanResult;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.runtime.HiddenApi;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.xlua.LibUtil;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.HiddenApi;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.xlua.LibUtil;

public class InformationElementCreator {
    private static final String TAG = LibUtil.generateTag(InformationElementCreator.class);
    private static final String CLASS_INFORMATION_ELEMENT = "android.net.wifi.ScanResult$InformationElement";

    static {
        HiddenApi.bypassHiddenApiRestrictions();
    }

    // Constants from ScanResult.InformationElement class
    public static final int EID_SSID = 0;
    public static final int EID_SUPPORTED_RATES = 1;
    public static final int EID_TIM = 5;
    public static final int EID_COUNTRY = 7;
    public static final int EID_BSS_LOAD = 11;
    public static final int EID_ERP = 42;
    public static final int EID_HT_CAPABILITIES = 45;
    public static final int EID_RSN = 48;
    public static final int EID_EXTENDED_SUPPORTED_RATES = 50;
    public static final int EID_HT_OPERATION = 61;
    public static final int EID_INTERWORKING = 107;
    public static final int EID_ROAMING_CONSORTIUM = 111;
    public static final int EID_EXTENDED_CAPS = 127;
    public static final int EID_VHT_CAPABILITIES = 191;
    public static final int EID_VHT_OPERATION = 192;
    public static final int EID_RNR = 201;
    public static final int EID_VSA = 221;
    public static final int EID_EXTENSION_PRESENT = 255;

    // Extension IDs
    public static final int EID_EXT_HE_CAPABILITIES = 35;
    public static final int EID_EXT_HE_OPERATION = 36;
    public static final int EID_EXT_EHT_OPERATION = 106;
    public static final int EID_EXT_MULTI_LINK = 107;
    public static final int EID_EXT_EHT_CAPABILITIES = 108;

    // Dynamic access to fields of InformationElement
    private static final DynamicField FIELD_ID = new DynamicField(CLASS_INFORMATION_ELEMENT, "id")
            .setAccessible(true);

    private static final DynamicField FIELD_ID_EXT = new DynamicField(CLASS_INFORMATION_ELEMENT, "idExt")
            .setAccessible(true);

    private static final DynamicField FIELD_BYTES = new DynamicField(CLASS_INFORMATION_ELEMENT, "bytes")
            .setAccessible(true);

    // Default constructor method access
    private static final DynamicMethod METHOD_CONSTRUCTOR = new DynamicMethod(CLASS_INFORMATION_ELEMENT, "<init>", int.class, int.class, byte[].class)
            .setAccessible(true);

    /**
     * Creates an empty InformationElement object using reflection
     * @return Empty InformationElement object or null if creation fails
     */
    public static ScanResult.InformationElement createEmptyInformationElement() {
        try {
            // First try to use the default no-args constructor
            Class<?> ieClass = Class.forName(CLASS_INFORMATION_ELEMENT);
            Constructor<?> constructor = ieClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (ScanResult.InformationElement) constructor.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "Failed to Create Empty Information Element! Error=" + e);
            return null;
        }
    }

    /**
     * Creates a single InformationElement with the specified element type
     *
     * @param elementType The element ID type (use constants from this class)
     * @return An InformationElement object or null if creation fails
     */
    public static ScanResult.InformationElement generateInformationElement(int elementType) {
        try {
            // Create data based on element type
            byte[] data;
            int id = elementType;
            int idExt = 0;

            switch (elementType) {
                case EID_SSID:
                    // Generate a random SSID (1-32 bytes)
                    int ssidLength = RandomGenerator.nextInt(1, 33);
                    data = new byte[ssidLength];
                    for (int i = 0; i < ssidLength; i++) {
                        // ASCII printable characters (32-126)
                        data[i] = (byte) RandomGenerator.nextInt(32, 127);
                    }
                    break;

                case EID_SUPPORTED_RATES:
                    // Basic rates - typically 2-8 bytes with rates
                    data = new byte[RandomGenerator.nextInt(2, 9)];
                    for (int i = 0; i < data.length; i++) {
                        // Standard rate values (multiplied by 2)
                        // Common values: 2, 4, 11, 22, 12, 18, 24, 36, 48, 72, 96, 108
                        int[] rates = {2, 4, 11, 22, 12, 18, 24, 36, 48, 72, 96, 108};
                        data[i] = (byte) rates[RandomGenerator.nextInt(rates.length)];
                    }
                    break;

                case EID_RSN:
                    // Basic RSN IE structure
                    data = new byte[20]; // Typical RSN size
                    // Version (2 bytes)
                    data[0] = 1;
                    data[1] = 0;
                    // Group cipher (4 bytes) - usually CCMP
                    data[2] = 0x00;
                    data[3] = 0x0F;
                    data[4] = (byte) 0xAC;  // Cast to byte
                    data[5] = 0x04;
                    // Pairwise cipher count
                    data[6] = 1;
                    data[7] = 0;
                    // Fill remaining bytes with random data
                    for (int i = 8; i < data.length; i++) {
                        data[i] = (byte) RandomGenerator.nextInt(256);
                    }
                    break;

                case EID_HT_CAPABILITIES:
                case EID_VHT_CAPABILITIES:
                    // Capabilities IEs are usually fixed length
                    data = new byte[RandomGenerator.nextInt(10, 25)];
                    for (int i = 0; i < data.length; i++) {
                        data[i] = (byte) RandomGenerator.nextInt(256);
                    }
                    break;

                case EID_EXTENSION_PRESENT:
                    // Extension element
                    idExt = RandomGenerator.nextInt(1, 109); // Random extension ID
                    data = new byte[RandomGenerator.nextInt(1, 32)]; // Random data length
                    for (int i = 0; i < data.length; i++) {
                        data[i] = (byte) RandomGenerator.nextInt(256);
                    }
                    break;

                default:
                    // For other types, just generate random data
                    data = new byte[RandomGenerator.nextInt(1, 32)];
                    for (int i = 0; i < data.length; i++) {
                        data[i] = (byte) RandomGenerator.nextInt(256);
                    }
                    break;
            }

            // Create and populate the InformationElement
            ScanResult.InformationElement ie = createEmptyInformationElement();
            if (ie == null) {
                return null;
            }

            // Set the fields using reflection
            FIELD_ID.trySetValueInstanceEx(ie, id);
            FIELD_ID_EXT.trySetValueInstanceEx(ie, idExt);
            FIELD_BYTES.trySetValueInstanceEx(ie, data);

            if (DebugUtil.isDebug()) {
                Log.d(TAG, "Created InformationElement type=" + id + " idExt=" + idExt + " dataLength=" + data.length);
            }

            return ie;
        } catch (Exception e) {
            Log.e(TAG, "Error creating InformationElement: " + e.getMessage() + "\n" +
                    RuntimeUtils.getStackTraceSafeString(e));
            return null;
        }
    }

    /**
     * Generates an array of InformationElements
     *
     * @param includeBasicElements Whether to include basic elements (SSID, rates, etc.)
     * @param includeExtensions Whether to include extension elements
     * @param additionalElements Additional specific element IDs to include
     * @return An array of InformationElement objects
     */
    public static ScanResult.InformationElement[] generateInformationElements(boolean includeBasicElements,
                                                                              boolean includeExtensions,
                                                                              int... additionalElements) {
        List<ScanResult.InformationElement> elements = new ArrayList<>();

        // Add basic elements commonly found in beacons
        if (includeBasicElements) {
            // SSID is almost always present
            elements.add(generateInformationElement(EID_SSID));

            // Supported rates
            elements.add(generateInformationElement(EID_SUPPORTED_RATES));

            // Security (RSN)
            elements.add(generateInformationElement(EID_RSN));

            // HT Capabilities (common in modern networks)
            elements.add(generateInformationElement(EID_HT_CAPABILITIES));

            // Add some VHT capabilities for 5GHz
            elements.add(generateInformationElement(EID_VHT_CAPABILITIES));
            elements.add(generateInformationElement(EID_VHT_OPERATION));
        }

        // Add extension elements for newer standards
        if (includeExtensions) {
            // Extension present marker (using reflection)
            ScanResult.InformationElement extPresent = createEmptyInformationElement();
            if (extPresent != null) {
                FIELD_ID.trySetValueInstanceEx(extPresent, EID_EXTENSION_PRESENT);
                FIELD_ID_EXT.trySetValueInstanceEx(extPresent, 0);
                FIELD_BYTES.trySetValueInstanceEx(extPresent, new byte[]{0});
                elements.add(extPresent);
            }

            // HE Capabilities (Wi-Fi 6)
            ScanResult.InformationElement heCapabilities = createEmptyInformationElement();
            if (heCapabilities != null) {
                byte[] heData = new byte[RandomGenerator.nextInt(10, 25)];
                for (int i = 0; i < heData.length; i++) {
                    heData[i] = (byte) RandomGenerator.nextInt(256);
                }

                FIELD_ID.trySetValueInstanceEx(heCapabilities, EID_EXTENSION_PRESENT);
                FIELD_ID_EXT.trySetValueInstanceEx(heCapabilities, EID_EXT_HE_CAPABILITIES);
                FIELD_BYTES.trySetValueInstanceEx(heCapabilities, heData);
                elements.add(heCapabilities);
            }

            // EHT Capabilities (Wi-Fi 7) - 50% chance
            if (RandomGenerator.nextInt(2) == 1) {
                ScanResult.InformationElement ehtCapabilities = createEmptyInformationElement();
                if (ehtCapabilities != null) {
                    byte[] ehtData = new byte[RandomGenerator.nextInt(10, 25)];
                    for (int i = 0; i < ehtData.length; i++) {
                        ehtData[i] = (byte) RandomGenerator.nextInt(256);
                    }

                    FIELD_ID.trySetValueInstanceEx(ehtCapabilities, EID_EXTENSION_PRESENT);
                    FIELD_ID_EXT.trySetValueInstanceEx(ehtCapabilities, EID_EXT_EHT_CAPABILITIES);
                    FIELD_BYTES.trySetValueInstanceEx(ehtCapabilities, ehtData);
                    elements.add(ehtCapabilities);
                }
            }
        }

        // Add any additional specific elements requested
        for (int elementId : additionalElements) {
            ScanResult.InformationElement element = generateInformationElement(elementId);
            if (element != null) {
                elements.add(element);
                if(DebugUtil.isDebug())
                    Log.e(TAG, "Adding Information Element=" + Str.toStringOrNull(element));
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Information Elements Size=" + ListUtil.size(elements));

        // Convert list to array - needs to be Object[] for reflection
        return elements.toArray(new ScanResult.InformationElement[0]);
    }
}