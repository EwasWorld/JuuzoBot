package DatabaseBox;

/**
 * TODO Refactor, is this even needed? Rather than Map<String, DatabaseFieldArgs> why not just Map<String, SQLType>. Generate and store creation string on databaseTable instantiation?
 * created: 27/09/18
 */
public class DatabaseFieldArgs {
    private DatabaseTable.SQLType sqlType;
    private String options;


    public DatabaseFieldArgs(DatabaseTable.SQLType sqlType, String options, boolean isRequired) {
        this.sqlType = sqlType;
        this.options = options;
        if (isRequired) {
            this.options = "NOT NULL " + options;
        }
    }


    public DatabaseFieldArgs(DatabaseTable.SQLType sqlType, boolean isRequired) {
        this.sqlType = sqlType;
        if (isRequired) {
            this.options = "NOT NULL";
        }
        else {
            this.options = "";
        }
    }


    String createTableLine() {
        return String.format("%s %s", sqlType.getSqlName(), options);
    }


    public DatabaseTable.SQLType getSqlType() {
        return sqlType;
    }
}
