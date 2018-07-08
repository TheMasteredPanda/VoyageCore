package cm.pvp.voyagepvp.voyagecore.features.veconomy;

import cm.pvp.voyagepvp.voyagecore.api.db.DBUtil;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.PlayerAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class DataHandler
{
    private VEconomy feature;
    private HikariDataSource source;

    public DataHandler(VEconomy feature)
    {
        this.feature = feature;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb:" + feature.getSection().getString("mariadb.host") + "/" + feature.getSection().getString("mariadb.database"));
        config.setUsername(feature.getSection().getString("mariadb.username"));
        config.setPassword(feature.getSection().getString("mariadb.password"));
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        source = new HikariDataSource(config);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("create table if not exists player_accounts(owner VARCHAR(32), balance DOUBLE(13, 2))");
                statement.execute();
                feature.getLogger().info("Created player accounts table.");
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).thenRun(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("create table if not exists player_ledger(owner varchar(32), transactionType int, balance double(13, 2), amount double(13, 2), date date)");
                statement.execute();
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });


        if (feature.getSection().getBoolean("sharedaccounts.enabled")) {
            future.thenRun(() -> {
                Connection connection = null;
                PreparedStatement statement = null;

                try {
                    connection = source.getConnection();
                    statement = connection.prepareStatement("create table if not exists shared_accounts(accountId VARCHAR(32) primary key, balance double(13, 2), name TEXT)");
                    statement.execute();
                    feature.getLogger().info("Created shared account balances table.");
                    DBUtil.close(config, statement);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }).thenRun(() -> {
                Connection connection = null;
                PreparedStatement statement = null;

                try {
                    connection = source.getConnection();
                    statement = connection.prepareStatement("create table if not exists shared_account_members(accountId VARCHAR(32) primary key, memberId varchar(32), type int, foreign key (accountId) references shared_account_balances (accountId) on delete cascade)");
                    statement.execute();
                    feature.getLogger().info("Created shared account members table.");
                    DBUtil.close(connection, statement);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }).thenRun(() -> {
                Connection connection = null;
                PreparedStatement statement = null;

                try {
                    connection = source.getConnection();
                    statement = connection.prepareStatement("create table if not exists shared_account_ledger(accountId varchar(32) primary key , memberId varchar(32), transactionType int, balance double(13, 2), amount double(13, 2))");
                    statement.execute();
                    DBUtil.close(connection, statement);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void updateSharedAccountMember(UUID accountId, UUID memberId, SharedAccount.Type type)
    {
        CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("update shared_account_members set type=? where memberId=? and accountId=?");
                statement.setInt(1, type.getNumericID());
                statement.setString(2, memberId.toString());
                statement.setString(3, accountId.toString());
                statement.executeUpdate();
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }).complete(null);
    }

    public void removedSharedAccount(UUID accountId)
    {
        CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("delete from shared_account_members where accountId=?");
                statement.setString(1, accountId.toString());
                statement.execute();
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).thenRunAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("delete from shared_accounts where accountId=?");
                statement.setString(1, accountId.toString());
                statement.execute();
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).complete(null);
    }

    public void updatePlayerAccount(PlayerAccount account)
    {
        CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("update player_accounts set balance=? where owner=?");
                statement.setDouble(1, account.getBalance());
                statement.setString(2, account.getOwner().toString());
                statement.executeUpdate();
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).complete(null);
    }

    public void updateSharedAccount(SharedAccount account)
    {
        CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("update shared_accounts set balance=?, name=? where accountId=?");
                statement.setDouble(1, account.getBalance());
                statement.setString(2, account.getName());
                statement.setString(3, account.getId().toString());
                statement.executeUpdate();
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).complete(null);
    }

    public void addSharedAccountMember(UUID accountId, UUID memberId, SharedAccount.Type type)
    {
        if (feature.getAccount(accountId).isMember(memberId)) {
            throw new RuntimeException("You cannot add a member that is already added to a shared account.");
        }

        CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("insert into shared_account_members (accountId, memberId, type) values (?,?,?)");
                statement.setString(1, accountId.toString());
                statement.setString(2, memberId.toString());
                statement.setInt(3, type.getNumericID());
                statement.execute();
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).complete(null);
    }


    public void removeSharedAccountMember(UUID accountId, UUID memberId)
    {
        if (feature.getAccount(accountId).getOwner().equals(memberId)) {
            throw new RuntimeException("You cannot remove the owner of the shared account.");
        }

        CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("delete from shared_account_members where accountId=? and memberId=?");
                statement.setString(1, accountId.toString());
                statement.setString(2, memberId.toString());
                statement.execute();
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).complete(null);
    }


    public SharedAccount createSharedAccount(UUID owner, String name)
    {
        UUID id = generateSharedAccountID();

        if (id == null) {
            throw new RuntimeException("Couldn't create a shared account for " + owner + " as we couldn't create an id.");
        }

        CompletableFuture<SharedAccount> future = CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("insert into shared_accounts (accountId, balance, name) values (?,?,?)");
                statement.setString(1, id.toString());
                statement.setDouble(2, 0D);
                statement.setString(3, name);
                statement.execute();
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).thenApplyAsync(aVoid -> {
            Connection connection = null;
            PreparedStatement statement = null;
            SharedAccount account = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("insert into shared_account_members (accountId, memberId, type) values (?,?,?)");
                statement.setString(1, id.toString());
                statement.setString(2, owner.toString());
                statement.setInt(3, SharedAccount.Type.OWNER.getNumericID());
                statement.execute();
                DBUtil.close(connection, statement);
                account = SharedAccount.builder(this).name(name).member(owner, SharedAccount.Type.OWNER).balance(0D).build();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return account;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't create a shared account for " + owner + ".");
    }

    protected VEconomyPlayer createPlayer(Player owner)
    {
        CompletableFuture<VEconomyPlayer> future = CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("insert into player_accounts (owner, balance) value (?,?);");
                statement.setString(1, owner.getUniqueId().toString());
                statement.setDouble(2, 0D);
                statement.execute();
                DBUtil.close(connection, statement);
                return new VEconomyPlayer(owner, PlayerAccount.builder(DataHandler.this).balance(0D).owner(owner.getUniqueId()).build(), Lists.newArrayList());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't create VEconomyPlayer instance for " + owner.getName() + ".");
    }

    protected VEconomyPlayer getPlayer(Player owner)
    {
        CompletableFuture<VEconomyPlayer> future = CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet result = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("select * from player_accounts where owner=?");
                statement.setString(1, owner.getUniqueId().toString());
                result = statement.executeQuery();
                DBUtil.close(statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return result;
        }).thenApplyAsync(set -> {
            VEconomyPlayer player = null;
            Connection connection = null;
            PreparedStatement statement = null;


            try {
                double balance = 0D;

                while (set.next()) {
                    balance = set.getDouble("balance");
                }

                DBUtil.close(set);

                connection = source.getConnection();
                statement = connection.prepareStatement("select * from shared_account_members where memberId=?");
                statement.setString(1, owner.getUniqueId().toString());

                ResultSet set1 = statement.executeQuery();

                ArrayList<UUID> sharedAccounts = Lists.newArrayList();

                while (set1.next()) {
                    sharedAccounts.add(UUID.fromString(set1.getString("accountId")));
                }

                player = new VEconomyPlayer(owner, PlayerAccount.builder(DataHandler.this).balance(balance).owner(owner.getUniqueId()).build(), sharedAccounts);
                DBUtil.close(connection, statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return player;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't get economy player instance for " + owner.getUniqueId());
    }

    protected SharedAccount getSharedAccount(UUID id)
    {
        CompletableFuture<SharedAccount> future = CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet set = null;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("select * from shared_accounts where accountId=?");
                statement.setString(1, id.toString());
                set = statement.executeQuery();
                DBUtil.close(statement);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return set;
        }).thenApplyAsync(set -> {
            Connection connection = null;
            PreparedStatement statement = null;
            SharedAccount.Builder account = SharedAccount.builder(this);

            try {
                while (set.next()) {
                    account.balance(set.getDouble("balance")).id(id).name(set.getString("name"));
                }

                connection = source.getConnection();
                statement = connection.prepareStatement("select * from shared_accounts_members where accountId=?");
                statement.setString(1, id.toString());

                ResultSet set1 = statement.executeQuery();

                while (set1.next()) {
                    SharedAccount.Type type = SharedAccount.Type.getType(set1.getInt("type"));
                    account.member(UUID.fromString(set1.getString("memberId")), type);
                }

                DBUtil.close(connection, statement, set1, set);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return account.build();
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't get shared account with id " + id.toString() + ".");
    }

    public boolean sharedAccountExists(UUID id)
    {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            boolean result = false;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("select name from shared_accounts where accountId=?");
                statement.setString(1, id.toString());

                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    result = true;
                }

                DBUtil.close(connection, statement, set);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return result;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't determine whether a shared account under the id of " + id.toString() + " exists.");
    }

    public boolean playerExists(UUID id)
    {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            boolean result = false;

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("select balance from player_accounts where owner=?");
                statement.setString(1, id.toString());

                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    result = true;
                }

                DBUtil.close(connection, statement, set);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return result;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't determine if player " + id.toString() + " existed.");
    }

    public UUID generateSharedAccountID()
    {
        CompletableFuture<UUID> future = CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            UUID id = UUID.randomUUID();

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("select balance from shared_accounts where accountId=?");
                statement.setString(1, id.toString());

                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    id = generateSharedAccountID();
                }

                DBUtil.close(connection, statement, set);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return id;
        });


        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't generate UUID for a shared account.");
    }

    public boolean sharedAcccountExists(String name)
    {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                Connection connection = null;
                PreparedStatement statement = null;
                boolean result = false;

                try {
                    connection = source.getConnection();
                    statement = connection.prepareStatement("select accountId from shared_accounts where name=?");
                    statement.setString(1, name);

                    ResultSet set = statement.executeQuery();

                    while (set.next()) {
                        result = true;
                    }

                    DBUtil.close(connection, statement, set);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return result;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't determine whether an account under the name of " + name + " exists.");
    }

    public List<UUID> getSharedAccountsNamed(String name)
    {
        CompletableFuture<List<UUID>> future = CompletableFuture.supplyAsync(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            List<UUID> accountIds = Lists.newArrayList();

            try {
                connection = source.getConnection();
                statement = connection.prepareStatement("select accountId from shared_accounts where name=?");
                statement.setString(1, name);

                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    accountIds.add(UUID.fromString(set.getString("accountId")));
                }

                DBUtil.close(set, statement, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return accountIds;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't get the shared accounts under the name " + name + ".");
    }

    public void shutdown()
    {
        source.close();
    }


}
