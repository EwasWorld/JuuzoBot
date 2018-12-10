package DatabaseBox;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;



/**
 * created 14/11/2018
 */
public class PrimaryKey {
    private Set<String> primaryKeys;
    private boolean needsConstraintLine = true;


    public PrimaryKey(@NotNull Set<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }


    PrimaryKey(@NotNull String primaryKey) {
        this.primaryKeys = new HashSet<>(Collections.singleton(primaryKey));
        needsConstraintLine = false;
    }


    public String toString() {
        if (primaryKeys.size() != 1) {
            throw new IllegalStateException("Cannot give primary key name for composite key");
        }
        return primaryKeys.toArray(new String[]{})[0];
    }


    /**
     * @return what can be appended to an SQL CREATE TABLE statement to define the primary key (or "" if a primary
     * key is not needed)
     */
    String getPrimaryKeySQLLine(@NotNull String tableName) {
        if (needsConstraintLine) {
            switch (primaryKeys.size()) {
                case 0:
                    throw new IllegalStateException("No primary key given");
                case 1:
                    return String.format(", PRIMARY KEY(%s)", primaryKeys.iterator().next());
                default:
                    final StringBuilder sb = new StringBuilder();
                    for (String key : primaryKeys) {
                        sb.append(key);
                        sb.append(", ");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    return String.format(", CONSTRAINT PK_%s PRIMARY KEY(%s)", tableName, sb.toString());
            }
        }
        else {
            return "";
        }
    }
}
