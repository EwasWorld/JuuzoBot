package CharacterBox;

public class Alignment {
    public enum LawChaosEnum {LAWFUL, NEUTRAL, CHAOTIC}



    public enum GoodEvilEnum {GOOD, NEUTRAL, EVIL}



    private LawChaosEnum lawChaos;
    private GoodEvilEnum goodEvil;


    public Alignment(LawChaosEnum lawChaos, GoodEvilEnum goodEvil) {
        this.lawChaos = lawChaos;
        this.goodEvil = goodEvil;
    }


    public Alignment(LawChaosEnum lawChaos) {
        this.lawChaos = lawChaos;
    }


    public Alignment(GoodEvilEnum goodEvil) {
        this.goodEvil = goodEvil;
    }


    public Alignment() {
    }


    public LawChaosEnum getLawChaos() {
        return lawChaos;
    }


    public GoodEvilEnum getGoodEvil() {
        return goodEvil;
    }


    public String getAlignmentInitials() {
        final String[] alignment = getAlignment().split(" ");
        final String alignmentInitials = alignment[0].substring(0, 1) + alignment[1].substring(0, 1);

        if (alignmentInitials.equalsIgnoreCase("TN")) {
            return "N";
        }
        else {
            return alignmentInitials;
        }
    }


    public String getAlignment() {
        if (lawChaos == LawChaosEnum.NEUTRAL && goodEvil == GoodEvilEnum.NEUTRAL) {
            return "TRUE NEUTRAL";
        }
        else {
            return lawChaos.toString() + " " + goodEvil.toString();
        }
    }


    public boolean checkMatches(Alignment alignment) {
        if (lawChaos == null && goodEvil == null) {
            return true;
        }
        else {
            boolean match = true;
            if (lawChaos != null && lawChaos != alignment.lawChaos) {
                match = false;
            }
            if (goodEvil != null && goodEvil != alignment.goodEvil) {
                match = false;
            }
            return match;
        }
    }
}
