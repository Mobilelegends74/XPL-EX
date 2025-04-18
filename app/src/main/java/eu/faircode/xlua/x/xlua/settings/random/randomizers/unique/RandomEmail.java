package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomEmail extends RandomElement {
    public static List<String> DOMAINS = Arrays.asList(
            // ✅ Mainstream Email Providers
            "@gmail.com",
            "@yahoo.com",
            "@hotmail.com",
            "@outlook.com",
            "@live.com",
            "@msn.com",
            "@icloud.com",
            "@me.com",
            "@mac.com",
            "@aol.com",
            "@protonmail.com",
            "@tutanota.com",
            "@zoho.com",
            "@mail.com",
            "@gmx.com",
            "@yandex.com",
            "@pm.me",
            "@hey.com",
            "@fastmail.com",

            // ✅ Country-specific or Regional Providers
            "@web.de",
            "@gmx.de",
            "@orange.fr",
            "@laposte.net",
            "@libero.it",
            "@alice.it",
            "@t-online.de",
            "@seznam.cz",
            "@naver.com",
            "@daum.net",
            "@qq.com",
            "@126.com",
            "@163.com",
            "@sina.com",
            "@rediffmail.com",
            "@indiatimes.com",
            "@bigpond.com",
            "@shaw.ca",
            "@bell.net",
            "@sympatico.ca",

            // ✅ Educational / Institutional (generic)
            "@edu.com",
            "@edu.au",
            "@ac.uk",
            "@edu.ph",
            "@university.edu",
            "@college.edu",
            "@school.edu",

            // ✅ Developer/Tech-friendly Providers
            "@pm.me",
            "@disroot.org",
            "@riseup.net",
            "@openmailbox.org",
            "@nym.hush.com",

            // 🐓 Cock.li Domains (NSFW-ish or humorous themed)
            // -- Cock.li block starts here --
            "@cock.li",
            "@firemail.cc",
            "@airmail.cc",
            "@nuke.africa",
            "@420blaze.it",
            "@trollmail.net",
            "@memeware.net",
            "@cocaine.ninja",
            "@cumallover.me",
            "@fbi-agent.info",
            "@goat.si",
            "@horsefucker.org",
            "@is.not.moe",
            "@ivory.email",
            "@lel.fail",
            "@mail.thegpm.org",
            "@neet.email",
            "@nice.knowledge",
            "@openmail.ink",
            "@redneck.me",
            "@tfwno.gf",
            "@vaporwave.gq",
            "@weed.lu",
            "@yiff.life"
            // -- Cock.li block ends here --
    );


    public static List<String> PREFIXES = Arrays.asList(
            // Common names
            "john",
            "jane",
            "mike",
            "michael",
            "emily",
            "emma",
            "dan",
            "danny",
            "sarah",
            "sam",
            "steve",
            "steven",
            "jess",
            "jessica",
            "alex",
            "alexander",
            "anna",
            "lucas",
            "luke",
            "kate",
            "katie",
            "nate",
            "nathan",
            "liz",
            "lisa",
            "mark",
            "marcus",
            "eric",
            "erika",
            "brian",
            "bryan",
            "laura",
            "lauren",
            "kevin",
            "karen",

            // Stylized/internet-style
            "xXdarklordXx",
            "1337haxor",
            "admin",
            "root",
            "noobmaster69",
            "ghost",
            "shadow",
            "ninja",
            "sniper",
            "hacker",
            "cyberwolf",
            "crimsonblade",
            "pixelpunch",
            "matrixman",
            "voidwalker",
            "zero",

            // Nicknames and initials
            "tj",
            "aj",
            "mj",
            "cj",
            "dj",
            "kb",
            "jsmith",
            "bobby",
            "tom",
            "tommy",
            "jan",
            "jil",
            "debra",
            "jimmy",
            "joey",
            "al",
            "sal",
            "ben",
            "chris",
            "christy",
            "dave",
            "ron",
            "ricky",

            // Fun or short
            "zuzu",
            "papa",
            "lulu",
            "toto",
            "pip",
            "moe",
            "zee",
            "obi",
            "rex",
            "ace"
    );

    public RandomEmail() {
        super("Email");
        putSettings(RandomizersCache.SETTING_EMAIL);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //Was 16 Char Length
        String setting = context.stack.pop();
        if(setting == null)
            return;

        boolean uidBased = RandomGenerator.chance();
        if(uidBased) {
            context.pushSpecial(setting, Str.combine(UUID.randomUUID().toString(), RandomGenerator.nextElement(DOMAINS)));
            return;
        }

        boolean randomStringBased = RandomGenerator.chance();
        if(randomStringBased) {
            context.pushSpecial(setting, Str.combine(RandomGenerator.nextString(9, 25), RandomGenerator.nextElement(DOMAINS)));
            return;
        }

        boolean hexStringBased = RandomGenerator.chance();
        if(hexStringBased) {
            context.pushSpecial(setting, Str.combine(RandomGenerator.nextStringHex(9, 25), RandomGenerator.nextElement(DOMAINS)));
            return;
        }

        List<String> items = RandomGenerator.nextElements(PREFIXES);
        StringBuilder prefix = new StringBuilder();
        for(int i = 0; i < Math.min(6, items.size()); i++) {
            String item = items.get(i);
            if(prefix.length() > 0) {
                boolean isSep = RandomGenerator.chance();
                if(isSep) {
                    boolean isUnder = RandomGenerator.chance();
                    if(isUnder) prefix.append("_");
                    else prefix.append("-");
                    prefix.append(item);
                } else {
                    prefix.append(item);
                }
            } else {
                prefix.append(item);
            }

        }

        context.pushSpecial(setting,
                Str.combine(
                        prefix.toString(),
                        RandomGenerator.nextElement(DOMAINS)));
    }
}