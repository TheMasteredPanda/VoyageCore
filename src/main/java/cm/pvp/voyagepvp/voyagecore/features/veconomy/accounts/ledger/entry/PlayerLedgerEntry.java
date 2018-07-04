package cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry;

import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerLedgerEntry
{
    private Action action;
    private double amount;
    private double balance;
}
