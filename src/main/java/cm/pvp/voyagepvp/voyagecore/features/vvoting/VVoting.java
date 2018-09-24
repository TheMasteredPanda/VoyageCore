package cm.pvp.voyagepvp.voyagecore.features.vvoting;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.exception.MojangException;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import cm.pvp.voyagepvp.voyagecore.api.player.Players;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.TestVoteCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.ViewGARewardsCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.VoteClaimCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty.VotePartyCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty.admin.VotePartyAdminCommand;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vexsoftware.votifier.model.VotifierEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Getter
public class VVoting extends Feature implements Listener
{
    private Random r = new Random();
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
    private List<String> dailyRewards;

    @ConfigPopulate("features.vvoting.voteparty.requiredvotes")
    private int requiredVotes;

    @ConfigPopulate("features.vvoting.messages.voteparty.admin.partystartedannouncement")
    private String partyStartedAnnouncement;

    @ConfigPopulate("features.vvoting.voteparty.cooldown.interval")
    private int interval;

    @ConfigPopulate("features.vvoting.voteparty.cooldown.enabled")
    private boolean cooldownEnabled;

    @ConfigPopulate("features.vvoting.voteparty.repeat")
    private boolean repeatEnabled;

    private Map<String, GAEntry> gaRewards = Maps.newHashMap();

    @Setter
    private VotePartyCountdown countdown;

    public VVoting()
    {
        super("VVoting", 2.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        handler = new DataHandler(this);
        getInstance().register(new VotePartyCommand(this), new TestVoteCommand(), new VoteClaimCommand(this), new VotePartyAdminCommand(this), new ViewGARewardsCommand(this));
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        settingsFile = new File(getInstance().getDataFolder(), "vvoting-settings.json");

        try {
            if (!settingsFile.exists()) {
                if (settingsFile.createNewFile()) {
                    getLogger().info("Created settings file for VVoting.");
                }
                JsonObject data = new JsonObject();
                data.addProperty("startedParty", false);

                try (FileWriter writer = new FileWriter(settingsFile)) {
                    gson.toJson(data, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            try (FileReader reader = new FileReader(settingsFile)) {
                settingsObject = gson.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            getInstance().getMainConfig().populate(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (cooldownEnabled && repeatEnabled) {
            throw new RuntimeException("You cannot have the cooldown and repeat functions enabled at the same time.");
        }


        if (settingsObject.has("countdown") && cooldownEnabled) {
            countdown = new VotePartyCountdown(this, settingsObject.get("countdown").getAsInt());
        }

        if (countdown == null && cooldownEnabled) {
            countdown = new VotePartyCountdown(this, interval);
        }

        if (!settingsObject.get("startedParty").getAsBoolean() && repeatEnabled) {
            startVotingParty();
        }

        new VVotingPlaceholderExtension(this).register();

        for (String key : getSection().getConfigurationSection("garewards").getKeys(true)) {
            String[] fraction = key.split("/");

            if (fraction.length == 1) {
                continue;
            }

            if (!NumberUtil.parseable(fraction[0], int.class) || !NumberUtil.parseable(fraction[1], int.class)) {
                continue;
            }

            gaRewards.put(key, new GAEntry(getSection().getStringList("garewards." + key + ".commands"), getSection().getStringList("garewards." + key + ".message")));
        }

        return true;
    }

    public void startVotingParty()
    {
        if (cooldownEnabled) {
            if (countdown != null && !countdown.isCancelled()) {
                countdown.cancel();
                settingsObject.remove("countdown");
            }
        }

        Bukkit.broadcastMessage(Format.colour(partyStartedAnnouncement));
        settingsObject.addProperty("startedParty", true);
        saveSettingsFile();
    }

    public void stopVotingParty()
    {
        if (cooldownEnabled) {
            if (countdown.isCancelled()) {
                countdown = new VotePartyCountdown(this, interval);
            }
        }

        handler.cleanParty().whenCompleteAsync((aVoid, throwable) -> {
            settingsObject.addProperty("startedParty", false);
            saveSettingsFile();
        });
    }

    public void saveSettingsFile()
    {
        try (FileWriter writer = new FileWriter(settingsFile)) {
            gson.toJson(settingsObject, writer);
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

            dailyRewards.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Format.format(cmd, "@p;" + p.getName())));
            pick(p);
            p.sendMessage(Format.colour(Format.format(thanksForVotingMessage, "{player};" + p.getName(), "{servicename};" + e.getVote().getServiceName())));
        } else {
            handler.dailyInsert(id);
        }

        if (settingsObject.get("startedParty").getAsBoolean()) {
            handler.party().whenCompleteAsync((claims, throwable) -> {
                if (!claims.contains(id)) {
                    handler.partyInsert(id);
                }

                if ((claims.size() + 1) >= requiredVotes) {
                    handler.cleanParty().whenCompleteAsync((aVoid, throwable12) -> {
                        Bukkit.broadcastMessage(Format.colour(votePartyCompleteMessage));
                        settingsObject.addProperty("startedParty", false);
                        saveSettingsFile();

                        if (repeatEnabled) {
                            startVotingParty();
                        }

                        if (cooldownEnabled) {
                            countdown = new VotePartyCountdown(this, interval);
                        }

                    });
                }
            });
        }
    }

    public void pick(Player player)
    {
        for (Map.Entry<String, GAEntry> entry : gaRewards.entrySet()) {
            String[] fraction = entry.getKey().split("/");
            int numerator = NumberUtil.parse(fraction[0], int.class);
            int denominator = NumberUtil.parse(fraction[1], int.class);

            if (r.nextInt(denominator) < numerator) {
                entry.getValue().getCommands().forEach(cmd -> Bukkit.dispatchCommand(player, Format.format(cmd, "@p;" + player.getName())));
                entry.getValue().getMessage().forEach(line -> player.sendMessage(Format.colour(line)));
                return;
            }
        }
    }
}