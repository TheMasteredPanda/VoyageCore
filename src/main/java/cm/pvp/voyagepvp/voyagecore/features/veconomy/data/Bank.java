package cm.pvp.voyagepvp.voyagecore.features.veconomy.data;

import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import com.google.common.collect.Maps;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class Bank
{
    private VEconomy feature;
    private HashMap<UUID, MemberType> members;

    @Getter
    private String name;

    @Getter
    private UUID id;

    @Getter
    private BigDecimal balance;

    private Bank(VEconomy feature, BigDecimal balance, HashMap<UUID, MemberType> members)
    {
        this.feature = feature;
        this.balance = balance;
        this.members = members;
    }

    public enum MemberType
    {
        OWNER,
        MEMBER
    }

    public void addMember(UUID member, MemberType type)
    {
        if (!members.containsKey(member)) {
            members.put(member, type);
        }
    }

    public void removeMember(UUID member, MemberType type)
    {
        if (members.containsKey(member)) {
            members.remove(member, type);
        }
    }

   public MemberType get(UUID member)
   {
       return members.getOrDefault(member, null);
   }

   public boolean exists(UUID member)
   {
       return get(member) != null;
   }

   public void addMoney(double amount)
   {
       balance = balance.add(new BigDecimal(amount));
   }

    public void removeMoney(double amount)
    {
        balance = balance.subtract(new BigDecimal(amount));
    }

    public static class BankBuilder
    {
        private BigDecimal balance;
        private HashMap<UUID, MemberType> members = Maps.newHashMap();

        public BankBuilder()
        {
        }

        public BankBuilder balance(BigDecimal balance)
        {
            this.balance = balance;
            return this;
        }

        public BankBuilder member(UUID member, MemberType type)
        {
            if (!members.containsKey(member)) {
                members.put(member, type);

            }

            return this;
        }

        public Bank build(VEconomy feature)
        {
            return new Bank(feature, balance, members);
        }
    }

    public static BankBuilder builder()
    {
        return new BankBuilder();
    }
}
