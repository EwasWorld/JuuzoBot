package BlackJackBox;

import CommandsBox.BlackJackCommand;
import ExceptionsBox.BadStateException;
import net.dv8tion.jda.core.entities.Member;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * A game of blackjack
 * Not at all thread safe
 * TODO Implement Bets
 * TODO Implement Insurance
 * refactored: 1/10/18
 */
public class GameInstance {
    public enum GameState {LOBBY, STARTED, FINISHED}
    public static final String blackjackGameMessageTitleString = "__**Blackjack**__";
    static final String lineBreak = "-------------------------------------\n";
    static final String dottedLineBreak = "- - - - - - - - - - - - - - - - - - - - - - - \n";
    private static final String gameStartedMessage = "Game started!";
    private Table table;
    private List<Member> players;
    private Set<Integer> retiredPlayers;
    private int turn;
    private GameState gameState;
    private Member gameOwner;
    private String mostRecentGameError = "";
    private String mostRecentGameMessage = "";


    /**
     * Sets up a new game with an initial player and allows others to join it
     */
    public GameInstance(Member player) {
        players = new ArrayList<>();
        retiredPlayers = new HashSet<>();
        players.add(player);
        gameState = GameState.LOBBY;
        gameOwner = player;
    }


    public boolean isGameOwner(Member player) {
        return player == gameOwner;
    }


    public void setMostRecentGameErrorInvalidPermissions() {
        mostRecentGameError = "Only an admin or the game owner can end the game";
    }


