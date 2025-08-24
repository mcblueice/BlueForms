package net.mcblueice.blueforms.forms.residence.manage;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import net.mcblueice.blueforms.ConfigManager;
import net.mcblueice.blueforms.forms.residence.ResidenceMainForm;
import net.mcblueice.blueforms.utils.ResidenceUtils;

public class ResidenceManageSelectForm {
    private final Player player;
    private final ConfigManager lang;

    public ResidenceManageSelectForm(Player player, ConfigManager lang) {
        this.player = player;
        this.lang = lang;
    }

    public void open() {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) return;

        List<String> residenceList = new ArrayList<>();
        String currentResidence = ResidenceUtils.getCurrentResidenceName(player);
        boolean hasCurrentResidence = currentResidence != null && !currentResidence.isEmpty();
        if (hasCurrentResidence) {
            if (ResidenceUtils.canManage(player, ResidenceUtils.parseClaimedResidence(currentResidence))) {
                residenceList.add(lang.get("forms.residence.manage.select.current", currentResidence));
            } else {
                residenceList.add(lang.get("forms.residence.manage.select.nopermission", currentResidence));
            }
        }
        residenceList.add(lang.get("forms.residence.manage.select.manual"));
        List<String> managedResidences = ResidenceUtils.getPlayerAdminResListNames(player);
        residenceList.addAll(managedResidences);

        CustomForm.Builder builder = CustomForm.builder()
            .title(lang.get("forms.residence.manage.select.title"))
            .dropdown(lang.get("forms.residence.manage.select.dropdown"), residenceList)
            .input(lang.get("forms.residence.manage.select.input1"), lang.get("forms.residence.manage.select.input2"));

        builder.validResultHandler((form, response) -> {
            int dropdown = response.asDropdown(0);
            String input = response.asInput(1);
            String residence = null;
            int offset = hasCurrentResidence ? 2 : 1;
            if (hasCurrentResidence && dropdown == 0) residence = currentResidence;
            if ((hasCurrentResidence && dropdown == 1) || (!hasCurrentResidence && dropdown == 0)) residence = input.trim();
            if ((hasCurrentResidence && dropdown >= 2) || (!hasCurrentResidence && dropdown >= 1)) {
                if (dropdown - offset >= 0 && dropdown - offset < managedResidences.size()) {
                    residence = managedResidences.get(dropdown - offset);
                }
            }

            if (ResidenceUtils.parseClaimedResidence(residence) == null || residence == null || residence.isEmpty()) {
                player.sendMessage(lang.get("forms.residence.manage.select.invalid"));
                return;
            }
            if (!ResidenceUtils.canManage(player, ResidenceUtils.parseClaimedResidence(residence)) && !player.hasPermission("residence.admin")) {
                player.sendMessage(lang.get("forms.residence.manage.select.nopermissionmessage", residence));
                return;
            }
            new ResidenceManageMainForm(player, lang, ResidenceUtils.parseClaimedResidence(residence)).open();
        });

        builder.closedOrInvalidResultHandler((form, response) -> {
            new ResidenceMainForm(player, lang).open();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }
}
