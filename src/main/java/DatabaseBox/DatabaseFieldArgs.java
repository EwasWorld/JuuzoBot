package DatabaseBox;

/**
 * created: 27/09/18
 */
class DatabaseFieldArgs {
    private static final String notNull = "NOT NULL";
    private DatabaseTable.SQLType sqlType;
    private String options;


    DatabaseFieldArgs(DatabaseTable.SQLType sqlType, String options, boolean isRequired) {
        this.sqlType = sqlType;
        this.options = options;
        if (isRequired) {
            this.options = notNull + " " + options;
        }
    }


    DatabaseFieldArgs(DatabaseTable.SQLType sqlType, boolean isRequired) {
        this.sqlType = sqlType;
        if (isRequired) {
            this.options = notNull;
        }
        else {
            this.options = "";
        }
    }


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
