package cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry;

import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.Map;
import java.util.UUID;


//TODO
@Getter
@AllArgsConstructor
public class PlayerLedgerEntry
{
    private UUID owner;
    private Action action;
    private double amount;
    private double balance;
    private Date date;
    private Map<String, Object> data;
}
