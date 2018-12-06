package DatabaseBox;

import CoreBox.Bot;
import ExceptionsBox.BadStateException;
import ExceptionsBox.ContactEwaException;

import javax.annotation.Nullable;
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
 * TODO Optimisation create some sort of Args class to make extra parts of SQL like ORDER simpler
 * created: 27/09/18
 */
public class DatabaseTable {
    public enum SQLType {
        TEXT("text", String.class.getName(), Types.VARCHAR), INTEGER("INTEGER", Integer.class.getName(), Types.INTEGER),
        BIT("bit", Boolean.class.getName(), Types.BOOLEAN), INT("int", Integer.class.getName(), Types.INTEGER),
        DOUBLE("float", Double.class.getName(), Types.DOUBLE), DATE("text", ZonedDateTime.class.getName(), Types.DATE);

        private String sqlName;
        private String className;
        private int javaType;


        SQLType(String sqlName, String className, int javaType) {
            this.sqlName = sqlName;
            this.className = className;
            this.javaType = javaType;
        }


        public String getSqlName() {
            return sqlName;
        }
    }



    public enum ColumnFunction {
        MAX, SUM, COUNT(false);

        boolean requireIntColType = true;


        ColumnFunction() {
        }


        ColumnFunction(boolean requireIntColType) {
            this.requireIntColType = requireIntColType;
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
    public static String setDateFormatStr = "yyyy-M-dd HH:mm";
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


    public static ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now(zoneId);
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


    public static String databaseStringToPrintableString(String string) {
        try {
            return formatDateForPrint(parseDateFromDatabase(string));
        } catch (ParseException ignore) {

        }
        return null;
    }


    public static String formatDateForPrint(ZonedDateTime zonedDateTime) {
        return printDateFormat.format(Date.from(zonedDateTime.toInstant()));
    }


    public static ZonedDateTime parseDateFromDatabase(String date) throws ParseException {
        return ZonedDateTime.ofInstant(setDateFormat.parse(date).toInstant(), zoneId);
    }


    public void deleteTable() {
        getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
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
     * Creates and executes an SQL INSERT INTO statement
     *
     * @param args Map<field, value> row information to insert
     */
    public void insert(Map<String, Object> args) {
        // For testing on laptop with outdated compiler - this method can't be used
//        if (2 < 1) {
//            throw new IllegalStateException("Currently invalid");
//        }
        // Check that all required fields are given
        for (String field : fields.keySet()) {
            if (fields.get(field).isRequired() && !args.containsKey(field)) {
                throw new BadStateException("Required field missing: " + field);
            }
        }
        final BuildSQLArgsString buildArgs = new BuildSQLArgsString(args, ", ", 2);
        String values = "?,".repeat(args.size());
        values = values.substring(0, values.length() - 1);
        final String sql = String.format("INSERT INTO %s(%s) VALUES (%s)", tableName, buildArgs.string, values);
        executePreparedStatement(sql, buildArgs.types, buildArgs.values);
    }


    private void executePreparedStatement(String sql, List<SQLType> types, List<Object> objects) {
        getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (types != null && objects != null) {
                setValuesInPreparedStatement(ps, types, objects);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void setValuesInPreparedStatement(PreparedStatement ps, List<SQLType> types, List<Object> objects)
            throws SQLException {
        if (ps == null || types == null || objects == null) {
            throw new IllegalStateException("Null arguments");
        }

        for (int i = 0; i < types.size(); i++) {
            if (objects.get(i) == null) {
                ps.setNull(i + 1, types.get(i).javaType);
                continue;
            }

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
                case DOUBLE:
                    ps.setDouble(i + 1, (double) objects.get(i));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid SQLType: " + types.get(i));
            }
        }
    }


    /**
     * Creates and executes an SQL UPDATE statement
     *
     * @param setArgs Map<field, value> columns to update and their new values
     * @param whereArgs Map<field, value> WHERE arguments which will be ANDed together
     */
    public void updateAND(Map<String, Object> setArgs, @Nullable Map<String, Object> whereArgs) {
        final BuildSQLArgsString setBuildArgs = new BuildSQLArgsString(setArgs, "=?, ", 2);
        final List<SQLType> types = setBuildArgs.types;
        final List<Object> objects = setBuildArgs.values;
        String sql = "UPDATE %s SET %s";
        if (whereArgs == null) {
            sql = String.format(sql, tableName, setBuildArgs.string);
        }
        else {
            final BuildSQLArgsString whereBuildArgs = new BuildSQLArgsString(whereArgs, "=? AND ", 5);
            types.addAll(whereBuildArgs.types);
            objects.addAll(whereBuildArgs.values);
            sql = String.format(sql + " WHERE %s", tableName, setBuildArgs.string, whereBuildArgs.string);
        }
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


    public Object selectAND(@Nullable Map<String, Object> whereArgs, ResultsSetAction resultsSetAction) {
        return selectAND(whereArgs, null, null, resultsSetAction);
    }


    /**
     * Create an execute a SELECT * statement
     *
     * @param whereArgs Map<field, value> WHERE arguments which will be ANDed together
     * @param resultsSetAction what to do with the results set when it's found (must be done within the try/catch
     * containing the prepared statement else rs becomes 'inactive')
     * @return result of resultsSetAction
     */
    public Object selectAND(@Nullable Map<String, Object> whereArgs, @Nullable String sqlStatementSuffix,
                            @Nullable Map<String, Object> suffixValues, ResultsSetAction resultsSetAction) {
        if (whereArgs == null || whereArgs.size() == 0) {
            return executePreparedStatement(String.format("SELECT * FROM %s", tableName), null, null, resultsSetAction);
        }
        sqlStatementSuffix = getSuffixString(sqlStatementSuffix, suffixValues);
        final BuildSQLArgsString buildArgs = new BuildSQLArgsString(whereArgs, "=? AND ", 5);
        final String sql = String.format(
                "SELECT * FROM %s WHERE %s%s", tableName, buildArgs.string, sqlStatementSuffix);
        return executePreparedStatement(sql, buildArgs.types, buildArgs.values, resultsSetAction);
    }


    /**
     * @param types can be null
     * @param objects can be null
     * @param resultsSetAction what to do when the
     * @return what the ResultsSetAction returns
     */
    private Object executePreparedStatement(String sql, List<SQLType> types, List<Object> objects,
                                            ResultsSetAction resultsSetAction) {
        getConnection();
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


    private String getSuffixString(String sqlStatementSuffix, Map<String, Object> suffixValues) {
        if (sqlStatementSuffix == null) {
            return "";
        }
        else {
            if (suffixValues == null) {
                throw new NullPointerException("If a suffix is used, any values used in it must be stated");
            }
            else {
                for (String fieldName : suffixValues.keySet()) {
                    final Object value = suffixValues.get(fieldName);
                    if (value == null) {
                        fieldNameExists(fieldName);
                    }
                    else {
                        checkValidSQLConditionArguments(fieldName, value);
                    }
                }
            }
        }
        return sqlStatementSuffix;
    }


    private boolean fieldNameExists(String fieldName) {
        return fields.keySet().contains(fieldName);
    }


    private void checkValidSQLConditionArguments(String fieldName, Object value) {
        if (!fieldNameExists(fieldName)) {
            throw new IllegalArgumentException("Invalid database field name for " + tableName + ": " + fieldName);
        }
        else if (!validSQLType(fieldName, value)) {
            throw new IllegalArgumentException("Invalid argument type for " + fieldName);
        }
    }


    private boolean validSQLType(String fieldName, Object value) {
        // Bypass type check if the value is null
        return value == null || (fields.get(fieldName).getSqlType().className.equals(value.getClass().getName()));
    }


    public int getFunctionOfIntColumn(ColumnFunction function, String column, @Nullable Map<String, Object> whereArgs) {
        return getFunctionOfIntColumn(function, column, null, null, whereArgs);
    }


    /**
     * @param column must be of type INT
     * @return the column with the function applied to it
     * @throws NullPointerException if no rows in table
     */
    public int getFunctionOfIntColumn(ColumnFunction function, String column, String sqlStatementSuffix,
                                      Map<String, Object> suffixValues, @Nullable Map<String, Object> whereArgs) {
        if (function == null || column == null) {
            throw new IllegalArgumentException("Null arguments");
        }
        else if (!fields.keySet().contains(column)) {
            throw new IllegalArgumentException("No such column");
        }
        else if (function.requireIntColType && fields.get(column).getSqlType() != SQLType.INT && fields.get(column)
                .getSqlType() != SQLType.INTEGER) {
            throw new IllegalArgumentException("Column is not of type int");
        }
        sqlStatementSuffix = getSuffixString(sqlStatementSuffix, suffixValues);

        final ResultsSetAction resultsSetAction = rs -> {
            if (rs.next()) {
                return rs.getInt(function.toString() + "(" + column + ")");
            }
            else {
                throw new NullPointerException("No rows in table");
            }
        };
        String sql = "SELECT %s(%s) FROM %s";
        if (whereArgs == null) {
            sql = String.format(sql, function.toString(), column, tableName);
            return (int) executePreparedStatement(sql + sqlStatementSuffix, null, null, resultsSetAction);
        }
        else {
            final BuildSQLArgsString buildArgs = new BuildSQLArgsString(whereArgs, "=? AND ", 5);
            sql = String.format(
                    sql + " WHERE %s" + sqlStatementSuffix, function.toString(), column, tableName, buildArgs.string);
            return (int) executePreparedStatement(sql, buildArgs.types, buildArgs.values, resultsSetAction);
        }
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
     * Used to turn a map into a string to add to the SQL statement along with the values to insert into the prepared
     * statement and their types
     */
    private class BuildSQLArgsString {
        private List<SQLType> types;
        private List<Object> values;
        private String string;


        /**
         * @param args Map<field, value> arguments which will be turned into a string
         * @param separator in the string what to separate each of the field names using
         * @param removableLength the amount to remove from the end of the string to remove any trailing field
         * separators
         * @throws IllegalArgumentException if the field name or type of the value is invalid
         */
        private BuildSQLArgsString(Map<String, Object> args, String separator, int removableLength) {
            types = new ArrayList<>();
            values = new ArrayList<>();
            final StringBuilder sb = new StringBuilder();
            for (String arg : args.keySet()) {
                final Object value = args.get(arg);
                checkValidSQLConditionArguments(arg, value);
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
