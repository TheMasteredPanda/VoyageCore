package cm.pvp.voyagepvp.voyagecore.features.veconomy;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.module.ModuleManager;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.VEconomyResponse;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import javax.naming.OperationNotSupportedException;
import java.util.List;

public class VEconomyVaultHook extends AbstractEconomy
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.currency.symbol")
    private String currencySymbol;

    @ConfigPopulate("features.veconomy.currency.name.plural")
    private String currencyNamePlural;

    @ConfigPopulate("features.veconomy.currency.name.singular")
    private String currencyNameSingular;

    public VEconomyVaultHook(Plugin plugin)
    {
        feature = VoyageCore.get().get(ModuleManager.class).getModule(VoyageCore.get(), VEconomy.class);

        try {
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if economy method is enabled.
     * @return Success or Failure
     */
    @Override
    public boolean isEnabled()
    {
        return feature.isEnabled();
    }

    /**
     * Gets name of economy method
     * @return Name of Economy Method
     */
    @Override
    public String getName()
    {
        return "VEconomy";
    }

    /**
     * Returns true if the given implementation supports banks.
     * @return true if the implementation supports banks
     */
    @Override
    public boolean hasBankSupport()
    {
        return true;
    }

    /**
     * Some economy plugins round off after a certain number of digits.
     * This function returns the number of digits the plugin keeps
     * or -1 if no rounding occurs.
     * @return number of digits after the decimal point kept
     */
    @Override
    public int fractionalDigits()
    {
        return 0;
    }

    /**
     * Format amount into a human readable String This provides translation into
     * economy specific formatting to improve consistency between plugins.
     *
     * @param amount to format
     * @return Human readable string describing amount
     */
    @Override
    public String format(double amount)
    {
        return currencySymbol  + String.valueOf(amount);
    }

    /**
     * Returns the name of the currency in plural form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (plural)
     */
    @Override
    public String currencyNamePlural()
    {
        return currencyNamePlural;
    }


    /**
     * Returns the name of the currency in singular form.
     * If the economy being used does not support currency names then an empty string will be returned.
     *
     * @return name of the currency (singular)
     */
    @Override
    public String currencyNameSingular()
    {
        return currencyNameSingular;
    }

    /**
     *
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer)} instead.
     */
    @Deprecated @Override
    public boolean hasAccount(String playerName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this player has an account on the server yet
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player to check
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(OfflinePlayer player)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());
        return vEconomyPlayer != null;
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #hasAccount(OfflinePlayer, String)} instead.
     */
    @Deprecated @Override
    public boolean hasAccount(String playerName, String worldName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if this player has an account on the server yet on the given world
     * This will always return true if the player has joined the server at least once
     * as all major economy plugins auto-generate a player account when the player joins the server
     *
     * @param player to check in the world
     * @param worldName world-specific account
     * @return if the player has an account
     */
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());
        return vEconomyPlayer != null;
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer)} instead.
     */
    @Deprecated @Override
    public double getBalance(String playerName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets balance of a player
     *
     * @param player of the player
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(OfflinePlayer player)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());
        return vEconomyPlayer.getAccount().getBalance();
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #getBalance(OfflinePlayer, String)} instead.
     */
    @Deprecated @Override
    public double getBalance(String playerName, String world)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets balance of a player on the specified world.
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @param player to check
     * @param world name of the world
     * @return Amount currently held in players account
     */
    @Override
    public double getBalance(OfflinePlayer player, String world)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());
        return vEconomyPlayer.getAccount().getBalance();
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #has(OfflinePlayer, double)} instead.
     */
    @Deprecated @Override
    public boolean has(String playerName, double amount)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to check
     * @param amount to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(OfflinePlayer player, double amount)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());
        return vEconomyPlayer.getAccount().getBalance() >= amount;
    }

    /**
     * @deprecated As of VaultAPI 1.4 use @{link {@link #has(OfflinePlayer, String, double)} instead.
     */
    @Deprecated @Override
    public boolean has(String playerName, String worldName, double amount)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if the player account has the amount in a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player to check
     * @param worldName to check with
     * @param amount to check for
     * @return True if <b>player</b> has <b>amount</b>, False else wise
     */
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());
        return vEconomyPlayer.getAccount().getBalance() >= amount;
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, double)} instead.
     */
    @Deprecated @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to withdraw from
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());

        VEconomyResponse res = vEconomyPlayer.getAccount().subtract(amount, null);

        if (res.getResponse() == Response.SUCCESS) {
            return new EconomyResponse(amount, vEconomyPlayer.getAccount().getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
        } else {
            return new EconomyResponse(amount, vEconomyPlayer.getAccount().getBalance(), EconomyResponse.ResponseType.FAILURE, (String)res.getValues().get("message"));
        }
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #withdrawPlayer(OfflinePlayer, String, double)} instead.
     */
    @Deprecated @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @param player to withdraw from
     * @param worldName - name of the world
     * @param amount Amount to withdraw
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());

        VEconomyResponse res = vEconomyPlayer.getAccount().subtract(amount, null);

        if (res.getResponse() == Response.SUCCESS) {
            return new EconomyResponse(amount, vEconomyPlayer.getAccount().getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
        } else {
            return new EconomyResponse(amount, vEconomyPlayer.getAccount().getBalance(), EconomyResponse.ResponseType.FAILURE, (String)res.getValues().get("message"));
        }
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, double)} instead.
     */
    @Deprecated @Override
    public EconomyResponse depositPlayer(String playerName, double amount)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param player to deposit to
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());

        VEconomyResponse res = vEconomyPlayer.getAccount().add(amount, null);

        if (res.getResponse() == Response.SUCCESS) {
            return new EconomyResponse(amount, vEconomyPlayer.getAccount().getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
        } else {
            return new EconomyResponse(amount, vEconomyPlayer.getAccount().getBalance(), EconomyResponse.ResponseType.FAILURE, (String)res.getValues().get("message"));
        }
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {@link #depositPlayer(OfflinePlayer, String, double)} instead.
     */
    @Deprecated @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     *
     * @param player to deposit to
     * @param worldName name of the world
     * @param amount Amount to deposit
     * @return Detailed response of transaction
     */
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount)
    {
        VEconomyPlayer vEconomyPlayer = feature.get(player.getUniqueId());

        VEconomyResponse res = vEconomyPlayer.getAccount().add(amount, null);

        if (res.getResponse() == Response.SUCCESS) {
            return new EconomyResponse(amount, vEconomyPlayer.getAccount().getBalance(), EconomyResponse.ResponseType.SUCCESS, null);
        } else {
            return new EconomyResponse(amount, vEconomyPlayer.getAccount().getBalance(), EconomyResponse.ResponseType.FAILURE, (String)res.getValues().get("message"));
        }
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #createBank(String, OfflinePlayer)} instead.
     */
    @Deprecated @Override
    public EconomyResponse createBank(String name, String player)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a bank account with the specified name and the player as the owner
     * @param name of account
     * @param player the account should be linked to
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes a bank account with the specified name.
     * @param name of the back to delete
     * @return if the operation completed successfully
     */
    @Override
    public EconomyResponse deleteBank(String name)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the amount the bank has
     * @param name of the account
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankBalance(String name)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name of the account
     * @param amount to check for
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankHas(String name, double amount)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name of the account
     * @param amount to withdraw
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankWithdraw(String name, double amount)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
     *
     * @param name of the account
     * @param amount to deposit
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse bankDeposit(String name, double amount)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankOwner(String, OfflinePlayer)} instead.
     */
    @Deprecated @Override
    public EconomyResponse isBankOwner(String name, String playerName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Check if a player is the owner of a bank account
     *
     * @param name of the account
     * @param player to check for ownership
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #isBankMember(String, OfflinePlayer)} instead.
     */
    @Deprecated @Override
    public EconomyResponse isBankMember(String name, String playerName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Check if the player is a member of the bank account
     *
     * @param name of the account
     * @param player to check membership
     * @return EconomyResponse Object
     */
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the list of banks
     * @return the List of Banks
     */
    @Override
    public List<String> getBanks()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer)} instead.
     */
    @Deprecated @Override
    public boolean createPlayerAccount(String playerName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to create a player account for the given player
     * @param player OfflinePlayer
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(OfflinePlayer player)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated As of VaultAPI 1.4 use {{@link #createPlayerAccount(OfflinePlayer, String)} instead.
     */
    @Deprecated @Override
    public boolean createPlayerAccount(String playerName, String worldName)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to create a player account for the given player on the specified world
     * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     * @param player OfflinePlayer
     * @param worldName String name of the world
     * @return if the account creation was successful
     */
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName)
    {
        throw new UnsupportedOperationException();
    }
}
