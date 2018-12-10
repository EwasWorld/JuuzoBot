package DatabaseBox;

import CoreBox.Bot;
import ExceptionsBox.BadStateException;
import ExceptionsBox.ContactEwaException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.*;



/**
 * Abstracts database connections and provides methods for querying the database
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



    /**
     * Functions that can be applied to columns e.g. MAX(columnName)
     */
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
    private static String setDateFormatStr = "yyyy-M-dd HH:mm";
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


    public DatabaseTable(@NotNull String tableName, @NotNull DatabaseField[] fields, @NotNull PrimaryKey primaryKey) {
        this(tableName, fieldsArrayToMap(fields), primaryKey);
    }


    public DatabaseTable(@NotNull String tableName, @NotNull Map<String, DatabaseFieldArgs> fields,
                         @NotNull PrimaryKey primaryKey) {
        this.tableName = tableName;
        this.fields = fields;
        this.primaryKey = primaryKey;
    }


    private static Map<String, DatabaseFieldArgs> fieldsArrayToMap(@NotNull DatabaseField[] fields) {
        final Map<String, DatabaseFieldArgs> fieldsMap = new HashMap<>();
        for (DatabaseField field : fields) {
            fieldsMap.put(field.getFieldName(), new DatabaseFieldArgs(field.getSqlType(), field.isRequired()));
        }
        return fieldsMap;
    }


    public DatabaseTable(@NotNull String tableName, @NotNull DatabaseField[] fields) {
        this(tableName, fieldsArrayToMap(fields));
    }


    /**
     * TODO change default primary key to rowID
     * Primary key: tableName + "ID"
     */
    public DatabaseTable(@NotNull String tableName, Map<String, @NotNull DatabaseFieldArgs> fields) {
        this.tableName = tableName;
        this.fields = fields;

        final String primaryKeyName = tableName.toLowerCase() + "ID";
        fields.put(primaryKeyName, new DatabaseFieldArgs(SQLType.INTEGER, "PRIMARY KEY", false));
        primaryKey = new PrimaryKey(primaryKeyName);
    }


    public static String getSetDateFormatStr() {
        return setDateFormatStr;
    }


    /**
     * Change the database mode so that testing doesn't affect live data
     */
    public static void setTestMode() {
        url = urlPrefix + "JuuzoTest.db";
    }


    public static boolean isInTestMode() {
        return !url.equals(urlPrefix + databaseFileLocation);
    }


    /**
     * @throws ContactEwaException if an SQLException occurs
     */
    private static void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new ContactEwaException("Close connection error");
        }
    }


    public static ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now(zoneId);
    }


    /**
     * @param databaseDate a date in the form {@link #setDateFormat}
     * @return the given string in the form {@link #printDateFormat}
     * @throws ContactEwaException if an SQLException occurs
     */
    public static String databaseStringToPrintableString(@NotNull String databaseDate) {
        try {
            return formatDateForPrint(parseDateFromDatabase(databaseDate));
        } catch (ParseException ignore) {

        }
        throw new ContactEwaException("Date parse error");
    }


    /**
     * @return the given date in the form {@link #printDateFormat}
     */
    public static String formatDateForPrint(@NotNull ZonedDateTime zonedDateTime) {
        return printDateFormat.format(Date.from(zonedDateTime.toInstant()));
    }


    /**
     * @param date a date in the form {@link #setDateFormat}
     */
    public static ZonedDateTime parseDateFromDatabase(@NotNull String date) throws ParseException {
        return ZonedDateTime.ofInstant(setDateFormat.parse(date).toInstant(), zoneId);
    }


    /**
     * @throws ContactEwaException if an SQLException occurs
     */
    public void dropTable() {
        getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
        } catch (SQLException e) {
            throw new ContactEwaException("Drop table error");
        }
    }


    /**
     * Open a connection to the database, populating {@link #connection}, and create the table if it doesn't already
     * exist
     *
     * @throws ContactEwaException if connection cannot be established
     */
    private void getConnection() {
        if (connection == null) {
            /*
             * Fetches the existing database or creates a new one
             */
            try {
                connection = DriverManager.getConnection(url);
                if (!new File(databaseFileLocation).exists()) {
                    connection.getMetaData();
                }
            } catch (SQLException e) {
                throw new ContactEwaException("Database connection error");
            }
        }

        /*
         * Create table
         */
        try (Statement stmt = connection.createStatement()) {
            final StringBuilder sb = new StringBuilder();
            for (String fieldName : fields.keySet()) {
                sb.append(String.format("%s %s,", fieldName, fields.get(fieldName).createTableLine()));
            }
            sb.deleteCharAt(sb.length() - 1);
            stmt.execute(String.format("CREATE TABLE IF NOT EXISTS %s (%s%s);", tableName, sb.toString(),
                                       primaryKey.getPrimaryKeySQLLine(tableName)));
        } catch (SQLException e) {
            throw new ContactEwaException("Table creation error");
        }
    }


    /**
     * Creates and executes an SQL INSERT INTO statement
     */
    public void insert(@NotNull SetArgs args) {
        // For testing on laptop with outdated compiler - this method can't be used
//        if (2 < 1) {
//            throw new IllegalStateException("Currently invalid");
//        }
        // Check that all required fields are given
        final Set<String> argsFieldNames = args.getAllFieldNames();
        for (String field : fields.keySet()) {
            if (fields.get(field).isRequired() && !argsFieldNames.contains(field)) {
                throw new BadStateException("Required field missing: " + field);
            }
        }
        final BuildSQLArgsString buildArgs = args.getSetSQLString(", ", 2);
        String values = "?,".repeat(argsFieldNames.size());
        values = values.substring(0, values.length() - 1);
        final String sql = String.format("INSERT INTO %s(%s) VALUES (%s)", tableName, buildArgs.getString(), values);
        executePreparedStatement(sql, buildArgs.getTypes(), buildArgs.getValues());
    }


    /**
     * Sets the parameters using a prepared statement and executes the statement
     *
     * @param sql the SQL statement to execute
     * @param types the types of the parameters which must be set
     * @param objects the values of the parameters which must be set
     * @throws ContactEwaException if an SQLException occurs
     */
    private void executePreparedStatement(@NotNull String sql, List<SQLType> types, List<Object> objects) {
        getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (types != null && objects != null) {
                setValuesInPreparedStatement(ps, types, objects);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ContactEwaException("Execute statement error");
        }
    }


    private void setValuesInPreparedStatement(@NotNull PreparedStatement ps, @NotNull List<SQLType> types,
                                              @NotNull List<Object> objects) throws SQLException {
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
                    ps.setString(i + 1, formatDateForDatabase(zonedDateTime));
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
     * @return the given date in the form {@link #setDateFormat}
     */
    public static String formatDateForDatabase(@NotNull ZonedDateTime date) {
        return setDateFormat.format(Date.from(date.withZoneSameInstant(zoneId).toInstant()));
    }


    /**
     * Creates and executes an SQL UPDATE statement
     */
    public void update(@NotNull SetArgs setArgs, Args whereArgs) {
        final BuildSQLArgsString setBuildArgs = setArgs.getSetSQLString("=?, ", 2);
        final List<SQLType> types = setBuildArgs.getTypes();
        final List<Object> objects = setBuildArgs.getValues();
        String sql = "UPDATE %s SET %s";
        if (whereArgs == null) {
            sql = String.format(sql, tableName, setBuildArgs.getString());
        }
        else {
            final BuildSQLArgsString whereBuildArgs = whereArgs.getSQLString(false);
            types.addAll(whereBuildArgs.getTypes());
            objects.addAll(whereBuildArgs.getValues());
            sql = String.format(sql + whereBuildArgs.getString(), tableName, setBuildArgs.getString());
        }
        executePreparedStatement(sql, types, objects);
    }


    /**
     * Creates and executes an SQL DELETE statement
     */
    public void delete(@NotNull Args args) {
        final BuildSQLArgsString buildArgs = args.getSQLString(false);
        final String sql = String.format("DELETE FROM %s%s", tableName, buildArgs.getString());
        executePreparedStatement(sql, buildArgs.getTypes(), buildArgs.getValues());
    }


    /**
     * Create an execute a SELECT * statement
     *
     * @param resultsSetAction what to do with the results set when it's found (must be done within the try/catch
     * containing the prepared statement else rs becomes 'inactive')
     * @return return value of the parameter {@code resultsSetAction}
     */
    public Object select(Args whereArgs, @NotNull ResultsSetAction resultsSetAction) {
        if (whereArgs == null || whereArgs.isEmpty()) {
            return executePreparedStatement("SELECT * FROM " + tableName, null, null, resultsSetAction);
        }
        final BuildSQLArgsString buildArgs = whereArgs.getSQLString();
        final String sql = String.format("SELECT * FROM %s%s", tableName, buildArgs.getString());
        return executePreparedStatement(sql, buildArgs.getTypes(), buildArgs.getValues(), resultsSetAction);
    }


    /**
     * @param types can be null
     * @param objects can be null
     * @param resultsSetAction what to do when the
     * @return return value of the parameter {@code resultsSetAction}
     * @throws ContactEwaException if an SQLException occurs
     */
    private Object executePreparedStatement(@NotNull String sql, List<SQLType> types, List<Object> objects,
                                            @NotNull ResultsSetAction resultsSetAction) {
        getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (types != null && objects != null) {
                // Check that every parameter will be set
                int placeholders = StringUtils.countMatches(sql, "?");
                if (types.size() != placeholders || objects.size() != placeholders) {
                    throw new ContactEwaException("Parameters in SQL not set");
                }
                setValuesInPreparedStatement(ps, types, objects);
            }
            // If types and objects are null, there should be no parameters to set
            else if (sql.contains("?")) {
                throw new ContactEwaException("Parameters in SQL not set");
            }
            final ResultSet rs = ps.executeQuery();
            return resultsSetAction.execute(rs);
        } catch (SQLException e) {
            throw new ContactEwaException("Database query failed");
        }
    }


    /**
     * @throws IllegalArgumentException if fieldName is not a field name for this table
     */
    void checkValidFieldName(@NotNull String fieldName) {
        if (!fields.keySet().contains(fieldName)) {
            throw new IllegalArgumentException("Invalid database field name for " + tableName + ": " + fieldName);
        }
    }


    /**
     * @param value the value that will be given for the fieldName e.g. {@code fieldName > value}, or {@code SET
     * fieldname=value}
     * @throws IllegalArgumentException if the type of value is inappropriate
     */
    void checkValidSQLConditionArguments(@NotNull String fieldName, @Nullable Object value) {
        if (value != null && !(fields.get(fieldName).getSqlType().className.equals(value.getClass().getName()))) {
            throw new IllegalArgumentException("Invalid argument type for " + fieldName);
        }
    }


    /**
     * @param column must be of type INT
     * @return the column with the function applied to it
     * @throws IllegalArgumentException if the column doesn't exist or is of an inappropriate type
     * @throws NullPointerException if there are no rows in the table
     */
    public int getFunctionOfIntColumn(@NotNull ColumnFunction function, @NotNull String column, Args whereArgs) {
        if (!fields.keySet().contains(column)) {
            throw new IllegalArgumentException("No such column");
        }
        else if (function.requireIntColType && fields.get(column).getSqlType() != SQLType.INT
                && fields.get(column).getSqlType() != SQLType.INTEGER) {
            throw new IllegalArgumentException("Column is not of type int");
        }

        final ResultsSetAction resultsSetAction = rs -> {
            if (rs.next()) {
                return rs.getInt(function.toString() + "(" + column + ")");
            }
            else {
                throw new NullPointerException("No rows in table");
            }
        };
        String sql = "SELECT %s(%s) FROM %s";
        if (whereArgs == null || whereArgs.isEmpty()) {
            sql = String.format(sql, function.toString(), column, tableName);
            return (int) executePreparedStatement(sql, null, null, resultsSetAction);
        }
        else {
            final BuildSQLArgsString buildArgs = whereArgs.getSQLString();
            sql = String.format(
                    sql + buildArgs.getString(), function.toString(), column, tableName);
            return (int) executePreparedStatement(sql, buildArgs.getTypes(), buildArgs.getValues(), resultsSetAction);
        }
    }


    SQLType getFieldType(@NotNull String fieldName) {
        return fields.get(fieldName).getSqlType();
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
}
