package cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared;

import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class HistoryEntry
{
    private UUID account;
    private UUID member;
    private Action action;
    private Date date;
    private Map<String, Object> data;
}
