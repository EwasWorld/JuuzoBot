package BlackJackBox;

import ExceptionsBox.BadStateException;
import net.dv8tion.jda.core.entities.Member;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * created 24/10/18
 * TODO FIX ensure winnings are not paid out if the player has left
 */
class Bets {
    private Map<Member, Integer> mainBetsTemp;
    private Map<Member, Integer> perfectPairsTemp;
    private Map<Member, Integer> twentyOnePlusThreesTemp;
    private Map<Integer, Integer> mainBets;
    private Map<Integer, Integer> perfectPairs;
    private Map<Integer, Integer> twentyOnePlusThrees;

    /**
     * Betting values: all are in the form x:1
     * Betters receive stake + stake * x
     */
    private static final double blackjack = 1.5;
    private static final double win = 1;
    private static final double tie = 0;

    // Matched suits and ranks
    private static final double perfectPair = 25;
    // Matched colours and ranks
    private static final double colouredPair = 15;
    // Matched ranks
    private static final double mixedPair = 5;

    // Matched suits and ranks
    private static final double suitedTrips = 100;
    // Matched suit and consecutive ranks
    private static final double straightFlush = 40;
    // Matched ranks
    private static final double threeOfAKind = 30;
    // Consecutive ranks
    private static final double straight = 10;
    // Matched suits
    private static final double flush = 5;


    Bets() {
        mainBetsTemp = new HashMap<>();
        perfectPairsTemp = new HashMap<>();
        twentyOnePlusThreesTemp = new HashMap<>();
    }


    void clearAllBets(Member player) {
        mainBetsTemp.remove(player);
        perfectPairsTemp.remove(player);
        twentyOnePlusThreesTemp.remove(player);
    }


    /**
     * Adds the desired bets to a player, removes players from map for values < 1
     */
    void setBet(Member player, int main, int perfectPair, int twentyOnePlusThree) {
        setMainBet(player, main);
        setSideBets(player, perfectPair, twentyOnePlusThree);
    }


    void setMainBet(Member player, int bet) {
        if (bet > 0) {
            mainBetsTemp.put(player, bet);
        }
        else {
            mainBetsTemp.remove(player);
        }
    }


    void setSideBets(Member player, int perfectPair, int twentyOnePlusThree) {
        if (perfectPair > 0) {
            perfectPairsTemp.put(player, perfectPair);
        }
        else {
            perfectPairsTemp.remove(player);
        }
        if (twentyOnePlusThree > 0) {
            twentyOnePlusThreesTemp.put(player, twentyOnePlusThree);
        }
        else {
            twentyOnePlusThreesTemp.remove(player);
        }
    }


    void duplicateMainBet(int fromPlayer, int toPlayer) {
        if (mainBets.containsKey(fromPlayer)) {
            mainBets.put(toPlayer, mainBets.get(fromPlayer));
        }
    }


    void doubleMainBet(int player) {
        if (mainBets.containsKey(player)) {
            mainBets.put(player, mainBets.get(player) * 2);
        }
    }


    boolean hasBet(int player) {
        return mainBets.containsKey(player);
    }


    /**
     * @see Bets#getBet(Object, Map, Map, Map)
     */
    String getBet(int player) {
        return getBet(player, mainBets, perfectPairs, twentyOnePlusThrees);
    }


    /**
     * @see Bets#getBet(Object, Map, Map, Map)
     */
    String getBet(Member player) {
        return getBet(player, mainBetsTemp, perfectPairsTemp, twentyOnePlusThreesTemp);
    }


    /**
     * Turn a particular player's bet into a string that can be displayed to the users
     */
    private <T> String getBet(T player, Map<T, Integer> mainBets, Map<T, Integer> perfectPairs, Map<T, Integer> twentyOnePlusThrees) {
        int main = 0;
        int perfectPair = 0;
        int twentyOnePlusThree = 0;
        if (mainBets.containsKey(player)) {
            main = mainBets.get(player);
        }
        if (perfectPairs.containsKey(player)) {
            perfectPair = perfectPairs.get(player);
        }
        if (twentyOnePlusThrees.containsKey(player)) {
            twentyOnePlusThree = twentyOnePlusThrees.get(player);
        }

        if (main == 0 && perfectPair == 0 && twentyOnePlusThree == 0) {
            return "";
        }
        else {
            return String.format(" (**%s**, %s/%s)", main, perfectPair, twentyOnePlusThree);
        }
    }


