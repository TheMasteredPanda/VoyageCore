package cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared;

import cm.pvp.voyagepvp.voyagecore.features.veconomy.DataHandler;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.SharedLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.VEconomyResponse;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SharedAccount
{
    private DataHandler handler;
    private UUID id;
    private String name;
    private HashMap<UUID, Type> members;
    private double balance;

    public VEconomyResponse add(double amount, UUID member)
    {
        if (Double.isInfinite(balance + amount)) {
            return VEconomyResponse.builder().action(Action.DEPOSIT_MONEY)
                    .response(Response.INFINITY_OCCURRED)
                    .value("amount", amount).value("currentBalance", balance).value("member", member).build();
        } else {
            balance = balance + amount;
            handler.updateSharedAccount(this);
            handler.addSharedLedgerEntry(id, new SharedLedgerEntry(Action.DEPOSIT_MONEY, member, balance, amount, new Date()));
            return VEconomyResponse.builder().action(Action.DEPOSIT_MONEY)
                    .response(Response.SUCCESS).value("member", member).build();
        }
    }

    public VEconomyResponse subtract(double amount, UUID member)
    {
        if (Double.isInfinite(balance - amount)) {
            return VEconomyResponse.builder().action(Action.WITHDRAW_MONEY)
                    .response(Response.INFINITY_OCCURRED).value("amount", amount)
                    .value("currentBalance", balance).value("member", member).build();
        } else {
            balance = balance - amount;
            handler.updateSharedAccount(this);
            handler.addSharedLedgerEntry(id, new SharedLedgerEntry(Action.WITHDRAW_MONEY, member, balance, amount, new Date()));
            return VEconomyResponse.builder().action(Action.WITHDRAW_MONEY)
                    .response(Response.SUCCESS).value("member", member).build();
        }
    }

    public boolean isMember(UUID id)
    {
        return members.containsKey(id);
    }

    public UUID getOwner()
    {
        for (Map.Entry<UUID, Type> entry : members.entrySet()) {
            if (!entry.getValue().equals(Type.OWNER)) {
                continue;
            }

            return entry.getKey();
        }

        throw new RuntimeException("Couldn't find the owner of shared account " + name + ".");
    }

    public VEconomyResponse transferOwnership(UUID newOwner)
    {
        if (!members.containsKey(newOwner)) {
            return VEconomyResponse.builder().response(Response.FAILURE).action(Action.TRANSFER_OWNERSHIP).value("message", "The deigned owner is not a member of the shared account.").build();
        }

        UUID owner = members.entrySet().stream().filter(entry -> entry.getValue() == Type.OWNER).findFirst().get().getKey();
        handler.updateSharedAccountMember(id, owner, Type.POA);
        handler.updateSharedAccountMember(id, newOwner, Type.OWNER);
        members.put(owner, Type.POA);
        members.put(newOwner, Type.OWNER);
        return VEconomyResponse.builder().response(Response.SUCCESS).action(Action.TRANSFER_OWNERSHIP).build();
    }

    public VEconomyResponse demoteMember(UUID memberId)
    {
        if (!members.containsKey(memberId)) {
            return VEconomyResponse.builder().action(Action.DEMOTE_MEMBER).response(Response.FAILURE).value("message", "The member is not apart of the shared account.").build();
        }

        if (getOwner().equals(memberId)) {
            return VEconomyResponse.builder().response(Response.FAILURE).action(Action.DEMOTE_MEMBER).value("message", "You cannot demote an owner.").build();
        }

        Type type = members.entrySet().stream().filter(entry -> entry.getKey().equals(memberId)).map(Map.Entry::getValue).findFirst().get();

        if (type == Type.MEMBER) {
            return VEconomyResponse.builder().value("message", "Can't demote a normal member.").action(Action.DEMOTE_MEMBER).response(Response.FAILURE).build();
        }

        handler.updateSharedAccountMember(this.id, memberId, Type.getType(type.getNumericID() - 1));
        members.put(memberId, Type.getType(type.getNumericID() - 1));
        return VEconomyResponse.builder().response(Response.SUCCESS).action(Action.DEMOTE_MEMBER).build();
    }

    public VEconomyResponse promoteMember(UUID memberId)
    {
        if (!members.containsKey(memberId)) {
            return VEconomyResponse.builder().action(Action.PROMOTE_MEMBER).response(Response.FAILURE).value("message", "The member is not apart of the shared account.").build();
        }

        if (getOwner().equals(memberId)) {
            return VEconomyResponse.builder().value("message", "You can't promote an owner.").response(Response.FAILURE).action(Action.PROMOTE_MEMBER).build();
        }

        Type type = members.entrySet().stream().filter(entry -> entry.getKey().equals(memberId)).map(Map.Entry::getValue).findFirst().get();

        if (type == Type.POA) {
            return VEconomyResponse.builder().action(Action.PROMOTE_MEMBER).response(Response.FAILURE).value("message", "You cannot promote a POA.").build();
        }


        handler.updateSharedAccountMember(id, memberId, Type.getType(type.getNumericID() + 1));
        members.put(memberId, Type.getType(type.getNumericID() + 1));
        return VEconomyResponse.builder().response(Response.SUCCESS).action(Action.PROMOTE_MEMBER).build();
    }

    public VEconomyResponse addMember(UUID memberId, Type type)
    {
        if (type == Type.OWNER) {
            return VEconomyResponse.builder().value("message", "You cannot make a player an owner when there already is an owner.").response(Response.FAILURE).action(Action.ADD_MEMBER).build();
        }

        if (members.containsKey(memberId)) {
            return VEconomyResponse.builder().value("message", "You cannot add a member than is already a member.").response(Response.FAILURE).action(Action.ADD_MEMBER).build();
        }

        handler.addSharedAccountMember(id, memberId, type);
        members.put(memberId, type);
        return VEconomyResponse.builder().action(Action.ADD_MEMBER).response(Response.SUCCESS).build();
    }

    public VEconomyResponse removeMember(UUID memberId, Type type)
    {
        if (type == Type.OWNER) {
            return VEconomyResponse.builder().value("message", "You cannot remove the owner from their own shared account.").response(Response.FAILURE).action(Action.REMOVE_MEMBER).build();
        }

        if (!members.containsKey(memberId)) {
            return VEconomyResponse.builder().value("message", "The member is not apart of this bank.").response(Response.FAILURE).action(Action.REMOVE_MEMBER).build();
        }

        members.remove(memberId);
        handler.removeSharedAccountMember(id, memberId);
        return VEconomyResponse.builder().action(Action.REMOVE_MEMBER).response(Response.SUCCESS).build();
    }

    public VEconomyResponse setName(String name)
    {
        this.name = name;
        handler.updateSharedAccount(this);
        return VEconomyResponse.builder().response(Response.SUCCESS).action(Action.SET_NAME).build();
    }

    public Type get(UUID id)
    {
        return members.get(id);
    }

    public static Builder builder(DataHandler handler)
    {
        return new Builder(handler);
    }

    public enum Type
    {
        OWNER(2),
        POA(1),
        MEMBER(0);

        public static Type getType(int i)
        {
            switch (i) {
                case 0: return MEMBER;
                case 1: return POA;
                case 2: return OWNER;
                default: return null;
            }
        }

        public int getNumericID()
        {
            return i;
        }

        int i;

        Type(int i)
        {
            this.i = i;
        }
    }

    public static class Builder
    {
        private DataHandler handler;
        private UUID id;
        private String name;
        private HashMap<UUID, Type> members;
        private double balance;

        private Builder(DataHandler handler)
        {
            this.handler = handler;
            members = Maps.newHashMap();
        }

        public Builder balance(double balance)
        {
            this.balance = balance;
            return this;
        }

        public Builder member(UUID id, Type type)
        {
            members.put(id, type);
            return this;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder id(UUID id)
        {
            this.id = id;
            return this;
        }

        public SharedAccount build()
        {
            return new SharedAccount(handler, id, name, members, balance);
        }
    }
}
