package DatabaseBox;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;



/**
 * Helper class for building an SQL statement with ? to be set by the prepared statement
 * moved from inner class 7/12/18
 */
class BuildSQLArgsString {
    private List<DatabaseTable.SQLType> types = new ArrayList<>();
    private List<Object> values = new ArrayList<>();
    private StringBuilder string;


    BuildSQLArgsString(@NotNull String stringStart) {
        string = new StringBuilder(stringStart);
    }


    /**
     * @param sqlType the type that '?' will be
     * @param value the value that '?' will be. Does not check the types are correct
     */
    void append(@NotNull String string, @NotNull DatabaseTable.SQLType sqlType, Object value) {
        this.string.append(string);
        types.add(sqlType);
        values.add(value);
    }


    void append(@NotNull String string) {
        this.string.append(string);
    }


    void deleteCharsFromEnd(int charsToDelete) {
        string.delete(string.length() - charsToDelete, string.length());
    }


    List<DatabaseTable.SQLType> getTypes() {
        return types;
    }


    List<Object> getValues() {
        return values;
    }


    String getString() {
        return string.toString();
    }
}
