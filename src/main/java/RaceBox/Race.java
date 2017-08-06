package main.java.RaceBox;

import main.java.Const.AbilitySkillConstants;
import main.java.Const.Constants;

import java.util.*;

/*
 * Class information used for setting up a character
 */
public class Race {

    private String secondary;
    private Map<AbilitySkillConstants.AbilityEnum, Integer> abilityIncreases;
    private int ageUpperBound;
    private Constants.Size size;
    private int speed;
    private Set<Constants.Language> languages;

    public Race(String secondary, Map<AbilitySkillConstants.AbilityEnum, Integer>
            abilityIncreases, int ageUpperBound, Constants.Size size, int speed, Set<Constants.Language> languages) {
        this.secondary = secondary;
        this.abilityIncreases = abilityIncreases;
        this.ageUpperBound = ageUpperBound;
        this.size = size;
        this.speed = speed;
        this.languages = languages;
    }

    public Optional<String> getSecondary() {
        return Optional.of(secondary);
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
}
