package cm.pvp.voyagepvp.voyagecore.features.veconomy;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.db.DBUtil;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.data.Bank;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.data.EconomyPlayer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DataHandler implements Listener
{
    private VoyageCore instance;
    private VEconomy feature;
    private HikariDataSource source;

    public HashMap<UUID, Bank> banks = Maps.newHashMap();
    private HashMap<UUID, EconomyPlayer> players = Maps.newHashMap();

    public DataHandler(VoyageCore instance, VEconomy feature)
    {
        this.instance = instance;
        this.feature = feature;

        File dbFile = new File(instance.getDataFolder(), "veconomy.db");
        HikariConfig config = new HikariConfig();
        config.setPoolName("VEconomyDatahandler");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        source = new HikariDataSource(config);

        CompletableFuture future = CompletableFuture.runAsync(() -> {
            Connection connection = null;
            PreparedStatement accountsTable = null;
            PreparedStatement banksTable = null;
            PreparedStatement bankMembers = null;

            try {
                connection = source.getConnection();
                accountsTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS accounts(owner VARCHAR(36) PRIMARY KEY, balance DOUBLE)");
                banksTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS banks(bankID VARCHAR(36) PRIMARY KEY , balance DOUBLE)");
                bankMembers = connection.prepareStatement("CREATE TABLE IF NOT EXISTS banksMembers(memberID VARCHAR(36), bankID VARCHAR(36), memberType INTEGER, FOREIGN KEY (bankID) REFERENCES banks.bankID)");
                accountsTable.execute();
                banksTable.execute();
                bankMembers.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(connection, accountsTable, banksTable, bankMembers);
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean load(Player player)
    {
        Connection connection;
        PreparedStatement loadAccount;
        PreparedStatement checkBanks;
        PreparedStatement loadBankMembers;
        PreparedStatement loadBanks;

        try {
            connection = source.getConnection();
            loadAccount = connection.prepareStatement("SELECT * FROM accounts WHERE owner=?");
            loadAccount.setString(1, player.getUniqueId().toString());

            ResultSet accountData = loadAccount.executeQuery();
            EconomyPlayer economyPlayer = null;

            while (accountData.next()) {
                economyPlayer = new EconomyPlayer(feature, player, accountData.getBigDecimal("balance"));
            }


            if (economyPlayer == null) {
                feature.getLogger().severe("EconomyPlayer instance is null.");
                return false;
            }

            DBUtil.close(accountData, loadAccount);
            checkBanks = connection.prepareStatement("SELECT * FROM bankMembers WHERE memberId=?");

            ResultSet check = checkBanks.executeQuery();

            boolean banks = false;

            while (check.next()) {
                banks = true;
                break;
            }

            DBUtil.close(checkBanks, check);


            if (banks) {
                loadBankMembers = connection.prepareStatement("SELECT * FROM bankMembers WHERE memberId=?");

                ResultSet bankIds = loadBankMembers.executeQuery();
                StringBuilder statement = new StringBuilder("SELECT * FROM banks WHERE bankID=");
                List<String> ids = Lists.newArrayList();

                while (bankIds.next()) {
                    if (this.banks.containsKey(UUID.fromString(bankIds.getString("bankId")))) {
                        continue;
                    }

                    ids.add(bankIds.getString("bankId"));
                    statement.append("?");

                    if (!bankIds.isLast()) {
                        statement.append(" OR ");
                    }
                }

                loadBanks = connection.prepareStatement(statement.toString());

                for (int i = 0; i < ids.size(); i++) {
                    String id = ids.get(i);
                    loadBanks.setString(i + 1, id);
                }

                ResultSet banksData = loadBanks.executeQuery();

                while (banksData.next()) {
                    Bank.BankBuilder builder = Bank.builder().balance(banksData.getBigDecimal("balance"));

                    PreparedStatement loadMembers = connection.prepareStatement("SELECT * FROM bankMembers WHERE bankId=?");
                    loadMembers.setString(1, banksData.getString("bankID"));

                    ResultSet members = loadMembers.executeQuery();

                    while (members.next()) {
                        builder.member(UUID.fromString(members.getString("memberID")), members.getInt("memberType") == 1 ? Bank.MemberType.OWNER : Bank.MemberType.MEMBER);
                    }

                    UUID bankID = UUID.fromString(banksData.getString("bankID"));
                    this.banks.put(bankID, builder.build(feature));
                    economyPlayer.addBankReference(bankID);
                    DBUtil.close(loadMembers, members);
                }

                DBUtil.close(banksData, loadBankMembers, loadBanks, bankIds);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}
