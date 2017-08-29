package CharacterBox;

import CharacterBox.BroadInfo.Background;

import java.io.Serializable;



public class UserBackground implements Serializable {
    private String name;
    private String possibility;
    private String trait;
    private String idealName;
    private String idealDescription;
    private String bond;
    private String flaw;


    public UserBackground(String name, Alignment alignment) {
        this.name = name;
        final Background background = Background.getBackgroundInfo(name);

        possibility = background.getRandomPossibility();
        trait = background.getRandomTrait();
        final Background.Ideal ideal = background.getRandomIdeal(alignment);
        idealName = ideal.getName();
        idealDescription = ideal.getDescription();
        bond = background.getRandomBond();
        flaw = background.getRandomFlaw();
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        String flaw = this.flaw;
        if (flaw.charAt(0) != "I".charAt(0)) {
            flaw = String.valueOf(flaw.charAt(0)).toLowerCase() + flaw.substring(1);
        }

        String string = "";
        string += String.format("%s %s", possibility, trait);
        string += String.format(" I'm driven by %s. %s", idealName.toLowerCase(), idealDescription);
        string += String.format(" %s However, %s", bond, flaw);

        return string;
    }
}
