package CharacterBox;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;



public class CharacterConstants {
    public enum BackgroundEnum {
        ACOLYTE, CHARLATAN, CRIMINAL, ENTERTAINER, FOLKHERO, GUILDARTISAN,
        HERMIT, NOBLE, OUTLANDER, SAGE, SAILOR, SOLDIER, URCHIN
    }



    public enum Size {TINY, SMALL, MEDIUM, LARGE, HUGE, GARGANTUAN}



    public enum Language {
        COMMON, DWARVISH, ELVISH, GIANT, GNOMISH, GOBLIN,
        HALFLING, ORC, ABYSSAL, CELESTIAL, DRACONIC, DEEPSPEECH,
        INFERNAL, PRIMORDIAL, SYLVAN, UNDERCOMMON, WILDCARD
    }



    public enum AbilityEnum {STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA}



    public enum SkillEnum {
        ACROBATICS, ANIMALHANDLING, ARCANA, ATHLETICS, DECEPTION, HISTORY,
        INSIGHT, INTIMIDATION, INVESTIGATION, MEDICINE, NATURE, PERCEPTION,
        PERFORMANCE, PERSUASION, RELIGION, SLEIGHTOFHAND, STEALTH, SURVIVAL;


        static {
            ATHLETICS.mainAbility = AbilityEnum.STRENGTH;

            ACROBATICS.mainAbility = AbilityEnum.DEXTERITY;
            SLEIGHTOFHAND.mainAbility = AbilityEnum.DEXTERITY;
            STEALTH.mainAbility = AbilityEnum.DEXTERITY;

            ARCANA.mainAbility = AbilityEnum.INTELLIGENCE;
            HISTORY.mainAbility = AbilityEnum.INTELLIGENCE;
            INVESTIGATION.mainAbility = AbilityEnum.INTELLIGENCE;
            NATURE.mainAbility = AbilityEnum.INTELLIGENCE;
            RELIGION.mainAbility = AbilityEnum.INTELLIGENCE;

            ANIMALHANDLING.mainAbility = AbilityEnum.WISDOM;
            INSIGHT.mainAbility = AbilityEnum.WISDOM;
            MEDICINE.mainAbility = AbilityEnum.WISDOM;
            PERCEPTION.mainAbility = AbilityEnum.WISDOM;
            SURVIVAL.mainAbility = AbilityEnum.WISDOM;

            DECEPTION.mainAbility = AbilityEnum.CHARISMA;
            INTIMIDATION.mainAbility = AbilityEnum.CHARISMA;
            PERFORMANCE.mainAbility = AbilityEnum.CHARISMA;
            PERSUASION.mainAbility = AbilityEnum.CHARISMA;
        }


        private AbilityEnum mainAbility;


        public AbilityEnum getMainAbility() {
            return mainAbility;
        }
    }



    public static final int[] startingAbilityScores = {15, 14, 13, 12, 10, 8};


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


    public static AbilityEnum convertAbilityToEnum(String ability) {
        switch (ability) {
            case "Strength":
            case "Str":
                return AbilityEnum.STRENGTH;
            case "Dexterity":
            case "Dex":
                return AbilityEnum.DEXTERITY;
            case "Constitution":
            case "Con":
                return AbilityEnum.CONSTITUTION;
            case "Intelligence":
            case "Int":
            case "Inte":
                return AbilityEnum.INTELLIGENCE;
            case "Wisdom":
            case "Wis":
                return AbilityEnum.WISDOM;
            case "Charisma":
            case "Cha":
                return AbilityEnum.CHARISMA;
            default:
                return null;
        }
    }


    public static int getModifier(int score) {
        return Math.floorDiv(score, 2) - 5;
    }


    public static int getProficiencyBonus(int level) {
        return Math.floorDiv(level - 1, 4) + 2;
    }
}
