import CharacterBox.AttackBox.Weapon;
import CharacterBox.BroadInfo.Background;
import CharacterBox.BroadInfo.Clazz;
import CharacterBox.UserCharacter;
import CharacterBox.BroadInfo.Race;
import CharacterBox.BroadInfo.SubRace;
import CoreBox.IDs;
import DatabaseBox.DatabaseTable;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import junit.framework.TestCase;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;



/**
 * refactored 19/11/18
 */
public class CharacterTests {
    @Before
    public void setup() {
        DatabaseTable.setTestMode();
    }

    @After
    public void teardown() {
        UserCharacter.deleteAllTables();
    }


    @Test
    public void testClassInfoRetrieval() {
        Clazz.getClassInfo(Clazz.ClassEnum.ROGUE);
    }


    @Test
    public void testRaceInfoRetrieval() {
        Race.getRaceInfo(Race.RaceEnum.HUMAN);
        Race.getRaceInfo(SubRace.SubRaceEnum.WOOD);
    }


    @Test
    public void testWeaponInfoRetrieval() {
        Weapon.getWeaponInfo(Weapon.WeaponsEnum.LONGBOW);
    }


    @Test
    public void testBackgroundInfoRetrieval() {
        Background.getBackgroundInfo(Background.BackgroundEnum.CRIMINAL);
    }


    @Test
    public void testCreateCharacter() {
        // Standard character with subrace
        UserCharacter userCharacter = new UserCharacter("Fi");
        userCharacter.setRace(Race.RaceEnum.ELF);
        userCharacter.setSubRace(SubRace.SubRaceEnum.WOOD);
        userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
        userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
        userCharacter.completeCreation(IDs.eywaID);
        System.out.println(userCharacter.getDescription());
        int[] rowCounts = UserCharacter.getRowCounts();
        Assert.assertEquals(1, rowCounts[0]);
        for (int i = 1; i < rowCounts.length; i++) {
            Assert.assertTrue(UserCharacter.getRowCounts()[i] > 0);
        }

        // Standard character without subrace
        userCharacter = new UserCharacter("Jo");
        userCharacter.setRace(Race.RaceEnum.DRAGONBORN);
        userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
        userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
        userCharacter.completeCreation("dfglijdfg");
        System.out.println(userCharacter.getDescription());
        Assert.assertEquals(2,  UserCharacter.getRowCounts()[0]);

        // Standard character with invalid subrace
        int[] startingRowCounts = UserCharacter.getRowCounts();
        boolean exceptionThrown = false;
        try {
            userCharacter = new UserCharacter("Jo");
            userCharacter.setRace(Race.RaceEnum.DRAGONBORN);
            userCharacter.setSubRace(SubRace.SubRaceEnum.WOOD);
            userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
            userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
            userCharacter.completeCreation(IDs.eywaID);
            System.out.println(userCharacter.getDescription());
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        for (int i = 1; i < startingRowCounts.length; i++) {
            Assert.assertEquals(startingRowCounts[i], UserCharacter.getRowCounts()[i]);
        }
    }


    @Test
    public void testDeleteCharacter() {
        final String userID = IDs.eywaID;
        final String name1 = "Fi";
        UserCharacter userCharacter = new UserCharacter(name1);
        userCharacter.setRace(Race.RaceEnum.ELF);
        userCharacter.setSubRace(SubRace.SubRaceEnum.WOOD);
        userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
        userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
        userCharacter.completeCreation(userID);

        final String name2 = "Jo";
        userCharacter = new UserCharacter(name2);
        userCharacter.setRace(Race.RaceEnum.DRAGONBORN);
        userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
        userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
        userCharacter.completeCreation(userID);
        int[] rowCounts = UserCharacter.getRowCounts();
        Assert.assertEquals(2, rowCounts[0]);
        for (int i = 1; i < rowCounts.length; i++) {
            Assert.assertTrue(rowCounts[i] > 0);
        }

        UserCharacter.deleteCharacter(userID, name1);
        int[] newRowCounts = UserCharacter.getRowCounts();
        Assert.assertEquals(1, newRowCounts[0]);
        for (int i = 1; i < newRowCounts.length; i++) {
            Assert.assertTrue(newRowCounts[i] > 0);
            Assert.assertTrue(newRowCounts[i] < rowCounts[i]);
        }

        UserCharacter.getCharacterDescription(userID, name2);
    }


    @Test
    public void testGetCharacterDescription() {
        final String userID = IDs.eywaID;
        final String name1 = "THEMIGHTYGREATFI";
        UserCharacter userCharacter = new UserCharacter(name1);
        userCharacter.setRace(Race.RaceEnum.ELF);
        userCharacter.setSubRace(SubRace.SubRaceEnum.WOOD);
        userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
        userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
        userCharacter.completeCreation(userID);

        final String name2 = "THE MIGHTY GREAT FI";
        userCharacter = new UserCharacter(name2);
        userCharacter.setRace(Race.RaceEnum.DRAGONBORN);
        userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
        userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
        userCharacter.completeCreation(userID);

        Assert.assertTrue(UserCharacter.getCharacterDescription(userID, name1).contains(name1));
        Assert.assertTrue(UserCharacter.getCharacterDescription(userID, name2).contains(name2));

        boolean exceptionThrown = false;
        try {
            UserCharacter.getCharacterDescription("sdfkusjhdg", name2);
        } catch (BadStateException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            UserCharacter.getCharacterDescription(userID, "kujhsdfgkjhsdg");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }


    @Test
    public void testGetWeaponsList() {
        System.out.println(Weapon.getWeaponsList());
    }


    @Test
    public void testChangeWeapon() {
        final String userID = IDs.eywaID;
        final String name = "Fi";
        UserCharacter userCharacter = new UserCharacter(name);
        userCharacter.setRace(Race.RaceEnum.ELF);
        userCharacter.setSubRace(SubRace.SubRaceEnum.WOOD);
        userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
        userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
        userCharacter.completeCreation(userID);
        System.out.println(UserCharacter.getCharacterDescription(userID, name));
        UserCharacter.changeCharacterWeapon(userID, name, Weapon.WeaponsEnum.LIGHTCROSSBOW.toString());
        System.out.println(UserCharacter.getCharacterDescription(userID, name));

        boolean exceptionThrown = false;
        try {
            UserCharacter.changeCharacterWeapon(userID, name, "asdfkujjhdfkg");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }


    @Test
    public void testAttack() {
        final String userID = IDs.eywaID;
        final String name = "Fi";
        UserCharacter userCharacter = new UserCharacter(name);
        userCharacter.setRace(Race.RaceEnum.ELF);
        userCharacter.setSubRace(SubRace.SubRaceEnum.WOOD);
        userCharacter.setClazz(Clazz.ClassEnum.ROGUE);
        userCharacter.setBackground(Background.BackgroundEnum.CRIMINAL);
        userCharacter.completeCreation(userID);
        System.out.println(UserCharacter.attack(userID, name, "Jeff"));
    }
}
