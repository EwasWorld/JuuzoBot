package BlackJackBox;

import ExceptionsBox.BadStateException;
import net.dv8tion.jda.core.entities.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * A blackjack table
 * refactored: 1/10/18
 */
class Table {
    private static final int numberOfDecks = 8;
    // Players are modelled as integers because they can have multiple times turns (when a hand is split)
    // Integers correspond to the indexes in playerObjects
    private Map<Integer, Hand> players = new HashMap<>();
    private List<Member> playerObjects;
    private Hand dealer;
    private Deck deck;


    /**
     * Generates the decks to play with, gives the dealer and each player two cards
     * Calculates side bet winnings
     */
    Table(List<Member> playerObjects, Bets bets) {
        deck = new Deck(numberOfDecks);
        this.playerObjects = new ArrayList<>(playerObjects);

        /*
         * Generate hands
         */
        dealer = new Hand();
        for (int i = 0; i < this.playerObjects.size(); i++) {
            players.put(i, new Hand());
        }

        /*
         * Draws 2 cards for the dealer and each of the players
         * Done like this to be realistic rather than neat with one for i loop outside
         */
        for (int player : players.keySet()) {
            for (int i = 0; i < 2; i++) {
                players.get(player).add(deck.drawCard());
            }
        }
        for (int i = 0; i < 2; i++) {
            dealer.add(deck.drawCard());
        }

        bets.calculateSideBets(players, dealer.get(0));
    }


    /**
     * Draws a card and adds it to the hand of the specified player
     * @return the drawn card
     * @throws BadStateException when player cannot hit
     */
    Card hitMe(int player) {
        final Card card;
        final Hand hand = players.get(player);
        if (hand.isBust() || hand.is5CardHand()) {
            throw new BadStateException("Player cannot hit");
        }
        if (!hand.canHit()) {
            throw new BadStateException("Cannot hit when aces have been split");
        }
        card = deck.drawCard();
        hand.add(card);
        return card;
    }


    /**
     * Players the dealer then checks whether each player won/lost/tied/bust and clears the table
     * @return Lists containing the indexes of the players sorted into blackjacks, winners, ties, and losers/busts in this order
     * TODO Implement add an overall winner(s) for who got the highest without going bust
     */
    List<Integer>[] finishRound() {
        /*
         * Dealer draws cards until their total is at 17 or more (hits on a soft 17), they go bust, or they have 5 cards
         */
        while (!dealer.isBust() && (dealer.total() < 17 || (dealer.total() == 17 && dealer.isSoft())) && !dealer
                .is5CardHand()) {
            dealer.add(deck.drawCard());
        }

        /*
         * Check winners and losers
         */
        final List<Integer> blackjacks = new ArrayList<>();
        final List<Integer> winners = new ArrayList<>();
        final List<Integer> ties = new ArrayList<>();
        final List<Integer> losers = new ArrayList<>();
        final int dealerTotal = dealer.total();
        for (int player : players.keySet()) {
            final Hand hand = players.get(player);
            if (hand.isBust()) {
                losers.add(player);
            }
            else {
                final int handTotal = hand.total();
                if (!dealer.isBlackjack() && hand.isBlackjack()) {
                    blackjacks.add(player);
                }
                else if (dealer.isBust() || (!dealer.is5CardHand() && (handTotal > dealerTotal || hand.is5CardHand()))) {
                    winners.add(player);
                }
                else if ((!dealer.isBlackjack() && handTotal == dealerTotal) || hand.isBlackjack()) {
                    ties.add(player);
                }
                else {
                    losers.add(player);
                }
            }
        }

        /*
         * Clear up
         */
        deck.gatherCards();
        return new List[]{blackjacks, winners, ties, losers};
    }


    private String getTableString(String dealerHand, Bets bets, int turn) {
        final String handString = "*%s*%s: %s\n";
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format(handString, "Dealer", "", dealerHand));
        for (Integer player : players.keySet()) {
            if (turn == player) {
                sb.append("* ");
            }
            sb.append(String.format(handString, GameInstance.getName(playerObjects.get(player)), bets.getBet(player), getHand(player)));
        }
        return sb.toString();
    }


    Hand getHand(int player) {
        return players.get(player);
    }


    /**
     * Splits the hand into two then adds another card to each hand,
     * adds the new hand to the end of the turns list (also adds the corresponding Member to playerObjects)
     * TODO Implement maximum number of splits: 3 (for a total of 4 hands)
     */
    void split(int player) {
        final Hand hand = players.get(player);
        final Hand newHand = hand.split();
        hand.add(deck.drawCard());
        newHand.add(deck.drawCard());
        playerObjects.add(playerObjects.get(player));
        players.put(players.size(), newHand);
    }


    String getEndGameString(Bets bets) {
        return getTableString(dealer.toString(), bets, -1);
    }


    String toStringDealerOneUpOneDown(Bets bets, int turn) {
        return getTableString(dealer.oneFaceUpOneFaceDownString(), bets, turn);
    }
}
