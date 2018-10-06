package cm.pvp.voyagepvp.voyagecore.features.chatreaction;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.db.DBUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DataHandler implements Listener
{
    private Cache<UUID, ReactionPlayer> players = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private HikariDataSource source;

    public DataHandler(VoyageCore instance, ChatReaction feature)
    {
        File dbFile = new File(instance.getDataFolder(), "chatreactiondata.db");
        HikariConfig config = new HikariConfig();
        config.setPoolName("ChatReactionDataHandler");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        source = new HikariDataSource(config);

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = source.getConnection();
            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS records(uuid VARCHAR (36), wins INTEGER, quickestTime INTEGER);");
            statement.execute();
            feature.getLogger().info("Created ChatReaction database.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement);
        }

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }

    public void close()
    {
        for (Map.Entry<UUID, ReactionPlayer> entry : players.asMap().entrySet()) {
            update(entry.getValue()).complete(null);
        }

        players.invalidateAll();
        source.close();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void on(PlayerJoinEvent e)
    {

        exists(e.getPlayer().getUniqueId()).whenComplete((exists, throwable) -> {
            if (!exists) {
                create(e.getPlayer().getUniqueId()).complete(null);
            } else {
                load(e.getPlayer().getUniqueId()).complete(null);
            }
        });
    }

    public CompletableFuture<Boolean> exists(UUID player)
    {
        return CompletableFuture.supplyAsync(() -> {
            if (!players.asMap().containsKey(player)) {
                return true;
            }


            boolean exists = false;
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("SELECT * FROM records WHERE uuid=?");
                statement.setString(1, player.toString());

                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    exists = true;
                }

                DBUtil.close(set);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }

            return exists;
        });
    }

    public CompletableFuture<Void> create(UUID player)
    {
        return CompletableFuture.runAsync(() -> {
            if (players.asMap().containsKey(player)) {
                throw new RuntimeException("Player already in database.");
            }

            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("INSERT INTO records(uuid, wins, quickestTime) VALUES (?, ?, ?);");
                statement.setString(1, player.toString());
                statement.setInt(2, 0);
                statement.setLong(3, 0);
                players.put(player, new ReactionPlayer(Bukkit.getPlayer(player), 0, 0L));
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    public CompletableFuture<Void> update(ReactionPlayer player)
    {
        return CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement statement = null;

                try (Connection connection = source.getConnection()) {
                    statement = connection.prepareStatement("UPDATE records SET wins=?, quickestTime=? WHERE uuid=?");
                    statement.setInt(1, player.getWins());
                    statement.setLong(2, player.getFastest());
                    statement.setString(3, player.getReference().get().getUniqueId().toString());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    DBUtil.close(statement);
                }
            } catch (NullPointerException e) {
                players.invalidate(player);
            }
        });
    }

    public CompletableFuture<Void> load(UUID player)
    {
        return CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("SELECT * FROM records WHERE uuid=?");
                statement.setString(1, player.toString());
                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    players.put(player, new ReactionPlayer(Bukkit.getPlayer(player), set.getInt("wins"), set.getLong("quickestTime")));
                }

                DBUtil.close(set);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(connection, statement);
            }
        });
    }

    public CompletableFuture<ReactionPlayer> get(UUID player)
    {
        return exists(player).thenApplyAsync(exists -> {
            if (exists) {
                ReactionPlayer rPlayer = players.getIfPresent(player);

                if (rPlayer == null) {
                    try {
                        load(player).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    rPlayer = players.getIfPresent(player);
                }

                return rPlayer;
            } else {
                return null;
            }
        });
    }
}