package Foo;

public class Help {
    public static final String help =
            "Working commands {required} [optional]: \n"
                    + " - !ping - test bot is working\n"
                    + " - !gameTime - prints time of your next game session\n"
                    + " - !roll [quantity] d {die size} [modifier] - roll a die\n"
                    + " - !potion - drink a potion\n"
                    + " - !charHelp - lists working commands related to characters";

    public static final String charHelp =
            "Working character related commands {required} [optional]: \n"
                    + " - !newChar {name} [subrace] {race} {class} - create a character\n"
                    + " - !description - shows the details of your current character\n"
                    + " - !races - list of possible races\n"
                    + " - !classes - list of possible classes\n"
                    + " - !weapons - list of possible weapons\n"
                    + " - !changeWeapon {weapon} - change your character's weapon\n"
                    + " - !attack {victim} - have your character (must be created) attack your chosen "
                    + "victim"
                    + " >:]\n"
                    + " - !deleteChar - deletes your character";

    public static final String dmHelp =
            " - !addSessionTime {HH:mm dd/M/yy z} - updates the next session time (see !dateFormat for help)\n"
                    + " - !dateFormat - shows what the above moon runes for date/time format mean";

    public static final String dateFormatHelp =
            "Dates should be in the form 'HH:mm dd/M/yy z'\n"
                    + "e.g. '16:00 21/8/17 BST'\n"
                    + "  or '16:00 21/8/17 GMT + 1' (spaces around '+' are important)";

    public static final String adminHelp =
            " - !addGame {role} {full name} - allows time sessions for a game to be added\n"
                    + " - !removeGame {game} - prevents time sessions for a game from being added\n"
                    + " - !lock - blocks commands from people other than Eywa (not in Junk Yard)\n"
                    + " - !unlock - Lets anyone use commands freely\n"
                    + " - !save - saves character info\n"
                    + " - !exit - runs !save then puts Juuzo to bed";
}
