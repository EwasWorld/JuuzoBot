package BlackJackBox;

import CommandsBox.Emoji;



/**
 * refactored: 1/10/18
 */
public class Card {
    public enum Suit {
        HEARTS(Emoji.HEARTS), CLUBS(Emoji.CLUBS), DIAMONDS(Emoji.DIAMONDS), SPADES(Emoji.SPADES);
        private Emoji emoji;


        Suit(Emoji emoji) {
            this.emoji = emoji;
        }


        public String getEmojiAlias() {
            return emoji.getDiscordAlias();
        }
    }



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


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

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

        stringBuilder.append(suit.getEmojiAlias());
        return stringBuilder.toString();
    }
}
