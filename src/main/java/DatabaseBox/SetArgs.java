package DatabaseBox;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;



/**
 * Used to add parts for setting elements in an SQL statement
 * created 10/12/18
 */
public class SetArgs {
    private DatabaseTable databaseTable;
    private Map<String, Object> setArgs = new HashMap<>();


    public SetArgs(@NotNull DatabaseTable databaseTable) {
        this.databaseTable = databaseTable;
    }


    public SetArgs(@NotNull DatabaseTable databaseTable, @NotNull Map<String, Object> setArgs) {
        this.databaseTable = databaseTable;
        addSetArgument(setArgs);
    }


    public void addSetArgument(@NotNull Map<String, Object> setArgs) {
        for (Map.Entry<String, Object> entry : setArgs.entrySet()) {
            databaseTable.checkValidSQLConditionArguments(entry.getKey(), entry.getValue());
        }
        this.setArgs.putAll(setArgs);
    }


    public SetArgs(@NotNull Args args) {
        databaseTable = args.getDatabaseTable();
        for (int i = 0; i < args.getWhereFields().size(); i++) {
            if (args.getWhereOperator().get(i) != Args.Operator.EQUALS || args.getWhereObjects().get(i) == null) {
                throw new IllegalStateException("Cannot change to set args");
            }
            setArgs.put(args.getWhereFields().get(i), args.getWhereObjects().get(i));
        }
    }


    public void addSetArgument(@NotNull String fieldName, Object value) {
        databaseTable.checkValidSQLConditionArguments(fieldName, value);
        setArgs.put(fieldName, value);
    }


    BuildSQLArgsString getSetSQLString(@NotNull String separator, int removableLength) {
        final BuildSQLArgsString buildSQLArgsString = new BuildSQLArgsString("");
        for (Map.Entry<String, Object> entry : setArgs.entrySet()) {
            final String fieldName = entry.getKey();
            buildSQLArgsString.append(fieldName + separator, databaseTable.getFieldType(fieldName), entry.getValue());
        }
        buildSQLArgsString.deleteCharsFromEnd(removableLength);
        return buildSQLArgsString;
    }


    Set<String> getAllFieldNames() {
        return setArgs.keySet();
    }
}
