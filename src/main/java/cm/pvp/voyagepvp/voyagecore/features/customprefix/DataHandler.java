package cm.pvp.voyagepvp.voyagecore.features.customprefix;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.db.DBUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DataHandler implements AutoCloseable
{
    private Cache<UUID, String> nodes = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private HikariDataSource source;

    public DataHandler(VoyageCore instance)
    {
        File dbFile = new File(instance.getDataFolder(), "customprefixdata.db");

        HikariConfig config = new HikariConfig();
        config.setPoolName("CustomPrefixDataHandler");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        source = new HikariDataSource(config);

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = source.getConnection();
            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS prefixes(id VARCHAR(36), node TEXT)");
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement);
        }

    }

    @Override
    public void close()
    {
        source.close();
    }

    public void add(UUID id, String node)
    {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = source.getConnection();
            statement = connection.prepareStatement("INSERT INTO prefixes (id, node) VALUES (?, ?)");
            statement.setString(1, id.toString());
            statement.setString(2, node);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement);
        }
    }

    public void remove(UUID id)
    {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = source.getConnection();
            statement = connection.prepareStatement("DELETE FROM prefixes WHERE id=?");
            statement.setString(1, id.toString());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement);
            nodes.invalidate(id);
        }

    }

    public void update(UUID id, String node)
    {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = source.getConnection();
            statement = connection.prepareStatement("UPDATE prefixes SET node=? WHERE id=?");
            statement.setString(1, node);
            statement.setString(2, id.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement);
        }
    }

    public boolean exists(UUID id)
    {
        return get(id).isPresent();
    }

    public Optional<String> get(UUID id)
    {
        Optional<String> node = Optional.empty();

        if (nodes.asMap().containsKey(id)) {
            return Optional.of(nodes.asMap().get(id));
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = source.getConnection();
            statement = connection.prepareStatement("SELECT * FROM prefixes WHERE id=?");
            statement.setString(1, id.toString());

            ResultSet set = statement.executeQuery();

            while (set.next()) {
                node = Optional.of(set.getString("node"));
            }

            DBUtil.close(set);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement);
        }

        return node;
    }
}
