package CommandsBox;

import CharacterBox.AttackBox.Weapon;
import CharacterBox.BroadInfo.Background;
import CharacterBox.BroadInfo.Clazz;
import CharacterBox.BroadInfo.Race;
import CharacterBox.BroadInfo.SubRace;
import CharacterBox.UserCharacter;
import CoreBox.AbstractCommand;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.ContactEwaException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.*;



/**
 * created 13/11/18 (collated other character commands)
 */
public class CharacterCommand extends AbstractCommand {
    // TODO: this better
    private static Map<Long, UserCharacter> partialCharacters = new HashMap<>();
    private static Map<Long, CreationStage> creationStages = new HashMap<>();
    private static Map<Long, Message> messages = new HashMap<>();
    private static BiMap<Emoji, Integer> emojiArguments = createEmojiArgumentsMap();


    private static BiMap<Emoji, Integer> createEmojiArgumentsMap() {
        BiMap<Emoji, Integer> emojiArguments = HashBiMap.create();
        emojiArguments.put(Emoji.LETTER_A, 0);
        emojiArguments.put(Emoji.LETTER_B, 1);
        emojiArguments.put(Emoji.LETTER_C, 2);
        emojiArguments.put(Emoji.LETTER_D, 3);
        emojiArguments.put(Emoji.LETTER_E, 4);
        emojiArguments.put(Emoji.LETTER_F, 5);
        emojiArguments.put(Emoji.LETTER_G, 6);
        emojiArguments.put(Emoji.LETTER_H, 7);
        emojiArguments.put(Emoji.LETTER_I, 8);
        emojiArguments.put(Emoji.LETTER_J, 9);
        emojiArguments.put(Emoji.LETTER_K, 10);
        emojiArguments.put(Emoji.LETTER_L, 11);
        emojiArguments.put(Emoji.LETTER_M, 12);
        emojiArguments.put(Emoji.LETTER_N, 13);
        emojiArguments.put(Emoji.LETTER_O, 14);
        emojiArguments.put(Emoji.LETTER_P, 15);
        emojiArguments.put(Emoji.LETTER_Q, 16);
        emojiArguments.put(Emoji.LETTER_R, 17);
        emojiArguments.put(Emoji.LETTER_S, 18);
        emojiArguments.put(Emoji.LETTER_T, 19);
        emojiArguments.put(Emoji.LETTER_U, 20);
        emojiArguments.put(Emoji.LETTER_V, 21);
        emojiArguments.put(Emoji.LETTER_W, 22);
        emojiArguments.put(Emoji.LETTER_X, 23);
        emojiArguments.put(Emoji.LETTER_Y, 24);
        emojiArguments.put(Emoji.LETTER_Z, 25);
        return emojiArguments;
    }


    /**
     * Executes the command associated with the emoji. Does nothing if there is no command associated with the emoji
     *
     * @param messageReaction an emoji in the form of a one/two char array
     * @param player the one who invoked the command
     * @return true if the reaction belonged to this method
     */
    public static boolean executeFromAddReaction(Long messageID, MessageReaction messageReaction, Member player) {
        if (!messages.containsKey(messageID)) {
            return false;
        }
        final Message message = messages.get(messageID);
        if (message.getMentionedMembers().size() != 1 || !message.getMentionedMembers().contains(player)) {
            return false;
        }

        final Optional<Emoji> emojiOptional = Emoji.getFromString(messageReaction.getReactionEmote().getName());
        if (!emojiOptional.isPresent() || !emojiArguments.containsKey(emojiOptional.get())) {
            return false;
        }
        final Long authorID = player.getUser().getIdLong();
        if (!partialCharacters.containsKey(authorID)) {
            throw new BadStateException("I can't find your character for some reason... Maybe try starting again?");
        }

        /*
         * Is emoji valid for this creation stage
         */
        final UserCharacter character = partialCharacters.get(authorID);
        final int selection = emojiArguments.get(emojiOptional.get());
        final CreationStage creationStage = creationStages.get(authorID);
        Race.RaceEnum race = null;
        if (creationStage != CreationStage.RACE) {
            race = character.getRace();
        }
        if (selection > creationStage.getOptions(race).length) {
            return false;
        }

        /*
         * Update
         */
        creationStage.update(character, selection);

        final Optional<CreationStage> nextStageOptional = creationStage.getNextStage();
        if (nextStageOptional.isPresent()) {
            /*
             * Prepare for the next choice
             */
            race = character.getRace();
            CreationStage nextStage = nextStageOptional.get();
            if (nextStage == CreationStage.SUBRACE && nextStage.getOptions(race).length == 0) {
                // Next stage will always be valid because subrace is not last
                nextStage = nextStage.getNextStage().get();
            }
            creationStages.put(authorID, nextStage);
            message.editMessage(message.getContentRaw().split("\n")[0] + "\n" + nextStage.optionsToString(race))
                    .queue();

            /*
             * Update reactions
             */
            message.clearReactions().complete();
            for (int i = 0; i < nextStage.getOptions(character.getRace()).length; i++) {
                emojiArguments.inverse().get(i).addAsReaction(message);
            }
        }
        else {
            /*
             * Complete the creation and clean up
             */
            character.completeCreation(authorID);
            message.editMessage("__**Creation complete**__ :tada: \n" + character.getDescription()).queue();
            partialCharacters.remove(authorID);
            creationStages.remove(authorID);
            messages.remove(messageID);
            message.clearReactions().queue();
        }

        return true;
    }


