package cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts;

import cm.pvp.voyagepvp.voyagecore.features.veconomy.DataHandler;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.VEconomyResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class PlayerAccount
{
    private DataHandler handler;
    private UUID owner;
    private double balance;

    public VEconomyResponse add(double amount)
    {

        if (Double.isInfinite(balance + amount)) {
            return VEconomyResponse.builder().action(Action.DEPOSIT_MONEY).
                    response(Response.INFINITY_OCCURRED)
                    .value("amount", amount).value("currentBalance", balance).build();
        } else {
            balance = balance + amount;
            handler.updatePlayerAccount(this);
            return VEconomyResponse.builder().action(Action.DEPOSIT_MONEY)
                    .response(Response.SUCCESS).build();
        }
    }

    public VEconomyResponse subtract(double amount)
    {
        if (Double.isInfinite(balance + amount)) {
            return VEconomyResponse.builder().action(Action.WITHDRAW_MONEY).
                    response(Response.INFINITY_OCCURRED)
                    .value("amount", amount).value("currentBalance", balance).build();
        } else {
            balance = balance - amount;
            handler.updatePlayerAccount(this);
            return VEconomyResponse.builder().action(Action.WITHDRAW_MONEY)
                    .response(Response.SUCCESS).build();
        }
    }

    public static Builder builder(DataHandler handler)
    {
        return new Builder(handler);
    }

    public static class Builder
    {
        private DataHandler handler;
        private UUID owner;
        private double balance;

        private Builder(DataHandler handler)
        {
            this.handler = handler;
        }

        public Builder owner(UUID owner)
        {
            this.owner = owner;
            return this;
        }

        public Builder balance(double balance)
        {
            this.balance = balance;
            return this;
        }

        public PlayerAccount build()
        {
            return new PlayerAccount(handler, owner, balance);
        }
    }
}
