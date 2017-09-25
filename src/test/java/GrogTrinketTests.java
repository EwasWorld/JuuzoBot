import CharacterBox.BroadInfo.Trinkets;
import CoreBox.GrogList;
import junit.framework.TestCase;



public class GrogTrinketTests extends TestCase {
    public void testPotion() {
        System.out.println(GrogList.drinkGrog("TestAuthor"));
    }

    public void testTrinket() {
        System.out.println(Trinkets.getTrinket("TestAuthor"));
    }
}
