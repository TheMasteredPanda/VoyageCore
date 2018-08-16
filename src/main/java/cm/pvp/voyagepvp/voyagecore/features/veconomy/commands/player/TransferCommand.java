package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.player;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.NumberCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.MojangLookup;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.PlayerAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransferCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.transfersuccess")
    private String transferSuccessMessage;

    @ConfigPopulate("features.veconomy.messages.bank.exceedsmaximumamount")
    private String exceedsMaximumAmountMessage;

    @ConfigPopulate("features.veconomy.messages.notenoughmoney")
    private String notEnoughMoneyMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.bank.specifyaccountowner")
    private String specifyAccountOwnerMessage;

    @ConfigPopulate("features.veconomy.messages.playernotfound")
    private String playerNotFoundMessage;

    public TransferCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.tranfer", "Transfer money from one bank to another", false, "transfer");
        this.feature = feature;
        ArgumentField field = new ArgumentField("player name or bank account name.", true);
        ArgumentField amountArg = new ArgumentField("amount", true);
        amountArg.setCheckFunction(new NumberCheckFunction(double.class));

        try {
            addArguments(new ArgumentField("b or p (b for bank, p for player)", true), field, amountArg);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        MojangLookup lookup = feature.getInstance().getMojangLookup();
        VEconomyPlayer player = feature.get((Player) sender);
        PlayerAccount playerAccount = player.getAccount();

        double amount = NumberUtil.parse(arguments.get(2), double.class);

        if ((playerAccount.getBalance() - amount) < 0) {
            sender.sendMessage(Format.colour(notEnoughMoneyMessage));
            return;
        }

        if (arguments.get(0).toLowerCase().equals("p") && lookup.lookup(arguments.get(1)).isPresent()) {
            VEconomyPlayer target = feature.get(lookup.lookup(arguments.get(1)).get().getId());

            if (target == null) {
                sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{target};" + arguments.get(1))));
                return;
            }
            
            if (Double.isInfinite(target.getAccount().getBalance() + amount) || (target.getAccount().getBalance() + amount) > feature.getPlayerAccountMaximumBalance()) {
                sender.sendMessage(Format.colour(Format.format(exceedsMaximumAmountMessage, "{amount};" + String.valueOf(amount), "{receiver};" + sender.getName())));
                return;
            }
            
            if (playerAccount.subtract(amount).getResponse() == Response.SUCCESS && target.getAccount().add(amount).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + String.valueOf(amount), "{receiver};" + arguments.get(1))));
            }
        } else if (arguments.get(0).equals("b")) {
            SharedAccount target = null;


            String split[] = arguments.get(1).split("/");
            List<UUID> accounts = feature.getHandler().getSharedAccountsNamed(split[1]);

            if (accounts.size() > 1) {
                if (arguments.get(1).split("/").length == 1) {
                    sender.sendMessage(Format.colour(Format.format(specifyAccountOwnerMessage, "{amount};" + String.valueOf(accounts.size()))));
                    return;
                }

                Optional<PlayerProfile> ownerProfile = feature.getInstance().getMojangLookup().lookup(split[0]);

                if (!ownerProfile.isPresent()) {
                    sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{target};" + split[0])));
                    return;
                }


                VEconomyPlayer owner = feature.get(ownerProfile.get().getId());

                List<UUID> ownedAccounts = owner.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getOwner().equals(owner.getReference().get().getUniqueId())).collect(Collectors.toCollection(Lists::newArrayList));

                if (ownedAccounts.size() == 0 || ownedAccounts.stream().noneMatch(id -> feature.getAccount(id).getName().equals(split[1]))) {
                    sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{target};" + split[1])));
                    return;
                }

                target = ownedAccounts.stream().filter(id -> feature.getAccount(id).getName().equals(split[1])).map(id -> feature.getAccount(id)).findFirst().get();
            } else {
                target = feature.getAccount(accounts.get(0));
            }

            if (target == null) {
                sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{target};" + arguments.get(1))));
                return;
            }

            if (Double.isInfinite(target.getBalance() + amount) || (target.getBalance() + amount) > feature.getSharedAccountMaximumBalance()) {
                sender.sendMessage(Format.colour(Format.format(exceedsMaximumAmountMessage, "{amount};" + String.valueOf(amount), "{receiver};" + target.getName())));
                return;
            }

            if (playerAccount.subtract(amount).getResponse() == Response.SUCCESS && target.add(amount, player.getReference().get().getUniqueId()).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + feature.getVaultHook().format(amount), "{receiver};" + target.getName())));
            }
        }
    }
}