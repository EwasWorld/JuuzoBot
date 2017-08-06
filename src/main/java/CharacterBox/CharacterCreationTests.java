package main.java.CharacterBox;

import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Races;



public class CharacterCreationTests {
    public static void main(String[] args) {
        Classes.getClassInfo(Classes.ClassEnum.FIGHTER);
        Races.getRaceInfo(Races.RaceEnum.HUMAN);
    }
}
