package BlackJackBox;

import java.util.*;



public class Deck {
    private Queue<Card> deck = new LinkedList<>();
    private List<Card> discardedPile = new ArrayList<>();


    public Deck(int numberOfDecks) {
        for (Card.Suit suit : Card.Suit.values()) {
            for (int value = 1; value <= 13; value++) {
                for (int i = 0; i < numberOfDecks; i++) {
                    discardedPile.add(new Card(suit, value));
                }
            }
        }

        shuffle();
    }


    public void shuffle() {
        if (discardedPile.size() > 0) {
            Collections.shuffle(discardedPile);
            deck.addAll(discardedPile);
            discardedPile = new ArrayList<>();
        }
    }


    public Card drawCard() {
        if (deck.size() == 0) {
            shuffle();
        }

        Card card = deck.remove();
        discardedPile.add(card);
        return card;
    }
}
