package main.java;

import main.java.CharClassBox.Classes;
import main.java.RaceBox.Races;



public class CharacterCreationTests {
    public static void main(String[] args) {
        Classes.getClassInfo(Classes.ClassEnum.FIGHTER);
        Races.getRaceInfo(Races.RaceEnum.HUMAN);
    }
}
