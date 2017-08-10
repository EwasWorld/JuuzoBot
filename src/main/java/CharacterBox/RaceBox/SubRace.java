package main.java.CharacterBox.RaceBox;

import main.java.CharacterBox.AbilitySkillConstants;
import main.java.CharacterBox.CharacterAbilities;

import java.util.HashMap;
import java.util.Map;



public class SubRace extends Race {
    public enum SubRaceEnum {
        HILL, MOUNTAIN, FOREST, STOUT, ROCK, HIGH, WOOD, DARK, LIGHTFOOT;
    }

    private Races.RaceEnum mainRace;
    private CharacterAbilities extraAbilityIncreases;


    public SubRace() {
        final Map<AbilitySkillConstants.AbilityEnum, Integer> abilities = new HashMap<>();
        for (AbilitySkillConstants.AbilityEnum abilityEnum : AbilitySkillConstants.AbilityEnum.values()) {
            abilities.put(abilityEnum, 0);
        }
        extraAbilityIncreases = new CharacterAbilities(abilities);
    }


    public SubRace(Races.RaceEnum mainRace, CharacterAbilities extraAbilityIncreases)
    {
        this.mainRace = mainRace;
        this.extraAbilityIncreases = extraAbilityIncreases;
    }


    public Races.RaceEnum getMainRace() {
        return mainRace;
    }


    public int getExtraAbilityIncreases(AbilitySkillConstants.AbilityEnum ability) {
        return extraAbilityIncreases.getStat(ability);
    }
}
