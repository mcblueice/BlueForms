package net.mcblueice.blueforms.forms.residence.manage.pset;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.forms.residence.manage.ResidenceManageMainForm;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceManagePsetSelectForm {
	private final Player player;
	private final ConfigManager lang;
	private final ClaimedResidence claimedResidence;

	public ResidenceManagePsetSelectForm(Player player, ConfigManager lang, ClaimedResidence claimedResidence) {
		this.player = player;
		this.lang = lang;
		this.claimedResidence = claimedResidence;
	}

	public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (!ResidenceUtils.canManage(player, claimedResidence)) {
			player.sendMessage(lang.get("forms.residence.manage.pset.nopermissionmessage", claimedResidence.getName()));
			return;
		}

		List<String> adminList = new ArrayList<>();
		if (claimedResidence != null) {
			List<String> admins = ResidenceUtils.getResAdminPlayerName(claimedResidence);
			if (admins != null) adminList.addAll(admins);
		}
		adminList.sort(String.CASE_INSENSITIVE_ORDER);

		List<String> trustedList = new ArrayList<>();
		if (claimedResidence != null) {
			List<String> trusted = ResidenceUtils.getResTrustedPlayerName(claimedResidence);
			if (trusted != null) trustedList.addAll(trusted);
		}
		trustedList.sort(String.CASE_INSENSITIVE_ORDER);

		List<String> inResList = new ArrayList<>(ResidenceUtils.getInResidencePlayerNames(claimedResidence));
		inResList.sort(String.CASE_INSENSITIVE_ORDER);

		List<String> OnlineList = new ArrayList<>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p == null || p.getName() == null) continue;
			OnlineList.add(p.getName());
		}
		OnlineList.sort(String.CASE_INSENSITIVE_ORDER);

		LinkedHashMap<String, String> displayByRaw = new LinkedHashMap<>();

        displayByRaw.putIfAbsent(claimedResidence.getOwner(), lang.get("forms.residence.colors.OwnerColor") + claimedResidence.getOwner() + "§r");
		for (String n : adminList) displayByRaw.putIfAbsent(n, lang.get("forms.residence.colors.AdminColor") + n + "§r");
		for (String n : trustedList) displayByRaw.putIfAbsent(n, lang.get("forms.residence.colors.TrustedColor") + n + "§r");
		for (String n : inResList) displayByRaw.putIfAbsent(n, lang.get("forms.residence.colors.InResColor") + n + "§r");
		for (String n : OnlineList) displayByRaw.putIfAbsent(n, lang.get("forms.residence.colors.OnlineColor") + n + "§r");

		List<String> candidates = new ArrayList<>(displayByRaw.keySet());
		List<String> displayCandidates = new ArrayList<>(displayByRaw.values());

		List<String> options = new ArrayList<>();
		options.add(lang.get("forms.residence.manage.pset.select.manual"));
		options.addAll(displayCandidates);

		CustomForm.Builder builder = CustomForm.builder()
			.title(lang.get("forms.residence.manage.pset.select.title", claimedResidence.getName()))
			.dropdown(lang.get("forms.residence.manage.pset.select.dropdown"), options)
			.input(
				lang.get("forms.residence.manage.pset.select.input1"),
				lang.get("forms.residence.manage.pset.select.input2")
			);

		builder.validResultHandler((form, response) -> {
			int dropdown = response.asDropdown(0);
			String input = response.asInput(1);
			String targetPlayer = null;
			if (dropdown == 0) targetPlayer = input.trim();
            if (dropdown > 0 && dropdown - 1 < candidates.size()) targetPlayer = candidates.get(dropdown - 1);

			if (targetPlayer == null || targetPlayer.isEmpty()) {
				player.sendMessage(lang.get("forms.residence.manage.pset.select.invalid"));
				return;
			}
			new ResidenceManagePsetForm(player, lang, claimedResidence, targetPlayer).open();
		});

		builder.closedOrInvalidResultHandler((form, response) -> {
			new ResidenceManageMainForm(player, lang, claimedResidence).open();
		});

		FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
	}
}
