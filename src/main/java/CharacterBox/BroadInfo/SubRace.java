package CharacterBox.BroadInfo;

import CharacterBox.CharacterConstants;
import CharacterBox.DiscordPrintable;

import java.util.HashMap;
import java.util.Map;



public class SubRace extends Race {
    public enum SubRaceEnum implements DiscordPrintable {
        HILL, MOUNTAIN, FOREST, STOUT, ROCK, HIGH, WOOD, DARK, LIGHTFOOT;


        @Override
        public String toPrintableString() {
            String enumStr = super.toString();
            enumStr = enumStr.charAt(0) + enumStr.substring(1).toLowerCase();
            if (enumStr.startsWith("Light")) {
                enumStr = enumStr.substring(0, 5) + "-" + String.valueOf(enumStr.charAt(5)).toUpperCase() + enumStr
                        .substring(6);
            }
            return enumStr;
        }
    }



    private RaceEnum mainRace;
    private Map<CharacterConstants.AbilityEnum, Integer> extraAbilityIncreases;


    public SubRace() {
        extraAbilityIncreases = new HashMap<>();
        for (CharacterConstants.AbilityEnum abilityEnum : CharacterConstants.AbilityEnum.values()) {
            extraAbilityIncreases.put(abilityEnum, 0);
        }
    }


    public SubRace(RaceEnum mainRace, Map<CharacterConstants.AbilityEnum, Integer> extraAbilityIncreases) {
        this.mainRace = mainRace;
        this.extraAbilityIncreases = extraAbilityIncreases;
    }


    public RaceEnum getMainRace() {
        return mainRace;
    }


    public Map<CharacterConstants.AbilityEnum, Integer> getExtraAbilityIncreases() {
        return extraAbilityIncreases;
    }
}
