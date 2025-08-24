package net.mcblueice.blueforms.forms.residence.manage.pset;

import java.util.*;

import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.forms.residence.manage.ResidenceManageMainForm;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceManagePsetForm {
	private final Player player;
	private final ConfigManager lang;
	private final String targetPlayer;
	private ClaimedResidence claimedResidence;

	private HashMap<String, FlagPermissions.FlagState> flags;
	private List<String> permissionList;

	public ResidenceManagePsetForm(Player player, ConfigManager lang, ClaimedResidence claimedResidence, String targetPlayer) {
		this.player = player;
		this.lang = lang;
		this.claimedResidence = claimedResidence;
		this.targetPlayer = targetPlayer;
	}

	public void open() {
		if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (!ResidenceUtils.canManage(player, claimedResidence)) {
			player.sendMessage(lang.get("forms.residence.manage.pset.nopermissionmessage", claimedResidence.getName()));
			return;
		}

		flags = ResidenceUtils.getResidencePlayerFlags(player, targetPlayer, claimedResidence);
		permissionList = new ArrayList<>(flags.keySet());
		permissionList.sort(String.CASE_INSENSITIVE_ORDER);

		CustomForm.Builder builder = CustomForm.builder()
			.title(lang.get("forms.residence.manage.pset.title", claimedResidence.getName(), targetPlayer));

		addPermissionList(builder);

		builder.validResultHandler((form, response) -> {
			for (int i = 0; i < permissionList.size(); i++) {
				String flagName = permissionList.get(i);
				FlagPermissions.FlagState oldState = flags.get(flagName);
				FlagPermissions.FlagState newState = ResidenceUtils.intToFlag(response.asStepSlider(i));
				if (newState != oldState) {
					claimedResidence.getPermissions().setPlayerFlag(targetPlayer, flagName, newState);
				}
			}
			new ResidenceManageMainForm(player, lang, claimedResidence).open();
		});

		builder.closedOrInvalidResultHandler((form, response) -> {
			new ResidenceManageMainForm(player, lang, claimedResidence).open();
		});

		FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
	}

	private void addPermissionList(CustomForm.Builder builder) {
		for (String flagName : permissionList) {
			int defaultIndex = ResidenceUtils.flagToInt(flags.get(flagName));
			Flags flag = Flags.getFlag(flagName);

			String name = flag != null ? flag.getName() : "";
			String description = flag != null ? flag.getDesc() : "";

			builder.stepSlider(
				lang.get("forms.residence.manage.set.content.template", name, description),
				defaultIndex,
				lang.get("forms.residence.manage.set.permission.disabled"),
				lang.get("forms.residence.manage.set.permission.not-set"),
				lang.get("forms.residence.manage.set.permission.enable")
			);
		}
	}
}
