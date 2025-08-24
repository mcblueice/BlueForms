package net.mcblueice.blueforms.forms.residence.manage.admin;

import org.bukkit.entity.Player;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceManageAdminRenameForm {
    private final Player player;
    private final ConfigManager lang;
    private final ClaimedResidence claimedResidence;

    public ResidenceManageAdminRenameForm(Player player, ConfigManager lang, ClaimedResidence claimedResidence) {
        this.player = player;
        this.lang = lang;
        this.claimedResidence = claimedResidence;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

		if (!ResidenceUtils.isOwner(player, claimedResidence)) {
			player.sendMessage(lang.get("forms.residence.manage.admin.rename.nopermissionmessage", claimedResidence.getName()));
			return;
		}

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.manage.admin.rename.title"))
            .input(lang.get("forms.residence.manage.admin.rename.content", claimedResidence.getName()) + "\n" + lang.get("forms.residence.manage.admin.rename.input1"), lang.get("forms.residence.manage.admin.rename.input2"));

        builder.validResultHandler((form, response) -> {
            String input = response.asInput(0);
            String residence = input.trim();

            if (residence == null || residence.isEmpty()) {
                player.sendMessage(lang.get("forms.residence.manage.admin.rename.invalid"));
                return;
            }
            Residence.getInstance().getResidenceManager().renameResidence(player, claimedResidence.getName(), residence, false);
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceManageAdminMainForm(player, lang, claimedResidence).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
    
}
    