package BlackJackBox;

import ExceptionsBox.BadStateException;
import net.dv8tion.jda.core.entities.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/*
 * Represents a state of a blackjack table
 */
class Table {
    // Players are modelled as integers because they can have multiple times turns (when a hand is split)
    // Integers correspond to the indexes in playerObjects
    //      (players with multiple turns are stored multiple times in playerObjects)
    private Map<Integer, Hand> players = new HashMap<>();
    private List<Member> playerObjects;
    private Hand dealer;
    private Deck deck;


    /*
     * Generates the decks to play with, gives the dealer and each player an empty hand then fills the hand
     */
    Table(List<Member> playerObjects, int numberOfDecks) {
        deck = new Deck(numberOfDecks);
        this.playerObjects = new ArrayList<>(playerObjects);

        dealer = new Hand();
        for (int i = 0; i < this.playerObjects.size(); i++) {
            players.put(i, new Hand());
        }

        drawInitialCards();
    }


    /*
     * Draws 2 cards for the dealer and each of the players
     */
    private void drawInitialCards() {
        for (int j = 0; j < 2; j++) {
            dealer.add(deck.drawCard());

            for (int player : players.keySet()) {
                players.get(player).add(deck.drawCard());
            }
        }
    }


    Hand getHand(int player) {
        return players.get(player);
    }


    /*
     * Returns one face up one face down dealer card
     */
    String getDealersInitialHand() {
        return dealer.oneFaceUpOneFaceDownString();
    }


    /*
     * Draws a card and adds it to the hand of the specified player
     * Returns the drawn card
     */
    Card hitMe(int player) {
        final Card card;
        final Hand hand = players.get(player);

        if (hand.isBust() || hand.is5CardHand()) {
            throw new IllegalStateException("Player cannot hit");
        }
        if (!hand.canHit()) {
            throw new BadStateException("Cannot hit when aces have been split");
        }

        card = deck.drawCard();
        hand.add(card);
        return card;
    }


    /*
     * Players the dealer then checks whether each player won/lost/tied/bust
     * Returns player's results
     * TODO Fix check that players who split show up twice (or however many times they took a turn)
     * TODO Implement add an overall winner(s) for who got the highest without going bust
     */
    Results finishRound(List<Member> members) {
        final Results results = new Results(dealer);
        final int dealerTotal = dealerPlays();

        for (int player : players.keySet()) {
            final Hand hand = players.get(player);

            if (hand.isBust()) {
                results.addBust(members.get(player));
            }
            else {
                final int handTotal = hand.total();

                if (dealer.isBust() || !dealer.is5CardHand() && (handTotal > dealerTotal || hand.is5CardHand())) {
                    results.addWinner(members.get(player));
                }
                else if (handTotal == dealerTotal) {
                    results.addTie(members.get(player));
                }
                else {
                    results.addLoser(members.get(player));
                }
            }
        }

        deck.gatherCards();
        return results;
    }


    /*
     * Dealer draws cards until their total is at least 17, they go bust, or they have 5 cards
     */
    private int dealerPlays() {
        while (!dealer.isBust() && dealer.total() < 17 && !dealer.is5CardHand()) {
            dealer.add(deck.drawCard());
        }
        return dealer.total();
    }


    /*
     * Splits the hand into two then adds another card to each hand,
     *      adds the new hand to the end of the turns list (also adds the corresponding Member to playerObjects)
     * TODO Implement after splitting aces the player can only draw one card
     * TODO Implement maximum number of splits: 3
     */
    public void split(int player) {
        final Hand hand = players.get(player);
        final Hand newHand = hand.split();

        hand.add(deck.drawCard());
        newHand.add(deck.drawCard());

        playerObjects.add(playerObjects.get(player));
        players.put(players.size(), newHand);
    }
}
