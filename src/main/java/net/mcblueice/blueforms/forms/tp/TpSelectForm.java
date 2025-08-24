package net.mcblueice.blueforms.forms.tp;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.BlueCrossServerUtils;
import net.mcblueice.blueforms.BlueForms;

public class TpSelectForm {
	private final Player player;
	private final ConfigManager lang;
	private final String mode;

	public TpSelectForm(Player player, ConfigManager lang, String mode) {
		this.player = player;
		this.lang = lang;
		this.mode = mode;
	}

	public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (BlueForms.getInstance().isCrossServerEnabled()) {
			BlueCrossServerUtils.refreshNow(this::Send);
		} else {
			List<String> localplayerlist = new ArrayList<>();
			for (Player p : Bukkit.getOnlinePlayers()) localplayerlist.add(p.getName());
			Send(localplayerlist);
		}
	}

	private void Send(List<String> source) {
		Set<String> rawSet = new LinkedHashSet<>();
		for (String name : source) if (!name.equals(player.getName())) rawSet.add(name);
		List<String> playerlist = new ArrayList<>(rawSet);
		playerlist.sort(String.CASE_INSENSITIVE_ORDER);

		List<String> options = new ArrayList<>();
		options.add(lang.get("forms.tp.select.manual"));
		options.addAll(playerlist);

		CustomForm.Builder builder = CustomForm.builder()
			.title(lang.get("forms.tp.select.title"))
			.dropdown(lang.get("forms.tp.select.dropdown"), options)
			.input(lang.get("forms.tp.select.input1"), lang.get("forms.tp.select.input2"));

		builder.validResultHandler((form, response) -> {
			int dropdown = response.asDropdown(0);
			String input = response.asInput(1);
			String targetPlayer = null;
			if (dropdown == 0) targetPlayer = input.trim();
			if (dropdown > 0 && dropdown - 1 < playerlist.size()) targetPlayer = playerlist.get(dropdown - 1);
			if (targetPlayer == null || targetPlayer.isEmpty()) {
				player.sendMessage(lang.get("forms.tp.select.invalid"));
				return;
			}
			switch (mode) {
				case "tpa": Bukkit.dispatchCommand(player, "huskhomes:tpa " + targetPlayer); break;
				case "tpahere": Bukkit.dispatchCommand(player, "huskhomes:tpahere " + targetPlayer); break;
				default: player.sendMessage(lang.get("forms.etc.unknownoption")); break;
			}
		});

        builder.closedOrInvalidResultHandler((form, response) -> {
            new TpMainForm(player, lang).open();
        });

		FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
	}
}
