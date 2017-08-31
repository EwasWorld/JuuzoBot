import CharacterBox.AttackBox.Weapon;
import CharacterBox.BroadInfo.Background;
import CharacterBox.UserCharacter;
import CharacterBox.BroadInfo.Class_;
import CharacterBox.BroadInfo.Race;
import CharacterBox.BroadInfo.SubRace;
import ExceptionsBox.BadUserInputException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;



public class CharacterTests extends TestCase {
    public void testClassInfoRetrieval() {
        Class_.getClassInfo(Class_.ClassEnum.ROGUE);
    }


    public void testRaceInfoRetrieval() {
        Race.getRaceInfo(Race.RaceEnum.HUMAN);
    }


    public void testWeaponInfoRetrieval() {
        Weapon.getWeaponInfo(Weapon.WeaponsEnum.LONGBOW);
    }


    public void testBackgroundInfoRetrieval() {
        Background.getBackgroundInfo("Criminal");
    }


    public void testCreateCharacter() {
        System.out.println(new UserCharacter(
                "Akatsuki", Race.RaceEnum.ELF, null, Class_.ClassEnum.ROGUE, "Criminal").getDescription());
        System.out.println(new UserCharacter(
                "Akatsuki", Race.RaceEnum.ELF, SubRace.SubRaceEnum.DARK, Class_.ClassEnum.ROGUE, "Criminal"
        ).getDescription());
        System.out.println("\n\n");

        List<String> testStrings = new ArrayList<>();
        testStrings.add("Fi");
        testStrings.add("Fi Elf");
        testStrings.add("Fi Wood Elf");
        testStrings.add("Fi Wood Elf Rogue");
        testStrings.add("Fi Drow Rogue Criminal");
        testStrings.add("Fi Drow Elf Rogue Criminal");
        testStrings.add("Fi Wood Elf Rogue Criminal");
        testStrings.add("Fi Wood Elf Criminal Rogue");
        testStrings.add("Fi Elf Wood Criminal Rogue");

        // TODO Shouldn't have to manually check the output...
        for (String testString : testStrings) {
            System.out.println("\n\nTest String: " + testString);
            try {
                System.out.println(UserCharacter.createUserCharacter(testString).getDescription());
            } catch (BadUserInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
