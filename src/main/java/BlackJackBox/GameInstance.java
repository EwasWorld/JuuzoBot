package BlackJackBox;

import CommandsBox.BlackJackCommand;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.Member;

import java.util.ArrayList;
import java.util.List;



/*
 * Not at all thread safe
 */
public class GameInstance {
    private Board currentGame;
    private List<Member> players;
    private int turn;
    private boolean canJoin = true;


    public GameInstance(Member player) {
        players = new ArrayList<>();
        players.add(player);
    }


    /*
     * Adds a player to the game (game must not have started)
     * Returns false if the player is already in the game or if the game has already started
     */
    public String addPlayer(Member player) {
        if (!canJoin) {
            throw new BadStateException("This game is already running");
        }

        if (!isPlayerInList(player)) {
            players.add(player);
            return "You have been added to the game";
        }
        else {
            throw new BadStateException("You're already in the game!!!");
        }
    }


    /*
     * Returns whether a player has already joined the game
     */
    private boolean isPlayerInList(Member player) {
        for (Member member : players) {
            if (member.getUser().getIdLong() == player.getUser().getIdLong()) {
                return true;
            }
        }
        return false;
    }



    /*
     * Creates the board and prevents more players from joining
     * Returns who the first player is
     */
    public String startGame() {
        if (!canJoin) {
            throw new BadStateException("Game has already started");
        }

        canJoin = false;
        turn = 0;
        currentGame = new Board(players.size(), 1);
        StringBuilder stringBuilder = new StringBuilder("Game started\n\n");

        stringBuilder.append(String.format("Dealer: %s\n", currentGame.getDealersInitialHand()));
        for (Member member : players) {
            stringBuilder.append(String.format("%s: %s\n", getName(member), getHand(member)));
        }

        stringBuilder.append("\n");
        stringBuilder.append(getTurn());

        return stringBuilder.toString();
    }

    /*
     * Returns who's turn it is
     */
    public String getTurn() {
        if (canJoin) {
            throw new BadStateException("Game hasn't started yet");
        }

        return String.format("It's %s's turn", getName(players.get(turn)));
    }


    public String getDealerHand() {
        if (canJoin) {
            throw new BadStateException("Game hasn't started yet");
        }

        return "Dealer: " + currentGame.getDealersInitialHand();
    }


    /*
     * Returns the hand of the specified member
     */
    public String getHand(Member player) {
        if (canJoin) {
            throw new BadStateException("Game hasn't started yet");
        }

        for (int i = 0; i < players.size(); i++) {
            if (playersEqualIDs(players.get(i), player)) {
                return getHandString(currentGame.getHand(i));
            }
        }
        throw new BadStateException("You don't seem to have a hand");
    }


    /*
     * Check whether the IDs of two members are equal
     * TODO: Is this necessary?
     */
    private boolean playersEqualIDs(Member one, Member two) {
        return one.getUser().getIdLong() == two.getUser().getIdLong();
    }


    /*
     * Returns a hand as a string
     */
    static String getHandString(List<Card> hand) {
        StringBuilder stringBuilder = new StringBuilder("");

        for (Card card : hand) {
            stringBuilder.append(getCardString(card));
            stringBuilder.append(" ");
        }

        return stringBuilder.toString();
    }


    /*
     * Returns the card's value and suit as a string
     */
    static String getCardString(Card card) {
        StringBuilder stringBuilder = new StringBuilder("");

        switch (card.getValue()) {
            case 1:
                stringBuilder.append("A");
                break;
            case 11:
                stringBuilder.append("J");
                break;
            case 12:
                stringBuilder.append("Q");
                break;
            case 13:
                stringBuilder.append("K");
                break;
            default:
                stringBuilder.append(card.getValue());
                break;
        }

        stringBuilder.append(card.getSuit().toString().charAt(0));

        return stringBuilder.toString();
    }


    /*
     * The current player draws another card
     * Returns the drawn card and the new hand
     */
    public String hitMe(Member player) {
        if (canJoin) {
            throw new BadStateException("Game hasn't started yet");
        }
        if (!isPlayersTurn(player)) {
            throw new BadStateException("It's not your turn");
        }

        final Card drawnCard = currentGame.hitMe(turn);
        final List<Card> hand = currentGame.getHand(turn);

        if (Board.isBust(hand)) {
            turn++;
            if (turn < players.size()) {
                return String.format("%s - BUST\n%s", getCardString(drawnCard), getHandString(hand));
            }
            else {
                return end();
            }
        }

        return String.format("%s\nYour current hand: %s", getCardString(drawnCard), getHandString(hand));
    }


    /*
     * Checks if it is the given player's turn
     */
    private boolean isPlayersTurn(Member player) {
        return playersEqualIDs(players.get(turn), player);
    }


    /*
     * Ends the players turn. If all players have had a turn it ends the game
     */
    public String stand(Member player) {
        if (canJoin) {
            throw new BadStateException("Game hasn't started yet");
        }
        if (!isPlayersTurn(player)) {
            throw new BadStateException("It's not your turn");
        }

        turn++;
        if (turn < players.size()) {
            return String.format("%s, you're up next!", players.get(turn));
        }
        else {
            return end();
        }
    }


    /*
     * Ends the game
     */
    private String end() {
        final String endStr = currentGame.finishRound(players).getAsString() + "\nGame has ended and new players can join. Start again or make a new game";
        BlackJackCommand.setGameRunningFalse();
        canJoin = true;
        turn = 0;

        return endStr;
    }


    static String getName(Member member) {
        return member.getUser().getName();
    }
}
