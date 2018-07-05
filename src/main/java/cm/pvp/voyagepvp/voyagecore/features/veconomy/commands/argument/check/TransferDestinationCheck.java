package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.argument.check;

import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.ArgumentCheckFunction;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount;

import java.util.List;
import java.util.UUID;

public class TransferDestinationCheck implements ArgumentCheckFunction
{
    private VEconomy feature;

    public TransferDestinationCheck(VEconomy feature)
    {
        this.feature = feature;
    }

    @Override
    public boolean check(String argument)
    {
        if (feature.getInstance().getMojangLookup().lookup(argument).isPresent()) {
            return true;
        } else {
            UUID id = feature.getInstance().getMojangLookup().lookup(argument).get().getId();
            List<UUID> accessibleBanks = feature.getHandler().getAccessibleBanks(id);

            for (UUID bank : accessibleBanks) {
                SharedAccount account = feature.getAccount(bank);

                //Just a precautionary check to ensure they cannot access banks they are not membered with.
                if (account.isMember(id) && account.getName().equals(argument)) {
                    return true;
                }
            }
        }

        return false;
    }
}
