import CharacterBox.AttackBox.Weapon;
import CharacterBox.Character;
import CharacterBox.BroardInfo.Class_;
import CharacterBox.BroardInfo.Race;
import CharacterBox.BroardInfo.SubRace;
import junit.framework.TestCase;



public class CharacterTests extends TestCase {
    public void testClassInfoRetrieval() {
        Class_.getClassInfo(Class_.ClassEnum.FIGHTER);
    }


    public void testRaceInfoRetrieval() {
        Race.getRaceInfo(Race.RaceEnum.HUMAN);
    }


    public void testWeaponInfoRetrieval() {
        Weapon.getWeaponInfo(Weapon.WeaponsEnum.LONGBOW);
    }


    public void testCreateCharacter() {
        System.out.println(new Character("Akatsuki", Race.RaceEnum.ELF, null, Class_.ClassEnum.ROGUE).getDescription());
        System.out.println(
                new Character("Akatsuki", Race.RaceEnum.ELF, SubRace.SubRaceEnum.DARK, Class_.ClassEnum.ROGUE)
                        .getDescription());

        // TODO Test creating from string
    }
}
