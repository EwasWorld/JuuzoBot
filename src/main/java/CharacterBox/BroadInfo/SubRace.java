package CharacterBox.BroadInfo;

import CharacterBox.CharacterConstants;

import java.util.HashMap;
import java.util.Map;



public class SubRace extends Race {
    public enum SubRaceEnum {HILL, MOUNTAIN, FOREST, STOUT, ROCK, HIGH, WOOD, DARK, LIGHTFOOT;}



    private RaceEnum mainRace;
    private Map<CharacterConstants.AbilityEnum, Integer> extraAbilityIncreases;


    public SubRace() {
        extraAbilityIncreases = new HashMap<>();
        for (CharacterConstants.AbilityEnum abilityEnum : CharacterConstants.AbilityEnum.values()) {
            extraAbilityIncreases.put(abilityEnum, 0);
        }
    }


    public SubRace(RaceEnum mainRace, Map<CharacterConstants.AbilityEnum, Integer> extraAbilityIncreases)
    {
        this.mainRace = mainRace;
        this.extraAbilityIncreases = extraAbilityIncreases;
    }


    public RaceEnum getMainRace() {
        return mainRace;
    }


    public Map<CharacterConstants.AbilityEnum, Integer> getExtraAbilityIncreases(
            CharacterConstants.AbilityEnum ability)
    {
        return extraAbilityIncreases;
    }
}
