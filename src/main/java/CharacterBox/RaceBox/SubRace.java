package CharacterBox.RaceBox;

import CharacterBox.AbilitySkillConstants;
import CharacterBox.Abilities;

import java.util.HashMap;
import java.util.Map;



public class SubRace extends Race {
    public enum SubRaceEnum {
        HILL, MOUNTAIN, FOREST, STOUT, ROCK, HIGH, WOOD, DARK, LIGHTFOOT;
    }

    private Races.RaceEnum mainRace;
    private Abilities extraAbilityIncreases;


    public SubRace() {
        final Map<AbilitySkillConstants.AbilityEnum, Integer> abilities = new HashMap<>();
        for (AbilitySkillConstants.AbilityEnum abilityEnum : AbilitySkillConstants.AbilityEnum.values()) {
            abilities.put(abilityEnum, 0);
        }
        extraAbilityIncreases = new Abilities(abilities);
    }


    public SubRace(Races.RaceEnum mainRace, Abilities extraAbilityIncreases)
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
