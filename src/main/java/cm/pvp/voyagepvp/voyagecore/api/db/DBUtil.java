package cm.pvp.voyagepvp.voyagecore.api.db;

import cm.pvp.voyagepvp.voyagecore.api.exception.UtilityException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil
{
    private DBUtil()
    {
        throw new UtilityException();
    }

    /**
     * Close Statements, Connections, ResultSets
     * @param os - array.
     */
    public static void close(Object... os)
    {
        for (Object o : os) {
            try {
                if (o instanceof Connection) {
                    Connection connection = (Connection) o;

                    if (!connection.isClosed()) {
                        connection.close();
                    }
                }

                if (o instanceof PreparedStatement) {
                    PreparedStatement statement = (PreparedStatement) o;

                    if (!statement.isClosed()) {
                        statement.close();
                    }
                }

                if (o instanceof ResultSet) {
                    ResultSet set = (ResultSet) o;

                    if (!set.isClosed()) {
                        set.close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