    public GameState getGameState() {
        return gameState;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s :upside_down: instance created by %s\n", blackjackGameMessageTitleString,
                                getName(gameOwner)
        ));
        sb.append(lineBreak);

        if (gameState == GameState.LOBBY) {
            sb.append("**Current players**:\n");
            if (!players.isEmpty()) {
                for (Member player : players) {
                    sb.append(getName(player));
                    sb.append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                sb.append("\n");
            }
            sb.append(dottedLineBreak);
            sb.append("Hit :ok_hand: when you're ready!\n");
        }
        else if (gameState == GameState.STARTED) {
            sb.append(table.toString());
            sb.append(dottedLineBreak);
            sb.append(String.format("**It's currently %s turn!**\n",
                                    getPossessiveFormOfName(getName(players.get(turn)))
            ));
        }

        if (!mostRecentGameMessage.equals("") && (gameState != GameState.FINISHED || (!mostRecentGameMessage
                .equals(gameStartedMessage))))
        {
            sb.append(mostRecentGameMessage);
            sb.append("\n");
            if (gameState == GameState.FINISHED) {
                sb.append(lineBreak);
            }
        }
        if (!mostRecentGameError.equals("")) {
            sb.append(mostRecentGameError);
            sb.append("\n");
        }
        if (gameState != GameState.FINISHED) {
            sb.append(lineBreak);
            sb.append(BlackJackCommand.getEmojiHelpReactionString(gameState));
        }
        return sb.toString();
    }


    /**
     * @param player must be in the game
     * @return the printable nickname of the player
     */
    static String getName(Member player) {
        return player.getUser().getName();
    }


    private static String getPossessiveFormOfName(String name) {
        if (name.charAt(name.length() - 1) == 's') {
            return name + "'";
        }
        else {
            return name + "'s";
        }
    }


    /**
     * Adds a player to the game (game must not have started)
     */
    public void join(Member player) {
        final String name = getName(player);
        if (gameState != GameState.LOBBY) {
            mostRecentGameError = name + ", this game is already running";
        }
        else if (isPlayerInGame(player)) {
            mostRecentGameError = name + ", you're already in the game!!!";
        }
        else {
            players.add(player);
            mostRecentGameMessage = name + " has joined!";
        }
    }


    /**
     * Returns whether a player has already joined the game
     */
    private boolean isPlayerInGame(Member player) {
        for (Member member : players) {
            if (member.getUser().getIdLong() == player.getUser().getIdLong()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Removes a player from the game. If the game has started they will be automatically stood
     */
    public void leave(Member player) {
        final String name = getName(player);
        if (retiredPlayers.contains(players.indexOf(player))) {
            mostRecentGameError = name + ", you've already left!";
        }
        else if (!isPlayerInGame(player)) {
            mostRecentGameError = name + ", you're not even in the game!!!";
        }
        else {
            if (gameState != GameState.LOBBY) {
                retiredPlayers.add(players.indexOf(player));
                if (isPlayersTurn(player)) {
                    stand(player);
                }
                mostRecentGameMessage = name + " has retired from the game";
            }
            else {
                players.remove(player);
                mostRecentGameMessage = name + " has left";
            }
        }
    }


    /**
     * Creates the board, sets the first player's turn, and prevents more players from joining
     */
    public void startGame() {
        if (gameState != GameState.LOBBY) {
            mostRecentGameError = "Game has already started";
            return;
        }
        gameState = GameState.STARTED;
        turn = 0;
        table = new Table(players, 1);
        startTimeoutThread();
        mostRecentGameMessage = gameStartedMessage;
    }


    private void startTimeoutThread() {
        new Thread(new Timeout(turn)).start();
    }


    /**
     * Check whether the IDs of two members are equal
     * TODO Optimisation Is this necessary? - run tests
     */
    private boolean playersEqualIDs(Member one, Member two) {
        boolean test = one == two;
        boolean test2 = one.getUser() == two.getUser();
        return one.getUser().getIdLong() == two.getUser().getIdLong();
    }


    /**
     * The current player draws another card (calls stand() if they bust)
     */
    public void hitMe(Member player) {
        if (validAction(player)) {
            final Card drawnCard;
            try {
                drawnCard = table.hitMe(turn);
            } catch (BadStateException e) {
                mostRecentGameError = e.getMessage();
                stand();
                return;
            }
            final Hand hand = table.getHand(turn);
            mostRecentGameMessage = String.format("%s drew a %s", getName(player), drawnCard.toString());
            if (hand.isBust()) {
                mostRecentGameMessage += " and went bust. rip.";
                stand();
            }
        }
    }


    // TODO Implement write a specific message if the player isn't even in the game
    private boolean isPlayersTurn(Member player) {
        return playersEqualIDs(players.get(turn), player);
    }


    /**
     * Ends the current turn. If all players have had a turn it ends the game
     */
    private void stand() {
        turn++;
        if (turn < players.size()) {
            if (!table.getHand(turn).canHit() || retiredPlayers.contains(turn)) {
                stand();
            }
            else {
                startTimeoutThread();
            }
        }
        else {
            end();
        }
    }


    /**
     * Plays the dealer, calculates game results, and sets all necessary game states
     */
    private void end() {
        mostRecentGameError = table.finishRound(players) + "Game has ended";
        gameState = GameState.FINISHED;
        BlackJackCommand.setGameRunningFalse();
        BlackJackCommand.setGameInstanceToNull();
    }


    /**
     * Ends the current turn
     */
    public void stand(Member player) {
        if (validAction(player)) {
            stand();
        }
    }


    /**
     * Splits the hand in two, adding the new hand to the end
     */
    public void split(Member player) {
        if (validAction(player)) {
            try {
                table.split(turn);
                if (!table.getHand(turn).canHit()) {
                    mostRecentGameMessage = "Cannot hit on split aces, therefore your turn is over";
                    stand();
                }
                else {
                    mostRecentGameMessage
                            = "Split complete, continue your turn and you can play your other hand at the end.";
                }
            } catch (BadStateException e) {
                mostRecentGameError = e.getMessage();
            }
        }
    }


    /**
     * Checks if the player can make an action, if not it writes to mostRecentGameError
     * @return <code>true</code> if the game is not in the lobby and it is the given player's turn
     */
    private boolean validAction(Member player) {
        if (gameState == GameState.LOBBY) {
            mostRecentGameError = "Game hasn't started yet";
            return false;
        }
        else if (retiredPlayers.contains(players.indexOf(player))) {
            mostRecentGameError = getName(player) + ", you've left the game, it's too late to turn back now!";
            return false;
        }
        else if (!isPlayersTurn(player)) {
            mostRecentGameError = getName(player) + ", it's not your turn";
            return false;
        }
        return true;
    }


    /**
     * If a player takes too long, automatically make them stand
     */
    public class Timeout implements Runnable {
        private static final int waitTime = 30 * 1000;
        private static final String timeoutMessage = "Bzzt! %s, you took too long and have been automatically stood";
        private int initialPlayerTurn;


        Timeout(int initialPlayerTurn) {
            this.initialPlayerTurn = initialPlayerTurn;
        }


        /**
         * Waits for a period of time. If the turn hasn't moved on they are forced to stand
         */
        @Override
        public void run() {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ignored) {
            }
            if (turn == initialPlayerTurn) {
                mostRecentGameMessage = String.format(timeoutMessage, getName(players.get(turn)));
                stand();
            }
        }
    }
}
