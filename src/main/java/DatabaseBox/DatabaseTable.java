package DatabaseBox;

import CoreBox.Bot;
import ExceptionsBox.ContactEwaException;

import java.io.File;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Date;



/**
 * Abstracts database connections and provides methods for querying the database
 * TODO have a test database that inherits from here so that live data is not edited when testing
 * created: 27/09/18
 */
public class DatabaseTable {
    public enum SQLType {
        TEXT("text", String.class.getName()), BIT("bit", Boolean.class.getName()), INT("int", Integer.class.getName()),
        DATE("text", ZonedDateTime.class.getName()), INTEGER("INTEGER", Integer.class.getName());

        private String sqlName;
        private String className;


        SQLType(String sqlName, String className) {
            this.sqlName = sqlName;
            this.className = className;
        }


        public String getSqlName() {
            return sqlName;
        }


        public String getClassName() {
            return className;
        }
    }



    /*
     * Database connection
     */
    private static final String databaseFileLocation = "Juuzo2.db";
    // Used to establish the connection to the database
    private static final String urlPrefix = "jdbc:sqlite:" + Bot.getPathToJuuzoBot();
    /*
     * Date formats (all dates are stored in UTC)
     * TODO FIX All dates are not being stored in UTC Q.Q plz fix
     */
    // Date format given as an argument and stored in the database
    public static String setDateFormatStr = "HH:mm dd/M/yy z";
    private static DateFormat setDateFormat = new SimpleDateFormat(setDateFormatStr);
    // Date format when printing the date
    private static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM 'at' HH:mm z");
    private static ZoneId zoneId = ZoneId.of("UTC");
    private static String url = urlPrefix + databaseFileLocation;
    private static Connection connection = null;
    /*
     * Table information
     */
    private String tableName;
    private Map<String, DatabaseFieldArgs> fields;
    private PrimaryKey primaryKey;


    public DatabaseTable(String tableName, Map<String, DatabaseFieldArgs> fields, PrimaryKey primaryKey) {
        this.tableName = tableName;
        this.fields = fields;
        this.primaryKey = primaryKey;
    }


    /**
     * Primary key: tableName + "ID"
     */
    public DatabaseTable(String tableName, Map<String, DatabaseFieldArgs> fields) {
        this.tableName = tableName;
        this.fields = fields;

        final String primaryKeyName = tableName.toLowerCase() + "ID";
        fields.put(primaryKeyName, new DatabaseFieldArgs(SQLType.INTEGER, "PRIMARY KEY", false));
        primaryKey = new PrimaryKey(primaryKeyName);
    }


    public static void setTestMode() {
        url = urlPrefix + "JuuzoTest.db";
    }


    public static boolean isInTestMode() {
        return !url.equals(urlPrefix + databaseFileLocation);
    }


    private static void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static ZonedDateTime parseDate(String string) throws ParseException {
        return ZonedDateTime.ofInstant(setDateFormat.parse(string).toInstant(), zoneId);
    }


    public static String zonedDateTimeToString(ZonedDateTime zonedDateTime) {
        return printDateFormat.format(Date.from(zonedDateTime.toInstant()));
    }


