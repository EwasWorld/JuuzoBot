package CharacterBox.BroardInfo;

import CharacterBox.Abilities;
import CharacterBox.CharacterConstants;

import java.util.HashMap;
import java.util.Map;



public class SubRace extends Race {
    public enum SubRaceEnum {HILL, MOUNTAIN, FOREST, STOUT, ROCK, HIGH, WOOD, DARK, LIGHTFOOT;}



    private RaceEnum mainRace;
    private Abilities extraAbilityIncreases;


    public SubRace() {
        final Map<CharacterConstants.AbilityEnum, Integer> abilities = new HashMap<>();
        for (CharacterConstants.AbilityEnum abilityEnum : CharacterConstants.AbilityEnum.values()) {
            abilities.put(abilityEnum, 0);
        }
        extraAbilityIncreases = new Abilities(abilities);
    }


    public SubRace(RaceEnum mainRace, Abilities extraAbilityIncreases)
    {
        this.mainRace = mainRace;
        this.extraAbilityIncreases = extraAbilityIncreases;
    }


    public RaceEnum getMainRace() {
        return mainRace;
    }


    public int getExtraAbilityIncreases(CharacterConstants.AbilityEnum ability) {
        return extraAbilityIncreases.getStat(ability);
    }
}