    void convertBetKeysToIntegers(List<Member> players) {
        mainBets = new HashMap<>();
        perfectPairs = new HashMap<>();
        twentyOnePlusThrees = new HashMap<>();
        for (Member player : mainBetsTemp.keySet()) {
            mainBets.put(players.indexOf(player), mainBetsTemp.get(player));
        }
        for (Member player : perfectPairsTemp.keySet()) {
            perfectPairs.put(players.indexOf(player), perfectPairsTemp.get(player));
        }
        for (Member player : twentyOnePlusThreesTemp.keySet()) {
            twentyOnePlusThrees.put(players.indexOf(player), twentyOnePlusThreesTemp.get(player));
        }
        mainBetsTemp = null;
        perfectPairsTemp = null;
        twentyOnePlusThreesTemp = null;
    }


    /**
     * Calculate perfect pairs and 21+3 bets, editing the values to the payback
     */
    void calculateSideBets(Map<Integer, Hand> playerHands, Card dealerFaceUpCard) {
        if (mainBetsTemp != null) {
            throw new BadStateException("Bet keys not converted to integers");
        }

        /*
         * Perfect pairs
         */
        for (int player : perfectPairs.keySet()) {
            final Hand hand = playerHands.get(player);
            final Card card1 = hand.get(0);
            final Card card2 = hand.get(1);

            int perfectPairsBet = perfectPairs.get(player);
            if (card1.getValue() == card2.getValue()) {
                if (card1.getSuit() == card2.getSuit()) {
                    perfectPairs.put(player, calculateReturnedWinnings(perfectPair, perfectPairsBet));
                }
                else if (card1.getSuit().getSuitColour() == card2.getSuit().getSuitColour()) {
                    perfectPairs.put(player, calculateReturnedWinnings(colouredPair, perfectPairsBet));
                }
                else {
                    perfectPairs.put(player, calculateReturnedWinnings(mixedPair, perfectPairsBet));
                }
            }
            else {
                perfectPairs.put(player, 0);
            }
        }

        /*
         * 21+3
         */
        for (int player : twentyOnePlusThrees.keySet()) {
            final Hand hand = playerHands.get(player);
            final Card card1 = hand.get(0);
            final Card card2 = hand.get(1);

            boolean matchedSuits = false;
            boolean matchedRanks = false;
            boolean consecutiveRanks = false;
            if (card1.getSuit() == card2.getSuit() && card1.getSuit() == dealerFaceUpCard.getSuit()) {
                matchedSuits = true;
            }
            if (card1.getValue() == card2.getValue() && card1.getValue() == dealerFaceUpCard.getValue()) {
                matchedRanks = true;
            }
            int[] cardRanks = {card1.getValue(), card2.getValue(), dealerFaceUpCard.getValue()};
            Arrays.sort(cardRanks);
            if (cardRanks[0] + 1 == cardRanks[1] && cardRanks[1] + 1 == cardRanks[2]) {
                consecutiveRanks = true;
            }

            final int twentyOnePlusThreeBet = twentyOnePlusThrees.get(player);
            if (matchedSuits && matchedRanks) {
                twentyOnePlusThrees.put(player, calculateReturnedWinnings(suitedTrips, twentyOnePlusThreeBet));
            }
            else if (matchedSuits && consecutiveRanks) {
                twentyOnePlusThrees.put(player, calculateReturnedWinnings(straightFlush, twentyOnePlusThreeBet));
            }
            else if (matchedRanks) {
                twentyOnePlusThrees.put(player, calculateReturnedWinnings(threeOfAKind, twentyOnePlusThreeBet));
            }
            else if (consecutiveRanks) {
                twentyOnePlusThrees.put(player, calculateReturnedWinnings(straight, twentyOnePlusThreeBet));
            }
            else if (matchedSuits) {
                twentyOnePlusThrees.put(player, calculateReturnedWinnings(flush, twentyOnePlusThreeBet));
            }
            else {
                twentyOnePlusThrees.put(player, 0);
            }
        }
    }


    private int calculateReturnedWinnings(double odds, int stake) {
        return stake + (int) Math.ceil(stake * odds);
    }


    /**
     * Calculate the main bets
     */
    void calculateFinalBets(List<Integer>[] results) {
        double[] bettingOdds = {blackjack, win, tie};

        for (int i = 0; i < bettingOdds.length; i++) {
            for (int player : results[i]) {
                if (mainBets.containsKey(player)) {
                    mainBets.put(player, calculateReturnedWinnings(bettingOdds[i], mainBets.get(player)));
                }
            }
        }
        for (int player : results[bettingOdds.length]) {
            mainBets.remove(player);
        }
    }
}
