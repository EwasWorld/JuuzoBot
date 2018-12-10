package DatabaseBox;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * Used to add parts to an SQL statement such as WHERE and ORDER BY (not including set arguments). WHERE args are
 * always ANDed together
 *
 * @see SetArgs for arguments to do with setting values such as when using UPDATE or INSERT
 * created 10/12/18
 */
public class Args {
    public enum Operator {
        EQUALS("="), GREATER_THAN(">"), LESS_THAN("<");

        String string;


        Operator(String string) {
            this.string = string;
        }
    }



    private DatabaseTable databaseTable;
    private List<String> whereFields = new ArrayList<>();
    private List<Operator> whereOperator = new ArrayList<>();
    private List<Object> whereObjects = new ArrayList<>();
    private List<String> customWhere = new ArrayList<>();
    private String orderBy;
    private boolean orderAscending;


    public Args(@NotNull DatabaseTable databaseTable) {
        this.databaseTable = databaseTable;
    }


    public Args(@NotNull DatabaseTable databaseTable, @NotNull Map<String, Object> whereArgs) {
        this.databaseTable = databaseTable;
        for (Map.Entry<String, Object> entry : whereArgs.entrySet()) {
            addWhereField(entry.getKey(), entry.getValue());
        }
    }


    public void addWhereField(@NotNull String fieldName, @NotNull Object value) {
        addWhereField(fieldName, value, Operator.EQUALS);
    }


    public void addWhereField(@NotNull String fieldName, @NotNull Object value, @NotNull Operator operator) {
        databaseTable.checkValidSQLConditionArguments(fieldName, value);
        whereFields.add(fieldName);
        whereOperator.add(operator);
        whereObjects.add(value);
        customWhere.add(null);
    }


    public Args(@NotNull DatabaseTable databaseTable, @NotNull String whereFieldName, @NotNull Object whereValue) {
        this.databaseTable = databaseTable;
        addWhereField(whereFieldName, whereValue);
    }


    public void addCustomWhereField(@NotNull String fieldName, @NotNull String value, @NotNull Operator operator) {
        databaseTable.checkValidFieldName(fieldName);
        whereFields.add(fieldName);
        whereOperator.add(operator);
        whereObjects.add(null);
        customWhere.add(value);
    }


    /**
     * Removes the first occurrence of the given field name
     */
    public void removeWhereField(@NotNull String fieldName) {
        int i = whereFields.indexOf(fieldName);
        whereFields.remove(i);
        whereOperator.remove(i);
        whereObjects.remove(i);
        customWhere.remove(i);
    }


    /**
     * @param orderAscending if true, ints: small-large, text: a-z, dates: oldest-newest
     */
    public void setOrderBy(@NotNull String fieldName, boolean orderAscending) {
        databaseTable.checkValidFieldName(fieldName);
        orderBy = fieldName;
        this.orderAscending = orderAscending;
    }


    BuildSQLArgsString getSQLString() {
        return getSQLString(true);
    }


    BuildSQLArgsString getSQLString(boolean isOrderByAllowed) {
        final BuildSQLArgsString buildSQLArgsString = new BuildSQLArgsString("");
        if (whereFields.size() > 0) {
            buildSQLArgsString.append(" WHERE ");
            final String separator = " AND ";
            for (int i = 0; i < whereFields.size(); i++) {
                final String fieldName = whereFields.get(i);
                if (whereObjects.get(i) != null) {
                    buildSQLArgsString.append(fieldName + whereOperator.get(i).string + "?",
                                              databaseTable.getFieldType(fieldName), whereObjects.get(i));
                }
                else {
                    buildSQLArgsString.append(fieldName + whereOperator.get(i).string + customWhere.get(i));
                }
                buildSQLArgsString.append(separator);
            }
            buildSQLArgsString.deleteCharsFromEnd(separator.length());
        }
        if (isOrderByAllowed && orderBy != null) {
            buildSQLArgsString.append(" ORDER BY " + orderBy);
            if (orderAscending) {
                buildSQLArgsString.append(" ASC");
            }
            else {
                buildSQLArgsString.append(" DESC");
            }
        }
        return buildSQLArgsString;
    }


    public boolean isEmpty() {
        return whereFields.size() == 0 && orderBy == null;
    }


    DatabaseTable getDatabaseTable() {
        return databaseTable;
    }


    List<String> getWhereFields() {
        return whereFields;
    }


    List<Operator> getWhereOperator() {
        return whereOperator;
    }


    List<Object> getWhereObjects() {
        return whereObjects;
    }
}
