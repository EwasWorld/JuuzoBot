package BlackJackBox;

import ExceptionsBox.BadStateException;

import java.util.ArrayList;
import java.util.List;



/*
 * Represents a player's hand of cards
 */
public class Hand {
    private List<Card> hand = new ArrayList<>();


    /*
     * Blanks out the first cards and displays the second as a string
     */
    String oneFaceUpOneFaceDownString() {
        if (hand.size() != 2) {
            throw new IllegalStateException("Can't do one up one down unless there're exactly 2 cards");
        }

        return "-- " + hand.get(1);
    }


    /*
     * Returns all the cards in the hand as strings separated by spaces
     */
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("");

        for (Card card : hand) {
            stringBuilder.append(card.toString());
            stringBuilder.append(" ");
        }

        return stringBuilder.toString();
    }


    boolean isBust() {
        return total() > 21;
    }


    /*
     * Gets the hand total (counts aces as 11 unless it makes the hand bust)
     */
    int total() {
        int total = 0;
        int countAces = 0;
        for (Card card : hand) {
            int value = card.getValue();
            // Face cards
            if (value > 10) {
                total += 10;
            }
            else {
                total += value;
            }

            if (value == 1) {
                countAces++;
            }
        }

        // Count the ace as an 11 if it won't make the hand bust (only once because 11 + 11 = bust)
        if (countAces > 0 && total <= 11) {
            total += 10;
        }

        return total;
    }


    /*
     * True if there are at least 5 cards in the hand
     */
    boolean is5CardHand() {
        return hand.size() >= 5;
    }


    /*
     * Splits a hand (of 2 identically valued cards) into 2 hands of a single card
     * Returns the new hand
     */
    Hand split() {
        if (hand.size() != 2 || hand.get(0).getValue() != hand.get(1).getValue()) {
            throw new BadStateException("Can't split this hand - it must be a hand of 2 cards with the same value");
        }

        final Hand newHand = new Hand();
        newHand.add(hand.get(1));
        hand.remove(1);

        return newHand;
    }


    /*
     * Adds a card to the hand
     */
    void add(Card card) {
        hand.add(card);
    }
}
