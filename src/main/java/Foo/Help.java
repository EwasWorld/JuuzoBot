package Foo;

public class Help {
    public static final String dateFormatHelp =
            "Dates should be in the form 'HH:mm dd/M/yy z'\n"
                    + "e.g. '16:00 21/8/17 BST'\n"
                    + "  or '16:00 21/8/17 GMT + 1' (spaces around '+' are important)";
    private static final String start = "\t:carrot: !";
    public static final String charHelp =
            "Working character related commands {required} [optional]: \n"
                    + start + "newChar {name} [subrace] {race} {class} - create a character\n"
                    + start + "description - shows the details of your current character\n"
                    + start + "races - list of possible races\n"
                    + start + "classes - list of possible classes\n"
                    + start + "weapons - list of possible weapons\n"
                    + start + "changeWeapon {weapon} - change your character's weapon\n"
                    + start + "attack {victim} - have your character (must be created) attack your chosen victim >:]\n"
                    + start + "deleteChar - deletes your character\n";
    private static final String end = "\n(NB: tHeY'rE cAsE sEnSiTivE)";
    private static final String help =
            "Working commands {required} [optional]:\n"
                    + start + "charHelp - lists working commands related to characters\n"
                    + start + "ping - test bot is working\n"
                    + start + "gameTime - prints time of your next game session\n"
                    + start + "roll [quantity] d {die size} [modifier] - roll a die\n"
                    + start + "roll {skill/ability/initiative} - roll a skill check, saving throw, or initiative\n"
                    + start + "potion - drink a potion\n"
                    + start + "addQuote {start of message} - adds a quote to the bot to be preserved forever ｡◕‿◕｡✿ "
                    + "(NB: Juuzo has the memory of a goldfish and thus can only quote from the last 20 messages)\n"
                    + start + "getQuote [quote number] - "
                    + "retrieves the desired quote (or a random one if no number is given)\n";
    private static final String dmHelp =
            start + "addSessionTime {HH:mm dd/M/yy z} - updates the next session time (see !dateFormat for help)\n"
                    + start + "dateFormat - shows what the above moon runes for date/time format mean\n"
                    + start + "gameReminder - @ mentions the game role and displays the countdown\n";
    private static final String adminHelp =
            start + "addGame {role} {full name} - allows time sessions for a game to be added\n"
                    + start + "removeGame {game} - prevents time sessions for a game from being added\n"
                    + start + "removeQuote {quote number} - removes a quote from the bot\n"
                    + start + "lock - blocks commands from people other than Eywa (not in Junk Yard)\n"
                    + start + "unlock - Lets anyone use commands freely\n"
                    + start + "save - saves character info\n"
                    + start + "exit - runs !save then puts Juuzo to bed\n";


    public static String getHelp() {
        return help + end;
    }


    public static String getDmHelp() {
        return help + dmHelp + end;
    }


    public static String getAdminHelp() {
        return help + dmHelp + adminHelp + end;
    }
}
