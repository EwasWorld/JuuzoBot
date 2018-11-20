import CharacterBox.BroadInfo.Trinkets;
import CoreBox.GrogList;
import junit.framework.TestCase;
import org.junit.Test;



/**
 * refactored: 19/11/18
 */
public class GrogTrinketTests {
    @Test
    public void testPotion() {
        System.out.println(GrogList.drinkGrog("TestAuthor1"));
        System.out.println(GrogList.drinkGrog("TestAuthor1"));
    }

    @Test
    public void testTrinket() {
        System.out.println(Trinkets.getTrinket("TestAuthor2"));
        System.out.println(Trinkets.getTrinket("TestAuthor2"));
    }
}
