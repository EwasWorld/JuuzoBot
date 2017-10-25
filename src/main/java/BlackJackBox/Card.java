package BlackJackBox;

public class Card {
    public enum Suit {HEARTS, CLUBS, DIAMONDS, SPADES}



    private Suit suit;
    private int value;


    Card(Suit suit, int value) {
        this.suit = suit;
        this.value = value;
    }


    public Suit getSuit() {
        return suit;
    }


    public int getValue() {
        return value;
    }


    /*
     * Returns a string of ValueSuit e.g. 10C for 10 of Clubs or KS for King of Spades
     *      Value is A/J/Q/K for Ace, Jack, Queen, King or a number
     *      Suit is H/C/D/S for Hearts, Clubs, Diamonds, Spades
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");

        switch (value) {
            case 1:
                stringBuilder.append("A");
                break;
            case 11:
                stringBuilder.append("J");
                break;
            case 12:
                stringBuilder.append("Q");
                break;
            case 13:
                stringBuilder.append("K");
                break;
            default:
                stringBuilder.append(value);
                break;
        }

        stringBuilder.append(suit.toString().charAt(0));

        return stringBuilder.toString();
    }
}
