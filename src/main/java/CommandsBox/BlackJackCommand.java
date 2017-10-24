package CommandsBox;

import BlackJackBox.GameInstance;
import CoreBox.AbstractCommand;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class BlackJackCommand extends AbstractCommand {
    private static boolean gameRunning = false;
    private static GameInstance gameInstance;


    public static void setGameRunningFalse() {
        gameRunning = false;
    }


    @Override
    public String getCommand() {
        return "blackjack";
    }


    @Override
    public String getDescription() {
        return "";
    }


    @Override
    public String getArguments() {
        return "{new/join/start/hit/stand/turn/hand/dealer}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        final BlackJackArguments argument = getArgument(args);
        argumentSwitcher(event.getTextChannel(), argument, event.getMember());
    }
    
    
    private void argumentSwitcher(TextChannel channel, BlackJackArguments argument, Member player) {
        if (argument == BlackJackArguments.NEW) {
            newGame(player, channel);
        }
        else {
            if (gameRunning) {
                switch (argument) {
                    case JOIN:
                        sendMessage(channel, gameInstance.addPlayer(player));
                        break;
                    case START:
                        sendMessage(channel, gameInstance.startGame());
                        break;
                    case HIT:
                        sendMessage(channel, gameInstance.hitMe(player));
                        break;
                    case STAND:
                        sendMessage(channel, gameInstance.stand(player));
                        break;
                    case TURN:
                        sendMessage(channel, gameInstance.getTurn());
                        break;
                    case HAND:
                        sendMessage(channel, gameInstance.getHand(player));
                        break;
                    case DEALER:
                        sendMessage(channel, gameInstance.getDealerHand());
                        break;
                }
            }
            else {
                throw new BadStateException("You need to create a game first");
            }
        }
    }


    private void newGame(Member player, TextChannel channel) {
        if (!gameRunning) {
            gameRunning = true;
            gameInstance = new GameInstance(player);
            sendMessage(channel, "Game created");
        }
        else {
            throw new BadStateException("Game already created, try joining it");
        }
    }
    
    
    private BlackJackArguments getArgument(String args) {
        try {
            return BlackJackArguments.valueOf(args.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException("I don't understand that argument. Use one of " + getArguments());
        }
    }
    
    
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
    
    

    private enum BlackJackArguments {NEW, JOIN, START, HIT, STAND, TURN, HAND, DEALER}
}
