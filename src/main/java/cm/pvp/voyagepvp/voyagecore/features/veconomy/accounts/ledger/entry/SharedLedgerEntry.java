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
public class SharedLedgerEntry
{
    private UUID accountId;
    private Action action;
    private UUID player;
    private double balance;
    private double amount;
    private Date date;
    private Map<String, Object> data;
}
