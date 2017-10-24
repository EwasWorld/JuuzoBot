package BlackJackBox;

import net.dv8tion.jda.core.entities.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/*
 * TODO Time out
 * TODO Play again
 * TODO Split
 * TODO Insurance
 * TODO Bets
 */
class Board {
    private Map<Integer, List<Card>> players = new HashMap<>();
    private List<Card> dealer = new ArrayList<>();
    private Deck deck;


    Board(int numberOfPlayers, int numberOfDecks) {
        deck = new Deck(numberOfDecks);

        for (int i = 0; i < numberOfPlayers; i++) {
            players.put(i, new ArrayList<>());
        }

        drawInitialCards();
    }


    List<Card> getHand(int player) {
        return players.get(player);
    }


    /*
     * Returns one face up one face down dealer card
     */
    String getDealersInitialHand() {
        return "-- " + GameInstance.getCardString(dealer.get(1));
    }


    private void drawInitialCards() {
        for (int j = 0; j < 2; j++) {
            dealer.add(deck.drawCard());

            for (int player : players.keySet()) {
                players.get(player).add(deck.drawCard());
            }
        }
    }


    /*
     *
     */
    Card hitMe(int player) {
        final Card card;
        final List<Card> hand = players.get(player);
        if (hand.size() < 5 && !isBust(hand)) {
            card = deck.drawCard();
            hand.add(card);
            return card;
        }
        else {
            throw new IllegalStateException("Player cannot hit");
        }
    }


    static boolean isBust(List<Card> hand) {
        return handTotal(hand) > 21;
    }


    private static int handTotal(List<Card> hand) {
        int total = 0;
        int countAces = 0;
        for (Card card : hand) {
            int value = card.getValue();
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

        if (total <= 11 && countAces > 0) {
            total += 10;
        }

        return total;
    }


    Results finishRound(List<Member> members) {
        Results results = new Results(dealer);
        int dealerTotal = dealerPlays();

        for (int player : players.keySet()) {
            final List<Card> hand = players.get(player);
            final int handTotal = handTotal(hand);
            if (handTotal > 21) {
                results.addBust(members.get(player));
            }
            else {
                if (dealerTotal > 21 || (handTotal > dealerTotal || hand.size() == 5) && dealer.size() < 5) {
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

        return results;
    }


    private int dealerPlays() {
        while (dealer.size() < 5 && handTotal(dealer) < 17) {
            dealer.add(deck.drawCard());
        }
        return handTotal(dealer);
    }
}
