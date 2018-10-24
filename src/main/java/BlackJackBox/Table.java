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
    // Players are modelled as integers because they can have multiple times turns (when a hand is split)
    // Integers correspond to the indexes in playerObjects
    private Map<Integer, Hand> players = new HashMap<>();
    private List<Member> playerObjects;
    private Hand dealer;
    private Deck deck;


    /**
     * Generates the decks to play with, gives the dealer and each player two cards
     */
    Table(List<Member> playerObjects, int numberOfDecks) {
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
         */
        for (int j = 0; j < 2; j++) {
            dealer.add(deck.drawCard());

            for (int player : players.keySet()) {
                players.get(player).add(deck.drawCard());
            }
        }
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
     * Players the dealer then checks whether each player won/lost/tied/bust
     * @param playerList players in the game
     * @return dealer's final hand and player's results
     * TODO Implement add an overall winner(s) for who got the highest without going bust
     */
    String finishRound(List<Member> playerList) {
        /*
         * Dealer draws cards until their total is at 17 or more (hits on a soft 17), they go bust, or they have 5 cards
         */
        while (!dealer.isBust() && (dealer.total() < 17 || (dealer.total() == 17 && dealer.isSoft())) && !dealer
                .is5CardHand()) {
            dealer.add(deck.drawCard());
        }

        /*
         * Create results string
         */
        final StringBuilder returnString = new StringBuilder(
                getTableString(dealer.toString()) + GameInstance.dottedLineBreak);
        final StringBuilder winners = new StringBuilder();
        final StringBuilder ties = new StringBuilder();
        final StringBuilder losers = new StringBuilder();
        final StringBuilder busts = new StringBuilder();
        final int dealerTotal = dealer.total();
        for (int player : players.keySet()) {
            final Hand hand = players.get(player);
            final String playerName = GameInstance.getName(playerList.get(player));
            if (hand.isBust()) {
                busts.append(playerName);
            }
            else {
                final int handTotal = hand.total();
                if (dealer.isBust() || !dealer.is5CardHand() && (handTotal > dealerTotal || hand.is5CardHand())) {
                    winners.append(playerName);
                }
                else if (handTotal == dealerTotal) {
                    ties.append(playerName);
                }
                else {
                    losers.append(playerName);
                }
            }
        }
        createResultsStringAppendMembers(returnString, winners, "Winners");
        createResultsStringAppendMembers(returnString, ties, "Ties");
        createResultsStringAppendMembers(returnString, losers, "Losers");
        createResultsStringAppendMembers(returnString, busts, "Busts");
        returnString.append(GameInstance.lineBreak);

        /*
         * Clear up
         */
        deck.gatherCards();
        return returnString.toString();
    }


    private String getTableString(String dealerHand) {
        final String handString = "*%s*: %s\n";
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format(handString, "Dealer", dealerHand));
        // TODO Optimisation add better differentiation for split hands (atm they both show with the player's nickname)
        for (Integer i : players.keySet()) {
            sb.append(String.format(handString, GameInstance.getName(playerObjects.get(i)), getHand(i)));
        }
        return sb.toString();
    }


    /**
     * Append the title and the members list to the string builder in the form "listTitle: membersList\n"
     * @param sb the string builder to append the members list and list title
     */
    private void createResultsStringAppendMembers(StringBuilder sb, StringBuilder membersList, String listTitle) {
        if (membersList.length() > 0) {
            sb.append(listTitle);
            sb.append(": ");
            sb.append(membersList);
            sb.append("\n");
        }
    }


    Hand getHand(int player) {
        return players.get(player);
    }


    /**
     * Splits the hand into two then adds another card to each hand,
     * adds the new hand to the end of the turns list (also adds the corresponding Member to playerObjects)
     * TODO Implement maximum number of splits: 3 (for a total of 4 hands)
     */
    public void split(int player) {
        final Hand hand = players.get(player);
        final Hand newHand = hand.split();
        hand.add(deck.drawCard());
        newHand.add(deck.drawCard());
        playerObjects.add(playerObjects.get(player));
        players.put(players.size(), newHand);
    }


    public String toString() {
        return getTableString(dealer.oneFaceUpOneFaceDownString());
    }
}
