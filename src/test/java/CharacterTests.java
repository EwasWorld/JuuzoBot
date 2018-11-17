import CharacterBox.AttackBox.Weapon;
import CharacterBox.BroadInfo.Background;
import CharacterBox.BroadInfo.Clazz;
import CharacterBox.UserCharacter;
import CharacterBox.BroadInfo.Race;
import CharacterBox.BroadInfo.SubRace;
import CoreBox.IDs;
import ExceptionsBox.BadUserInputException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;



public class CharacterTests extends TestCase {
    public void testClassInfoRetrieval() {
        Clazz.getClassInfo(Clazz.ClassEnum.ROGUE);
    }


    public void testRaceInfoRetrieval() {
        Race.getRaceInfo(Race.RaceEnum.HUMAN);
    }


    public void testWeaponInfoRetrieval() {
        Weapon.getWeaponInfo(Weapon.WeaponsEnum.LONGBOW);
    }


    public void testBackgroundInfoRetrieval() {
        Background.getBackgroundInfo(Background.BackgroundEnum.CRIMINAL);
    }


    public void testCreateCharacter() {
        UserCharacter userCharacter = new UserCharacter("Fi");
        userCharacter.setRace(Race.RaceEnum.ELF);
        userCharacter.setSubRace(SubRace.SubRaceEnum.WOOD);
        userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
        userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
        userCharacter.completeCreation(IDs.eywaID);
    }
}
