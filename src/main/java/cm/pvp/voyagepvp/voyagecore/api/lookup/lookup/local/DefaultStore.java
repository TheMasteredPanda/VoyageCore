package cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.local;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.db.DBUtil;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.api.tasks.Tasks;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class DefaultStore implements LocalStore
{
    private Gson gson = new Gson();
    private HikariDataSource source;
    private Logger logger;

    public DefaultStore(String username, String password, String database, String host)
    {
        logger = VoyageCore.get().getLogger();
        HikariConfig config = new HikariConfig();
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);

        if (username != null && password != null && host != null) {
            config.setPassword(password);
            config.setUsername(username);
            config.setJdbcUrl("jdbc:mysql://" + host + "/" + database);
            source = new HikariDataSource(config);
        } else {
            return;
        }

        Tasks.runAsync(() -> {
           PreparedStatement statement = null;

           try (Connection connection = source.getConnection()) {
               statement = connection.prepareStatement("create table if not exists local_profiles(id varchar(40) not null primary key, name varchar(40) not null, properties text not null, createdAt date not null, updatedAt date not null)");
               statement.execute();
           } catch (SQLException e) {
               e.printStackTrace();
           } finally {
               DBUtil.close(statement);
           }
        });
    }

    @Override
    public void insert(PlayerProfile playerProfile)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;


           try (Connection connection = source.getConnection()) {
               statement = connection.prepareStatement("insert into local_profiles (id, name, properties, createdAt, updatedAt) values (?,?,?,?,?)");
               statement.setString(1, playerProfile.getId().toString());
               statement.setString(2, playerProfile.getName());
               statement.setString(3, playerProfile.getProperties().toString());
               Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
               statement.setDate(4, sqlDate);
               statement.setDate(5, sqlDate);
               statement.execute();
           } catch (SQLException e) {
               e.printStackTrace();
           } finally {
               DBUtil.close(statement);
           }
        });
    }

    @Override
    public void update(PlayerProfile playerProfile)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("update local_profiles set name=?, properties=?, updatedAt=? where id=?");
                statement.setString(1, playerProfile.getName());
                statement.setString(2, playerProfile.getProperties().toString());
                statement.setString(3, playerProfile.getId().toString());
                statement.setDate(4, new Date(new java.util.Date().getTime()));
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    @Override
    public void delete(PlayerProfile playerProfile)
    {
        Tasks.runAsync(() -> {
           PreparedStatement statement = null;

           try (Connection connection = source.getConnection()) {
               statement = connection.prepareStatement("delete from local_profiles where id=?");
               statement.setString(1, playerProfile.getId().toString());
               statement.executeUpdate();
           } catch (SQLException e) {
               e.printStackTrace();
           } finally {
               DBUtil.close(statement);
           }
        });
    }

    @Override
    public Optional<List<PlayerProfile>> fetch(UUID[] uuids)
    {
        CompletableFuture<List<PlayerProfile>> future = CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            ArrayList<PlayerProfile> list = Lists.newArrayList();

            StringBuilder sb = new StringBuilder("select * from local_profiles where id=");

            for (UUID id : uuids) {
                sb.append(id.toString());

                if (id != uuids[uuids.length - 1]) {
                    sb.append(" or ");
                }
            }

            sb.append(";");


            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement(sb.toString());
                set = statement.executeQuery();

                while (set.next()) {
                    list.add(new PlayerProfile(set.getString("name"), UUID.fromString(set.getString("id")), gson.fromJson(set.getString("properties"), JsonArray.class)));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return list;
        });


        try {
            return Optional.of(future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }


    @Override
    public Optional<List<PlayerProfile>> fetch(String[] names)
    {
        CompletableFuture<List<PlayerProfile>> future = CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            ArrayList<PlayerProfile> list = Lists.newArrayList();

            StringBuilder sb = new StringBuilder("select * from local_profiles where id=");

            for (String name : names) {
                sb.append(name);

                if (!name.equals(names[names.length - 1])) {
                    sb.append(" or ");
                }
            }

            sb.append(";");


            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement(sb.toString());
                set = statement.executeQuery();

                while (set.next()) {
                    list.add(new PlayerProfile(set.getString("name"), UUID.fromString(set.getString("id")), gson.fromJson(set.getString("properties"), JsonArray.class)));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return list;
        });


        try {
            return Optional.of(future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public CompletableFuture<Optional<PlayerProfile>> fetch(UUID uuid)
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            PlayerProfile profile = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select * from local_profiles where id=?");
                statement.setString(1, uuid.toString());
                set = statement.executeQuery();

                while (set.next()) {
                    profile = new PlayerProfile(
                            set.getString("name"),
                            uuid,
                            gson.fromJson(set.getString("properties"), JsonArray.class)
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return Optional.of(profile);
        });
    }

    @Override
    public CompletableFuture<Optional<PlayerProfile>> fetch(String name)
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            PlayerProfile profile = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select * from local_profiles where name=?");
                statement.setString(1, name);
                set = statement.executeQuery();

                while (set.next()) {
                    profile = new PlayerProfile(
                            name,
                            UUID.fromString(set.getString("id")),
                            gson.fromJson(set.getString("properties"), JsonArray.class)
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return Optional.of(profile);
        });
    }

    @Override
    public String id()
    {
        return "default";
    }

    @Override
    public CompletableFuture<java.util.Date> getLastUpdated(UUID id)
    {
        return CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            java.util.Date date = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select updatedAt from local_profiles where id=?");
                statement.setString(1, id.toString());
                set = statement.executeQuery();

                while (set.next()) {
                    date = new java.util.Date(set.getDate("updatedAt").getTime());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return date;
        });
    }
}
