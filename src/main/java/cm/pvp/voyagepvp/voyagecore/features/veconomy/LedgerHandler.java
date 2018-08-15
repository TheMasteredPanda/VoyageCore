package cm.pvp.voyagepvp.voyagecore.features.veconomy;

import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.PlayerLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.SharedLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LedgerHandler
{
    private DataHandler handler;
    private Cache<UUID, ArrayList<SharedLedgerEntry>> sharedLedgers = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).build();
    private Cache<UUID, ArrayList<PlayerLedgerEntry>> playerLedgers = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.MINUTES).build();

    public LedgerHandler(DataHandler handler)
    {
        this.handler = handler;
    }

    public LinkedList<SharedLedgerEntry> getLedgersFrom(UUID sharedAccountId, Date date)
    {
        if (sharedLedgers.asMap().containsKey(sharedAccountId)) {
            return sharedLedgers.getIfPresent(sharedAccountId).stream().filter(entry -> entry.getDate().equals(date)).collect(Collectors.toCollection(Lists::newLinkedList));
        }


        CompletableFuture<LinkedList<SharedLedgerEntry>> future = CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = handler.getSource().getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from shared_account_ledgers where accountId=? and date=?");
                statement.setString(1, sharedAccountId.toString());
                statement.setDate(2, new java.sql.Date(date.getTime()));
                return statement.executeQuery();
            } catch (SQLException e) {
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

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public LinkedList<SharedLedgerEntry> getEntireLedger(UUID sharedAccountId)
    {
        CompletableFuture<LinkedList<SharedLedgerEntry>> future = CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = handler.getSource().getConnection();
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

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public LinkedList<PlayerLedgerEntry> getPersonalLedgersFrom(UUID playerId, Date date)
    {
        CompletableFuture<LinkedList<PlayerLedgerEntry>> future = CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = handler.getSource().getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from shared_account_ledgers where accountId=? and date=?");
                statement.setString(1, playerId.toString());
                statement.setDate(2, new java.sql.Date(date.getTime()));
                return statement.executeQuery();
            } catch (SQLException e) {
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

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public LinkedList<PlayerLedgerEntry> getEntirePersonalLedger(UUID playerId)
    {
        CompletableFuture<LinkedList<PlayerLedgerEntry>> future = CompletableFuture.supplyAsync(() -> {
            try {
                Connection connection = handler.getSource().getConnection();
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

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }
}