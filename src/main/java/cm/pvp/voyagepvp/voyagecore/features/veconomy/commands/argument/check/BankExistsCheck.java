package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.argument.check;

import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.ArgumentCheckFunction;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;

import java.util.UUID;

public class BankExistsCheck implements ArgumentCheckFunction
{
    private VEconomy feature;

    public BankExistsCheck(VEconomy feature)
    {
        this.feature = feature;
    }

    @Override
    public boolean check(String argument)
    {
        String[] split = argument.split("/");
        UUID player = UUID.fromString(split[0]);
        String bank = split[1];

        return feature.getHandler().getAccessibleBanks(player).stream().anyMatch(id -> feature.getAccount(id).getName().equals(bank));
    }
}
