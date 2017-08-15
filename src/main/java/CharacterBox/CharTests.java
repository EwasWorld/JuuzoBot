package CharacterBox;

import CharacterBox.AttackBox.Weapon;
import CharacterBox.ClassBox.Class_;
import junit.framework.TestCase;
import CharacterBox.RaceBox.Race;
import CharacterBox.RaceBox.SubRace;



public class CharTests extends TestCase {
    public void testClassInfoRetrieval() {
        Class_.getClassInfo(Class_.ClassEnum.FIGHTER);
    }

    public void testRaceInfoRetrieval() {
        Race.getRaceInfo(Race.RaceEnum.HUMAN);
    }

    public void testWeaponInfoRetrieval() {
        Weapon.getWeaponInfo(Weapon.WeaponsEnum.LONGBOW);
    }

    public void testCharacterDescription() {
        System.out.println(new Character("Akatsuki", Race.RaceEnum.ELF, null, Class_.ClassEnum.ROGUE).getDescription());
        System.out.println(new Character("Akatsuki", Race.RaceEnum.ELF, SubRace.SubRaceEnum.DARK, Class_.ClassEnum.ROGUE).getDescription());
    }
}
