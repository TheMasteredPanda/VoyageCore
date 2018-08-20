package cm.pvp.voyagepvp.voyagecore.features.vvoting;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.exception.MojangException;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.api.player.Players;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.TestVoteCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.VoteClaimCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty.VotePartyCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty.admin.VotePartyAdminCommand;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.vexsoftware.votifier.model.VotifierEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
public class VVoting extends Feature implements Listener
{
    private DataHandler handler;
    private Gson gson = new Gson();
    private File settingsFile;
    private JsonObject settingsObject;

    @ConfigPopulate("features.vvoting.messages.vote")
    private String voteMessage;

    @ConfigPopulate("features.vvoting.messages.voteparty.claimsavailable")
    private String votePartyClaimsAvailable;

    @ConfigPopulate("features.vvoting.messages.voteparty.complete")
    private String votePartyCompleteMessage;

    @ConfigPopulate("features.vvoting.messages.thanksforvoting")
    private String thanksForVotingMessage;

    @ConfigPopulate("features.vvoting.dailyreward")
    private List<String> dailyReward;

    @ConfigPopulate("features.vvoting.voteparty.requiredVotes")
    private int requiredVotes;

    public VVoting(VoyageCore instance)
    {
        super(instance, "VVoting", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        handler = new DataHandler(this);
        getInstance().register(new VotePartyCommand(this), new TestVoteCommand(), new VoteClaimCommand(this), new VotePartyAdminCommand(this));
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        settingsFile = new File(getInstance().getDataFolder(), "vvoting-settings.json");

        try {
            if (!settingsFile.exists()) {
                if (settingsFile.createNewFile()) {
                    getLogger().info("Created settings file for vvoting.");
                }
                JsonObject data = new JsonObject();
                data.addProperty("startedParty", false);
                gson.toJson(data, new JsonWriter(new FileWriter(settingsFile)));
            }

            settingsObject = gson.fromJson(new JsonReader(new FileReader(settingsFile)), JsonObject.class);

            getInstance().getMainConfig().populate(this);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    public void saveSettingsFile()
    {
        try {
            gson.toJson(settingsObject, new JsonWriter(new FileWriter(settingsFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void on(PlayerJoinEvent e)
    {
        handler.voted(e.getPlayer().getUniqueId()).whenCompleteAsync((voted, throwable) -> {
            if (!voted) {
                e.getPlayer().sendMessage(Format.colour(voteMessage));
            }

            handler.partyClaims().whenCompleteAsync((claims, throwable1) -> {
                if (!claims.containsKey(e.getPlayer().getUniqueId())) {
                    return;
                }

                e.getPlayer().sendMessage(Format.colour(Format.format(votePartyClaimsAvailable, "{amount};" + String.valueOf(claims.get(e.getPlayer().getUniqueId())))));
            });
        });
    }

    @EventHandler
    public void on(VotifierEvent e)
    {
        Optional<PlayerProfile> profile = getInstance().getBackupLookup().lookup(e.getVote().getUsername());

        if (!profile.isPresent()) {
            throw new MojangException("Couldn't find the uuid for " + e.getVote().getUsername() + ", a user who has voted on service " + e.getVote().getServiceName() + ".");
        }

        UUID id = profile.get().getId();

        if (Players.online().stream().anyMatch(p -> p.getName().equalsIgnoreCase(e.getVote().getUsername()))) {
            Player p = Bukkit.getPlayer(e.getVote().getUsername());

            dailyReward.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Format.format(cmd, "@p;" + p.getName())));
            p.sendMessage(Format.colour(Format.format(thanksForVotingMessage, "{player};" + p.getName(), "{servicename};" + e.getVote().getServiceName())));
        } else {
            handler.dailyInsert(id);
        }

        handler.party().whenCompleteAsync((claims, throwable) -> {
            if (!claims.contains(id)) {
                handler.partyInsert(id);
            }

            if (claims.size() >= requiredVotes) {
                handler.cleanParty().whenCompleteAsync((aVoid, throwable12) -> {
                    Bukkit.broadcastMessage(Format.colour(votePartyCompleteMessage));
                    settingsObject.addProperty("startedParty", false);
                    saveSettingsFile();
                });
            }

        });
    }
}
