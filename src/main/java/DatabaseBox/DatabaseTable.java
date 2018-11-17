package DatabaseBox;

import CoreBox.Bot;

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
 * created: 27/09/18
 */
public class DatabaseTable {
    /*
     * Database connection
     */
    private static final String databaseFileLocation = Bot.getPathToJuuzoBot() + "Juuzo2.db";
    // Used to establish the connection to the database
    private static final String url = "jdbc:sqlite:" + databaseFileLocation;
    private static Connection connection = null;

    /*
     * Date formats (all dates are stored in UTC)
     * TODO FIX All dates are not being stored in UTC Q.Q plz fix
     */
    // Date format given as an argument and stored in the database
    public static String setDateFormatStr = "HH:mm dd/M/yy z";
    public static DateFormat setDateFormat = new SimpleDateFormat(setDateFormatStr);
    // Date format when printing the date
    public static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM 'at' HH:mm z");
    public static ZoneId zoneId = ZoneId.of("UTC");

    /*
     * Table information
     */
    private String tableName;
    private Map<String, DatabaseFieldArgs> fields;
    private PrimaryKey primaryKey;


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


    public static ZonedDateTime getDatabaseDateFromString(String date) {
        try {
            return ZonedDateTime.ofInstant(setDateFormat.parse(date).toInstant(), zoneId);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Database date parse failed");
        }
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


    public static DatabaseTable createDatabaseTable(String tableName, DatabaseFieldsEnum[] fields) {
        return new DatabaseTable(tableName, fieldsArrayToMap(fields));
    }


    public static DatabaseTable createDatabaseTable(String tableName, DatabaseFieldsEnum[] fields,
                                                    PrimaryKey primaryKey) {
        return new DatabaseTable(tableName, fieldsArrayToMap(fields), primaryKey);
    }


    private static Map<String, DatabaseFieldArgs> fieldsArrayToMap(DatabaseFieldsEnum[] fields) {
        final Map<String, DatabaseFieldArgs> fieldsMap = new HashMap<>();
        for (DatabaseFieldsEnum field : fields) {
            fieldsMap.put(field.getFieldName(), new DatabaseFieldArgs(field.getSqlType(), field.isRequired()));
        }
        return fieldsMap;
    }


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
        final List<SQLType> types = new ArrayList<>();
        final List<Object> objects = new ArrayList<>();
        final StringBuilder fieldNames = new StringBuilder();
        final StringBuilder values = new StringBuilder();
        // TODO Optimisation think about making these loops generic?
        for (String arg : args.keySet()) {
            if (!fields.keySet().contains(arg)) {
                throw new IllegalArgumentException("Invalid database field name for " + tableName + ": " + arg);
            }
            else {
                SQLType sqlType = fields.get(arg).getSqlType();
                if (!sqlType.getClassName().equals(args.get(arg).getClass().getName())) {
                    throw new IllegalArgumentException("Invalid argument type for " + arg);
                }
                types.add(sqlType);
                objects.add(args.get(arg));
                fieldNames.append(arg);
                fieldNames.append(", ");
                values.append("?,");
            }
        }
        fieldNames.delete(fieldNames.length() - 2, fieldNames.length());
        values.deleteCharAt(values.length() - 1);
        final String sql = String
                .format("INSERT INTO %s(%s) VALUES (%s)", tableName, fieldNames.toString(), values.toString());

        getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setValuesInPreparedStatement(ps, types, objects);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates and executes an SQL UPDATE statement
     *
     * @param setArgs Map<field, value> columns to update and their new values
     * @param whereArgs Map<field, value> WHERE arguments which will be ANDed together
     */
    public void updateAND(Map<String, Object> setArgs, Map<String, Object> whereArgs) {
        final List<SQLType> types = new ArrayList<>();
        final List<Object> objects = new ArrayList<>();
        final StringBuilder set = new StringBuilder();
        final StringBuilder where = new StringBuilder();
        for (String setArg : setArgs.keySet()) {
            if (!fields.keySet().contains(setArg)) {
                throw new IllegalArgumentException("Invalid database field name for " + tableName + ": " + setArg);
            }
            else {
                SQLType sqlType = fields.get(setArg).getSqlType();
                if (!sqlType.getClassName().equals(setArgs.get(setArg).getClass().getName())) {
                    throw new IllegalArgumentException("Invalid argument type for " + setArg);
                }
                types.add(sqlType);
                objects.add(setArgs.get(setArg));
                set.append(String.format("%s=?, ", setArg));
            }
        }
        for (String whereArg : whereArgs.keySet()) {
            if (!fields.keySet().contains(whereArg)) {
                throw new IllegalArgumentException("Invalid database field name for " + tableName + ": " + whereArg);
            }
            else {
                SQLType sqlType = fields.get(whereArg).getSqlType();
                if (!sqlType.getClassName().equals(whereArgs.get(whereArg).getClass().getName())) {
                    throw new IllegalArgumentException("Invalid argument type for " + whereArg);
                }
                types.add(sqlType);
                objects.add(whereArgs.get(whereArg));
                where.append(String.format("%s=? AND ", whereArg));
            }
        }
        set.delete(set.length() - 2, set.length());
        where.delete(where.length() - 5, where.length());
        final String sql = String.format("UPDATE %s SET %s WHERE %s", tableName, set.toString(), where.toString());

        getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setValuesInPreparedStatement(ps, types, objects);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates and executes an SQL DELETE statement
     *
     * @param args Map<field, value> WHERE arguments which will be ANDed together
     */
    public void deleteAND(Map<String, Object> args) {
        final List<SQLType> types = new ArrayList<>();
        final List<Object> objects = new ArrayList<>();
        final StringBuilder where = new StringBuilder();
        for (String arg : args.keySet()) {
            if (!fields.keySet().contains(arg)) {
                throw new IllegalArgumentException("Invalid database field name for " + tableName + ": " + arg);
            }
            else {
                DatabaseFieldArgs databaseFieldArgs = fields.get(arg);
                SQLType sqlType = databaseFieldArgs.getSqlType();
                if (!sqlType.getClassName().equals(args.get(arg).getClass().getName())) {
                    throw new IllegalArgumentException("Invalid argument type for " + arg);
                }
                where.append(String.format("%s=? AND ", arg));
                types.add(sqlType);
                objects.add(args.get(arg));
            }
        }
        where.delete(where.length() - 5, where.length());
        final String sql = String.format("DELETE FROM %s WHERE %s", tableName, where.toString());

        getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setValuesInPreparedStatement(ps, types, objects);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void setValuesInPreparedStatement(PreparedStatement ps, List<SQLType> types, List<Object> objects)
            throws SQLException
    {
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


    /**
     * Create an execute a SELECT * statemen
     *
     * @param args             Map<field, value> WHERE arguments which will be ANDed together
     * @param resultsSetAction what to do with the results set when it's found (must be done within the try/catch containing the prepared statement else rs becomes 'inactive')
     * @return result of resultsSetAction
     */
    public Object selectAND(Map<String, Object> args, ResultsSetAction resultsSetAction) {
        getConnection();
        if (args == null || args.size() == 0) {
            getConnection();
            try (PreparedStatement ps = connection.prepareStatement(String.format("SELECT * FROM %s", tableName))) {
                ResultSet rs = ps.executeQuery();
                return resultsSetAction.execute(rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            throw new IllegalStateException("Database query SimpleSelectAND failed");
        }

        final List<SQLType> types = new ArrayList<>();
        final List<Object> objects = new ArrayList<>();
        final StringBuilder fieldNames = new StringBuilder();
        for (String arg : args.keySet()) {
            if (!fields.keySet().contains(arg)) {
                throw new IllegalArgumentException("Invalid database field name for " + tableName + ": " + arg);
            }
            else {
                SQLType sqlType = fields.get(arg).getSqlType();
                if (!sqlType.getClassName().equals(args.get(arg).getClass().getName())) {
                    throw new IllegalArgumentException("Invalid argument type for " + arg);
                }
                types.add(sqlType);
                objects.add(args.get(arg));
                fieldNames.append(arg);
                fieldNames.append("=? AND ");
            }
        }
        fieldNames.delete(fieldNames.length() - 5, fieldNames.length());

        final String sql = String.format("SELECT * FROM %s WHERE %s", tableName, fieldNames.toString());
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setValuesInPreparedStatement(ps, types, objects);
            ResultSet rs = ps.executeQuery();
            return resultsSetAction.execute(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Database query SelectAND failed");
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



    public interface ResultsSetAction {
        Object execute(ResultSet rs) throws SQLException;
    }



    public interface DatabaseFieldsEnum {
        String getFieldName();


        SQLType getSqlType();


        boolean isRequired();
    }
}
