package cm.pvp.voyagepvp.voyagecore.features.veconomy;

import cm.pvp.voyagepvp.voyagecore.api.player.PlayerWrapper;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.PlayerAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.MembershipRequest;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.VEconomyResponse;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

@Getter
public class VEconomyPlayer extends PlayerWrapper
{
    private PlayerAccount account;
    private ArrayList<UUID> sharedAccounts;
    private ArrayList<MembershipRequest> membershipRequests;

    public VEconomyPlayer(Player p, PlayerAccount account, ArrayList<UUID> sharedAccounts, ArrayList<MembershipRequest> requests)
    {
        super(p);
        this.account = account;
        this.sharedAccounts = sharedAccounts;
        this.membershipRequests = requests;
    }

    public VEconomyResponse addRequest(MembershipRequest request)
    {
        if (membershipRequests.stream().anyMatch(req -> req.getAccountId().equals(request.getAccountId()))) {
            return VEconomyResponse.builder().action(Action.ADD_MEMBERSHIP_INVITATION).response(Response.FAILURE).build();
        }

        membershipRequests.add(request);
        account.getHandler().addMembershipInvitation(getReference().get().getUniqueId(), request.getRequester(), request.getAccountId());
        return VEconomyResponse.builder().response(Response.SUCCESS).action(Action.ADD_MEMBERSHIP_INVITATION).build()
    }

    public VEconomyResponse removeRequest(UUID accountId)
    {
        if (membershipRequests.stream().noneMatch(req -> req.getAccountId().equals(accountId))) {
            return VEconomyResponse.builder().response(Response.FAILURE).action(Action.REMOVE_MEMBERSHIP_INVITATION).build();
        }

        membershipRequests.removeIf(req -> req.getAccountId().equals(accountId));
        account.getHandler().removedMembershipInvitation(getReference().get().getUniqueId(), accountId);
        return VEconomyResponse.builder().action(Action.REMOVE_MEMBERSHIP_INVITATION).response(Response.SUCCESS).build();
    }
}
