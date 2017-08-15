package CharacterBox;

public class AbilitySkillConstants {
    public enum AbilityEnum {
        STRENGTH, DEXTERITY,
        CONSTITUTION, INTELLIGENCE,
        WISDOM, CHARISMA
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

    public enum SkillEnum {
        ACROBATICS, ANIMALHANDLING,
        ARCANA, ATHLETICS,
        DECEPTION, HISTORY,
        INSIGHT, INTIMIDATION,
        INVESTIGATION, MEDICINE,
        NATURE, PERCEPTION,
        PERFORMANCE, PERSUASION,
        RELIGION, SLEIGHTOFHAND,
        STEALTH, SURVIVAL
    }

    public static AbilityEnum convertSkillToAbility(SkillEnum skill) {
        switch (skill) {
            case ATHLETICS:
                return AbilityEnum.STRENGTH;
            case ACROBATICS:
            case SLEIGHTOFHAND:
            case STEALTH:
                return AbilityEnum.DEXTERITY;
            case ARCANA:
            case HISTORY:
            case INVESTIGATION:
            case NATURE:
            case RELIGION:
                return AbilityEnum.INTELLIGENCE;
            case ANIMALHANDLING:
            case INSIGHT:
            case MEDICINE:
            case PERCEPTION:
            case SURVIVAL:
                return AbilityEnum.WISDOM;
            case DECEPTION:
            case INTIMIDATION:
            case PERFORMANCE:
            case PERSUASION:
                return AbilityEnum.CHARISMA;
            default:
                return null;
        }
    }

    public static final int[] startingAbilityScores = {
            15, 14, 13, 12, 10, 8
    };

    public static int getModifier(int score) {
        return Math.floorDiv(score, 2) - 5;
    }

    public static int getProficiencyBonus(int level) {
        return Math.floorDiv(level - 1, 4) + 2;
    }
}
