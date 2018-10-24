package BlackJackBox;

import java.util.*;



/**
 * Simulates one or more decks of cards
 * refactored: 1/10/18
 */
class Deck {
    private Queue<Card> drawPile = new LinkedList<>();
    private List<Card> discardPile = new ArrayList<>();
    // Used so that when the deck is reshuffled the next card drawn cannot be one which is already in play
    private List<Card> cardsOnTable = new ArrayList<>();


    /**
     * Generates a standard 52 card deck as many times as necessary, then shuffles the cards
     */
    Deck(int numberOfDecks) {
        for (Card.Suit suit : Card.Suit.values()) {
            for (int value = 1; value <= 13; value++) {
                for (int i = 0; i < numberOfDecks; i++) {
                    discardPile.add(new Card(suit, value));
                }
            }
        }
        shuffle();
    }


    /**
     * Shuffles cards in discardPile then adds them to drawPile
     */
    private void shuffle() {
        if (discardPile.size() > 0) {
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile = new ArrayList<>();
        }
    }


    /**
     * Draws a card and adds it to the cards on the table
     * @return the first card in the drawPile
     */
    Card drawCard() {
        if (discardPile.size() + cardsOnTable.size() > drawPile.size()) {
            if (discardPile.size() == 0) {
                throw new IllegalStateException("Run out of cards to shuffle. "
                                                        + "When a round is finished make sure to gather the cards on the table");
            }
            shuffle();
        }
        final Card card = drawPile.remove();
        cardsOnTable.add(card);
        return card;
    }


    /**
     * Collects the cards from the table and puts them in the discard pile
     */
    void gatherCards() {
        discardPile.addAll(cardsOnTable);
        cardsOnTable = new ArrayList<>();
    }
}
