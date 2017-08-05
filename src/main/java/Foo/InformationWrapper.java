package main.java.Foo;

import main.java.CharClassBox.CharClass;
import main.java.Grog.GrogList;
import main.java.RaceBox.Race;

import java.util.List;

public class InformationWrapper {
    private List<CharClass> classes;
    private List<Race> races;
    private GrogList grogList;

    public InformationWrapper(List<CharClass> classes, List<Race> races, GrogList grogList) {
        this.classes = classes;
        this.races = races;
        this.grogList = grogList;
    }

    public List<CharClass> getClasses() {
        return classes;
    }

    public List<Race> getRaces() {
        return races;
    }

    public GrogList getGrogList() {
        return grogList;
    }
}
