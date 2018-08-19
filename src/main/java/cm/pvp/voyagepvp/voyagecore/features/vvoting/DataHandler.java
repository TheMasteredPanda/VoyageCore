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
            PreparedStatement table2 = null;

            try (Connection connection0 = source.getConnection(); Connection connection1 = source.getConnection(); Connection connection2 = source.getConnection()) {
                table0 = connection0.prepareStatement("create table if not exists vvoting_party(player varchar(40) primary key)");
                table1 = connection1.prepareStatement("create table if not exists vvoting_party_claims(player varchar(40) primary key, count int default 0)");
                table2 = connection2.prepareCall("create table if not exists vvoting_daily_claims(player varchar(40) primary key, count int default 0)");
                table0.execute();
                table1.execute();
                table2.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                feature.shutdown();
            } finally {
                DBUtil.close(table0, table1);
            }
        });
    }

    public CompletableFuture<Void> cleanParty()
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            PreparedStatement statement1 = null;

            try (Connection connection = source.getConnection(); Connection connection1 = source.getConnection()) {
                statement = connection.prepareStatement("insert into vvoting_party_claims select * from vvoting_party on duplicate key update count=count+1;");
                statement1 = connection1.prepareStatement("delete from vvoting_party;");
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
                statement = connection.prepareStatement("select * from vvoting_party;");
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

    public CompletableFuture<Map<UUID, Integer>> partyClaims()
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            HashMap<UUID, Integer> claims = Maps.newHashMap();

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select * from vvoting_part_claims");
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

    public CompletableFuture<Map<UUID, Integer>> dailyClaims()
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            HashMap<UUID, Integer> claims = Maps.newHashMap();

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select * from vvoting_daily");
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

    public CompletableFuture<Void> updatePartyClaimCount(UUID player, int count)
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                if (count <= 0) {
                    statement = connection.prepareStatement("delete from vvoting_party_claims where player=?");
                    statement.setString(1, player.toString());
                } else {
                    statement = connection.prepareStatement("update vvoting_party_claims set count=? where player=?");
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

    public CompletableFuture<Void> updateDailyClaimCount(UUID player, int count)
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;

            try (Connection connection = source.getConnection()) {
                if (count <= 0) {
                    statement = connection.prepareStatement("delete from vvoting_daily_claims where player=?");
                    statement.setString(1, player.toString());
                } else {
                    statement = connection.prepareStatement("update vvoting_daily_claims set count=? where player=?");
                    statement.setString(1, player.toString());
                    statement.setInt(2, count);
                }

                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return null;
        });
    }

    public void partyInsert(UUID player)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("insert into vvoting_party values (?)");
                statement.setString(1, player.toString());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    public void dailyInsert(UUID player)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("insert into vvoting_daily_claims values (?,?)");
                statement.setString(1, player.toString());
                statement.setInt(2, 0);
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
                statement = connection.prepareStatement("select * from vvoting_party where player=?");
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