    public static ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now(zoneId);
    }


    public static ZonedDateTime getDatabaseDateFromString(String date) {
        try {
            return ZonedDateTime.ofInstant(setDateFormat.parse(date).toInstant(), zoneId);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Database date parse failed");
        }
    }


    public static DatabaseTable createDatabaseTable(String tableName, DatabaseField[] fields) {
        return new DatabaseTable(tableName, fieldsArrayToMap(fields));
    }


    private static Map<String, DatabaseFieldArgs> fieldsArrayToMap(DatabaseField[] fields) {
        final Map<String, DatabaseFieldArgs> fieldsMap = new HashMap<>();
        for (DatabaseField field : fields) {
            fieldsMap.put(field.getFieldName(), new DatabaseFieldArgs(field.getSqlType(), field.isRequired()));
        }
        return fieldsMap;
    }


    public static DatabaseTable createDatabaseTable(String tableName, DatabaseField[] fields,
                                                    PrimaryKey primaryKey) {
        return new DatabaseTable(tableName, fieldsArrayToMap(fields), primaryKey);
    }


    public void deleteTable() {
        if (connection == null) {
            createConnection();
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Fetches the existing database or creates a new database if one is not found
     */
    private static void createConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(url);

                if (!new File(databaseFileLocation).exists()) {
                    if (connection != null) {
                        connection.getMetaData();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Creates and executes an SQL INSERT INTO statement
     *
     * @param args Map<field, value> row information to insert
     */
    public void insert(Map<String, Object> args) {
        // TODO Check that all required fields are given
        final BuildSQLArgsString buildArgs = new BuildSQLArgsString(args, ", ", 2);
        String values = "?,".repeat(args.size());
        values = values.substring(0, values.length() - 1);
        final String sql = String.format("INSERT INTO %s(%s) VALUES (%s)", tableName, buildArgs.string, values);
        executePreparedStatement(sql, buildArgs.types, buildArgs.values);
    }


    private void executePreparedStatement(String sql, List<SQLType> types, List<Object> objects) {
        getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setValuesInPreparedStatement(ps, types, objects);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Make a connection to the database and create the table (if it doesn't already exist)
     */
    public Connection getConnection() {
        if (connection == null) {
            createConnection();
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(getCreateTableString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }


    private void setValuesInPreparedStatement(PreparedStatement ps, List<SQLType> types, List<Object> objects)
            throws SQLException {
        for (int i = 0; i < types.size(); i++) {
            switch (types.get(i)) {
                case TEXT:
                    ps.setString(i + 1, (String) objects.get(i));
                    break;
                case BIT:
                    ps.setBoolean(i + 1, (boolean) objects.get(i));
                    break;
                case DATE:
                    final ZonedDateTime zonedDateTime = (ZonedDateTime) objects.get(i);
                    ps.setString(i + 1, setDateFormat.format(Date.from(zonedDateTime.toInstant())));
                    break;
                case INT:
                case INTEGER:
                    ps.setInt(i + 1, (int) objects.get(i));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid SQLType: " + types.get(i));
            }
        }
    }


    public static String databaseStringToPrintableString(String string) {
        return zonedDateTimeToString(getDatabaseDateFromString(string));
    }


    /**
     * @return SQL CREATE statement for this the table
     */
    private String getCreateTableString() {
        final StringBuilder sb = new StringBuilder();
        for (String fieldName : fields.keySet()) {
            sb.append(String.format("%s %s,", fieldName, fields.get(fieldName).createTableLine()));
        }
        Optional<String> primaryKeyOptional = primaryKey.getPrimaryKeySQLLine(tableName);
        String primaryKeyString;
        if (!primaryKeyOptional.isPresent()) {
            sb.deleteCharAt(sb.length() - 1);
            primaryKeyString = "";
        }
        else {
            primaryKeyString = primaryKeyOptional.get();
        }
        return String.format("CREATE TABLE IF NOT EXISTS %s (%s%s);", tableName, sb.toString(), primaryKeyString);
    }


    /**
     * Creates and executes an SQL UPDATE statement
     *
     * @param setArgs Map<field, value> columns to update and their new values
     * @param whereArgs Map<field, value> WHERE arguments which will be ANDed together
     */
    public void updateAND(Map<String, Object> setArgs, Map<String, Object> whereArgs) {
        final BuildSQLArgsString setBuildArgs = new BuildSQLArgsString(setArgs, "=?, ", 2);
        final List<SQLType> types = setBuildArgs.types;
        final List<Object> objects = setBuildArgs.values;
        final BuildSQLArgsString whereBuildArgs = new BuildSQLArgsString(whereArgs, "=? AND ", 5);
        types.addAll(whereBuildArgs.types);
        objects.addAll(whereBuildArgs.values);
        final String sql = String.format(
                "UPDATE %s SET %s WHERE %s", tableName, setBuildArgs.string, whereBuildArgs.string);
        executePreparedStatement(sql, types, objects);
    }


    /**
     * Creates and executes an SQL DELETE statement
     *
     * @param args Map<field, value> WHERE arguments which will be ANDed together
     */
    public void deleteAND(Map<String, Object> args) {
        final BuildSQLArgsString buildArgs = new BuildSQLArgsString(args, "=? AND ", 5);
        final String sql = String.format("DELETE FROM %s WHERE %s", tableName, buildArgs.string);
        executePreparedStatement(sql, buildArgs.types, buildArgs.values);
    }


    /**
     * Create an execute a SELECT * statement
     *
     * @param args Map<field, value> WHERE arguments which will be ANDed together
     * @param resultsSetAction what to do with the results set when it's found (must be done within the try/catch
     * containing the prepared statement else rs becomes 'inactive')
     * @return result of resultsSetAction
     */
    public Object selectAND(Map<String, Object> args, ResultsSetAction resultsSetAction) {
        getConnection();
        if (args == null || args.size() == 0) {
            return executePreparedStatement(String.format("SELECT * FROM %s", tableName), null, null, resultsSetAction);
        }

        final BuildSQLArgsString buildArgs = new BuildSQLArgsString(args, "=? AND ", 5);
        final String sql = String.format("SELECT * FROM %s WHERE %s", tableName, buildArgs.string);
        return executePreparedStatement(sql, buildArgs.types, buildArgs.values, resultsSetAction);
    }


    public Object selectAll(ResultsSetAction resultsSetAction) {
        return selectAND(null, resultsSetAction);
    }


    /**
     * @param types can be null
     * @param objects can be null
     * @param resultsSetAction what to do when the
     * @return what the ResultsSetAction returns
     */
    private Object executePreparedStatement(String sql, List<SQLType> types, List<Object> objects,
                                            ResultsSetAction resultsSetAction) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (types != null && objects != null) {
                setValuesInPreparedStatement(ps, types, objects);
            }
            else if (sql.contains("?")) {
                throw new ContactEwaException("Parameters in SQL not set");
            }
            ResultSet rs = ps.executeQuery();
            return resultsSetAction.execute(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new ContactEwaException("Database query failed");
    }


    /**
     * @return the number of rows in the table
     */
    public int getRowCount() {
        getConnection();
        final String sql = "SELECT COUNT(*) FROM " + tableName;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            final ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("COUNT(*)");
            }
            else {
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Database query GetRowCount failed");
    }


    /**
     * @param column must be of type INT
     * @return the max value in the given column
     */
    public int getMaxInColumn(String column) {
        if (!fields.keySet().contains(column)) {
            throw new IllegalArgumentException("No such column");
        }
        else if (fields.get(column).getSqlType() != SQLType.INT) {
            throw new IllegalArgumentException("Column is not of type int");
        }

        getConnection();
        final String sql = "SELECT MAX(" + column + ") FROM " + tableName;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            final ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("MAX(" + column + ")");
            }
            else {
                throw new NullPointerException("No rows in table");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Database query GetMaxInColumn failed");
    }


    String getTableName() {
        return tableName;
    }


    public String getPrimaryKey() {
        return primaryKey.toString();
    }


    /**
     * Defines what to do with a results set from an SQL query
     */
    public interface ResultsSetAction {
        Object execute(ResultSet rs) throws SQLException;
    }


    public interface DatabaseField {
        String getFieldName();


        SQLType getSqlType();


        boolean isRequired();
    }



    /**
     * Used to turn a map into a string to add to the SQL statement along with the values to insert into the prepared statement and their types
     */
    private class BuildSQLArgsString {
        private List<SQLType> types;
        private List<Object> values;
        private String string;


        /**
         * @param args Map<field, value> arguments which will be turned into a string
         * @param separator in the string what to separate each of the field names using
         * @param removableLength the amount to remove from the end of the string to remove any trailing field separators
         * @throws IllegalArgumentException if the field name or type of the value is invalid
         */
        private BuildSQLArgsString(Map<String, Object> args, String separator, int removableLength) {
            types = new ArrayList<>();
            values = new ArrayList<>();
            final StringBuilder sb = new StringBuilder();
            for (String arg : args.keySet()) {
                final Object value = args.get(arg);
                if (!fields.keySet().contains(arg)) {
                    throw new IllegalArgumentException("Invalid database field name for " + tableName + ": " + arg);
                }
                else if (!fields.get(arg).getSqlType().getClassName().equals(value.getClass().getName())) {
                    throw new IllegalArgumentException("Invalid argument type for " + arg);
                }
                types.add(fields.get(arg).getSqlType());
                values.add(value);
                sb.append(arg);
                sb.append(separator);
            }
            sb.delete(sb.length() - removableLength, sb.length());
            string = sb.toString();
        }
    }
}
