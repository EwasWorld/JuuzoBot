package DatabaseBox;

import org.jetbrains.annotations.NotNull;



/**
 * Gives the properties of a database field
 * created: 27/09/18
 */
class DatabaseFieldArgs {
    private static final String notNull = "NOT NULL";
    private DatabaseTable.SQLType sqlType;
    private String options;


    DatabaseFieldArgs(@NotNull DatabaseTable.SQLType sqlType, @NotNull String options, boolean isRequired) {
        this.sqlType = sqlType;
        this.options = options;
        if (isRequired) {
            this.options = notNull + " " + options;
        }
    }


    DatabaseFieldArgs(@NotNull DatabaseTable.SQLType sqlType, boolean isRequired) {
        this.sqlType = sqlType;
        if (isRequired) {
            this.options = notNull;
        }
        else {
            this.options = "";
        }
    }


    /**
     * @return the part of a CREATE TABLE string that comes after the name of the field
     */
    String createTableLine() {
        return String.format("%s %s", sqlType.getSqlName(), options);
    }


    DatabaseTable.SQLType getSqlType() {
        return sqlType;
    }


    boolean isRequired() {
        return options.contains(notNull);
    }
}
