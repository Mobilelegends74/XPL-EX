package eu.faircode.xlua.x.xlua.settings.random.randomizers.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomLocCountryIso extends RandomElement {
    // Static map for ISO to calling code mapping
    private static final Map<String, String> countryCallingCodes = new HashMap<>();

    static {
        // Initialize country calling codes
        initializeCountryCallingCodes();
    }

    /**
     * Initializes the map of ISO country codes to calling codes
     */
    /**
     * Initializes the map of ISO country codes to calling codes
     */
    private static void initializeCountryCallingCodes() {
        // North America (NANP countries)
        countryCallingCodes.put("US", "01");    // United States
        countryCallingCodes.put("CA", "01");    // Canada
        countryCallingCodes.put("BS", "1242"); // Bahamas
        countryCallingCodes.put("BB", "1246"); // Barbados
        countryCallingCodes.put("AI", "1264"); // Anguilla
        countryCallingCodes.put("AG", "1268"); // Antigua and Barbuda
        countryCallingCodes.put("VG", "1284"); // British Virgin Islands
        countryCallingCodes.put("VI", "1340"); // US Virgin Islands
        countryCallingCodes.put("KY", "1345"); // Cayman Islands
        countryCallingCodes.put("BM", "1441"); // Bermuda
        countryCallingCodes.put("GD", "1473"); // Grenada
        countryCallingCodes.put("TC", "1649"); // Turks and Caicos Islands
        countryCallingCodes.put("MS", "1664"); // Montserrat
        countryCallingCodes.put("MP", "1670"); // Northern Mariana Islands
        countryCallingCodes.put("GU", "1671"); // Guam
        countryCallingCodes.put("AS", "1684"); // American Samoa
        countryCallingCodes.put("LC", "1758"); // Saint Lucia
        countryCallingCodes.put("DM", "1767"); // Dominica
        countryCallingCodes.put("VC", "1784"); // Saint Vincent and the Grenadines
        countryCallingCodes.put("PR", "1787"); // Puerto Rico
        countryCallingCodes.put("DO", "1809"); // Dominican Republic
        countryCallingCodes.put("TT", "1868"); // Trinidad and Tobago
        countryCallingCodes.put("KN", "1869"); // Saint Kitts and Nevis
        countryCallingCodes.put("JM", "1876"); // Jamaica

        // Europe
        countryCallingCodes.put("RU", "07");    // Russia
        countryCallingCodes.put("KZ", "07");    // Kazakhstan
        countryCallingCodes.put("EG", "20");   // Egypt
        countryCallingCodes.put("ZA", "27");   // South Africa
        countryCallingCodes.put("GR", "30");   // Greece
        countryCallingCodes.put("NL", "31");   // Netherlands
        countryCallingCodes.put("BE", "32");   // Belgium
        countryCallingCodes.put("FR", "33");   // France
        countryCallingCodes.put("ES", "34");   // Spain
        countryCallingCodes.put("HU", "36");   // Hungary
        countryCallingCodes.put("IT", "39");   // Italy
        countryCallingCodes.put("RO", "40");   // Romania
        countryCallingCodes.put("CH", "41");   // Switzerland
        countryCallingCodes.put("AT", "43");   // Austria
        countryCallingCodes.put("GB", "44");   // United Kingdom
        countryCallingCodes.put("DK", "45");   // Denmark
        countryCallingCodes.put("SE", "46");   // Sweden
        countryCallingCodes.put("NO", "47");   // Norway
        countryCallingCodes.put("PL", "48");   // Poland
        countryCallingCodes.put("DE", "49");   // Germany

        // Americas
        countryCallingCodes.put("PE", "51");   // Peru
        countryCallingCodes.put("MX", "52");   // Mexico
        countryCallingCodes.put("CU", "53");   // Cuba
        countryCallingCodes.put("AR", "54");   // Argentina
        countryCallingCodes.put("BR", "55");   // Brazil
        countryCallingCodes.put("CL", "56");   // Chile
        countryCallingCodes.put("CO", "57");   // Colombia
        countryCallingCodes.put("VE", "58");   // Venezuela
        countryCallingCodes.put("GY", "592");  // Guyana
        countryCallingCodes.put("BO", "591");  // Bolivia
        countryCallingCodes.put("EC", "593");  // Ecuador
        countryCallingCodes.put("PY", "595");  // Paraguay
        countryCallingCodes.put("SR", "597");  // Suriname
        countryCallingCodes.put("UY", "598");  // Uruguay

        // Asia, Oceania
        countryCallingCodes.put("MY", "60");   // Malaysia
        countryCallingCodes.put("AU", "61");   // Australia
        countryCallingCodes.put("ID", "62");   // Indonesia
        countryCallingCodes.put("PH", "63");   // Philippines
        countryCallingCodes.put("NZ", "64");   // New Zealand
        countryCallingCodes.put("SG", "65");   // Singapore
        countryCallingCodes.put("TH", "66");   // Thailand
        countryCallingCodes.put("JP", "81");   // Japan
        countryCallingCodes.put("KR", "82");   // South Korea
        countryCallingCodes.put("VN", "84");   // Vietnam
        countryCallingCodes.put("CN", "86");   // China
        countryCallingCodes.put("TR", "90");   // Turkey
        countryCallingCodes.put("IN", "91");   // India
        countryCallingCodes.put("PK", "92");   // Pakistan
        countryCallingCodes.put("AF", "93");   // Afghanistan
        countryCallingCodes.put("LK", "94");   // Sri Lanka
        countryCallingCodes.put("MM", "95");   // Myanmar
        countryCallingCodes.put("IR", "98");   // Iran

        // Europe (continued)
        countryCallingCodes.put("AL", "355");  // Albania
        countryCallingCodes.put("MT", "356");  // Malta
        countryCallingCodes.put("CY", "357");  // Cyprus
        countryCallingCodes.put("FI", "358");  // Finland
        countryCallingCodes.put("BG", "359");  // Bulgaria
        countryCallingCodes.put("LT", "370");  // Lithuania
        countryCallingCodes.put("LV", "371");  // Latvia
        countryCallingCodes.put("EE", "372");  // Estonia
        countryCallingCodes.put("MD", "373");  // Moldova
        countryCallingCodes.put("AM", "374");  // Armenia
        countryCallingCodes.put("BY", "375");  // Belarus
        countryCallingCodes.put("AD", "376");  // Andorra
        countryCallingCodes.put("MC", "377");  // Monaco
        countryCallingCodes.put("SM", "378");  // San Marino
        countryCallingCodes.put("VA", "379");  // Vatican City
        countryCallingCodes.put("UA", "380");  // Ukraine
        countryCallingCodes.put("RS", "381");  // Serbia
        countryCallingCodes.put("ME", "382");  // Montenegro
        countryCallingCodes.put("HR", "385");  // Croatia
        countryCallingCodes.put("SI", "386");  // Slovenia
        countryCallingCodes.put("BA", "387");  // Bosnia and Herzegovina
        countryCallingCodes.put("MK", "389");  // North Macedonia
        countryCallingCodes.put("CZ", "420");  // Czech Republic
        countryCallingCodes.put("SK", "421");  // Slovakia
        countryCallingCodes.put("LI", "423");  // Liechtenstein
        countryCallingCodes.put("LU", "352");  // Luxembourg
        countryCallingCodes.put("IE", "353");  // Ireland
        countryCallingCodes.put("IS", "354");  // Iceland
        countryCallingCodes.put("PT", "351");  // Portugal

        // Africa
        countryCallingCodes.put("MA", "212");  // Morocco
        countryCallingCodes.put("DZ", "213");  // Algeria
        countryCallingCodes.put("TN", "216");  // Tunisia
        countryCallingCodes.put("LY", "218");  // Libya
        countryCallingCodes.put("GM", "220");  // Gambia
        countryCallingCodes.put("SN", "221");  // Senegal
        countryCallingCodes.put("MR", "222");  // Mauritania
        countryCallingCodes.put("ML", "223");  // Mali
        countryCallingCodes.put("GN", "224");  // Guinea
        countryCallingCodes.put("CI", "225");  // Ivory Coast
        countryCallingCodes.put("BF", "226");  // Burkina Faso
        countryCallingCodes.put("NE", "227");  // Niger
        countryCallingCodes.put("TG", "228");  // Togo
        countryCallingCodes.put("BJ", "229");  // Benin
        countryCallingCodes.put("MU", "230");  // Mauritius
        countryCallingCodes.put("LR", "231");  // Liberia
        countryCallingCodes.put("SL", "232");  // Sierra Leone
        countryCallingCodes.put("GH", "233");  // Ghana
        countryCallingCodes.put("NG", "234");  // Nigeria
        countryCallingCodes.put("TD", "235");  // Chad
        countryCallingCodes.put("CF", "236");  // Central African Republic
        countryCallingCodes.put("CM", "237");  // Cameroon
        countryCallingCodes.put("CV", "238");  // Cape Verde
        countryCallingCodes.put("ST", "239");  // São Tomé and Príncipe
        countryCallingCodes.put("GQ", "240");  // Equatorial Guinea
        countryCallingCodes.put("GA", "241");  // Gabon
        countryCallingCodes.put("CG", "242");  // Republic of the Congo
        countryCallingCodes.put("CD", "243");  // DR Congo
        countryCallingCodes.put("AO", "244");  // Angola
        countryCallingCodes.put("GW", "245");  // Guinea-Bissau
        countryCallingCodes.put("IO", "246");  // British Indian Ocean Territory
        countryCallingCodes.put("SC", "248");  // Seychelles
        countryCallingCodes.put("SD", "249");  // Sudan
        countryCallingCodes.put("RW", "250");  // Rwanda
        countryCallingCodes.put("ET", "251");  // Ethiopia
        countryCallingCodes.put("SO", "252");  // Somalia
        countryCallingCodes.put("DJ", "253");  // Djibouti
        countryCallingCodes.put("KE", "254");  // Kenya
        countryCallingCodes.put("TZ", "255");  // Tanzania
        countryCallingCodes.put("UG", "256");  // Uganda
        countryCallingCodes.put("BI", "257");  // Burundi
        countryCallingCodes.put("MZ", "258");  // Mozambique
        countryCallingCodes.put("ZM", "260");  // Zambia
        countryCallingCodes.put("MG", "261");  // Madagascar
        countryCallingCodes.put("RE", "262");  // Réunion
        countryCallingCodes.put("ZW", "263");  // Zimbabwe
        countryCallingCodes.put("NA", "264");  // Namibia
        countryCallingCodes.put("MW", "265");  // Malawi
        countryCallingCodes.put("LS", "266");  // Lesotho
        countryCallingCodes.put("BW", "267");  // Botswana
        countryCallingCodes.put("SZ", "268");  // Eswatini
        countryCallingCodes.put("KM", "269");  // Comoros

        // Middle East
        countryCallingCodes.put("IQ", "964");  // Iraq
        countryCallingCodes.put("KW", "965");  // Kuwait
        countryCallingCodes.put("SA", "966");  // Saudi Arabia
        countryCallingCodes.put("YE", "967");  // Yemen
        countryCallingCodes.put("OM", "968");  // Oman
        countryCallingCodes.put("PS", "970");  // Palestine
        countryCallingCodes.put("AE", "971");  // United Arab Emirates
        countryCallingCodes.put("IL", "972");  // Israel
        countryCallingCodes.put("BH", "973");  // Bahrain
        countryCallingCodes.put("QA", "974");  // Qatar
        countryCallingCodes.put("BT", "975");  // Bhutan
        countryCallingCodes.put("MN", "976");  // Mongolia
        countryCallingCodes.put("NP", "977");  // Nepal
        countryCallingCodes.put("TJ", "992");  // Tajikistan
        countryCallingCodes.put("TM", "993");  // Turkmenistan
        countryCallingCodes.put("AZ", "994");  // Azerbaijan
        countryCallingCodes.put("GE", "995");  // Georgia
        countryCallingCodes.put("KG", "996");  // Kyrgyzstan
        countryCallingCodes.put("UZ", "998");  // Uzbekistan

        // Pacific Islands
        countryCallingCodes.put("FJ", "679");  // Fiji
        countryCallingCodes.put("PW", "680");  // Palau
        countryCallingCodes.put("WF", "681");  // Wallis and Futuna
        countryCallingCodes.put("CK", "682");  // Cook Islands
        countryCallingCodes.put("NU", "683");  // Niue
        countryCallingCodes.put("WS", "685");  // Samoa
        countryCallingCodes.put("KI", "686");  // Kiribati
        countryCallingCodes.put("NC", "687");  // New Caledonia
        countryCallingCodes.put("TV", "688");  // Tuvalu
        countryCallingCodes.put("PF", "689");  // French Polynesia
        countryCallingCodes.put("TK", "690");  // Tokelau
        countryCallingCodes.put("FM", "691");  // Micronesia
        countryCallingCodes.put("MH", "692");  // Marshall Islands
    }

    /**
     * Gets the country calling code for a given ISO country code
     *
     * @param isoCountryCode The ISO 3166-1 alpha-2 country code
     * @return The calling code or null if not found
     */
    public static String getCountryCallingCode(String isoCountryCode) {
        if (isoCountryCode == null || isoCountryCode.isEmpty()) {
            return null;
        }
        return countryCallingCodes.get(isoCountryCode.toUpperCase());
    }

    public static Map<String, String> createCountryIsoMap() {
        Map<String, String> countryMap = new TreeMap<>();

        Locale[] locales = Locale.getAvailableLocales();
        Set<String> addedCountryCodes = new HashSet<>();
        for (Locale locale : locales) {
            String countryCode = locale.getCountry();
            if (!Str.isEmpty(countryCode) && !addedCountryCodes.contains(countryCode) && countryCallingCodes.containsKey(countryCode)) {
                // Get the country name in English
                String countryName = locale.getDisplayCountry(Locale.ENGLISH);
                if (!countryName.isEmpty() && !countryName.equals(countryCode)) {
                    countryMap.put(countryName, countryCode);
                    addedCountryCodes.add(countryCode);
                }
            }
        }

        // To ensure we include major countries with mobile networks explicitly
        Map<String, String> majorCountries = new HashMap<>();
        majorCountries.put("United States", "US");
        majorCountries.put("United Kingdom", "GB");
        majorCountries.put("Canada", "CA");
        majorCountries.put("Australia", "AU");
        majorCountries.put("Germany", "DE");
        majorCountries.put("France", "FR");
        majorCountries.put("Japan", "JP");
        majorCountries.put("China", "CN");
        majorCountries.put("India", "IN");
        majorCountries.put("Brazil", "BR");
        majorCountries.put("Russia", "RU");
        majorCountries.put("Mexico", "MX");
        majorCountries.put("South Korea", "KR");
        majorCountries.put("Saudi Arabia", "SA");
        majorCountries.put("South Africa", "ZA");
        majorCountries.put("United Arab Emirates", "AE");
        majorCountries.put("Singapore", "SG");
        majorCountries.put("Spain", "ES");
        majorCountries.put("Italy", "IT");
        majorCountries.put("Netherlands", "NL");
        // Add any missing major countries
        countryMap.putAll(majorCountries);
        return countryMap;
    }

    public RandomLocCountryIso() {
        super("Country ISO 3166-1");
        putIndexSettings(RandomizersCache.SETTING_SIM_COUNTRY_ISO, 1, 2);
        List<IRandomizer> ops = new ArrayList<>();
        ops.add(RandomOptionNullElement.create());
        for(Map.Entry<String, String> entry : createCountryIsoMap().entrySet()) ops.add(RandomOptionString.create(entry.getKey(), entry.getValue()));
        putOptions(ArrayUtils.toArray(ops, IRandomizer.class));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        randomOption(true).randomize(context);
    }
}