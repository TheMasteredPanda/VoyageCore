package cm.pvp.voyagepvp.voyagecore.features.coinflip;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.Random;

public class CoinFlip extends Feature
{
    @ConfigPopulate("features.coinflip.messages.flipped")
    private String coinFlipMessage;

    public CoinFlip(VoyageCore instance)
    {
        super(instance, "CoinFlip", 1.0);

        getInstance().getMainConfig().populate(this);
    }

    @Override
    protected boolean enable() throws Exception
    {
        getInstance().register(new FlipCommand());
        return true;
    }

    public class FlipCommand extends VoyageCommand
    {
        private Random r = new Random();

        public FlipCommand()
        {
            super(null, "voyagecore.coinflip.flip", "Flip a coin.", true, "coinflip.");
        }

        @Override
        public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
        {
            sender.sendMessage(Format.colour(Format.format(coinFlipMessage, "{result};" + (r.nextInt(1) == 0 ? "Heads" : "Tails"))));
        }
    }
}
