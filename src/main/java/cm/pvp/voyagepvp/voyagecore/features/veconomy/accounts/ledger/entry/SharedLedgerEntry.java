package cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry;

import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class SharedLedgerEntry
{
    private Action action;
    private UUID member;
    private double balance;
    private double amount;
}
