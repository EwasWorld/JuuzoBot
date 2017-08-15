package CharacterBox;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;



public class CharacterConstants {
    public enum BackgroundEnum {
        ACOLYTE, CHARLATAN,
        CRIMINAL, ENTERTAINER,
        FOLKHERO, GUILDARTISAN,
        HERMIT, NOBLE,
        OUTLANDER, SAGE,
        SAILOR, SOLDIER,
        URCHIN
    }

    public enum Size {
        TINY, SMALL,
        MEDIUM, LARGE,
        HUGE, GARGANTUAN
    }

    public enum Language {
        COMMON, DWARVISH,
        ELVISH, GIANT,
        GNOMISH, GOBLIN,
        HALFLING, ORC,
        ABYSSAL, CELESTIAL,
        DRACONIC, DEEPSPEECH,
        INFERNAL, PRIMORDIAL,
        SYLVAN, UNDERCOMMON,
        WILDCARD
    }


    /*
     * Returns a random language (other than WILDCARD)
     */
    public static Language getRandomLanguage() {
        return getRandomLanguage(new HashSet<>());
    }


    /*
     * Returns a random language (other than WILDCARD) that is not in the usedLanguages set
     */
    public static Language getRandomLanguage(Set<Language> usedLanguages) {
        final Set<Language> languages = new HashSet<>(Arrays.asList(Language.values()));
        languages.removeAll(usedLanguages);
        languages.remove(Language.WILDCARD);
        return (Language) languages.toArray()[new Random().nextInt(languages.size())];
    }


    public static int getProficiencyBonus(int level) {
        switch (level) {
            default:
                return 2;
        }
    }
}