    /**
     * Adds the given message to the map of messages under the authorID
     */
    public static void addMessage(Message message) {
        if (message.getContentRaw().startsWith(SecondaryArg.NEW.messageStart)) {
            messages.put(message.getIdLong(), message);

            Long authorID = message.getMentionedUsers().get(0).getIdLong();
            for (int i = 0; i < creationStages.get(authorID).getOptions(null).length; i++) {
                emojiArguments.inverse().get(i).addAsReaction(message);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "char";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Create a d&d character... nerd.";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getArguments() {
        return "new {name} / description / attack {victim} / get weapons list / change weapon {weapon} / delete";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.CHARACTER;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());
        executeSecondaryArgument(SecondaryArg.class, args, event);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    private enum SecondaryArg implements SecondaryCommandAction {
        NEW("New character for ") {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                if (args.length() == 0) {
                    throw new BadUserInputException("No name for your brave adventurer?");
                }

                String message = messageStart + event.getMember().getAsMention() + "\n";
                long authorID = event.getAuthor().getIdLong();
                if (partialCharacters.containsKey(authorID)) {
                    message
                            += "You're already in the process of making a character, but I guess you can start again."
                            + "..\n";
                }

                partialCharacters.put(authorID, new UserCharacter(args));
                creationStages.put(authorID, CreationStage.RACE);
                message += CreationStage.RACE.optionsToString(null);
                sendMessage(event.getChannel(), message);
            }
        },
        DELETE {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                UserCharacter.deleteCharacter(event.getAuthor().getIdLong());
                sendMessage(event.getChannel(), "Character deleted");
            }
        },
        ATTACK {
            /**
             * {@inheritDoc}
             * @param args {victim}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                sendMessage(event.getChannel(), UserCharacter.attack(event.getAuthor(), args));
            }
        },
        GETWEAPONSLIST {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                sendMessage(event.getChannel(), Weapon.getWeaponsList());
            }
        },
        CHANGEWEAPON {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                UserCharacter.changeCharacterWeapon(event.getAuthor().getIdLong(), args);
                sendMessage(event.getChannel(), "Weapon change successful, enjoy your new toy.");
            }
        },
        DESCRIPTION {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                sendMessage(event.getChannel(), UserCharacter.getCharacterDescription(event.getAuthor().getIdLong()));
            }
        };

        String messageStart;


        SecondaryArg() {
        }


        SecondaryArg(String messageStart) {
            this.messageStart = messageStart;
        }
    }



    enum CreationStage implements CreationStageActions {
        RACE {
            /**
             * {@inheritDoc}
             */
            @Override
            public Object[] getOptions(Race.RaceEnum race) {
                return Race.RaceEnum.values();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void update(UserCharacter userCharacter, int index) {
                userCharacter.setRace(Race.RaceEnum.values()[index]);
            }
        },
        SUBRACE {
            /**
             * {@inheritDoc}
             */
            @Override
            public Object[] getOptions(Race.RaceEnum race) {
                if (race == null) {
                    throw new ContactEwaException("Some problem with racism and nulls...");
                }
                final SubRace.SubRaceEnum[] values = SubRace.SubRaceEnum.values();
                final List<SubRace.SubRaceEnum> validValues = new ArrayList<>();
                for (SubRace.SubRaceEnum value : values) {
                    if (Race.getRaceInfo(value).getMainRace() == race) {
                        validValues.add(value);
                    }
                }
                return validValues.toArray();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void update(UserCharacter userCharacter, int index) {
                userCharacter.setSubRace((SubRace.SubRaceEnum) getOptions(userCharacter.getRace())[index]);
            }
        },
        CLASS {
            /**
             * {@inheritDoc}
             */
            @Override
            public Object[] getOptions(Race.RaceEnum race) {
                return Clazz.ClassEnum.values();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void update(UserCharacter userCharacter, int index) {
                userCharacter.setClazz(Clazz.ClassEnum.values()[index]);
            }
        },
        BACKGROUND {
            /**
             * {@inheritDoc}
             */
            @Override
            public Object[] getOptions(Race.RaceEnum race) {
                return Background.BackgroundEnum.values();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void update(UserCharacter userCharacter, int index) {
                userCharacter.setBackground(Background.BackgroundEnum.values()[index].toString());
            }
        };


        Optional<CreationStage> getNextStage() {
            final CreationStage[] values = CreationStage.values();
            for (int i = 0; i < values.length; i++) {
                if (values[i] == this) {
                    if (i + 1 < values.length) {
                        return Optional.of(values[i + 1]);
                    }
                    else {
                        return Optional.empty();
                    }
                }
            }
            throw new IllegalArgumentException("This is somehow not a value...");
        }


        String optionsToString(Race.RaceEnum race) {
            StringBuilder sb = new StringBuilder("```");
            sb.append(String.format("Choose a %s:\n", this.toString().toLowerCase()));
            for (int i = 0; i < getOptions(race).length; i++) {
                sb.append('\t');
                sb.append((char) (i + 65));
                sb.append(" ");
                sb.append(getOptions(race)[i].toString());
                sb.append('\n');
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append("```");
            return sb.toString();
        }
    }



    private interface CreationStageActions {
        /**
         * Update the character with the desired update for the stage
         *
         * @param index the chosen option's index in getOptions
         */
        void update(UserCharacter userCharacter, int index);


        /**
         * @param race cannot be null for SubRace
         * @return all possible options the user can choose at this stage
         */
        Object[] getOptions(Race.RaceEnum race);
    }
}