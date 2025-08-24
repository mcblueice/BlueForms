package net.mcblueice.blueforms.forms.residence.manage;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceManageTrustForm {
	private final Player player;
	private final ConfigManager lang;
	private final ClaimedResidence claimedResidence;

	public ResidenceManageTrustForm(Player player, ConfigManager lang, ClaimedResidence claimedResidence) {
		this.player = player;
		this.lang = lang;
		this.claimedResidence = claimedResidence;
	}

	public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (!ResidenceUtils.canManage(player, claimedResidence)) {
			player.sendMessage(lang.get("forms.residence.manage.trust.nopermissionmessage", claimedResidence.getName()));
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
		options.add(lang.get("forms.residence.manage.trust.manual"));
		options.addAll(displayCandidates);

		List<String> actionOptions = Arrays.asList(
			lang.get("forms.residence.manage.trust.addTrust"),
			lang.get("forms.residence.manage.trust.removeTrust"),
			lang.get("forms.residence.manage.trust.addAdmin"),
			lang.get("forms.residence.manage.trust.removeAdmin"),
			lang.get("forms.residence.manage.trust.clearAll")
		);

        String name = claimedResidence.getName();
        String owner = claimedResidence.getOwner();
        List<String> adminNames = ResidenceUtils.getResAdminPlayerName(claimedResidence);
        String adminList2 = adminNames.isEmpty() ? lang.get("forms.etc.none") : String.join(", ", adminNames);
        List<String> trustedNames = ResidenceUtils.getResTrustedPlayerName(claimedResidence);
        String trustedList2 = trustedNames.isEmpty() ? lang.get("forms.etc.none") : String.join(", ", trustedNames);
        List<String> inResColored = new ArrayList<>(ResidenceUtils.getInResidencePlayerColoredMap(claimedResidence, lang).values());
        String inResList2 = inResColored.isEmpty() ? lang.get("forms.etc.none") : String.join(", ", inResColored);

		CustomForm.Builder builder = CustomForm.builder()
			.title(lang.get("forms.residence.manage.trust.title", claimedResidence.getName()))
			.dropdown(lang.get("forms.residence.manage.trust.content.template", name, owner, adminList2, trustedList2, inResList2) + "\n" + lang.get("forms.residence.manage.trust.dropdown1"), options)
			.input(
				lang.get("forms.residence.manage.trust.input1"),
				lang.get("forms.residence.manage.trust.input2")
				)
			.dropdown(lang.get("forms.residence.manage.trust.dropdown2"), actionOptions);

		builder.validResultHandler((form, response) -> {
			int playerDropdown = response.asDropdown(0);
			String input = response.asInput(1);
			int actionDropdown = response.asDropdown(2);
			String targetPlayer = null;
			if (playerDropdown == 0) {
				targetPlayer = input.trim();
			} else if (playerDropdown > 0 && playerDropdown - 1 < candidates.size()) {
				targetPlayer = candidates.get(playerDropdown - 1);
			}

			if (targetPlayer == null || targetPlayer.isEmpty()) {
				player.sendMessage(lang.get("forms.residence.manage.trust.invalid"));
				return;
			}

				switch (actionDropdown) {
				case 0:
					claimedResidence.getPermissions().setPlayerFlag(player, targetPlayer, "trusted", "true", false, false);
					new ResidenceManageTrustForm(player, lang, claimedResidence).open();
					break;
				case 1:
					claimedResidence.getPermissions().setPlayerFlag(player, targetPlayer, "trusted", "remove", false, false);
					claimedResidence.getPermissions().setPlayerFlag(targetPlayer, "admin", FlagPermissions.FlagState.NEITHER);
					new ResidenceManageTrustForm(player, lang, claimedResidence).open();
					break;
				case 2:
					claimedResidence.getPermissions().setPlayerFlag(targetPlayer, "admin", FlagPermissions.FlagState.TRUE);
					claimedResidence.getPermissions().setPlayerFlag(player, targetPlayer, "trusted", "true", false, false);
					new ResidenceManageTrustForm(player, lang, claimedResidence).open();
					break;
				case 3:
					claimedResidence.getPermissions().setPlayerFlag(targetPlayer, "admin", FlagPermissions.FlagState.NEITHER);
					new ResidenceManageTrustForm(player, lang, claimedResidence).open();
					break;
				case 4:
					ResidenceUtils.clearPlayerAllFlags(targetPlayer, claimedResidence);
					new ResidenceManageTrustForm(player, lang, claimedResidence).open();
					break;
				default:
                    player.sendMessage(lang.get("forms.etc.unknownoption"));
                    return;
			}
		});

		builder.closedOrInvalidResultHandler((form, response) -> {
			new ResidenceManageMainForm(player, lang, claimedResidence).open();
		});

		FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
	}
}
