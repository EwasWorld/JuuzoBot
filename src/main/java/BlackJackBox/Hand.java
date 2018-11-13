package BlackJackBox;

import ExceptionsBox.BadStateException;

import java.util.ArrayList;
import java.util.List;



/**
 * A player's hand of cards
 * refactored: 1/10/18
 */
public class Hand {
    private List<Card> hand = new ArrayList<>();
    // If a hand is made from splitting aces the player cannot hit on it again
    private boolean canHit = true;


    boolean canHit() {
        return canHit;
    }


    void setCanHitFalse() {
        canHit = false;
    }


    boolean isSoft() {
        for (Card card : hand) {
            if (card.getValue() == 1) {
                return true;
            }
        }
        return false;
    }


    boolean isBlackjack() {
        if (hand.size() != 2) {
            return false;
        }
        int card1 = hand.get(0).getValue();
        int card2 = hand.get(1).getValue();
        return (card1 == 1 && card2 >= 10) || (card2 == 1 && card1 >= 10);
    }


    /**
     * Blanks out the first cards and displays the second as a string
     */
    String oneFaceUpOneFaceDownString() {
        if (hand.size() != 2) {
            throw new IllegalStateException("Can't do one up one down unless there're exactly 2 cards");
        }

        return hand.get(0) + ", --";
    }


    Card get(int i) {
        return hand.get(i);
    }


    /**
     * Returns all the cards in the hand as strings separated by commas with a total at the end
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Card card : hand) {
            sb.append(card.toString());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(" (");
        sb.append(total());
        if (isBust()) {
            sb.append(" BUST");
        }
        sb.append(")");
        return sb.toString();
    }


    /**
     * Gets the hand total (counts aces as 11 unless it makes the hand bust)
     */
    int total() {
        int total = 0;
        boolean containsAce = false;
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
                containsAce = true;
            }
        }
        // Count an ace as an 11 if it won't make the hand bust
        if (containsAce && total <= 11) {
            total += 10;
        }
        return total;
    }


    boolean isBust() {
        return total() > 21;
    }


    /**
     * True if there are at least 5 cards in the hand
     */
    boolean is5CardHand() {
        return hand.size() >= 5;
    }


    /**
     * Splits a hand (of 2 identically valued cards) into 2 hands of a single card
     * After splitting aces the hand cannot be hit on
     * @return the newly created hand
     */
    Hand split() {
        if (hand.size() != 2 || hand.get(0).getValue() != hand.get(1).getValue()) {
            throw new BadStateException("Can't split this hand - it must be a hand of 2 cards with the same value");
        }
        final boolean isAces = hand.get(0).getValue() == 1;
        final Hand newHand = new Hand();
        newHand.add(hand.get(1));
        hand.remove(1);
        if (isAces) {
            canHit = false;
            newHand.canHit = false;
        }
        return newHand;
    }


    void add(Card card) {
        hand.add(card);
    }
}
