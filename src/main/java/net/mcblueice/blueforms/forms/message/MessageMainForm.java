
package net.mcblueice.blueforms.forms.message;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.BlueForms;
import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.BlueCrossServerUtils;
import net.mcblueice.blueforms.utils.TaskScheduler;

public class MessageMainForm {
    private final Player player;
    private final ConfigManager lang;

    public MessageMainForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (BlueForms.getInstance().isCrossServerEnabled()) {
			BlueCrossServerUtils.refreshNow(this::Send);
		} else {
			List<String> localplayerlist = new ArrayList<>();
			for (Player player : Bukkit.getOnlinePlayers()) localplayerlist.add(player.getName());
			Send(localplayerlist);
		}
    }

    private void Send(List<String> playerList) {
        Set<String> rawSet = new LinkedHashSet<>();
        for (String name : playerList) if (!name.equals(player.getName())) rawSet.add(name);
        List<String> dropdownList = new ArrayList<>(rawSet);
        dropdownList.sort(String.CASE_INSENSITIVE_ORDER);

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.message.main.title"))
            .dropdown(lang.get("forms.message.main.dropdown"), dropdownList)
            .input(lang.get("forms.message.main.input1"), lang.get("forms.message.main.input2"));

        builder.validResultHandler((form, response) -> {
            int dropdown = response.asDropdown(0);
            String inputMsg = response.asInput(1);
            String targetPlayer = null;
            if (dropdown >= 0 && dropdown < dropdownList.size()) targetPlayer = dropdownList.get(dropdown);
            if (targetPlayer == null || targetPlayer.isEmpty() || inputMsg == null || inputMsg.isEmpty()) {
                player.sendMessage(lang.get("forms.message.main.invalid"));
                return;
            }
            String commandTemplate = Bukkit.getPluginManager().getPlugin("BlueForms").getConfig().getString("features.message.command");
            String command = commandTemplate.replace("{player}", targetPlayer).replace("{message}", inputMsg);
            TaskScheduler.dispatchCommand(player, Bukkit.getPluginManager().getPlugin("BlueForms"), command);
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
