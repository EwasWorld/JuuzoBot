package main.java.CharacterBox;

import junit.framework.TestCase;
import main.java.CharacterBox.Attacking.Weapons;
import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Races;



public class CharacterCreationTests extends TestCase {
    public void testClassInfoRetrieval() {
        Classes.getClassInfo(Classes.ClassEnum.FIGHTER);
    }

    public void testRaceInfoRetrieval() {
        Races.getRaceInfo(Races.RaceEnum.HUMAN);
    }

    public void testWeaponInfoRetrieval() {
        Weapons.getWeaponInfo(Weapons.WeaponsEnum.LONGBOW);
    }

    public void testCharacterDescription() {
        System.out.println(new Character("Akatsuki", Races.RaceEnum.ELF, Classes.ClassEnum.ROGUE).getDescription());
    }
}
