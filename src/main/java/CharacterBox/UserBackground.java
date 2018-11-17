package CharacterBox;

import CharacterBox.BroadInfo.Background;

import java.io.Serializable;



public class UserBackground {
    private Background.BackgroundEnum background;
    private int possibility;
    private int trait;
    private int ideal;
    private int bond;
    private int flaw;


    public UserBackground(Background.BackgroundEnum background, int possibility, int trait, int ideal, int bond, int flaw) {
        this.background = background;
        this.possibility = possibility;
        this.trait = trait;
        this.ideal = ideal;
        this.bond = bond;
        this.flaw = flaw;
    }

    public UserBackground(Background.BackgroundEnum background, String specific) {
        this.background = background;
        final String[] specifics = specific.split(" ");
        possibility = Integer.parseInt(specifics[0]);
        trait = Integer.parseInt(specifics[1]);
        ideal = Integer.parseInt(specifics[2]);
        bond = Integer.parseInt(specifics[3]);
        flaw = Integer.parseInt(specifics[4]);
    }


    public Background.BackgroundEnum getBackgroundEnumVal() {
        return background;
    }


    public int getPossibility() {
        return possibility;
    }


    public int getTrait() {
        return trait;
    }


    public int getIdeal() {
        return ideal;
    }


    public int getBond() {
        return bond;
    }


    public int getFlaw() {
        return flaw;
    }


    public String getDescription() {
        return Background.getBackgroundInfo(background).getBackgroundDescription(this);
    }


    public String getForSpecificsDatabase() {
        return String.format("%s %s %s %s %s", possibility, trait, ideal, bond, flaw);
    }
}
