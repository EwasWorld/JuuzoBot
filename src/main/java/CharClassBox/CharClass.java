package main.java.CharClassBox;



import main.java.Const.AbilitySkillConstants;

import java.util.*;

/*
 * Class information used for setting up a character
 */
public class CharClass {
    public static final String fileLocation = "src/CharClassBox/Classes.json";
    public enum ClassEnum {
        BARBARIAN, BARD,
        CLERIC, DRUID,
        FIGHTER, MONK,
        PALADIN, RANGER,
        ROGUE, SORCERER,
        WARLOCK, WIZARD
    }

    private ClassEnum name;
    private Optional<String> secondary;
    private int hitDie;
    private AbilitySkillConstants.AbilityEnum[] abilityOrder;
    private Set<AbilitySkillConstants.AbilityEnum> savingThrows;
    private Set<AbilitySkillConstants.SkillEnum> skillProficiencies;
    private Funds funds;

    public ClassEnum getName() {
        return name;
    }

    public Optional<String> getSecondary() {
        return secondary;
    }

    public int getHitDie() {
        return hitDie;
    }

    public AbilitySkillConstants.AbilityEnum[] getAbilityOrder() {
        return abilityOrder;
    }

    public Set<AbilitySkillConstants.AbilityEnum> getSavingThrows() {
        return savingThrows;
    }

    public Set<AbilitySkillConstants.SkillEnum> getSkillProficiencies() {
        return skillProficiencies;
    }

    public Funds getFunds() {
        return funds;
    }

    private CharClass(ClassEnum name, Optional<String> secondary, int hitDie, AbilitySkillConstants.AbilityEnum[] abilityOrder,
                      Set<AbilitySkillConstants.AbilityEnum> savingThrows, Set<AbilitySkillConstants.SkillEnum> skillProficiencies,
                      Funds funds) {
        this.name = name;
        this.secondary = secondary;
        this.hitDie = hitDie;
        this.abilityOrder = abilityOrder;
        this.savingThrows = savingThrows;
        this.skillProficiencies = skillProficiencies;
        this.funds = funds;
    }

    // TODO Create ClassEnum from ClassJsonFormat instead
    public static List<CharClass> createClassesFromFile(ClassJsonFormat response) throws IllegalArgumentException {
        List<CharClass> classes = new ArrayList<>();
        for (ClassJsonFormat.ClassesTemp classTemp : response.getClasses()) {
            classes.add(new CharClass(
                    ClassEnum.valueOf(classTemp.getName().toUpperCase()),
                    classTemp.getSecondaryType(),
                    classTemp.getHitDie(),
                    createAbilityOrder(classTemp.getAbilityOrder()),
                    createSavingThrows(classTemp.getSavingThrows()),
                    createSkillProficiencies(classTemp.getSkillProficiencies()),
                    classTemp.getFunds()
            ));
        }
        return classes;
    }

    private static AbilitySkillConstants.AbilityEnum[] createAbilityOrder(ClassJsonFormat.ClassesTemp.AbilityOrder abilityOrder) {
        AbilitySkillConstants.AbilityEnum[] newAbilityOrder = new AbilitySkillConstants.AbilityEnum[6];
        newAbilityOrder[abilityOrder.getStr() - 1] = AbilitySkillConstants.AbilityEnum.STRENGTH;
        newAbilityOrder[abilityOrder.getDex() - 1] = AbilitySkillConstants.AbilityEnum.DEXTERITY;
        newAbilityOrder[abilityOrder.getCon() - 1] = AbilitySkillConstants.AbilityEnum.CONSTITUTION;
        newAbilityOrder[abilityOrder.getInte() - 1] = AbilitySkillConstants.AbilityEnum.INTELLIGENCE;
        newAbilityOrder[abilityOrder.getWis() - 1] = AbilitySkillConstants.AbilityEnum.WISDOM;
        newAbilityOrder[abilityOrder.getCha() - 1] = AbilitySkillConstants.AbilityEnum.CHARISMA;
        return newAbilityOrder;
    }

    private static Set<AbilitySkillConstants.AbilityEnum> createSavingThrows(String[] savingThrowStrings)
            throws IllegalArgumentException {
        Set<AbilitySkillConstants.AbilityEnum> savingThrowSet = new HashSet<>();
        for (String abilityStr : savingThrowStrings) {
            savingThrowSet.add(AbilitySkillConstants.AbilityEnum.valueOf(abilityStr.toUpperCase()));
        }
        return savingThrowSet;
    }

    private static Set<AbilitySkillConstants.SkillEnum> createSkillProficiencies(String[] skillProficiencyStrings)
            throws IllegalArgumentException {
        Set<AbilitySkillConstants.SkillEnum> skillProficiencySet = new HashSet<>();
        for (String skillStr : skillProficiencyStrings) {
            skillProficiencySet.add(AbilitySkillConstants.SkillEnum.valueOf(skillStr.toUpperCase()));
        }
        return skillProficiencySet;
    }
}
