package main.java.CharacterBox.RaceBox;

import main.java.CharacterBox.AbilitySkillConstants;
import main.java.CharacterBox.CharacterConstants;

import java.util.*;

/*
 * Class information used for setting up a character
 */
public class Race {

    private String secondary;
    private Map<AbilitySkillConstants.AbilityEnum, Integer> abilityIncreases;
    private int ageLowerBound;
    private int ageUpperBound;
    private CharacterConstants.Size size;
    private int speed;
    private Set<CharacterConstants.Language> languages;


    public Race(String secondary,
                Map<AbilitySkillConstants.AbilityEnum, Integer> abilityIncreases, int ageLowerBound, int ageUpperBound,
                CharacterConstants.Size size, int speed,
                Set<CharacterConstants.Language> languages)
    {
        if (ageLowerBound >= ageUpperBound) {
            throw new IllegalArgumentException("Lower bound age must be larger than upper bound");
        }

        this.secondary = secondary;
        this.abilityIncreases = abilityIncreases;
        this.ageLowerBound = ageLowerBound;
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


    public int getAgeLowerBound() {
        return ageLowerBound;
    }


    public int getAgeUpperBound() {
        return ageUpperBound;
    }

    public CharacterConstants.Size getSize() {
        return size;
    }

    public int getSpeed() {
        return speed;
    }

    public Set<CharacterConstants.Language> getLanguages() {
        return languages;
    }
}
