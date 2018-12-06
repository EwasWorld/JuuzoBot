package DatabaseBox;

import java.util.ArrayList;
import java.util.List;



/**
 * Used for testing
 * created 04/12/2018
 */
public class DatabaseWrapper {
    private DatabaseTable[] databaseTables;


    public DatabaseWrapper(DatabaseTable[] databaseTables) {
        this.databaseTables = databaseTables;
    }


    /**
     *
     * @param expectedRowCounts tables' expected values should be in the same order as given when creating the wrapper
     * @return whether vales given match the actual values exactly
     * @throws NullPointerException for null arguments
     * @throws IllegalArgumentException if incorrect array length
     */
    public boolean checkRowCounts(int[] expectedRowCounts) {
        if (expectedRowCounts == null) {
            throw new NullPointerException("Null argument");
        }
        else if (expectedRowCounts.length != databaseTables.length) {
            throw new IllegalArgumentException("Incorrect expected row lengths");
        }

        final List<Integer> actualRowCounts = getRowCounts();
        for (int i = 0; i < expectedRowCounts.length; i++) {
            if (expectedRowCounts[i] != actualRowCounts.get(i)) {
                return false;
            }
        }
        return true;
    }


    public List<Integer> getRowCounts() {
        return doToAllTables(table -> table.getFunctionOfIntColumn(DatabaseTable.ColumnFunction.COUNT, table.getPrimaryKey(), null));
    }


    private <T> List<T> doToAllTables(TableAction<T> tableAction) {
        List<T> list = new ArrayList<>();
        for (DatabaseTable databaseTable : databaseTables) {
            list.add(tableAction.action(databaseTable));
        }
        return list;
    }


    /**
     * Deletes all character data stored in the database
     */
    public void deleteAllTables() {
        DatabaseWrapper.checkDatabaseInTestMode();
        doToAllTables(table -> {
            table.deleteTable();
            return null;
        });
    }


    /**
     * @throws IllegalStateException if the database is not in test mode
     */
    public static void checkDatabaseInTestMode() {
        if (!DatabaseTable.isInTestMode()) {
            throw new IllegalStateException("This action can only be taken in test mode");
        }
    }


    public interface TableAction<T> {
        T action(DatabaseTable table);
    }
}
