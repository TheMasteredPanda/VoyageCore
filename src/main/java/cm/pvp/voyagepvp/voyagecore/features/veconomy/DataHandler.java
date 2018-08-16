package cm.pvp.voyagepvp.voyagecore.features.veconomy;

import cm.pvp.voyagepvp.voyagecore.api.db.DBUtil;
import cm.pvp.voyagepvp.voyagecore.api.tasks.Tasks;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.PlayerAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.PlayerLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.SharedLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.MembershipRequest;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DataHandler
{
    private VEconomy feature;

    @Getter(value = AccessLevel.PROTECTED)
    private HikariDataSource source;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public DataHandler(VEconomy feature)
    {
        this.feature = feature;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + feature.getSection().getString("mariadb.host") + "/" + feature.getSection().getString("mariadb.database"));
        config.setUsername(feature.getSection().getString("mariadb.username"));
        config.setPassword(feature.getSection().getString("mariadb.password"));
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        source = new HikariDataSource(config);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("create table if not exists player_accounts(owner VARCHAR(40) primary key, balance DOUBLE(13, 2) not null)");
                statement.execute();
                feature.getLogger().info("Created player accounts table.");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        }).thenRun(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("create table if not exists player_ledger(owner varchar(40) not null, action varchar(20) not null , balance double(13, 2) not null, amount double(13, 2) not null, date date not null, time time not null)");
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        }).thenRun(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("create table if not exists shared_accounts(accountId VARCHAR(40) primary key, balance double(13, 2), name TEXT)");
                statement.execute();
                feature.getLogger().info("Created shared account balances table.");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        }).thenRun(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection(); ){
                statement = connection.prepareStatement("create table if not exists shared_account_members(accountId VARCHAR(40) not null, memberId varchar(40) not null, action varchar(20) not null, foreign key (accountId) references shared_accounts (accountId) on delete cascade)");
                statement.execute();
                feature.getLogger().info("Created shared account members table.");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        }).thenRun(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("create table if not exists shared_account_ledgers(accountId varchar(40) not null, memberId varchar(40) not null, action varchar(20) not null, balance double(13, 2) not null, amount double(13, 2) not null, date datetime not null, time time not null)");
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        }).thenRun(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("create table if not exists membership_invitations(playerId varchar(40) not null, accountId varchar(40) not null, requesterId varchar(40) not null, date date not null, foreign key (accountId) references shared_accounts (accountId) on delete cascade)");
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void addMembershipInvitation(UUID playerId, UUID requesterId, UUID accountId, Date date)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("insert into membership_invitations (playerId, accountId, requesterId, date) values (?,?,?,?);");
                statement.setString(1, playerId.toString());
                statement.setString(3, requesterId.toString());
                statement.setString(2, accountId.toString());
                statement.setDate(4, new java.sql.Date(date.getTime()));
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    public void removedMembershipInvitation(UUID playerId, UUID accountId)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement =  null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("delete from membership_invitations where playerId=? and accountId=?");
                statement.setString(1, playerId.toString());
                statement.setString(2, accountId.toString());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    public void updateSharedAccountMember(UUID accountId, UUID memberId, SharedAccount.Type type)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("update shared_account_members set type=? where memberId=? and accountId=?");
                statement.setInt(1, type.getNumericID());
                statement.setString(2, memberId.toString());
                statement.setString(3, accountId.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }

        });
    }

    public void removedSharedAccount(UUID accountId)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("delete from shared_accounts where accountId=?");
                statement.setString(1, accountId.toString());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    public void updatePlayerAccount(PlayerAccount account)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("update player_accounts set balance=? where owner=?");
                statement.setDouble(1, account.getBalance());
                statement.setString(2, account.getOwner().toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    public void updateSharedAccount(SharedAccount account)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()){
                statement = connection.prepareStatement("update shared_accounts set balance=?, name=? where accountId=?");
                statement.setDouble(1, account.getBalance());
                statement.setString(2, account.getName());
                statement.setString(3, account.getId().toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    public void addSharedAccountMember(UUID accountId, UUID memberId, SharedAccount.Type type)
    {
        if (feature.getAccount(accountId).isMember(memberId)) {
            throw new RuntimeException("You cannot add a member that is already added to a shared account.");
        }

        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("insert into shared_account_members (accountId, memberId, type) values (?,?,?)");
                statement.setString(1, accountId.toString());
                statement.setString(2, memberId.toString());
                statement.setInt(3, type.getNumericID());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }


    public void removeSharedAccountMember(UUID accountId, UUID memberId)
    {
        if (feature.getAccount(accountId).getOwner().equals(memberId)) {
            throw new RuntimeException("You cannot remove the owner of the shared account.");
        }

        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("delete from shared_account_members where accountId=? and memberId=?");
                statement.setString(1, accountId.toString());
                statement.setString(2, memberId.toString());
                statement.execute();
                feature.getPlayers().invalidate(memberId);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }


    public SharedAccount createSharedAccount(UUID owner, String name)
    {
        UUID id = generateSharedAccountID();

        if (id == null) {
            throw new RuntimeException("Couldn't create a shared account for " + owner + " as we couldn't create an id.");
        }

        CompletableFuture<SharedAccount> future = CompletableFuture.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("insert into shared_accounts (accountId, balance, name) values (?,?,?)");
                statement.setString(1, id.toString());
                statement.setDouble(2, 0D);
                statement.setString(3, name);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        }).thenApplyAsync(aVoid -> {
            PreparedStatement statement = null;
            SharedAccount account = null;

            try (Connection connection = source.getConnection()){
                statement = connection.prepareStatement("insert into shared_account_members (accountId, memberId, type) values (?,?,?)");
                statement.setString(1, id.toString());
                statement.setString(2, owner.toString());
                statement.setInt(3, SharedAccount.Type.OWNER.getNumericID());
                statement.execute();
                account = SharedAccount.builder(this).id(id).name(name).member(owner, SharedAccount.Type.OWNER).balance(0D).build();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
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
            PreparedStatement statement = null;
            VEconomyPlayer player = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("insert into player_accounts (owner, balance) value (?,?);");
                statement.setString(1, owner.getUniqueId().toString());
                statement.setDouble(2, 0D);
                statement.execute();
                player = new VEconomyPlayer(owner, PlayerAccount.builder(DataHandler.this).balance(0D).owner(owner.getUniqueId()).build(), Lists.newArrayList(), Lists.newArrayList());
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }

            return player;
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
            PreparedStatement statement = null;
            Connection connection = null;
            ResultSet result = null;

            try  {
                connection = source.getConnection();
                statement = connection.prepareStatement("select * from player_accounts where owner=?");
                statement.setString(1, owner.getUniqueId().toString());
                result = statement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return result;
        }).thenApplyAsync(set -> {
            VEconomyPlayer player = null;
            ResultSet set1 = null;
            PreparedStatement statement = null;


            try (Connection connection = source.getConnection()) {
                double balance = 0D;

                while (set.next()) {
                    balance = set.getDouble("balance");
                }

                DBUtil.close(set);

                statement = connection.prepareStatement("select * from shared_account_members where memberId=?");
                statement.setString(1, owner.getUniqueId().toString());

                set1 = statement.executeQuery();

                ArrayList<UUID> sharedAccounts = Lists.newArrayList();

                while (set1.next()) {
                    sharedAccounts.add(UUID.fromString(set1.getString("accountId")));
                }

                player = new VEconomyPlayer(owner, PlayerAccount.builder(DataHandler.this).balance(balance).owner(owner.getUniqueId()).build(), sharedAccounts, Lists.newArrayList());
                DBUtil.close(set);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set1);
            }

            return player;
        }).thenApplyAsync(vEconomyPlayer -> {
            PreparedStatement statement = null;
            ResultSet set = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select * from membership_invitations where playerId=?");
                statement.setString(1, vEconomyPlayer.getReference().get().getUniqueId().toString());
                set = statement.executeQuery();

                while (set.next()) {
                    vEconomyPlayer.getMembershipRequests().add(MembershipRequest.builder().requester(UUID.fromString(set.getString("requesterId"))).date(set.getDate("date")).accountId(UUID.fromString(set.getString("accountId"))).build());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }

            return vEconomyPlayer;
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
            PreparedStatement statement = null;
            ResultSet set = null;
            Connection connection = null;
            try  {
                connection = source.getConnection();
                statement = connection.prepareStatement("select * from shared_accounts where accountId=?");
                statement.setString(1, id.toString());
                set = statement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return set;
        }).thenApplyAsync(set -> {
            PreparedStatement statement = null;
            ResultSet set1 = null;
            SharedAccount.Builder account = SharedAccount.builder(this);

            try (Connection connection = source.getConnection()) {
                while (set.next()) {
                    account.balance(set.getDouble("balance")).id(id).name(set.getString("name"));
                }

                statement = connection.prepareStatement("select * from shared_account_members where accountId=?");
                statement.setString(1, id.toString());

                set1 = statement.executeQuery();

                while (set1.next()) {
                    SharedAccount.Type type = SharedAccount.Type.getType(set1.getInt("type"));
                    account.member(UUID.fromString(set1.getString("memberId")), type);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(set1, set, statement);
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
            PreparedStatement statement = null;
            ResultSet set = null;
            boolean result = false;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select name from shared_accounts where accountId=?");
                statement.setString(1, id.toString());

                set = statement.executeQuery();

                while (set.next()) {
                    result = true;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
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
            PreparedStatement statement = null;
            ResultSet set = null;
            boolean result = false;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select balance from player_accounts where owner=?");
                statement.setString(1, id.toString());

                set = statement.executeQuery();

                while (set.next()) {
                    result = true;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
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
            PreparedStatement statement = null;
            ResultSet set = null;
            UUID id = UUID.randomUUID();

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select balance from shared_accounts where accountId=?");
                statement.setString(1, id.toString());
                set = statement.executeQuery();

                while (set.next()) {
                    id = generateSharedAccountID();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
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
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            boolean result = false;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select accountId from shared_accounts where name=?");
                statement.setString(1, name);
                set = statement.executeQuery();

                while (set.next()) {
                    result = true;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement, set);
            }


            return result;
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
        System.out.println(name);
        CompletableFuture<List<UUID>> future = CompletableFuture.supplyAsync(() -> {
            PreparedStatement statement = null;
            ResultSet set = null;
            List<UUID> accountIds = Lists.newArrayList();

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("select accountId from shared_accounts where `name`=?");
                statement.setString(1, name);

                set = statement.executeQuery();

                while (set.next()) {
                    accountIds.add(UUID.fromString(set.getString("accountId")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(set, statement);
            }

            return accountIds;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Couldn't get the shared accounts under the name " + name + ".");
    }

    public void shutdown()
    {
        source.close();
    }

    public CompletableFuture<LinkedList<SharedLedgerEntry>> getLedgersFrom(UUID sharedAccountId, Date date)
    {

        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = source.getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from shared_account_ledgers where accountId=? and date=?");
                statement.setString(1, sharedAccountId.toString());
                statement.setDate(2, new java.sql.Date(dateFormat.parse(dateFormat.format(date)).getTime()));
                return statement.executeQuery();
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }

            return null;
        }).thenApply(resultSet -> {
            LinkedList<SharedLedgerEntry> ledgerSection = Lists.newLinkedList();

            try {
                resultSet.last();

                int row = resultSet.getRow();

                if (row == 0) {
                    resultSet.close();
                    return ledgerSection;
                }

                while (resultSet.next()) {
                    ledgerSection.add(new SharedLedgerEntry(Action.valueOf(resultSet.getString("action").toUpperCase()), UUID.fromString(resultSet.getString("memberId")), resultSet.getDouble("balance"), resultSet.getDouble("amount"), new Date(resultSet.getDate("date").getTime())));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return ledgerSection;
        });
    }

    public CompletableFuture<LinkedList<SharedLedgerEntry>> getEntireLedger(UUID sharedAccountId)
    {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = source.getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from shared_account_ledgers where accountId=?");
                statement.setString(1, sharedAccountId.toString());
                return statement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }).thenApply(rs -> {
            LinkedList<SharedLedgerEntry> ledger = Lists.newLinkedList();

            try {
                rs.last();

                int row = rs.getRow();

                if (row == 0) {
                    rs.close();
                    return ledger;
                }

                while (rs.next()) {
                    ledger.add(new SharedLedgerEntry(Action.valueOf(rs.getString("action").toUpperCase()), UUID.fromString(rs.getString("memberId")), rs.getDouble("balance"), rs.getDouble("amount"), new Date(rs.getDate("date").getTime())));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return ledger;
        });
    }

    public CompletableFuture<LinkedList<PlayerLedgerEntry>> getPersonalLedgersFrom(UUID playerId, Date date)
    {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = source.getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from shared_account_ledgers where accountId=? and date=?");
                statement.setString(1, playerId.toString());
                statement.setDate(2, new java.sql.Date(dateFormat.parse(dateFormat.format(date)).getTime()));
                return statement.executeQuery();
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }

            return null;
        }).thenApply(resultSet -> {
            LinkedList<PlayerLedgerEntry> ledgerSection = Lists.newLinkedList();

            try {
                resultSet.last();

                int row = resultSet.getRow();

                if (row == 0) {
                    resultSet.close();
                    return ledgerSection;
                }

                while (resultSet.next()) {
                    ledgerSection.add(new PlayerLedgerEntry(Action.valueOf(resultSet.getString("action").toUpperCase()), resultSet.getDouble("balance"), resultSet.getDouble("amount"), new Date(resultSet.getDate("date").getTime())));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return ledgerSection;
        });
    }

    public CompletableFuture<LinkedList<PlayerLedgerEntry>> getEntirePersonalLedger(UUID playerId)
    {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = source.getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from shared_account_ledgers where accountId=?");
                statement.setString(1, playerId.toString());
                return statement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }).thenApply(rs -> {
            LinkedList<PlayerLedgerEntry> ledger = Lists.newLinkedList();

            try {
                rs.last();

                int row = rs.getRow();

                if (row == 0) {
                    rs.close();
                    return ledger;
                }

                while (rs.next()) {
                    ledger.add(new PlayerLedgerEntry(Action.valueOf(rs.getString("action").toUpperCase()), rs.getDouble("balance"), rs.getDouble("amount"), new Date(rs.getDate("date").getTime())));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return ledger;
        });
    }

    public void addSharedLedgerEntry(UUID accountId, SharedLedgerEntry entry)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection()) {
                statement = connection.prepareStatement("insert into shared_account_ledgers (accountId, memberId, action, balance, amount, date) values (?,?,?,?,?,?, ?)");
                statement.setString(1, accountId.toString());
                statement.setString(2, entry.getMember().toString());
                statement.setString(3, entry.getAction().name());
                statement.setDouble(4, entry.getBalance());
                statement.setDouble(5, entry.getAmount());
                statement.setDate(6, new java.sql.Date(dateFormat.parse(dateFormat.format(entry.getDate())).getTime()));
                statement.setTime(7, new Time(dateFormat.parse(timeFormat.format(entry.getDate())).getTime()));
                statement.execute();
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }

    public void addPlayerLedgerEntry(UUID playerId, PlayerLedgerEntry entry)
    {
        Tasks.runAsync(() -> {
            PreparedStatement statement = null;

            try (Connection connection = source.getConnection())  {
                statement = connection.prepareStatement("insert into player_ledger (owner, action, balance, amount, date, time) values (?,?,?,?,?,?)");
                statement.setString(1, playerId.toString());
                statement.setString(2, entry.getAction().name());
                statement.setDouble(3, entry.getBalance());
                statement.setDouble(4, entry.getAmount());
                statement.setDate(5, new java.sql.Date(dateFormat.parse(dateFormat.format(entry.getDate())).getTime()));
                statement.setTime(6, new Time(timeFormat.parse(timeFormat.format(entry.getDate())).getTime()));
                statement.execute();
            } catch (SQLException | ParseException e) {
               e.printStackTrace();
            } finally {
                DBUtil.close(statement);
            }
        });
    }
}
