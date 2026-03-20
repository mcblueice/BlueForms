package net.mcblueice.blueforms;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.forms.residence.ResidenceMainForm;
import net.mcblueice.blueforms.forms.tp.TpMainForm;
import net.mcblueice.blueforms.forms.home.HomeMainForm;
import net.mcblueice.blueforms.forms.message.MessageMainForm;

public class AliasCommands implements Listener {
    private final BlueForms plugin;

    public AliasCommands(BlueForms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        String commandString = String.join(" ", event.getMessage().substring(1).trim().split("\\s+")).toLowerCase();
        if (commandString.isEmpty()) return;

        Map<String, List<String>> aliases = plugin.getLanguageManager().getAliases();
        String targetForm = null;
        for (Map.Entry<String, List<String>> entry : aliases.entrySet()) {
            if (entry.getValue().contains(commandString)) {
                targetForm = entry.getKey();
                break;
            }
        }

        if (targetForm != null) {
            event.setCancelled(true);

            Commands commands = (Commands) plugin.getCommand("blueforms").getExecutor();
            String[] fakeArgs = new String[] { targetForm };

            switch (targetForm) {
                case "residence":
                    commands.openForm(player, fakeArgs, "blueforms.use.residence", plugin.isResidenceEnabled(), "residence",
                        (formPlayer, languageManager) -> new ResidenceMainForm(formPlayer, languageManager).open());
                    break;
                case "home":
                    commands.openForm(player, fakeArgs, "blueforms.use.home", plugin.isHomeEnabled(), "home",
                        (formPlayer, languageManager) -> new HomeMainForm(formPlayer, languageManager).open());
                    break;
                case "tp":
                    commands.openForm(player, fakeArgs, "blueforms.use.tp", plugin.isTpEnabled(), "tp",
                        (formPlayer, languageManager) -> new TpMainForm(formPlayer, languageManager).open());
                    break;
                case "message":
                    commands.openForm(player, fakeArgs, "blueforms.use.message", plugin.isMessageEnabled(), "message",
                        (formPlayer, languageManager) -> new MessageMainForm(formPlayer, languageManager).open());
                    break;
            }
        }
    }
}