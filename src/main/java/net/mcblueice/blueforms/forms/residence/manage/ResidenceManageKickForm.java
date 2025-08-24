package net.mcblueice.blueforms.forms.residence.manage;

import java.util.*;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceManageKickForm {
	private final Player player;
	private final ConfigManager lang;
	private final ClaimedResidence claimedResidence;

	public ResidenceManageKickForm(Player player, ConfigManager lang, ClaimedResidence claimedResidence) {
		this.player = player;
		this.lang = lang;
		this.claimedResidence = claimedResidence;
	}

	public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (!ResidenceUtils.canManage(player, claimedResidence)) {
			player.sendMessage(lang.get("forms.residence.manage.kick.nopermissionmessage", claimedResidence.getName()));
			return;
		}

		LinkedHashMap<String, String> coloredMap = ResidenceUtils.getInResidencePlayerColoredMap(claimedResidence, lang);
		List<String> candidates = new ArrayList<>(coloredMap.keySet());
		List<String> displayCandidates = new ArrayList<>(coloredMap.values());
		String inResList = displayCandidates.isEmpty() ? lang.get("forms.etc.none") : String.join(", ", displayCandidates);

		List<String> options = new ArrayList<>();
		options.add(lang.get("forms.residence.manage.kick.manual"));
		options.addAll(displayCandidates);


		CustomForm.Builder builder = CustomForm.builder()
			.title(lang.get("forms.residence.manage.kick.title", claimedResidence.getName()))
			.dropdown(lang.get("forms.residence.manage.kick.content.template", inResList) + "\n" + lang.get("forms.residence.manage.kick.dropdown"), options)
			.input(
				lang.get("forms.residence.manage.kick.input1"),
				lang.get("forms.residence.manage.kick.input2")
				);

		builder.validResultHandler((form, response) -> {
			int dropdown = response.asDropdown(0);
			String input = response.asInput(1);
			String targetPlayer = null;
			if (dropdown == 0) targetPlayer = input.trim();
			if (dropdown > 0 && dropdown - 1 < candidates.size()) targetPlayer = candidates.get(dropdown - 1);

			if (targetPlayer == null || targetPlayer.isEmpty()) {
				player.sendMessage(lang.get("forms.residence.manage.kick.invalid"));
				return;
			}

			ResidenceUtils.kickPlayer(targetPlayer, claimedResidence);
			player.sendMessage(lang.get("forms.residence.manage.kick.success", targetPlayer));
            return;
		});

		builder.closedOrInvalidResultHandler((form, response) -> {
			new ResidenceManageMainForm(player, lang, claimedResidence).open();
		});

		FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
	}
}
