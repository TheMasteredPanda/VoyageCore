package cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class MembershipRequest
{
    private UUID requester;
    private Date date;
    private UUID accountId;
}
