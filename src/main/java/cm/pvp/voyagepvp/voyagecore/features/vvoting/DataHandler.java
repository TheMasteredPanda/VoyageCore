package cm.pvp.voyagepvp.voyagecore.features.vvoting;

import cm.pvp.voyagepvp.voyagecore.api.db.DBUtil;
import cm.pvp.voyagepvp.voyagecore.api.tasks.Tasks;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DataHandler
{
    private HikariDataSource source;

    public DataHandler(VVoting feature)
    {
        ConfigurationSection section = feature.getSection().getConfigurationSection("sql");
        HikariConfig config = new HikariConfig();
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        config.setUsername(section.getString("username"));
        config.setPassword(section.getString("password"));
        config.setJdbcUrl("jdbc:mariadb:/" + section.getString("host") + "/" + section.getString("database"));

        source = new HikariDataSource(config);

        Tasks.runAsync(() -> {
            PreparedStatement table0 = null;
            PreparedStatement table1 = null;

            try (Connection connection0 = source.getConnection(); Connection connection1 = source.getConnection()) {
                table0 = connection0.prepareStatement("create table if not exists party(player varchar(40) primary key)");
                table1 = connection1.prepareStatement("create table if not exists claims(player varchar(40) primary key, count int default 0)");
                table0.execute();
                table1.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(table0, table1);
            }
        });
    }

    public CompletableFuture<Void> createNewParty()
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            PreparedStatement statement1 = null;

            try (Connection connection = source.getConnection(); Connection connection1 = source.getConnection()) {
                statement = connection.prepareStatement("insert into claims select * from party on duplicate key update count=count+1;");
                statement1 = connection1.prepareStatement("delete from party;");

                statement.executeUpdate();
                statement1.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, statement1);
            }

            return null;
        });
    }

    public CompletableFuture<List<UUID>> party()
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            ArrayList<UUID> party = Lists.newArrayList();

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select * from party;");
                set = statement.executeQuery();

                while (set.next()) {
                    party.add(UUID.fromString(set.getString("player")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return party;
        });
    }

    public CompletableFuture<Map<UUID, Integer>> claims()
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            HashMap<UUID, Integer> claims = Maps.newHashMap();

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select * from claims");
                set = statement.executeQuery();

                while (set.next()) {
                    claims.put(UUID.fromString(set.getString("player")), set.getInt("count"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return claims;
        });
    }

    public CompletableFuture<Void> updateClaimCount(UUID player, int count)
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                if (count == 0) {
                    statement = connection.prepareStatement("delete from claims where player=?");
                    statement.setString(1, player.toString());
                } else {
                    statement = connection.prepareStatement("update claims set count=? where player=?");
                    statement.setInt(1, count);
                    statement.setString(2, player.toString());
                }

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }

            return null;
        });
    }

    public void insert(UUID player)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("insert into party (?)");
                statement.setString(1, player.toString());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    public CompletableFuture<Boolean> voted(UUID player)
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            boolean result = false;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select * from party where player=?");
                statement.setString(1, player.toString());
                set = statement.executeQuery();
                result = set.next();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return result;
        });
    }
}
