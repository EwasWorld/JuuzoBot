package BlackJackBox;

import java.util.*;



/*
 * Simulates one or more decks of cards
 */
class Deck {
    private Queue<Card> drawPile = new LinkedList<>();
    private List<Card> discardPile = new ArrayList<>();


    /*
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


    /*
     * Shuffles cards in discardPile then adds them to drawPile
     */
    private void shuffle() {
        if (discardPile.size() > 0) {
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile = new ArrayList<>();
        }
    }


    /*
     * Returns the first card in the drawPile and adds it to the discardPile
     * TODO be smarter about when to shuffle the drawPile
     *      could shuffle half way through a game and re-draw a card that's already on the table
     */
    Card drawCard() {
        if (drawPile.size() == 0) {
            shuffle();
        }

        Card card = drawPile.remove();
        discardPile.add(card);
        return card;
    }
}
