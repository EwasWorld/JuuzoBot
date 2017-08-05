package main.java.RaceBox;

import main.java.Const.AbilitySkillConstants;
import main.java.Const.Constants;

import java.util.*;

/*
 * Class information used for setting up a character
 */
public class Race {
    public static final String fileLocation = "src/main.java.RaceBox/Races.json";
    public enum RaceEnum {
        DWARF, ELF,
        HALFLING, HUMAN,
        DRAGONBORN, GNOME,
        HALFELF, HALFORC,
        TIEFLING
    }

    private RaceEnum name;
    private Optional<String> secondary;
    private Map<AbilitySkillConstants.AbilityEnum, Integer> abilityIncreases;
    private int ageUpperBound;
    private Constants.Size size;
    private int speed;
    private Set<Constants.Language> languages;

    private Race(RaceEnum name, Optional<String> secondary, Map<AbilitySkillConstants.AbilityEnum, Integer>
            abilityIncreases, int ageUpperBound, Constants.Size size, int speed, Set<Constants.Language> languages) {
        this.name = name;
        this.secondary = secondary;
        this.abilityIncreases = abilityIncreases;
        this.ageUpperBound = ageUpperBound;
        this.size = size;
        this.speed = speed;
        this.languages = languages;
    }

    public RaceEnum getName() {
        return name;
    }

    public Optional<String> getSecondary() {
        return secondary;
    }

    public Map<AbilitySkillConstants.AbilityEnum, Integer> getAbilityIncreases() {
        return abilityIncreases;
    }

    public int getAgeUpperBound() {
        return ageUpperBound;
    }

    public Constants.Size getSize() {
        return size;
    }

    public int getSpeed() {
        return speed;
    }

    public Set<Constants.Language> getLanguages() {
        return languages;
    }

    public static List<Race> createClassesFromFile(RaceJsonFormat response) throws IllegalArgumentException {
        List<Race> races = new ArrayList<>();
        for (RaceJsonFormat.RacesTemp racesTemp : response.getRaces()) {
            races.add(new Race(
                    RaceEnum.valueOf(racesTemp.getName().toUpperCase()),
                    racesTemp.getSubrace(),
                    createAbilityIncreases(racesTemp.getAbilityIncreases()),
                    racesTemp.getAgeUpperBound(),
                    Constants.Size.valueOf(racesTemp.getSize().toUpperCase()),
                    racesTemp.getSpeed(),
                    createLanguages(racesTemp.getLanguages())
            ));
        }
        return races;
    }

    private static Map<AbilitySkillConstants.AbilityEnum, Integer> createAbilityIncreases(RaceJsonFormat.RacesTemp.AbilityIncreases abilityIncreases) {
        Map<AbilitySkillConstants.AbilityEnum, Integer> abilityIncreasesMap = new HashMap<>();
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.STRENGTH, abilityIncreases.getStr());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.DEXTERITY, abilityIncreases.getDex());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.CONSTITUTION, abilityIncreases.getCon());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.INTELLIGENCE, abilityIncreases.getInte());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.WISDOM, abilityIncreases.getWis());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.CHARISMA, abilityIncreases.getCha());
        return abilityIncreasesMap;
    }

    private static Set<Constants.Language> createLanguages(String[] languagesStrs) throws IllegalArgumentException {
        Set<Constants.Language> languagesSet = new HashSet<>();
        for (String language : languagesStrs) {
            if (!language.equals("WILDCARD")) {
                languagesSet.add(Constants.Language.valueOf(language.toUpperCase()));
            }
            else {
                // TODO Language wildcard
            }
        }
        return languagesSet;
    }
}
